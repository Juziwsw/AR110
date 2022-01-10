package com.example.mediaengine.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


import com.hileia.common.utils.XLog;
import com.hiscene.mediaengine.R;

import java.util.Stack;


/**
 * @author xujiangang
 * @date 30/03/2018
 * Email: jiangang.xu@hiscene.com
 */
public class SoundUtil {
    private static final String TAG = "SoundUtil";
    private static SoundUtil instance = null;
    private final float leftVolume = 0.5f;
    private final float rightVolume = 0.5f;
    private int soundPlayId = 0;
    private SoundPool soundPool;
    private int soundIdRing;
    private int soundIdEndCall;
    private int soundIdShoot;
    private int soundIdMsg;
    private Stack<Integer> stack;
    AudioManager mAudioManager;
    private boolean speakerWasOn;
    private int speakerPrevMode;
    private boolean bthA2dpWasOn;
    private boolean bthScoWasOn;

    private SoundUtil(Context context) {
        stack = new Stack<>();
        soundPool = new SoundPool(4, AudioManager.STREAM_SYSTEM, 5);
        soundIdEndCall = soundPool.load(context, R.raw.call_end, 1);
        soundIdShoot = soundPool.load(context, R.raw.screenshot, 1);
        soundIdRing = soundPool.load(context, R.raw.call_ringtone, 1);
        soundIdMsg = soundPool.load(context, R.raw.receive_msg, 1);
        mAudioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static SoundUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (SoundUtil.class) {
                if (instance == null) {
                    instance = new SoundUtil(context);
                }
            }
        }
        return instance;
    }

    public static void release() {
        if (instance != null && instance.soundPool != null) {
            instance.soundPool.release();
            instance = null;
        }
    }

    public void playRing() {
        XLog.i(TAG, "playRing");
        if (soundPool != null && mAudioManager != null) {
            speakerWasOn = mAudioManager.isSpeakerphoneOn();
            speakerPrevMode = mAudioManager.getMode();
            bthA2dpWasOn = mAudioManager.isBluetoothA2dpOn();
            bthScoWasOn = mAudioManager.isBluetoothScoOn();
            if (bthA2dpWasOn && bthScoWasOn) {
                mAudioManager.setMode(AudioManager.STREAM_VOICE_CALL);
                mAudioManager.setBluetoothA2dpOn(true);
                mAudioManager.setBluetoothScoOn(true);
            } else {
                mAudioManager.setSpeakerphoneOn(false);
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            soundPlayId = soundPool.play(soundIdRing, leftVolume, rightVolume, 1, -1, 1);
            stack.push(soundPlayId);
        }
    }

    public void stopRing() {
        XLog.i(TAG, "stopRing");
        if (soundPool != null) {
            for (int j = 0; j < stack.size(); j++) {
                soundPool.stop(stack.pop());
            }
        }
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(speakerWasOn);
            mAudioManager.setMode(speakerPrevMode);
            mAudioManager.setBluetoothA2dpOn(bthA2dpWasOn);
            mAudioManager.setBluetoothScoOn(bthScoWasOn);
        }
    }

    public void playShootSound() {
        XLog.i(TAG, "playShootSound");
        if (soundPool != null) {
            soundPool.play(soundIdShoot, leftVolume, rightVolume, 1, 0, 1);
        }
    }

    public void playEndCallSound() {
        XLog.i(TAG, "playEndCallSound");
        if (soundPool != null) {
            soundPool.play(soundIdEndCall, leftVolume, rightVolume, 1, 0, 1);
        }
    }

    public void playReceiveMsgSound() {
        XLog.i(TAG, "playReceiveMsgSound");
        if (soundPool != null) {
            soundPool.play(soundIdMsg, leftVolume, rightVolume, 1, 0, 1);
        }
    }

    /**
     * true: 播放， false: 暂停
     *
     * @param context
     */
    public void requestAudioFocus(Context context, boolean play) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //先判断后台是否再播放音乐
        if (audioManager.isMusicActive()) {
            if (play) {
                requestAudioFocus();
            } else {
                abandonAudioFocus();
            }
        }
    }

    private boolean mAudioFocus;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {

                case AudioManager.AUDIOFOCUS_GAIN:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                    XLog.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                default:

                    XLog.i(TAG, "AudioFocusChange focus = " + focusChange);

                    break;

            }

        }

    };

    private void requestAudioFocus() {

        XLog.i(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);

        if (!mAudioFocus) {

            int result = mAudioManager.requestAudioFocus(afChangeListener,

                    AudioManager.STREAM_MUSIC, // Use the music stream.

                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                mAudioFocus = true;

            } else {

                XLog.e(TAG, "AudioManager request Audio Focus result = " + result);

            }

        }

    }

    private void abandonAudioFocus() {

        XLog.i(TAG, "abandonAudioFocus mAudioFocus = " + mAudioFocus);

        if (mAudioFocus) {

            mAudioManager.abandonAudioFocus(afChangeListener);

            mAudioFocus = false;

        }

    }
}
