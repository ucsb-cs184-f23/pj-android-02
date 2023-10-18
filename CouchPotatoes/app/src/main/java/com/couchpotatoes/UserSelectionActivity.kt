package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class UserSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        val requesterButton = findViewById<Button>(R.id.requesterButton)
        requesterButton.setOnClickListener {
            val intent = Intent(this, RequesterActivity::class.java)
            startActivity(intent)
        }

        val dasherButton = findViewById<Button>(R.id.dasherButton)
        dasherButton.setOnClickListener {
            val intent = Intent(this, DasherActivity::class.java)
            startActivity(intent)
        }

    }
}