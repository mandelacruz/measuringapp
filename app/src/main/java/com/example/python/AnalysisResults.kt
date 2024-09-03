package com.example.python

class AnalysisResults(
    var analysisId: Int,
    var grainNumber: String,
    var grainHeight: String,
    var grainWidth: String,
    var grainShape: String,
    var imageData: ByteArray
) {
    var id: Int = 0 // Unique ID for the analysis result
}
