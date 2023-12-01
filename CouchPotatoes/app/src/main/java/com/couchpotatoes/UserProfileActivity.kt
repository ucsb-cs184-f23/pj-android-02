package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserProfileActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        createNavMenu(R.id.my_toolbar, this, auth)

        val userRef = database.child("users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(auth.currentUser!!.uid).child("Name")
                val address = dataSnapshot.child(auth.currentUser!!.uid).child("Address")
                val number = dataSnapshot.child(auth.currentUser!!.uid).child("Phone Number")
                val email = dataSnapshot.child(auth.currentUser!!.uid).child("email")

                findViewById<TextView>(R.id.textViewUserName).text = "Name: " + name.value.toString()
                findViewById<TextView>(R.id.textViewUserEmail).text = "Email: " + email.value.toString()
                findViewById<TextView>(R.id.textViewUserAddress).text = "Address: " + address.value.toString()
                findViewById<TextView>(R.id.textViewUserNumber).text = "Phone Number: " + number.value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "Failed to read value", databaseError.toException())
            }
        })

        val nameTextView = findViewById<TextView>(R.id.textViewUserName)
        val nameEditText = findViewById<EditText>(R.id.editViewUserName)
        val emailTextView = findViewById<TextView>(R.id.textViewUserEmail)
        val emailEditText = findViewById<EditText>(R.id.editViewUserEmail)
        val addressTextView = findViewById<TextView>(R.id.textViewUserAddress)
        val addressEditText = findViewById<EditText>(R.id.editViewUserAddress)
        val numberTextView = findViewById<TextView>(R.id.textViewUserNumber)
        val numberEditText = findViewById<EditText>(R.id.editViewUserNumber)
        val editButton = findViewById<Button>(R.id.ProfileEditButton)
        val submitButton = findViewById<Button>(R.id.ProfileSubmitButton)

        editButton.setOnClickListener {
            nameTextView.visibility = View.GONE
            nameEditText.setText(nameTextView.text.substring("Name: ".length))
            nameEditText.visibility = View.VISIBLE

            emailTextView.visibility = View.GONE
            emailEditText.setText(emailTextView.text.substring("Email: ".length))
            emailEditText.visibility = View.VISIBLE

            addressTextView.visibility = View.GONE
            addressEditText.setText(addressTextView.text.substring("Address: ".length))
            addressEditText.visibility = View.VISIBLE

            numberTextView.visibility = View.GONE
            numberEditText.setText(numberTextView.text.substring("Phone Number: ".length))
            numberEditText.visibility = View.VISIBLE

            editButton.visibility = View.GONE
            submitButton.visibility = View.VISIBLE
        }

        submitButton.setOnClickListener {
            val nameValue = nameEditText.text.toString()
            val emailValue = emailEditText.text.toString()
            val addressValue = addressEditText.text.toString()
            val numberValue = numberEditText.text.toString()

            database.child("users").child(auth.currentUser!!.uid).child("Name").setValue(nameValue)
            database.child("users").child(auth.currentUser!!.uid).child("email").setValue(emailValue)
            database.child("users").child(auth.currentUser!!.uid).child("Address").setValue(addressValue)
            database.child("users").child(auth.currentUser!!.uid).child("Phone Number").setValue(numberValue)

            nameTextView.visibility = View.VISIBLE
            nameEditText.visibility = View.GONE

            emailTextView.visibility = View.VISIBLE
            emailEditText.visibility = View.GONE

            addressTextView.visibility = View.VISIBLE
            addressEditText.visibility = View.GONE

            numberTextView.visibility = View.VISIBLE
            numberEditText.visibility = View.GONE

            editButton.visibility = View.VISIBLE
            submitButton.visibility = View.GONE
        }

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
