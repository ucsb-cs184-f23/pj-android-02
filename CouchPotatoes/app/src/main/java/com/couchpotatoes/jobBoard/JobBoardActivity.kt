package com.couchpotatoes.jobBoard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.R

class JobBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_board)

        val recycleView = findViewById<RecyclerView>(R.id.job_recycler_view)
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = JobBoardAdapter()
    }
}