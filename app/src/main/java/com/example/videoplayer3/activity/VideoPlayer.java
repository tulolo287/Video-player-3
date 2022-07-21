package com.example.videoplayer3.activity;

import static com.example.videoplayer3.adapter.VideosAdapter.videoFolder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.videoplayer3.R;

public class VideoPlayer extends AppCompatActivity implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener {

    int position = -1;

    VideoView videoView;
    LinearLayout custom_controls;
    RelativeLayout zoomLayout;
   private boolean isCustomControls;

   private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 5.0f;
    boolean intLeft, intRight;
    private Display display;
    private Point size;
    private Mode mode = Mode.NONE;
    private ScaleGestureDetector scaleDetector;
    private GestureDetectorCompat gestureDetector;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.playPause:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
                } else {
                    videoView.start();
                    playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
                }
                break;
            case R.id.back_conrol:
                videoView.seekTo(videoView.getCurrentPosition() - 10000);
                break;
            case R.id.forwardControl:
                videoView.seekTo(videoView.getCurrentPosition() + 10000);
                break;
        }
    }

    private enum Mode {
        NONE, DRAG, ZOOM

    };
    int device_width;
    private int sWidth;
    private boolean isEnable = true;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;
    private float startX = 0f;
    private float startY = 0f;
    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;

    ImageButton back, playPause, back10, forward10;
    TextView title;
    SeekBar videoSeekbar;
    TextView videoTime;

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float scaleFactor = scaleDetector.getScaleFactor();
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;
        } else {
            lastScaleFactor = 0;
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                hideCustomControls();
                if (scale > MIN_ZOOM) {
                    mode = Mode.DRAG;
                    startX = motionEvent.getX() - prevDx;
                    startY = motionEvent.getY() - prevDy;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                hideCustomControls();
                isEnable = false;
                if (mode == Mode.DRAG) {
                    dx = motionEvent.getX() - startX;
                    dy = motionEvent.getY() - startY;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = Mode.ZOOM;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = Mode.DRAG;
                break;
            case MotionEvent.ACTION_UP:
                mode = Mode.NONE;
                prevDx = dx;
                prevDy = dy;
                break;
        }
        scaleDetector.onTouchEvent(motionEvent);
        gestureDetector.onTouchEvent(motionEvent);
        if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
            zoomLayout.requestDisallowInterceptTouchEvent(true);
            float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
            float maxDy = (child().getHeight() - (child().getHeight() / scale)) / 2 * scale;
            dx = Math.min(Math.max(dx, -maxDx), maxDx);
            dy = Math.min(Math.max(dy, -maxDy), maxDy);
            applyScaleAndTranslation();
        }
        return true;
    }

    private void applyScaleAndTranslation() {
        child().setScaleX(scale);
        child().setScaleY(scale);
        child().setTranslationX(dx);
        child().setTranslationY(dy);
    }

    private View child() {
        return zoomLayout(0);
    }

    private View zoomLayout(int i) {
        return videoView;
    }


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

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        zoomLayout.setOnTouchListener(this);
        scaleDetector = new ScaleGestureDetector(getApplicationContext(), this);
        gestureDetector = new GestureDetectorCompat(getApplicationContext(), new GestureDetector());


        title = findViewById(R.id.title_control);
        back = findViewById(R.id.back);
        videoSeekbar = findViewById(R.id.seekControl);
        videoTime = findViewById(R.id.timeControl);

        back.setOnClickListener(this);

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

        initalizeSeekBar();
        setHandler();

    }

    private void setHandler() {

    }

    private void initalizeSeekBar() {
        videoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    videoView.start();
                    int currentPosition = videoView.getCurrentPosition();
                    videoTime.setText(""+ convertIntoTime(videoView.getDuration() - currentPosition));
                }
            }

            private String convertIntoTime(int ms) {
                String time;
                int x, seconds, minutes, hours;
                x = ms / 1000;
                seconds = x % 60;
                x /= 60;
                minutes = x % 60;
                x /= 60;
                hours = x % 24;
                if (hours != 0) {
                    time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" +
                            String.format("%02d", seconds);
                } else {
                    time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                }
                return time;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private class GestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isEnable) {
                hideCustomControls();
                isEnable = false;
            } else {
                showCustomControl();
                isEnable = true;
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (e.getX() < (sWidth / 2)) {
                intLeft = true;
                intRight = false;
                videoView.seekTo(videoView.getCurrentPosition() - 10000);
                Toast.makeText(VideoPlayer.this, "-10sec", Toast.LENGTH_SHORT).show();
            } else if (e.getX() > (sWidth / 2)) {
                intLeft = false;
                intRight = true;
                videoView.seekTo(videoView.getCurrentPosition() + 10000);
                Toast.makeText(VideoPlayer.this, "+10sec", Toast.LENGTH_SHORT).show();
            }
            return super.onDoubleTap(e);
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