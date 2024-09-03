package com.example.python

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.TableLayout
import android.widget.LinearLayout
import android.widget.TableRow
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.IOException
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.Date
import android.graphics.Typeface
import android.net.Uri
import org.json.JSONArray
import androidx.core.content.FileProvider
import android.view.View
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.content.res.AppCompatResources
import java.io.ByteArrayOutputStream
import java.util.Locale


class AnalysisActivity : AppCompatActivity() {
    private lateinit var img: ImageView // ImageView to display the captured image
    private lateinit var tv: TextView // TextView to display the analysis results
    private lateinit var captureButton: Button // Button to capture an image
    private lateinit var saveButton: Button // Button to save the analysis results
    private val cameraRequest = 1888 // Request code for camera
    private val imageCaptureRequest = 1 // Request code for image capture

    private var totalWidth: Float = 0.0f // Total width for averaging
    private var totalLength: Float = 0.0f // Total length for averaging
    private var totalShape: Float = 0.0f // Total shape for averaging
    private var avgShape: Float = 0.0f // Average shape
    private var avgWidth: Float = 0.0f // Average width
    private var avgLength: Float = 0.0f // Average length
    private var width: String = "" // Width of an object
    private var length: String = "" // Length of an object
    private var count: Int = 0 // Count of objects

    private lateinit var currentPhotoPath: String // Path to the current photo

    private lateinit var jsonArray : Any // JSON array holding the analysis data

    private var analysisId: Int = 0 // ID for the analysis


    private lateinit var imageByteArray: ByteArray // ByteArray for image data

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Close this activity and return to the previous one if any.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analysis_layout) // Set the layout for this activity

        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mIntent = intent
        val intValue = mIntent.getIntExtra("intVariableName", 0) // Get analysis ID from intent

        analysisId = intValue // Set the analysis ID

        // btn = findViewById(R.id.submitBtn) // (Unused)
        img = findViewById(R.id.image_view) // Initialize ImageView
        tv = findViewById(R.id.text_view) // Initialize TextView
        captureButton = findViewById(R.id.submitBtn) // Initialize capture button
        saveButton = findViewById(R.id.saveBtn) // Initialize save button

        saveButton.visibility = View.GONE // Hide the save button initially

        // Added: Start Python if it's not started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this)) // Initialize Python if not started
        }

        captureButton.setOnClickListener {
            // Reset averages and totals before capturing a new image
            avgWidth = 0F
            avgLength = 0F
            avgShape = 0F
            totalShape = 0F
            totalLength = 0F
            totalWidth = 0F
            count = 0

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission if not granted
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    imageCaptureRequest)
            } else {
                // Permission is already granted; open the camera
                dispatchTakePictureIntent()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            imageCaptureRequest -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted; open the camera
                    dispatchTakePictureIntent()
                } else {
                    // Permission was denied; explain to the user why the permission is necessary
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Intent to capture a photo
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile() // Create a file to save the image
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) // Set the URI for the image
                    startActivityForResult(takePictureIntent, cameraRequest) // Start the camera activity
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == cameraRequest && resultCode == Activity.RESULT_OK) {

            try {

                val py = Python.getInstance()
                val pyobj = py.getModule("script") // Provide the name for your Python script

                val imgpath = pyobj.callAttr("return_image", currentPhotoPath) // Call Python function to get image path

                val obj = pyobj.callAttr("analyze_image", currentPhotoPath) // Call Python function to analyze image

                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888

                val photo = BitmapFactory.decodeFile(imgpath.toString(), options) // Decode the image file
                img.setImageBitmap(photo) // Set the image to the ImageView

                // Convert Bitmap to ByteArray
                val outputStream = ByteArrayOutputStream()
                photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                imageByteArray = outputStream.toByteArray() // Convert image to ByteArray

                jsonArray = JSONArray(obj.toString()) // Convert analysis result to JSONArray

                val numberOfObjects = (jsonArray as JSONArray).length() // Get the number of objects in the JSON array

                // Add table rows
                val tableLayout: TableLayout = findViewById(R.id.table_layout) // Replace with your TableLayout's ID

                // Clear existing rows
                while (tableLayout.childCount > 0) {
                    tableLayout.removeViewAt(0)
                }

                val numberOfRows = numberOfObjects + 1 // Number of rows (including header)
                val numberOfColumns = 4 // Number of columns

                // Calculate the width for each column
                //val columnWidth = resources.displayMetrics.widthPixels / numberOfColumns

                for (i in 0 until numberOfRows) {
                    val tableRow = TableRow(this) // Create a new TableRow for each row
                    tableRow.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT, // Set the height to WRAP_CONTENT
                        //2.0f
                    )

                    for (j in 0 until numberOfColumns) {

                        val cellLayout = LinearLayout(this) // Create a LinearLayout for each cell
                        cellLayout.layoutParams = TableRow.LayoutParams(
                            247,
                            TableRow.LayoutParams.WRAP_CONTENT
                        )
                        cellLayout.orientation = LinearLayout.VERTICAL

                        val textView = TextView(this) // Create a TextView for each cell
                        textView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            //2.0f
                        )

                        // Header cells
                        if (i == 0) {
                            when (j) {
                                0 -> textView.text = "GRAIN"
                                1 -> textView.text = "LENGTH"
                                2 -> textView.text = "WIDTH"
                                3 -> textView.text = "SHAPE"
                            }
                            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                            textView.gravity = Gravity.CENTER // Center align text
                            textView.setTypeface(null, Typeface.BOLD) // Set text to bold
                            textView.setPadding(0, 0, 0, 0) // Set padding to zero
                        } else {

                            // Get amount of json array
                            val item = (jsonArray as JSONArray).getJSONObject(i-1) // Get JSON object
                            width = item.getString("width") // Get width from JSON
                            length = item.getString("length") // Get length from JSON
                            val ratio = length.toFloat() / width.toFloat() // Calculate ratio

                            // Data cells
                            when (j) {
                                0 -> textView.text = "G${i}"
                                1 -> textView.text = "$length mm"
                                2 -> textView.text = "$width mm"
                                3 -> textView.text = String.format("%.2f", ratio)
                            }
                            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black)) // Set text color
                            textView.gravity = Gravity.CENTER // Center align text
                            textView.setPadding(0, 0, 0, 0) // Set padding to zero

                            totalLength += length.toFloat() // Add to total length
                            totalWidth += width.toFloat() // Add to total width
                            totalShape += ratio // Add to total shape

                            count += 1 // Increment count
                        }

                        cellLayout.background = AppCompatResources.getDrawable(this, R.drawable.cell_border)
                        // Set cell background
                        cellLayout.addView(textView) // Add TextView to cell layout
                        tableRow.addView(cellLayout) // Add cell layout to row
                    }

                    tableLayout.addView(tableRow) // Add the TableRow to the TableLayout
                }

                avgWidth = totalWidth / count // Calculate average width
                avgLength = totalLength / count // Calculate average length
                avgShape = totalShape / count // Calculate average shape

                // Display the calculated averages in the TextView
                tv.text = "Average Length: ${String.format("%.2f", avgLength).toDouble()} mm\n Average Width: ${String.format("%.2f", avgWidth).toDouble()} mm\n Average Shape: ${String.format("%.2f", avgShape).toDouble()}"
                captureButton.text = "Re-Capture" // Change button text
                saveButton.visibility = View.VISIBLE // Show the save button

                // Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace() // Print stack trace for errors
            }
        }

        saveButton.setOnClickListener {
            val jsonObjectString = jsonArray.toString() // Convert JSON array to string
            val jsonArray = JSONArray(jsonObjectString) // Convert string to JSONArray

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i) // Get JSON object
                val n = jsonObject.getString("n") // Get index from JSON
                val length = jsonObject.getString("length") // Get length from JSON
                val width = jsonObject.getString("width") // Get width from JSON
                val ratio = length.toFloat() / width.toFloat() // Calculate ratio

                // Save the data results
                val analysisResults = AnalysisResults(
                    analysisId,
                    "G${n}",
                    length,
                    width,
                    String.format("%.2f", ratio),
                    imageByteArray
                )
                val db = DataBaseHandler(this)
                db.insertDataResults(analysisResults) // Insert data into database
                // Log the saved data
//                Log.d("SavedData", "Grain G${n} - Length: $length, Width: $width, Ratio: ${String.format("%.2f", ratio)}")
            }

            Toast.makeText(this, "Successfully saved analysis results.", Toast.LENGTH_SHORT).show() // Show success message

        }
    }
}
