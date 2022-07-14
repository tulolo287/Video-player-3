package com.example.videoplayer3.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.videoplayer3.adapter.FolderAdapter;
import com.example.videoplayer3.R;
import com.example.videoplayer3.VideoModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> folderList = new ArrayList<>();
    private ArrayList<VideoModel> videosList = new ArrayList<>();
    FolderAdapter folderAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.folderRV);
        videosList = fetchAllVideos(this);
        if (folderList != null && folderList.size() > 0 && videosList != null) {
            folderAdapter = new FolderAdapter(folderList, videosList, this);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));


        } else {
            Toast.makeText(this, "No files", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<VideoModel> fetchAllVideos(Context context) {
        ArrayList<VideoModel> videoModels = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION,

        };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy);
        if (cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String size = cursor.getString(3);
                String duration = cursor.getString(4);
                String resolution = cursor.getString(5);
                String disName = cursor.getString(6);
                String width_height = cursor.getString(7);

                VideoModel videoFiles = new VideoModel(id, path, title, size, duration, resolution, disName, width_height);

                int slashFirstIndex = path.lastIndexOf("/");
                String substring = path.substring(0, slashFirstIndex);

                if (!folderList.contains(substring)) {
                    folderList.add(substring);
                }
                videoModels.add(videoFiles);
            }
            cursor.close();
        }
        return videoModels;
    }
}