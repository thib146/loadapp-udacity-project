package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0

    private var urlSelected: String = ""
    private var fileName: String = ""

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var action: NotificationCompat.Action

    private val NOTIFICATION_ID = 146

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )

        binding.lifecycleOwner = this

        val glideRadioButton = binding.content.radioButtonGlide
        val loadAppRadioButton = binding.content.radioButtonLoadapp
        val retrofitRadioButton = binding.content.radioButtonRetrofit

        glideRadioButton.setOnClickListener {
            urlSelected = URL_GLIDE_REPO
            fileName = getString(R.string.radio_button_glide_text)
            loadAppRadioButton.isChecked = false
            retrofitRadioButton.isChecked = false
        }

        loadAppRadioButton.setOnClickListener {
            urlSelected = URL_LOADAPP_REPO
            fileName = getString(R.string.radio_button_loadapp_text)
            glideRadioButton.isChecked = false
            retrofitRadioButton.isChecked = false
        }

        retrofitRadioButton.setOnClickListener {
            urlSelected = URL_RETROFIT_REPO
            fileName = getString(R.string.radio_button_retrofit_text)
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

            // Finish the loading button animation
            binding.content.customButton.buttonState = ButtonState.Loading

            val status = getDownloadStatus(id)

            // Send a notification to the user
            sendNotification(context, status, fileName)
        }
    }

    private fun getDownloadStatus(downloadId: Long?): String {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId ?: 0)
        val cursor = downloadManager.query(query)

        var statusCode = 0
        if (cursor.moveToFirst()) {
            val column = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            statusCode = cursor.getInt(column)
        }

        return when (statusCode) {
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_success_text)
            else -> getString(R.string.download_fail_text)
        }
    }

    private fun sendNotification(context: Context?, status: String, fileName: String) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(DOWNLOAD_STATUS, status)
        contentIntent.putExtra(FILE_NAME, fileName)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.download_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                applicationContext.getString(R.string.notification_button),
                pendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager = ContextCompat.getSystemService(
            context!!,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download complete"

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL_GLIDE_REPO =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_LOADAPP_REPO =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT_REPO =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"

        const val DOWNLOAD_STATUS = "downloadStatus"
        const val FILE_NAME = "fileName"

        private const val CHANNEL_ID = "channelId"
    }
}