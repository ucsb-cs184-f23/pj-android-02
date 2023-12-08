package com.couchpotatoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.couchpotatoes.jobBoard.JobBoardActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        checkReviewList()
    }

    private fun checkReviewList() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        var reviewList = mutableListOf<String>()
        database.child("users").child(userId).child("usersToReview").get().addOnSuccessListener { snapshot ->
            val list = snapshot.value as? MutableList<String>
            if (list != null) {
                reviewList.addAll(list)
                showNextRating(reviewList, 0)
            }
        }
        database.child("users").child(userId).child("usersToReview").setValue(null)
    }

    private fun showNextRating(reviewList: MutableList<String>, index: Int) {
        if (index >= reviewList.size) return // Exit if no more items

        val currentUserId = reviewList[index]
        var totalJobs = 0
        var totalRating = 0

        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.activity_rating_system, null)
        bottomSheetDialog.setContentView(view)

        // Set the height of the bottom sheet
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val layoutParams = bottomSheet?.layoutParams
        layoutParams?.height = 800
        bottomSheet?.layoutParams = layoutParams

        val closeButton = view.findViewById<Button>(R.id.ratingCloseButton)
        closeButton.setOnClickListener {
            showNextRating(reviewList, index + 1)
            bottomSheetDialog.dismiss()
        }

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        var deliveryRating = 0.0
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            // Use the rating value. It will be a value between 1 and 5.
            deliveryRating = rating.toDouble()
        }

        database.child("users").child(currentUserId).child("totalJobs").get().addOnSuccessListener { snapshot ->
            val total = snapshot.value as? Int
            if (total != null) {
                totalJobs = total
            }
        }

        database.child("users").child(currentUserId).child("totalRating").get().addOnSuccessListener { snapshot ->
            val total = snapshot.value as? Int
            if (total != null) {
                totalRating = total
            }
        }

        val submitButton = view.findViewById<Button>(R.id.ratingSubmitButton)
        submitButton.setOnClickListener{
            database.child("users").child(currentUserId).child("Rating").get().addOnSuccessListener { snapshot ->
                val currentRating = snapshot.value as? Double
                if (currentRating != null) {
                    database.child("users").child(currentUserId).child("Rating").setValue((deliveryRating + totalRating) / totalJobs)
                }
                else {
                    database.child("users").child(currentUserId).child("Rating").setValue(deliveryRating)
                }

                database.child("users").child(currentUserId).child("totalJobs").get().addOnSuccessListener { snapshot ->
                    val total = snapshot.value as? Int
                    if (total != null) {
                        database.child("users").child(currentUserId).child("totalJobs")
                            .setValue(total + 1)
                    } else {
                        database.child("users").child(currentUserId).child("totalJobs").setValue(1)
                    }

                    database.child("users").child(currentUserId).child("totalRatings").get()
                        .addOnSuccessListener { snapshot ->
                            val total = snapshot.value as? Int
                            if (total != null) {
                                database.child("users").child(currentUserId).child("totalRatings")
                                    .setValue(total + deliveryRating)
                            } else {
                                database.child("users").child(currentUserId).child("totalRatings")
                                    .setValue(deliveryRating)
                            }
                        }
                    }
            }
            showNextRating(reviewList, index + 1)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}