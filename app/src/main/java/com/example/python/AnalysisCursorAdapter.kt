package com.example.python

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView

class AnalysisCursorAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {

    // Creates a new view for each item in the ListView
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Inflate the layout for a single analysis history item
        return LayoutInflater.from(context).inflate(R.layout.analysis_history_item, parent, false)
    }

    // Binds the data from the cursor to the views in each list item
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val tag = "AnalysisCursorAdapter" // Tag for logging

        // Find the TextViews in the inflated layout
        val analysisIdTextView = view.findViewById<TextView>(R.id.analysis_id)
        val researcherTextView = view.findViewById<TextView>(R.id.researcher)
        val dateAnalyzedTextView = view.findViewById<TextView>(R.id.date_analyzed)

        // Log an error if any of the TextViews are null
        if (researcherTextView == null) Log.e(tag, "researcherTextView is null")
        if (analysisIdTextView == null) Log.e(tag, "analysisIdTextView is null")
        if (dateAnalyzedTextView == null) Log.e(tag, "dateAnalyzedTextView is null")

        // Extract data from the cursor
        val analysisId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
        val researcher = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESEARCHER))
        val dateAnalyzed = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_ANALYZED))

        // Populate the TextViews with the extracted data
        analysisIdTextView?.text = context.getString(R.string.analysisid_text, analysisId)
        researcherTextView?.text = researcher
        dateAnalyzedTextView?.text = dateAnalyzed
    }
}
