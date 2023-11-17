package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserSetupActivity : BaseActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var isAllFieldsChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setup)
    }

    fun userSetupSubmitHandler(view: View) {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        isAllFieldsChecked = checkAllFields()

        val userSetupNameText = findViewById<EditText>(R.id.userSetupName).text.toString()
        val userSetupAddressText = findViewById<EditText>(R.id.userSetupAddress).text.toString()
        val userSetupNumberText = findViewById<EditText>(R.id.userSetupNumber).text.toString()

        if (isAllFieldsChecked) {
            val user = auth.currentUser

            database.child("users").child(user!!.uid).child("Name").setValue(userSetupNameText)
            database.child("users").child(user!!.uid).child("Address").setValue(userSetupAddressText)
            database.child("users").child(user!!.uid).child("Phone Number").setValue(userSetupNumberText)

            val intent = Intent(this, UserSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAllFields(): Boolean {
        var passed = true
        val userSetupName = findViewById<EditText>(R.id.userSetupName)
        val userSetupAddress = findViewById<EditText>(R.id.userSetupAddress)
        val userSetupNumber = findViewById<EditText>(R.id.userSetupNumber)

        if (userSetupName.length() === 0) {
            userSetupName.error = "Name is Required"
            passed = false
        }
        if (userSetupAddress.length() === 0) {
            userSetupAddress.error = "Home Address is Required"
            passed = false
        }
        if (userSetupNumber.length() === 0) {
            userSetupNumber.error = "Phone Number is Required"
            passed = false
        }

        // after all validation return true.
        if (passed) {
            return true
        }
        return false
    }
}