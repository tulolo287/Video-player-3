package com.example.videoplayer3.adapter;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videoplayer3.R;
import com.example.videoplayer3.VideoModel;
import com.example.videoplayer3.activity.VideoPlayer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyHolder> {

    public static ArrayList<VideoModel> videoFolder = new ArrayList<>();
    private Context context;


    public VideosAdapter(ArrayList<VideoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.files_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);

        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.size.setText(videoFolder.get(position).getSize());
        holder.resolution.setText(videoFolder.get(position).getResolution());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoPlayer.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
        holder.menu.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.file_menu, null);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

            bottomSheetView.findViewById(R.id.menu_close).setOnClickListener(view1 -> {
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.share).setOnClickListener(view2 -> {
                shareFile(position);
            });
            bottomSheetView.findViewById(R.id.rename).setOnClickListener(view3 -> {
                Toast.makeText(context, "Rename", Toast.LENGTH_SHORT).show();
                renameFile(position, view);
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.delete).setOnClickListener(view4 -> {
                deleteFile(position, view);
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.properties).setOnClickListener(view5 -> {
                propertiesFile(position);
            });
        });

    }



    private void propertiesFile(int position) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_properties);

        String name = videoFolder.get(position).getDisplayName();
        String size = videoFolder.get(position).getSize();
        String path = videoFolder.get(position).getPath();
        String resolution = videoFolder.get(position).getResolution();

        TextView propName = dialog.findViewById(R.id.prop_name);
        TextView propSize = dialog.findViewById(R.id.prop_size);
        TextView propPath = dialog.findViewById(R.id.prop_path);
        TextView propResolution = dialog.findViewById(R.id.prop_resolution);

        propName.setText(name);
        propSize.setText(size);
        propPath.setText(path);
        propResolution.setText(resolution);

        dialog.show();
    }

    private void deleteFile(int position, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete").setMessage(videoFolder.get(position).getTitle()).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(videoFolder.get(position).getPath()));
                File file = new File(videoFolder.get(position).getPath());
                boolean deleted = file.delete();
                if (deleted) {
                    context.getApplicationContext().getContentResolver().delete(contentUri, null, null);
                    videoFolder.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, videoFolder.size());
                    Snackbar.make(view,"File delete successfully", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(view, "Error deleting file", Snackbar.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    private void renameFile(int position, View view) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rename_layout);
        EditText editText = dialog.findViewById(R.id.renameET);
        Button cancel = dialog.findViewById(R.id.rename_cancel);
        Button rename_button = dialog.findViewById(R.id.rename_ok);
        final File renameFile = new File(videoFolder.get(position).getPath());
        String fileName = renameFile.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        editText.setText(fileName);
        editText.clearFocus();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        rename_button.setOnClickListener(view1 -> {
            String onlyPath = renameFile.getParentFile().getAbsolutePath();
            String ext = renameFile.getAbsolutePath();
            ext = ext.substring(ext.lastIndexOf("."));
            String newPath = onlyPath + "/" + editText.getText() + ext;
            File newFile = new File(newPath);


            Log.i("rename file", newPath);
            boolean rename = renameFile.renameTo(newFile);
            if (rename) {
                context.getApplicationContext().getContentResolver().
                        delete(MediaStore.Files.getContentUri("external"),
                                MediaStore.MediaColumns.DATA + "=?",
                        new String[] {renameFile.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(newFile));
                context.getApplicationContext().sendBroadcast(intent);
                Snackbar.make(view1, "File renamed", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Snackbar.make(view1, "Rename failed", Snackbar.LENGTH_SHORT).show();

            }

        });
        dialog.show();
    }

    private void shareFile(int position) {
        Uri uri = Uri.parse(videoFolder.get(position).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share"));
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public void updateSearchList(ArrayList<VideoModel> searchList) {
        videoFolder = new ArrayList<>();
        videoFolder.addAll(searchList);
        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, menu;
        TextView title, size, duration, resolution;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            size = itemView.findViewById(R.id.videoSize);
            title = itemView.findViewById(R.id.videoTitle);
            duration = itemView.findViewById(R.id.videoDuration);
            resolution = itemView.findViewById(R.id.videoQuality);
            menu = itemView.findViewById(R.id.videoMenu);
        }
    }
}
