package com.hiar.ar110.fragment

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity.Companion.isPushing
import com.hiar.ar110.adapter.AudioPlaylistAdapter
import com.hiar.ar110.adapter.AudioPlaylistAdapter.OnAudioItemClickListener
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.ProgressViewStatus
import com.hiar.ar110.data.audio.AudioFileInfo
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.helper.NavigationHelper.Companion.instance
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.AudioUploadViewModel
import com.hiar.ar110.widget.ProgressDialog
import kotlinx.android.synthetic.main.fragment_audio_upload.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

/**
 * 录音文件上传
 * create an instance of this fragment.
 */
class AudioUploadFragment : BaseFragment(), OnAudioItemClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private var mAdapter: AudioPlaylistAdapter? = null
    private var progressDialog: ProgressDialog? = null
    private var mAudioUploadViewModel: AudioUploadViewModel? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mLastPlayPosition = -1
    private var mHandler: Handler? = null
    override fun onBackPressed() {
        if (progressDialog!!.isShowing) {
            Util.showMessage("正在上传文件，请稍后")
        } else {
            instance.backToHandleJD(this)
        }
        //        return true;
    }

    override fun onHileiaComingCallCallback() {
        val act: Activity? = activity
        act?.runOnUiThread { layout_commu_phone!!.visibility = View.VISIBLE }
    }

    override fun onHileiaHangupCallback() {
        val act: Activity? = activity
        act?.runOnUiThread { layout_commu_phone!!.visibility = View.GONE }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MSG_UPDATE_PLAY_PROGRESS) {
                    if (null != mMediaPlayer && mMediaPlayer!!.isPlaying) {
                        var playPos = mMediaPlayer!!.currentPosition
                        audio_play_seekbar!!.progress = playPos
                        playPos /= 1000
                        val minute = playPos / 60
                        val second = playPos % 60
                        val playTime = String.format("%02d:%02d", minute, second)
                        text_play_start!!.text = playTime
                        updateProgress()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isPushing) {
            layout_commu_phone!!.visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                mAudioUploadViewModel!!.refreshAudio()
            }
        }
        mAdapter!!.setAudioListener(this)
    }

    override fun onStop() {
        super.onStop()
        mAdapter!!.setAudioListener(null)
        mHandler!!.removeCallbacksAndMessages(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_audio_upload
    }

    override fun initData() {
        mAudioUploadViewModel = getViewModel(AudioUploadViewModel::class.java)
        val mCopTask: CopTaskRecord? = arguments!!.getParcelable("key_cop_task")
        val mPatrolTask: PatrolRecord? = arguments!!.getParcelable("key_patrol_task")
        mAudioUploadViewModel!!.setCopTask(mCopTask)
        mAudioUploadViewModel!!.setPatrolTask(mPatrolTask)
        if (mCopTask != null) {
            mAudioUploadViewModel!!.setCjdbh(mCopTask.cjdbh2)
        } else if (mPatrolTask != null) {
            mAudioUploadViewModel!!.setCjdbh(mPatrolTask.number)
        }
    }

    override fun initView(view: View) {
        mAdapter = AudioPlaylistAdapter(this, mAudioUploadViewModel!!.audioRootPath)
        val mLinearLayoutManager = LinearLayoutManager(activity)
        audio_recyclerview.layoutManager = mLinearLayoutManager
        audio_recyclerview.adapter = mAdapter
        progressDialog = ProgressDialog(activity!!)
    }

    override fun initListener() {
        img_play_icon!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
                    mMediaPlayer!!.pause()
                    img_play_icon!!.setImageResource(R.drawable.ic_icon_audioplay_pause)
                    mHandler!!.removeCallbacksAndMessages(this)
                    return
                }
                if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying) {
                    val pos = mMediaPlayer!!.currentPosition
                    if (pos == 0) {
                        playMusic(mAudioUploadViewModel!!.audioList.value!![mLastPlayPosition].absPath)
                        return
                    }
                    mMediaPlayer!!.start()
                    img_play_icon!!.setImageResource(R.drawable.ic_icon_audioplay_playing)
                    updateProgress()
                }
            }
        })
        audio_play_seekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val seekPos = seekBar.progress
                if (mMediaPlayer != null) {
                    mMediaPlayer!!.seekTo(seekPos)
                }
            }
        })
        img_upload!!.setOnClickListener {  mAudioUploadViewModel!!.uploadAudio() }
        layout_back!!.setOnClickListener {
            if (progressDialog!!.isShowing) {
                return@setOnClickListener
            }
            instance.backToHandleJD(this@AudioUploadFragment)
        }
        mAudioUploadViewModel!!.audioList.observe(this, { audioFileInfos: ArrayList<AudioFileInfo>? ->
            val act: Activity? = activity
            act?.runOnUiThread { mAdapter!!.setAdapter(audioFileInfos) }
        })
        mAudioUploadViewModel!!.progressDialogStatus.observe(this, { progressViewStatus: ProgressViewStatus ->
            when (progressViewStatus.state) {
                ProgressViewStatus.SHOW -> {
                    progressDialog!!.setMaxProgress(progressViewStatus.maxProgress)
                    progressDialog!!.show()
                }
                ProgressViewStatus.UPDATE -> progressDialog!!.refreshProgress(progressViewStatus.progress,
                        progressViewStatus.filename)
                ProgressViewStatus.CANCEL -> {
                    progressDialog!!.dismiss()

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            mAudioUploadViewModel!!.refreshAudio()
                        }
                    }
                }
            }
        })
    }

    override fun onItemClicked(position: Int) {
        if (position == mLastPlayPosition) {
            if (mMediaPlayer != null) {
                if (mMediaPlayer!!.isPlaying) {
                    return
                }
            }
        }
        val mAudioList = mAudioUploadViewModel!!.audioList.value ?: return
        if (mAudioList.size == 0) {
            return
        }
        val audioInfo = mAudioList[position]
        val path = audioInfo.absPath
        mLastPlayPosition = position
        val fileName = audioInfo.mFileName
        var audioName = fileName.substring(0, fileName.indexOf(".mp3"))
        val timeArray = audioName.split("_").toTypedArray()
        timeArray[1] = timeArray[1].replace("-", ":")
        audioName = timeArray[0] + " " + timeArray[1]
        audio_cap_time!!.text = audioName
        audio_play_title!!.text = "采集序列" + (mAudioList.size - position)
        playMusic(path)
    }

    override fun onPrepared(mp: MediaPlayer) {
        //mp.start();
    }

    override fun onCompletion(mp: MediaPlayer) {
        mHandler!!.removeCallbacksAndMessages(this)
        audio_play_seekbar!!.progress = 0
        mp?.reset()
        text_play_start!!.text = "00:00"
        img_play_icon!!.setImageResource(R.drawable.ic_icon_audioplay_pause)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mHandler!!.removeCallbacksAndMessages(this)
        audio_play_seekbar!!.progress = 0
        mp?.reset()
        img_play_icon!!.setImageResource(R.drawable.ic_icon_audioplay_pause)
        text_play_start!!.text = "00:00"
        return true
    }

    private fun playMusic(path: String) {
        if (audio_play_seekbar!!.visibility != View.VISIBLE) {
            audio_play_seekbar!!.visibility = View.VISIBLE
            img_play_icon!!.visibility = View.VISIBLE
            no_play_notice!!.visibility = View.INVISIBLE
        }
        if (null == mMediaPlayer) {
            mMediaPlayer = MediaPlayer()
            try {
                mMediaPlayer!!.setDataSource(path)
                mMediaPlayer!!.prepare()
                mMediaPlayer!!.setOnErrorListener(this)
                mMediaPlayer!!.setOnCompletionListener(this)
                //mMediaPlayer.setOnPreparedListener(this);
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        } else {
            mMediaPlayer!!.reset()
            try {
                mMediaPlayer!!.setDataSource(path)
                mMediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        }
        layout_audio_play!!.visibility = View.VISIBLE
        mMediaPlayer!!.start()
        audio_play_seekbar!!.progress = 0
        audio_play_seekbar!!.max = mMediaPlayer!!.duration
        text_play_start!!.text = "00:00"
        val duration = mMediaPlayer!!.duration / 1000
        val minute = duration / 60
        val second = duration % 60
        val total = String.format("%02d:%02d", minute, second)
        text_audio_duration!!.text = total
        img_play_icon!!.setImageResource(R.drawable.ic_icon_audioplay_playing)
        updateProgress()
    }

    override fun onDestroy() {
        mHandler!!.removeMessages(MSG_UPDATE_PLAY_PROGRESS)
        if (null != mMediaPlayer) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.stop()
            }
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        super.onDestroy()
    }

    // 每间隔1s通知更新进度
    private fun updateProgress() {
        mHandler!!.sendEmptyMessageDelayed(MSG_UPDATE_PLAY_PROGRESS, 1000)
    }

    companion object {
        private const val MSG_UPDATE_PLAY_PROGRESS = 188
    }
}