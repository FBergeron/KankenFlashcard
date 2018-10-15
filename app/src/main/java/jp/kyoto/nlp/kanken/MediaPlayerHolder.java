package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class MediaPlayerHolder implements PlayerAdapter {

    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    public MediaPlayerHolder(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the {@link MainActivity} the {@link MediaPlayer} is
     * released. Then in the onStart() of the {@link MainActivity} a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new ImprovedMediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopUpdatingCallbackWithPosition(true);
                    Log.d(tag, "MediaPlayer playback completed");
                    if (playbackInfoListener != null) {
                        playbackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        playbackInfoListener.onPlaybackCompleted();
                    }
                }
            });
            Log.d(tag, "mediaPlayer = new MediaPlayer()");
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        playbackInfoListener = listener;
    }

    // Implements PlaybackControl.
    @Override
    public void loadMedia(int resourceId) {
        resId = resourceId;

        initializeMediaPlayer();

        AssetFileDescriptor assetFileDescriptor =
                context.getResources().openRawResourceFd(resId);
        try {
            Log.d(tag, "load() {1. setDataSource}");
            // Instead of using this call that requires API 24:
            // mediaPlayer.setDataSource(assetFileDescriptor);
            // Use the following code.
            if (assetFileDescriptor.getDeclaredLength() < 0) 
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor());
            else
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
        } catch (Exception e) {
            Log.d(tag, e.toString());
        }

        try {
            Log.d(tag, "load() {2. prepare}");
            mediaPlayer.prepare();
        } catch (Exception e) {
            Log.d(tag, e.toString());
        }

        initializeProgressCallback();
        Log.d(tag, "initializeProgressCallback()");
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            Log.d(tag, "release() and mediaPlayer = null");
            Log.d(tag, "currVol="+mediaPlayer.getVolume());
            for (float vol = mediaPlayer.getVolume(); vol > 0.0f; vol -= 0.2f) {
                mediaPlayer.setVolume(vol);
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                }
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void play() {
        Log.d(tag, "play mediaPlayer="+mediaPlayer+" playing="+(mediaPlayer==null?"n/a":mediaPlayer.isPlaying())+ " vol="+(mediaPlayer==null?"n/a":mediaPlayer.getVolume()));
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            Log.d(tag, String.format("playbackStart() %s",
                                  context.getResources().getResourceEntryName(resId)));
            mediaPlayer.start();
            for (float vol = mediaPlayer.getVolume(); vol < 1.0f; vol += 0.2f) {
                mediaPlayer.setVolume(vol);
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                }
            }
            if (playbackInfoListener != null) {
                playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            startUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void reset() {
        if (mediaPlayer != null) {
            Log.d(tag, "playbackReset()");
            mediaPlayer.reset();
            loadMedia(resId);
            if (playbackInfoListener != null) {
                playbackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET);
            }
            stopUpdatingCallbackWithPosition(true);
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            for (float vol = mediaPlayer.getVolume(); vol > 0.0f; vol -= 0.2f) {
                mediaPlayer.setVolume(vol);
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                }
            }
            mediaPlayer.pause();
            if (playbackInfoListener != null) {
                playbackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }
            Log.d(tag, "playbackPause()");
        }
    }

    @Override
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            Log.d(tag, String.format("seekTo() %d ms", position));
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public int getCurrentPosition() {
        return (mediaPlayer != null ? mediaPlayer.getCurrentPosition() : -1);
    }

    /**
     * Syncs the mediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private void startUpdatingCallbackWithPosition() {
        if (scheduleExecuter == null) {
            scheduleExecuter = Executors.newSingleThreadScheduledExecutor();
        }
        //if (mSeekbarPositionUpdateTask == null) {
        //    mSeekbarPositionUpdateTask = new Runnable() {
        //        @Override
        //        public void run() {
        //            updateProgressCallbackTask();
        //        }
        //    };
        //}
        //scheduleExecuter.scheduleAtFixedRate(
        //        mSeekbarPositionUpdateTask,
        //        0,
        //        PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
        //        TimeUnit.MILLISECONDS
        //);
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (scheduleExecuter != null) {
            scheduleExecuter.shutdownNow();
            scheduleExecuter = null;
            //mSeekbarPositionUpdateTask = null;
            if (resetUIPlaybackPosition && playbackInfoListener != null) {
                playbackInfoListener.onPositionChanged(0);
            }
        }
    }

    private void updateProgressCallbackTask() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            if (playbackInfoListener != null) {
                playbackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    @Override
    public void initializeProgressCallback() {
        final int duration = mediaPlayer.getDuration();
        if (playbackInfoListener != null) {
            playbackInfoListener.onDurationChanged(duration);
            playbackInfoListener.onPositionChanged(0);
            Log.d(tag, String.format("firing setPlaybackDuration(%d sec)",
                                  TimeUnit.MILLISECONDS.toSeconds(duration)));
            Log.d(tag, "firing setPlaybackPosition(0)");
        }
    }

    private final Context context;
    private ImprovedMediaPlayer mediaPlayer;
    private int resId;
    private PlaybackInfoListener playbackInfoListener;
    private ScheduledExecutorService scheduleExecuter;
    
    private final static String tag = "MediaPlayerHolder";

}
