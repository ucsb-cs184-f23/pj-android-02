package com.couchpotatoes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import com.couchpotatoes.classes.Job
import com.couchpotatoes.currentJob.CurrentJobActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import java.util.UUID


class RequestFormActivity : BaseActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var isAllFieldsChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_form)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        createNavMenu(R.id.my_toolbar, this, auth)

        val categoryDropdown = findViewById<Spinner>(R.id.categoryDropdown)

        val categories = arrayOf("Groceries", "Food", "Other")
        val arrayAdp = ArrayAdapter(this@RequestFormActivity, android.R.layout.simple_spinner_dropdown_item, categories)
        categoryDropdown.adapter = arrayAdp

        val hourPicker = findViewById<NumberPicker>(R.id.hourPicker)
        val dayPicker = findViewById<NumberPicker>(R.id.dayPicker)

        hourPicker.maxValue = 23
        hourPicker.minValue = 1

        dayPicker.maxValue = 7
        dayPicker.minValue = 0

        database = Firebase.database.reference

        val userRef = database.child("users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val initialAddress = dataSnapshot.child(userId).child("Address")
                if (initialAddress.exists()) {
                    findViewById<EditText>(R.id.address).setText(initialAddress.value.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "Failed to read value", databaseError.toException())
            }
        })
    }

    fun submitButtonHandler(view: View) {
        //Decide what happens when the user clicks the submit button
        database = Firebase.database.reference
        val userId = auth.currentUser!!.uid

        val whatEditText = findViewById<View>(R.id.what) as EditText
        val what = whatEditText.text.toString()
        val whereEditText = findViewById<View>(R.id.where) as EditText
        val where = whereEditText.text.toString()
        val costEditText = findViewById<View>(R.id.cost) as EditText
        val cost = costEditText.text.toString()
        val addressEditText = findViewById<View>(R.id.address) as EditText
        val address = addressEditText.text.toString()
        val durationEditHours = findViewById<NumberPicker>(R.id.hourPicker)
        val durationHours = durationEditHours.value
        val durationEditDays = findViewById<NumberPicker>(R.id.dayPicker)
        val durationDays = durationEditDays.value
        val categoryEdit = findViewById<Spinner>(R.id.categoryDropdown)
        val category = categoryEdit.selectedItem.toString()

        val expirationTime = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000) + (durationDays * 24 * 60 * 60 * 1000)

        // switch to better system later
        val jobId = UUID.randomUUID().toString()

        database.child("users").child(userId).child("rating").get().addOnSuccessListener { snapshot ->
            val rating = snapshot.value as? Double ?: (snapshot.value as? Long)?.toDouble()

            if (rating != null) {
                isAllFieldsChecked = checkAllFields()

                if (isAllFieldsChecked) {
                    // create job
                    val job = Job(
                        jobId,
                        FirebaseAuth.getInstance().currentUser!!.displayName,
                        FirebaseAuth.getInstance().currentUser!!.email,
                        what,
                        cost,
                        where,
                        address,
                        expirationTime,
                        category,
                        "pending",
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        null,
                        rating)

                    val user = FirebaseAuth.getInstance().currentUser

                    database.child("jobs").child(jobId).setValue(job)
                    database.child("users").child(user!!.uid).child("currentJobs").get().addOnSuccessListener { dataSnapshot ->
                        val jobs = dataSnapshot.value as? MutableList<String> ?: mutableListOf()
                        job.uid?.let { jobs.add(it) }
                        database.child("users").child(user!!.uid).child("currentJobs").setValue(jobs)
                    }
                    val confirmationPopup = Popup(this)
                    confirmationPopup.showConfirm()
                }
                else{
                    val confirmationPopup = Popup(this)
                    confirmationPopup.showError()
                }
            }
            else {
                isAllFieldsChecked = checkAllFields()

                if (isAllFieldsChecked) {
                    // create job
                    val job = Job(
                        jobId,
                        FirebaseAuth.getInstance().currentUser!!.displayName,
                        FirebaseAuth.getInstance().currentUser!!.email,
                        what,
                        cost,
                        where,
                        address,
                        expirationTime,
                        category,
                        "pending",
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        null,
                        0.0)

                    val user = FirebaseAuth.getInstance().currentUser

                    database.child("jobs").child(jobId).setValue(job)
                    database.child("users").child(user!!.uid).child("currentJobs").get().addOnSuccessListener { dataSnapshot ->
                        val jobs = dataSnapshot.value as? MutableList<String> ?: mutableListOf()
                        job.uid?.let { jobs.add(it) }
                        database.child("users").child(user!!.uid).child("currentJobs").setValue(jobs)
                    }
                    val confirmationPopup = Popup(this)
                    confirmationPopup.showConfirm()
                }
                else{
                    val confirmationPopup = Popup(this)
                    confirmationPopup.showError()
                }
            }
        }
    }

    private fun checkAllFields(): Boolean {
        var passed = true
        val whatEditText = findViewById<EditText>(R.id.what)
        val whereEditText = findViewById<EditText>(R.id.where)
        val costEditText = findViewById<EditText>(R.id.cost)
        val addressEditText = findViewById<EditText>(R.id.address)

        if (whatEditText.length() === 0) {
            whatEditText.error = "This Field is Required"
            passed = false
        }
        if (whereEditText.length() === 0) {
            whereEditText.error = "This Field is Required"
            passed = false
        }
        if (costEditText.length() === 0) {
            costEditText.error = "This Field is Required"
            passed = false
        }
        if (addressEditText.length() === 0) {
            addressEditText.error = "This Field is Required"
            passed = false
        }

        // after all validation return true.
        if (passed) {
            return true
        }
        return false
    }
}

class Popup(private val context: Context) {
    private lateinit var dialog: Dialog
    fun showConfirm() {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.request_confirmation)

        val okButton: Button = dialog.findViewById(R.id.okButton)

        okButton.setOnClickListener {
            dialog.dismiss()
            // Redirect to Current Job Page
            val intent = Intent(context, CurrentJobActivity::class.java)
            context.startActivity(intent)
        }

        dialog.show()
    }

    fun showError() {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.request_error)

        val okButton: Button = dialog.findViewById(R.id.okButton)

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}