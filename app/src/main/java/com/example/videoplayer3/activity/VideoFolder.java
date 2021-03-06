package com.example.videoplayer3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer3.R;
import com.example.videoplayer3.VideoModel;
import com.example.videoplayer3.adapter.VideosAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class VideoFolder extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnLongClickListener {
private String name;
private RecyclerView recyclerView;
private ArrayList<VideoModel> videoModelArrayList;
private VideosAdapter videosAdapter;
private final String MY_SORT_PREF = "sortOrder";
//private Context context;

    Toolbar toolbar;
    public boolean is_selectable = false;
    TextView countSelected;
    ArrayList<VideoModel> selectedList = new ArrayList<>();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_folder);

        recyclerView = findViewById(R.id.rvVideoFiles);
        countSelected = findViewById(R.id.count_selected);

        name = getIntent().getStringExtra("folderName");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));

        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        toolbar.setTitle(onlyFolderName);

        loadVideos();
        loadBannerAds();
    }

    private void loadBannerAds() {

    }
    private void refresh() {
        if (name != null && videoModelArrayList.size() > 0) {
            videoModelArrayList.clear();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadVideos();
                    videosAdapter.notifyDataSetChanged();
                    Toast.makeText(VideoFolder.this, "Videos refreshed", Toast.LENGTH_SHORT).show();
                }
            }, 1500);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        ImageView ivClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        //ivClose.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
        searchView.setQueryHint("Search file");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String input = s.toLowerCase(Locale.ROOT);
        ArrayList<VideoModel> searchList = new ArrayList<>();
        for (VideoModel model : videoModelArrayList) {
            if (model.getTitle().contains(input)) {
                searchList.add(model);
            }
        }
        videosAdapter.updateSearchList(searchList);
        return false;
    }


    private void loadVideos() {
        videoModelArrayList = getAllVideos(this, name);
        if (name != null && videoModelArrayList.size() > 0) {
            videosAdapter = new VideosAdapter(videoModelArrayList, this);
            recyclerView.setAdapter(videosAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        } else {
            Toast.makeText(this, "No videos", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<VideoModel> getAllVideos(Context context, String name) {
        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sort = preferences.getString("sorting", "sortByDate");
        String order = null;

        switch (sort) {
            case "sortByDate":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " ASC";
                break;
            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
        }

        ArrayList<VideoModel> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION

        };
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + name + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);
        if (cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                int size = cursor.getInt(3);
                int duration = cursor.getInt(4);
                String resolution = cursor.getString(5);
                String disName = cursor.getString(6);
                String width_height = cursor.getString(7);

                String human_can_read = null;
                if (size < 1024) {
                    human_can_read = String.format(context.getString(R.string.size_in_b), (double) size);
                } else if (size < Math.pow(1024, 2)) {
                    human_can_read = String.format(context.getString(R.string.size_in_kb), (double) size / 1024);
                } else if (size < Math.pow(1024, 3)) {
                    human_can_read = String.format(context.getString(R.string.size_in_mb), (double) size / Math.pow(1024, 2));
                } else {
                    human_can_read = String.format(context.getString(R.string.size_in_gb), (double) size / Math.pow(1024, 3));
                }

                String duration_formatted;
                int sec = (duration / 1000) % 60;
                int min = (duration / (1000 * 60)) % 60;
                int hrs = (duration / (1000 * 60 * 60));

                if (hrs == 0) {
                    duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                } else {
                    duration_formatted = String.valueOf(hrs).concat(":"
                            .concat(String.format(Locale.UK, "%02d", min)
                                .concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                }


                VideoModel files = new VideoModel(id, path, title, human_can_read, duration_formatted, resolution, disName, width_height);

                    //if (name.endsWith(bucket_display_name)) {
                        list.add(files);
                    //}
                }
            cursor.close();
        }
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        //String sort = preferences.getString("sorting", "sortByDate");
        String order = null;
        switch (item.getItemId()) {
            case R.id.sort_by_date:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                //order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
            case R.id.sort_by_name:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                //order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
            case R.id.sort_by_size:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                //order = MediaStore.MediaColumns.SIZE + " ASC";
                break;
            case R.id.refresh:
                refresh();
                break;
            case android.R.id.home:
                if (is_selectable) {
                    cleaSelectingToolbar();
                    videosAdapter.notifyDataSetChanged();
                } else {
                    onBackPressed();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onLongClick(View view) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.item_selected_menu);
        is_selectable = true;
        videosAdapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));

        return true;
    }

    public void prepareSelection(View view, int adapterPosition) {
        if (((CheckBox) view).isChecked()) {
            selectedList.add(videoModelArrayList.get(adapterPosition));
            count++;
            updateCount(count);
        } else {
            selectedList.remove(videoModelArrayList.get(adapterPosition));
            count--;
            updateCount(count);
        }
    }

    private void updateCount(int count) {
        if (count == 0) {
            countSelected.setText("0 item selected");
        } else {
            countSelected.setText(count + " items selected");
        }
    }

    private void cleaSelectingToolbar() {
        is_selectable = false;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.main_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));

        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        countSelected.setText(onlyFolderName);
        count = 0;
        selectedList.clear();
    }

    @Override
    public void onBackPressed() {
        if (is_selectable) {

            cleaSelectingToolbar();
            videosAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }

    }
}