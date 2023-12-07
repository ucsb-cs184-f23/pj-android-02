package com.couchpotatoes.currentJob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.classes.Job
import com.couchpotatoes.R

class CurrentJobsAdapter(private val jobsList: List<Job>, private val userEmail: String?) : RecyclerView.Adapter<CurrentJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val requesterName = view.findViewById<TextView>(R.id.requesterName)
        val requesterEmail = view.findViewById<TextView>(R.id.requesterEmail)
        val item = view.findViewById<TextView>(R.id.item)
        val price = view.findViewById<TextView>(R.id.price)
        val store = view.findViewById<TextView>(R.id.store)
        val deliveryAddress = view.findViewById<TextView>(R.id.deliveryAddress)
        val status = view.findViewById<TextView>(R.id.status)
        val cancelButton = view.findViewById<Button>(R.id.cancel)
        val completeButton = view.findViewById<Button>(R.id.complete)
        // Initialize other views
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.current_job_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobsList[position]
        holder.requesterName.text = job.requesterName
        holder.requesterEmail.text = job.requesterEmail
        holder.item.text = job.item
        holder.price.text = job.price.toString()
        holder.store.text = job.store
        holder.deliveryAddress.text = job.deliveryAddress
        holder.status.text = job.status

        if (userEmail == job.requesterEmail) {
            // Do stuff
        }
        holder.cancelButton.setOnClickListener { /* Implement cancel functionality */ }
        holder.completeButton.setOnClickListener { /* Implement complete functionality */ }

    }


    override fun getItemCount() = jobsList.size
}
