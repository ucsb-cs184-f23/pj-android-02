package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.couchpotatoes.jobBoard.JobBoardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class UserSelectionActivity : BaseActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        createNavMenu(R.id.my_toolbar, this, auth)

        val requesterButton = findViewById<CardView>(R.id.requesterButton)
        requesterButton.setOnClickListener {
            // set user role to requester
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            database.child("users").child(userId).child("role").setValue("requester")

            val intent = Intent(this, RequestFormActivity::class.java)
            startActivity(intent)
        }

        val dasherButton = findViewById<CardView>(R.id.HustlerButton)
        dasherButton.setOnClickListener {
            // set user role to dasher
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            database.child("users").child(userId).child("role").setValue("dasher")

            val intent = Intent(this, JobBoardActivity::class.java)
            startActivity(intent)
        }

    }
}