package com.example.python

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_layout)

        // Enable the back button in the custom Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Find views by their IDs
        val researcherInputLayout = findViewById<TextInputLayout>(R.id.researcherInput)
        val researcherEditText = researcherInputLayout.editText
        val ecosystemInputLayout = findViewById<TextInputLayout>(R.id.ecosystemInput)
        val ecosystemEditText = ecosystemInputLayout.editText
        val numberInputLayout = findViewById<TextInputLayout>(R.id.numberInput)
        val numberEditText = numberInputLayout.editText

        val startAnalysisBtn = findViewById<Button>(R.id.startAnalysisBtn)

        // Set an OnClickListener for the 'Start Analysis' button
        startAnalysisBtn.setOnClickListener {
            // Retrieve user input from EditText fields
            val researcherInput = researcherEditText?.text.toString()
            val ecosystemInput = ecosystemEditText?.text.toString()
            val numberInput = numberEditText?.text.toString()

            // Validate input fields to ensure none are empty
            if (numberInput.isBlank() || ecosystemInput.isBlank() || researcherInput.isBlank()) {
                // Display error messages if any field is blank
                if (numberInput.isBlank()) {
                    numberInputLayout.error = "Sample Number is required"
                } else {
                    numberInputLayout.error = null
                }

                if (ecosystemInput.isBlank()) {
                    ecosystemInputLayout.error = "Ecosystem is required"
                } else {
                    ecosystemInputLayout.error = null
                }

                if (researcherInput.isBlank()) {
                    researcherInputLayout.error = "Researcher Name is required"
                } else {
                    researcherInputLayout.error = null
                }
            } else {
                // Clear error messages and proceed if all fields are valid
                researcherInputLayout.error = null
                ecosystemInputLayout.error = null
                numberInputLayout.error = null

                // Create an Analysis object with the input data
                val analysis = Analysis(
                    researcherInput,
                    ecosystemInput,
                    numberInput
                )
                val db = DataBaseHandler(this)
                val rid = db.insertData(analysis) // Insert data into database

                // Create an Intent to start the AnalysisActivity
                val myIntent = Intent(this, AnalysisActivity::class.java)
                myIntent.putExtra("intVariableName", rid.toInt()) // Pass the ID as an extra
                startActivity(myIntent) // Start AnalysisActivity
            }
        }
    }
}
