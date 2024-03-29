package com.example.videoplayer3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer3.R;
import com.example.videoplayer3.VideoModel;
import com.example.videoplayer3.adapter.VideosAdapter;
import com.google.android.material.snackbar.Snackbar;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class VideoFolder extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnLongClickListener {
private String name;
private RecyclerView recyclerView;
private ArrayList<VideoModel> videoModelArrayList;
private VideosAdapter videosAdapter;
private final String MY_SORT_PREF = "sortOrder";
private ArrayList<Uri> uris = new ArrayList<>();

//private Context context;

    Toolbar toolbar;
    public boolean is_selectable = false;
    TextView countSelected;
    ArrayList<VideoModel> selectedList = new ArrayList<>();
    int count = 0;
    LinearLayout parent_layout;
    Paint mClearPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_folder);

        recyclerView = findViewById(R.id.rvVideoFiles);
        countSelected = findViewById(R.id.count_selected);
        parent_layout = findViewById(R.id.parent_layout);

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
                order = MediaStore.MediaColumns.SIZE + " DESC";
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

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, order);
        if (cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                long size = cursor.getLong(3);
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


                VideoModel files = new VideoModel(id, path, title, fileReadableSize(size), duration_formatted, resolution, disName, width_height);

                    //if (name.endsWith(bucket_display_name)) {
                        list.add(files);
                    //}
                }
            cursor.close();
        }
        return list;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Snackbar snackbar = Snackbar.make(parent_layout, "are you sure...", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isDeleted = selectedFile(selectedList, true);
                            if (isDeleted) {
                                selectedList.clear();
                                snackbar.dismiss();
                            } else {
                                Toast.makeText(VideoFolder.this, "Delete fail", Toast.LENGTH_SHORT).show();
                                Snackbar.make(parent_layout, "Delete fail", Snackbar.LENGTH_SHORT);
                            }
                        }
                    });
                }
            });
            snackbar.show();
            snackbar.setActionTextColor(Color.RED);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            ColorDrawable mBackground;
            int backgroundColor;
            Drawable deleteDrawable;
            int intrinsicWidth;
            int intrinsicHeight;
            mBackground = new ColorDrawable();
            backgroundColor = Color.parseColor("#0f5");
            mClearPaint = new Paint();
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            deleteDrawable = ContextCompat.getDrawable(VideoFolder.this, R.drawable.ic_baseline_delete_forever_24);
            intrinsicWidth = deleteDrawable.getIntrinsicWidth();
            intrinsicHeight = deleteDrawable.getIntrinsicHeight();

            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getHeight();
            boolean isCanceled = dx == 0 && !isCurrentlyActive;
            if (isCanceled) {
                clearCanvas(c, itemView.getRight() + dx, (float) itemView.getTop()),
                        (float) itemView.getRight(),(float) itemView.getBottom());
                super.onChildDraw();
            }
        }
    };



    private String fileReadableSize(long size) {
        String s = "";
        long kilobyte = 1024;
        long megabyte = kilobyte * kilobyte;
        long gigabyte = megabyte * megabyte;
        long terabyte = gigabyte * gigabyte;

        double kb = (double) size / kilobyte;
        double mb = (double) kb / kilobyte;
        double gb = (double) mb / kilobyte;
        double tb = (double) gb / kilobyte;

        if (size < kilobyte) {
            s = size + " bytes";
        } else if (size >= kilobyte && size <= megabyte) {
            s = String.format("%.2f", kb) + "KB";
        } else if (size >= megabyte && size <= gigabyte) {
            s = String.format("%.2f", mb) + "MB";
        } else if (size >= gigabyte && size <= terabyte) {
            s = String.format("%.2f", gb) + "GB";
        }
        return s;
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
            case R.id.delete_selected:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isDeleted = selectedFile(selectedList, true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isDeleted) {
                                    cleaSelectingToolbar();
                                    refresh();
                                    Toast.makeText(VideoFolder.this, "Deleted success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(VideoFolder.this, "Error deleting", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.share_selected:
                boolean isSuccess = selectedFile(selectedList, false);
                if (isSuccess) {
                    cleaSelectingToolbar();
                    refresh();
                }
                break;

            case R.id.select_all:
                selectAll();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void selectAll() {
        selectedList.addAll(videoModelArrayList);
        CheckBox ch = findViewById(R.id.video_folder_checkbox);
        ch.setChecked(true);

        System.out.println(selectedList);
        Toast.makeText(this, "Select all", Toast.LENGTH_SHORT).show();

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

    private boolean selectedFile(ArrayList<VideoModel> list, boolean canDelete) {
        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i).getId();
            String path = list.get(i).getPath();
            uris.add(Uri.parse(path));
            if(canDelete) {
                Uri contentUris = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
                File file = new File(path);
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    getApplicationContext().getContentResolver().delete(contentUris, null, null);

                } else {
                    Toast.makeText(this, "Error deleting", Toast.LENGTH_SHORT).show();
                }
            }
            if(!canDelete) {
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{String.valueOf(uris)}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.putExtra(Intent.EXTRA_STREAM, uris);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        startActivity(Intent.createChooser(intent, "share"));
                    }
                });
            }
        }
        return true;
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