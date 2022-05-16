package com.github.crayonxiaoxin.test_audio_record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val TAG = this.javaClass.name
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.entries.forEach {
                if (it.value == false) {
                    Toast.makeText(this, "请授予权限：${it.key}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions.launch(permissions)

        initRecord()

        val record = findViewById<MaterialButton>(R.id.record)
        record.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    record.setText("正在录音")
                    startRecord()
                }
                MotionEvent.ACTION_UP -> {
                    record.setText("长按录音")
                    stopRecord()
                }
            }
            view.performClick()
        }

        val play = findViewById<MaterialButton>(R.id.play)
        play.setOnClickListener {
            playRecord()
        }

        val nfc = findViewById<MaterialButton>(R.id.nfc)
        nfc.setOnClickListener {
            startActivity(Intent(this, NfcActivity::class.java))
        }
    }

    private fun playRecord() {
        lifecycleScope.launch(Dispatchers.IO) {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val audioTrack =
                AudioTrack(
                    audioAttributes,
                    audioFormat,
                    minBufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            audioTrack.play()
            val filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val fis = FileInputStream(File(filePath, FILE_NAME))
            val byteArray = ByteArray(recordBufferSize)
            var len = 0
            try {
                while (fis.read(byteArray, 0, byteArray.size).also { len = it } != -1) {
                    audioTrack.write(byteArray, 0, len)
                }
                fis.close()
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
    private val recordBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private fun initRecord() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "没有录音权限", Toast.LENGTH_SHORT).show()
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            recordBufferSize
        )
    }

    val FILE_NAME = "test.pcm"
    var job: Job? = null
    private fun startRecord() {
        if (isRecording || audioRecord == null) return
        isRecording = true
        audioRecord!!.startRecording()
        Log.e(TAG, "startRecord: ")
        job = lifecycleScope.launch(Dispatchers.IO) {
            val filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val byteArray = ByteArray(recordBufferSize)
            filePath?.mkdirs()
            val file = File(filePath, FILE_NAME)
            var fos: FileOutputStream? = null
            try {
                if (!file.exists()) {
                    file.createNewFile()
                }
                fos = FileOutputStream(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (fos != null) {
                var read: Int = 0
                while (isRecording) {
                    read = audioRecord!!.read(byteArray, 0, recordBufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fos.write(byteArray)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                try {
                    fos.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopRecord() {
        Log.e(TAG, "stopRecord: ")
        isRecording = false
        if (job != null && audioRecord != null) {
            audioRecord!!.stop()
            job = null
        }
    }

    override fun onDestroy() {
        audioRecord?.release()
        super.onDestroy()
    }
}