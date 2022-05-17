package com.github.crayonxiaoxin.test_audio_record

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class NfcActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)

//        val defaultAdapter = NfcAdapter.getDefaultAdapter(this)
//        if (defaultAdapter == null) {
//            Toast.makeText(this, "您的设备不支持NFC", Toast.LENGTH_SHORT).show()
//        } else {
//            if (!defaultAdapter.isEnabled) {
//                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
//            } else {
//
//            }
//        }
        NfcUtils(this)
    }

    override fun onResume() {
        super.onResume()
        NfcUtils.mNfcAdapter?.enableForegroundDispatch(
            this,
            NfcUtils.mPendingIntent,
            NfcUtils.mIntentFilter,
            NfcUtils.mTechList
        )
    }

    override fun onPause() {
        super.onPause()
        NfcUtils.mNfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
//            val str = NfcUtils.readNFCFromTag(intent)
//            Log.e("TAG", "onNewIntent: $str")
            if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.id?.let {
                    Log.e("TAG", "onNewIntent: tag = $it, ${NfcUtils.ByteArrayToHexString(it)}" )
                }
            }
//            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
//                intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
//                    ?.also { rawMessages ->
//                        val message = rawMessages.map { it as NdefMessage }
//                        message.forEach {
//                            it.records.forEach {
//                                val res = it.payload.toString()
//                                Log.e("TAG", "onNewIntent: nfc result = $res")
//                            }
//                        }
//                    }
//            }

        }

    }
}