package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.couchpotatoes.classes.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    lateinit var gestureDetector: GestureDetector

    var x2:Float = 0.0f
    var x1:Float = 0.0f
    var y2:Float = 0.0f
    var y1:Float = 0.0f

    companion object {
        const val MIN_DISTANCE = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val userName = intent.getStringExtra("userName")
        val userEmail = intent.getStringExtra("userEmail")

        findViewById<TextView>(R.id.textViewUserName).text = userName
        findViewById<TextView>(R.id.textViewUserEmail).text = userEmail

        // add user to database
        // under their uid (can improve later, easy solution for now)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // create user
        val user = User(userName, userEmail)
        // add to database
        database.child("users").child(userId).setValue(user)

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

        gestureDetector = GestureDetector(this, this)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }

        when (event?.action) {
            0-> {
                x1 = event.x
                y1 = event.y
            }

            1 -> {
                x2 = event.x
                y2 = event.y

                val valueX:Float = x2-x1
                val valueY:Float = y2-y1

                if (kotlin.math.abs(valueX) > MIN_DISTANCE) {
                    if (x1 > x2) {
                        val intent = Intent(this, RequestFormActivity::class.java)
                        startActivity(intent)
                    }
                }

            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false

    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false
    }
}
