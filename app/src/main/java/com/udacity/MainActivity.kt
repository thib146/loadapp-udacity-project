package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0

    private var urlSelected: String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.lifecycleOwner = this

        val glideRadioButton = binding.content.radioButtonGlide
        val loadAppRadioButton = binding.content.radioButtonLoadapp
        val retrofitRadioButton = binding.content.radioButtonRetrofit

        glideRadioButton.setOnClickListener {
            urlSelected = URL_GLIDE_REPO
            loadAppRadioButton.isChecked = false
            retrofitRadioButton.isChecked = false
        }

        loadAppRadioButton.setOnClickListener {
            urlSelected = URL_LOADAPP_REPO
            glideRadioButton.isChecked = false
            retrofitRadioButton.isChecked = false
        }

        retrofitRadioButton.setOnClickListener {
            urlSelected = URL_RETROFIT_REPO
            loadAppRadioButton.isChecked = false
            glideRadioButton.isChecked = false
        }

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.content.customButton.setOnClickListener {
            if (!glideRadioButton.isChecked && !loadAppRadioButton.isChecked && !retrofitRadioButton.isChecked) {
                Toast.makeText(this,
                    getString(R.string.no_radio_button_selected_toast_message), Toast.LENGTH_SHORT).show()
            } else {
                if (urlSelected.isNotEmpty()) download(urlSelected)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            binding.content.customButton.buttonState = ButtonState.Loading
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL_GLIDE_REPO =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_LOADAPP_REPO =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT_REPO =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"

        private const val CHANNEL_ID = "channelId"
    }
}