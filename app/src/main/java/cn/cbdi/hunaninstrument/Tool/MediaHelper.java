package cn.cbdi.hunaninstrument.Tool;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.cbdi.hunaninstrument.AppInit;


public class MediaHelper {
    public enum Text {
        FacePast,Door_Open,Door_Close
    }

    private static MediaPlayer mediaPlayer;

    public static void mediaOpen() {
        if (AppInit.getInstrumentConfig().noise()) {
            mediaPlayer = new MediaPlayer();
            try {
                Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
                Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
                Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
                Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
                Constructor constructor = cSubtitleController.getConstructor(
                        new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
                Object subtitleInstance = constructor.newInstance(AppInit.getContext(), null, null);
                Field f = cSubtitleController.getDeclaredField("mHandler");
                f.setAccessible(true);
                try {
                    f.set(subtitleInstance, new Handler());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    f.setAccessible(false);
                }
                Method setsubtitleanchor = mediaPlayer.getClass().getMethod("setSubtitleAnchor",
                        cSubtitleController, iSubtitleControllerAnchor);
                setsubtitleanchor.invoke(mediaPlayer, subtitleInstance, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loudly() {
        if (AppInit.getInstrumentConfig().noise()) {
            AudioManager audioMgr = (AudioManager) AppInit.getContext().getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
            Log.e("信息提示", "打开音量");
        }
    }

    public static void play(Text text) {
        if (AppInit.getInstrumentConfig().noise()) {
            try {
                AssetFileDescriptor fileDescriptor;
                switch (text) {
                    case FacePast:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "人脸比对通过.mp3");
                        play(fileDescriptor);
                        break;
                    case Door_Open:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "开启库房门.mp3");
                        play(fileDescriptor);
                        break;
                    case Door_Close:
                        fileDescriptor = AppInit.getContext().getAssets()
                                .openFd("mp3" + File.separator + "锁库门.mp3");
                        play(fileDescriptor);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void play(AssetFileDescriptor fileDescriptor) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void mediaRealese() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            Log.e("信息提示", "mediaPlayer解除函数被触发");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}



