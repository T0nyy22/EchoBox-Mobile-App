package com.example.echobox

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageDetailsActivity : ComponentActivity() {

    private val dao by lazy {
        MessagesDatabase.getInstance(applicationContext).messagesDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_details)
        findViewById<TextView>(R.id.btnClose).setOnClickListener {
            finish()
        }

        val messageId = intent.getIntExtra("message_id", -1)

        if (messageId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            val message = withContext(Dispatchers.IO) {
                dao.getMessageById(messageId)
            }

            if (message == null) {
                finish()
                return@launch
            }

            findViewById<TextView>(R.id.tvCapsuleTitle).text = message.title
            findViewById<TextView>(R.id.tvCategory).text =
                "Category: ${message.category}"

            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd hh:mm a",
                Locale.getDefault()
            )

            findViewById<TextView>(R.id.tvTargetDate).text =
                "Target date: ${dateFormat.format(Date(message.targetedDate))}"

            findViewById<TextView>(R.id.tvLockStatus).text =
                if (message.isUnlocked) "Unlocked" else "Locked"

            findViewById<TextView>(R.id.tvMessageContent).text =
                if (message.isUnlocked) {
                    message.message
                } else {
                    "This message is locked until its target date."
                }

            val imageView = findViewById<ImageView>(R.id.ivCapsuleImage)

            if (message.isUnlocked && !message.image.isNullOrBlank()) {
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(Uri.fromFile(File(message.image)))
            } else {
                imageView.visibility = View.GONE
            }
        }
    }
}