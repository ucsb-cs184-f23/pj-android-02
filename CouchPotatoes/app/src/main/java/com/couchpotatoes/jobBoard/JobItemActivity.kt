package com.couchpotatoes.jobBoard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.couchpotatoes.BaseActivity
import com.couchpotatoes.currentJob.CurrentJobActivity
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class JobItemActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var job: Job
    var adapter: MyAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_item)

        auth = FirebaseAuth.getInstance()
        createNavMenu(R.id.my_toolbar, this, auth)

        job = intent.getSerializableExtra("job") as Job

        val users = arrayOf(
            arrayOf("Requester:", job.requesterName),
            arrayOf("Requester Email:", job.requesterEmail),
            arrayOf("Item:", job.item),
            arrayOf("Price:", job.price),
            arrayOf("Store:", job.store),
            arrayOf("Delivery Address:", job.deliveryAddress),
            arrayOf("Status:", job.status)
        )

        adapter = MyAdapter(this, users)
        var mListView = findViewById<ListView>(R.id.job_list)
        mListView.adapter = adapter
    }

    fun rejectButtonHandler(view: View) {
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setMessage("Are you sure you want to reject this job?")
            .setPositiveButton("Yes") { _, _ ->
                // Redirect to Job Board Page
                val intent = Intent(this, JobBoardActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun acceptButtonHandler(view: View) {
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setMessage("Are you sure you want to accept this job?")
            .setPositiveButton("Yes") { _, _ ->
                // Update the job status to accepted
                database = Firebase.database.reference
                database.child("jobs").child(job.uid.toString()).child("status").setValue("accepted")

                // Get the current user
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    database.child("jobs").child(job.uid.toString()).child("hustlerId").setValue(user.uid)
                }

                // Reference to the user's currentJobs node
                val currentJobsRef = Firebase.database.reference.child("users").child(user!!.uid).child("currentJobs")

                currentJobsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentJobIds = snapshot.value as? MutableList<String> ?: mutableListOf()
                        if (!currentJobIds.contains(job.uid.toString())) {
                            currentJobIds.add(job.uid.toString())
                            currentJobsRef.setValue(currentJobIds)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Listener was cancelled, error: ${error.toException()}")
                    }
                })
                val intent = Intent(this, CurrentJobActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

}

class MyAdapter(private val context: Context, private val arrayList: Array<Array<String?>>) : BaseAdapter() {
    private lateinit var key: TextView
    private lateinit var value: TextView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.activity_job_details_item, parent, false)
        key = convertView.findViewById(R.id.key)
        value = convertView.findViewById(R.id.value)
        key.text = arrayList[position][0]
        value.text = arrayList[position][1]
        return convertView
    }
}