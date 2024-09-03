package com.example.python

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AnalysisHistoryDetailsActivity : AppCompatActivity() {
    // Declare UI elements and database handler
    private lateinit var dbHandler: DataBaseHandler
    private lateinit var analysisIdTextView: TextView
    private lateinit var grainImageView: ImageView
    private lateinit var tableLayout: TableLayout
    private lateinit var averageLengthTextView: TextView
    private lateinit var averageWidthTextView: TextView
    private lateinit var averageShapeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analysis_history_details)

        // Initialize the database handler
        dbHandler = DataBaseHandler(this)

        // Link UI elements to their corresponding views
        analysisIdTextView = findViewById(R.id.analysisIdTextView)
        grainImageView = findViewById(R.id.grainImageView)
        tableLayout = findViewById(R.id.table_layout)
        averageLengthTextView = findViewById(R.id.averageLengthTextView)
        averageWidthTextView = findViewById(R.id.averageWidthTextView)
        averageShapeTextView = findViewById(R.id.averageShapeTextView)

        // Get the analysis ID passed from the previous activity
        val analysisId = intent.getLongExtra("ANALYSIS_ID", -1)
        if (analysisId != -1L) {
            // Retrieve the analysis result from the database
            val cursor = dbHandler.getAnalysisResultById(analysisId.toInt())
            if (cursor.moveToFirst()) {
                // Extract and display the analysis ID
                val analysisIdStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_ANALYSIS_ID))
                analysisIdTextView.text = getString(R.string.analysis_id_text, analysisIdStr)


                // Display the grain image
                val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_IMAGE))
                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                grainImageView.setImageBitmap(bitmap)

                // Populate the table with analysis data
                populateTableLayout(analysisId.toInt())

                // Calculate and display the average values
                calculateAndDisplayAverages(analysisId.toInt())
            }
            cursor.close()
        } else {
            Log.e("AnalysisDetailsActivity", "Invalid analysis ID")
        }
    }

    // Function to populate the TableLayout with analysis data
    private fun populateTableLayout(analysisId: Int) {
        val resultsCursor = dbHandler.getAnalysisResultById(analysisId)
        if (resultsCursor.moveToFirst()) {
            // Create table header
            val headerRow = TableRow(this)
            val headers = arrayOf("GRAIN", "LENGTH", "WIDTH", "SHAPE")
            for (header in headers) {
                val textView = TextView(this)
                textView.text = header
                textView.setTextColor(resources.getColor(android.R.color.black, theme))
                textView.setTypeface(null, Typeface.BOLD)
                textView.gravity = Gravity.CENTER
                textView.setBackgroundResource(R.drawable.cell_border)
                textView.setPadding(1, 1, 1, 1)
                headerRow.addView(textView)
            }
            tableLayout.addView(headerRow)

            // Populate table rows with data from the cursor
            do {
                val tableRow = TableRow(this)

                val grainNumber = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_NUMBER))
                val grainHeight = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_HEIGHT))
                val grainWidth = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_WIDTH))
                val grainShape = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_SHAPE))

                // Create an array of grain data and add it to the table row
                val grainData = arrayOf(grainNumber, "$grainHeight mm", "$grainWidth mm", grainShape)
                for (data in grainData) {
                    val textView = TextView(this)
                    textView.text = data
                    textView.setTextColor(resources.getColor(android.R.color.black, theme))
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundResource(R.drawable.cell_border)
                    textView.setPadding(1, 1, 1, 1)
                    tableRow.addView(textView)
                }

                tableLayout.addView(tableRow)
            } while (resultsCursor.moveToNext())
        } else {
            Log.e("AnalysisDetailsActivity", "No results found for analysis ID: $analysisId")
        }
    }

    // Function to calculate and display the average length, width, and shape
    private fun calculateAndDisplayAverages(analysisId: Int) {
        val resultsCursor = dbHandler.getAnalysisResultById(analysisId)
        var totalLength = 0f
        var totalWidth = 0f
        var totalShape = 0f
        var count = 0

        if (resultsCursor.moveToFirst()) {
            do {
                // Extract grain height, width, and shape from the cursor
                val grainHeight = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_HEIGHT))
                val grainWidth = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_WIDTH))
                val grainShape = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(COL_GRAIN_SHAPE))

                // Accumulate the values for averaging
                totalLength += grainHeight.toFloat()
                totalWidth += grainWidth.toFloat()
                totalShape += grainShape.toFloat()

                count++
            } while (resultsCursor.moveToNext())
        }
        resultsCursor.close()

        // Calculate the averages
        val averageLength = totalLength / count
        val averageWidth = totalWidth / count
        val averageShape = totalShape / count

        // Display the averages in the corresponding TextViews
        averageLengthTextView.text = getString(R.string.average_length_text, averageLength)
        averageWidthTextView.text = getString(R.string.average_width_text, averageWidth)
        averageShapeTextView.text = getString(R.string.average_shape_text, averageShape)
    }
}
