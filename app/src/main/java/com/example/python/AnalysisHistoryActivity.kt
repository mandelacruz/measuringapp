package com.example.python

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class AnalysisHistoryActivity : AppCompatActivity() {
    // Declare database handler, ListView, and adapter
    private lateinit var dbHandler: DataBaseHandler
    private lateinit var listView: ListView
    private lateinit var adapter: AnalysisCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analysis_history)

        // Initialize the database handler
        dbHandler = DataBaseHandler(this)

        // Link the ListView to its corresponding view
        listView = findViewById(R.id.analysisListView)

        // Retrieve all analysis records from the database
        val cursor = dbHandler.getAllAnalysis()

        // Initialize the adapter with the cursor and set it to the ListView
        adapter = AnalysisCursorAdapter(this, cursor)
        listView.adapter = adapter

        // Set an item click listener for the ListView
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Move the cursor to the clicked item's position
            cursor.moveToPosition(position)

            // Get the analysis ID of the clicked item
            val analysisId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))

            // Start the AnalysisHistoryDetailsActivity, passing the analysis ID
            val intent = Intent(this, AnalysisHistoryDetailsActivity::class.java)
            intent.putExtra("ANALYSIS_ID", analysisId)
            startActivity(intent)
        }
    }

    // Close the cursor and database handler when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        adapter.cursor.close()
        dbHandler.close()
    }
}
