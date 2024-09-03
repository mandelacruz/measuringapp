package com.example.python

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private lateinit var btnAnalysis: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.getStartedBtn)
        btnAnalysis = findViewById(R.id.analysisHistoryBtn)

        btn.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

        btnAnalysis.setOnClickListener {
            val intent = Intent(this, AnalysisHistoryActivity::class.java)
            startActivity(intent)
        }
    }
}