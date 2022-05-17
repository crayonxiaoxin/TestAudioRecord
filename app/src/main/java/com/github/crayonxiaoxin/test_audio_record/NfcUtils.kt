package com.github.crayonxiaoxin.test_audio_record

import android.app.Activity
import com.github.crayonxiaoxin.test_audio_record.NfcUtils
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Parcelable
import android.provider.Settings
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.Throws

class NfcUtils(activity: Activity) {
    companion object {
        //nfc  
        var mNfcAdapter: NfcAdapter? = null
        var mIntentFilter: Array<IntentFilter>? = null
        var mPendingIntent: PendingIntent? = null
        var mTechList: Array<Array<String>>? = null

        /**
         * 检查NFC是否打开
         */
        fun NfcCheck(activity: Activity): NfcAdapter? {
            val mNfcAdapter: NfcAdapter = NfcAdapter.getDefaultAdapter(activity)
            if (mNfcAdapter == null) {
                return null
            } else {
                if (!mNfcAdapter.isEnabled()) {
                    val setNfc = Intent(Settings.ACTION_NFC_SETTINGS)
                    activity.startActivity(setNfc)
                }
            }
            return mNfcAdapter
        }

        /**
         * 初始化nfc设置
         */
        fun NfcInit(activity: Activity) {
            mPendingIntent = PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            val filter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            val filter2 = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            try {
                filter.addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                e.printStackTrace()
            }
            mIntentFilter = arrayOf<IntentFilter>(filter, filter2)
            mTechList = null
        }

        /**
         * 读取NFC的数据
         */
        @Throws(UnsupportedEncodingException::class)
        fun readNFCFromTag(intent: Intent): String {
            val rawArray: Array<Parcelable>? =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawArray != null) {
                val mNdefMsg: NdefMessage = rawArray[0] as NdefMessage
                val mNdefRecord: NdefRecord = mNdefMsg.getRecords().get(0)
                if (mNdefRecord != null) {
                    return String(mNdefRecord.getPayload(), Charset.forName("UTF-8"))
                }
            }
            return ""
        }

        /**
         * 往nfc写入数据
         */
        @Throws(IOException::class, FormatException::class)
        fun writeNFCToTag(data: String?, intent: Intent) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            val ndef: Ndef = Ndef.get(tag)
            ndef.connect()
            val ndefRecord: NdefRecord = NdefRecord.createTextRecord(null, data)
            val records: Array<NdefRecord> = arrayOf<NdefRecord>(ndefRecord)
            val ndefMessage = NdefMessage(records)
            ndef.writeNdefMessage(ndefMessage)
        }

        /**
         * 读取nfcID
         */
        @Throws(UnsupportedEncodingException::class)
        fun readNFCId(intent: Intent): String {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            return ByteArrayToHexString(tag?.getId()?: byteArrayOf())
        }

        /**
         * 将字节数组转换为字符串
         */
        fun ByteArrayToHexString(inarray: ByteArray): String {
            var i: Int
            var j: Int
            var `in`: Int
            val hex = arrayOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F"
            )
            var out = ""
            j = 0
            while (j < inarray.size) {
                `in` = inarray[j].toInt() and 0xff
                i = `in` shr 4 and 0x0f
                out += hex[i]
                i = `in` and 0x0f
                out += hex[i]
                ++j
            }
            return out
        }
    }

    /**
     * 构造函数，用于初始化nfc
     */
    init {
        mNfcAdapter = NfcCheck(activity)
        NfcInit(activity)
    }
}