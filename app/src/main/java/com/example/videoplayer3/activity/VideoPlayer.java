package com.example.videoplayer3.activity;

import static com.example.videoplayer3.adapter.VideosAdapter.videoFolder;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.videoplayer3.R;

public class VideoPlayer extends AppCompatActivity {

    int position = -1;

    VideoView videoView;
    LinearLayout custom_controls;
    RelativeLayout zoomLayout;
   private boolean isCustomControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        custom_controls = findViewById(R.id.custom_controls);
        custom_controls.setVisibility(View.GONE);

        position = getIntent().getIntExtra("position", -1);
        String path = videoFolder.get(position).getPath();

        videoView = findViewById(R.id.video_view);
        zoomLayout = findViewById(R.id.video_zoom);

        zoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isCustomControls) {
                    hideCustomControls();
                    isCustomControls = false;
                } else {
                    showCustomControl();
                    isCustomControls = true;
                }

            }
        });

        if (path != null) {
            videoView.setVideoPath(path);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    videoView.start();
                }
            });
        } else {
            Toast.makeText(this, "No video path", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideCustomControls() {
        custom_controls.setVisibility(View.GONE);

        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 14) {
                uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }
            if (Build.VERSION.SDK_INT >= 16) {
                uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= 19) {
                uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
                decorView.setSystemUiVisibility(uiOption);
        }
    }

    private void showCustomControl() {
        custom_controls.setVisibility(View.VISIBLE);

        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 14) {
                uiOption &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }
            if (Build.VERSION.SDK_INT >= 16) {
                uiOption &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= 19) {
                uiOption &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(uiOption);
        }
    }
}