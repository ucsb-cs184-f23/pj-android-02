package com.couchpotatoes.jobBoard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job


class JobBoardAdapter (private val jobList: ArrayList<Job>) : RecyclerView.Adapter<JobBoardAdapter.ViewHolder>() {
    private var itemCount = jobList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        lateinit var job: Job

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.job_item)
        }

        init {
            val moreDetailsButton = view.findViewById<Button>(R.id.moreDetailsButton)
            moreDetailsButton.setOnClickListener {
                val intent = Intent(textView.context, JobItemActivity::class.java).apply {
                    putExtra("job", job)
                }
                textView.context.startActivity(intent)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.job_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.job = jobList[position]

        // basic information to display on job card
        val display = "Requester: ${jobList[position].requesterName.toString()} \nItem: ${jobList[position].item.toString()} \nStore: ${jobList[position].store.toString()}"

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = display
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = itemCount
}