package com.example.videoplayer3.adapter;



import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer3.R;
import com.example.videoplayer3.VideoModel;

import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyHolder> {

    private ArrayList<VideoModel> videoFolder = new ArrayList<>();
    private Context context;

    public VideosAdapter(ArrayList<VideoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, size, duration, resolution;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.videoTitle);
            duration = itemView.findViewById(R.id.videoDuration);
            resolution = itemView.findViewById(R.id.videoQuality);

        }
    }
}
