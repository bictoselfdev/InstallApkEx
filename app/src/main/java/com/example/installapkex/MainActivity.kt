package com.example.installapkex

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.installapkex.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val requestCodeSAF = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        requestPermission(this)

        binding.btnInstallApkWithAssets.setOnClickListener {

            val apkPath = filesDir.absolutePath + "/MyApplication.apk"
            val file = File(apkPath)
            if (!file.exists()) {
                saveApkFileFromAssets()
            }

            val uri = getUriByProvider(filesDir.absolutePath + "/MyApplication.apk")
            installApkFile(this, uri)
        }

        binding.btnInstallApkWithSAF.setOnClickListener {

            openFileSAF(this)
        }
    }

    private fun requestPermission(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivityForResult(intent, 100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installApkFile(activity: Activity, uri: Uri?) {

        uri?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(it, "application/vnd.android.package-archive")
            activity.startActivity(intent)
        }
    }

    private fun saveApkFileFromAssets() {

        try {
            val inputStream = assets.open("MyApplication.apk")
            val outPath = filesDir.absolutePath + "/MyApplication.apk"
            val outputStream = FileOutputStream(outPath)
            while (true) {
                val data = inputStream.read()
                if (data == -1) break
                outputStream.write(data)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUriByProvider(path: String): Uri? {
        var uri: Uri? = null
        try {
            val file = File(path)
            if (file.exists()) {
                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri
    }

    private fun openFileSAF(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.android.package-archive"
        }

        activity.startActivityForResult(intent, requestCodeSAF)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            when (requestCode) {
                requestCodeSAF -> {
                    val uri = data?.data
                    uri?.let { installApkFile(this, it) }
                }
            }
        }
    }
}