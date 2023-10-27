package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()

        val userName = intent.getStringExtra("userName")
        val userEmail = intent.getStringExtra("userEmail")

        findViewById<TextView>(R.id.textViewUserName).text = userName
        findViewById<TextView>(R.id.textViewUserEmail).text = userEmail

        // Find the logout button and add a click listener
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            // Sign out from FirebaseAuth
            auth.signOut()

            // Sign out from GoogleSignInClient
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                // After signing out, redirect the user back to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                // Clear the activity stack to prevent the user from navigating back to the profile activity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
