package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HW2LandingPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hw2_landing_page)


        val name = intent.getStringExtra("userName")
        val email = intent.getStringExtra("userEmail")

        findViewById<TextView>(R.id.HW2Username).text = name
        findViewById<TextView>(R.id.HW2Email).text = email

        findViewById<Button>(R.id.NavigateButton).setOnClickListener {
            val intent = Intent(this, HW2NewPageActivity::class.java)
            startActivity(intent)
        }
    }
}
