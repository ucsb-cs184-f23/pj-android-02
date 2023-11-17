package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.couchpotatoes.classes.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("Google sign-in", "Google sign in failed", e)
                }
            }
        }

        val signInButton: Button = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Google sign-in", "signInWithCredential:success")
                    val currentUser = auth.currentUser

                    // add user to database if they don't exist already
                    // under their uid (can improve later, easy solution for now)
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid

                    // create user
                    val user = User(currentUser?.displayName, currentUser?.email)
                    val userRef = database.child("users")
                    userRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val checkUser = dataSnapshot.child(userId).child("Name").exists()
                            if (checkUser) {
                                val intent = Intent(this@MainActivity, UserSelectionActivity::class.java)
                                startActivity(intent)
                            }
                            else {
                                // add to database
                                database.child("users").child(userId).setValue(user)
                                val intent = Intent(this@MainActivity, UserSetupActivity::class.java)
                                startActivity(intent)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("TAG", "Failed to read value", databaseError.toException())
                        }
                    })
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google sign-in", "signInWithCredential:failure", task.exception)
                }
            }
    }
}
