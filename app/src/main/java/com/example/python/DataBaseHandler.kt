package com.example.python

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATABASE_NAME = "GrainMeasuringApp"

const val TABLE_NAME = "Analysis"
const val COL_RESEARCHER = "researcher"
const val COL_ECOSYSTEM = "ecosystem"
const val COL_NUMBER = "number"
const val COL_ID = "id"
const val COL_DATE_ANALYZED = "date_analyzed"

const val TABLE_NAME1 = "AnalysisResults"
const val COL_ANALYSIS_ID = "analysisId"
const val COL_GRAIN_NUMBER = "grainNumber"
const val COL_GRAIN_HEIGHT = "grainHeight"
const val COL_GRAIN_WIDTH = "grainWidth"
const val COL_GRAIN_SHAPE = "grainShape"
const val COL_IMAGE = "image"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 3){
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_RESEARCHER TEXT, " +
                "$COL_ECOSYSTEM TEXT, " +
                "$COL_NUMBER INTEGER," +
                "$COL_DATE_ANALYZED TEXT);"

        val createTable1 = "CREATE TABLE $TABLE_NAME1 (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_GRAIN_NUMBER TEXT, " +
                "$COL_GRAIN_HEIGHT TEXT, " +
                "$COL_GRAIN_WIDTH TEXT, " +
                "$COL_GRAIN_SHAPE TEXT, " +
                "$COL_ANALYSIS_ID INTEGER, " +
                "$COL_IMAGE BLOB);"

        db?.execSQL(createTable)
        db?.execSQL(createTable1)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME1")
        onCreate(db)
//        if (oldVersion < 3) {
//            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COL_DATE_ANALYZED TEXT DEFAULT CURRENT_TIMESTAMP")
//        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun insertData(analysis: Analysis): Long {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_RESEARCHER, analysis.researcher)
        cv.put(COL_ECOSYSTEM, analysis.ecosystem)
        cv.put(COL_NUMBER, analysis.number)
        cv.put(COL_DATE_ANALYZED, getCurrentDateTime())

        val result = db.insert(TABLE_NAME, null, cv)

        if (result == (-1).toLong())
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context, "Successfully saved data.", Toast.LENGTH_SHORT).show()

//        db.close()

        return result

    }

    fun insertDataResults(analysisResults: AnalysisResults) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_ANALYSIS_ID, analysisResults.analysisId)
        cv.put(COL_GRAIN_NUMBER, analysisResults.grainNumber)
        cv.put(COL_GRAIN_HEIGHT, analysisResults.grainHeight)
        cv.put(COL_GRAIN_WIDTH, analysisResults.grainWidth)
        cv.put(COL_GRAIN_SHAPE, analysisResults.grainShape)
        cv.put(COL_IMAGE, analysisResults.imageData)

        val result = db.insert(TABLE_NAME1, null, cv)

        if (result == (-1).toLong())
            Toast.makeText(context, "Failed to save analysis results", Toast.LENGTH_SHORT).show()
//        else
//            Toast.makeText(context, "Successfully saved analysis results.", Toast.LENGTH_SHORT).show()

//        db.close()
    }

    fun getAllAnalysis(): Cursor {
        val db = this.readableDatabase
        val query = """
        SELECT a.$COL_ID AS _id, a.$COL_RESEARCHER, a.$COL_ECOSYSTEM, a.$COL_NUMBER, a.$COL_DATE_ANALYZED 
        FROM $TABLE_NAME a
        JOIN $TABLE_NAME1 ar ON a.$COL_ID = ar.$COL_ANALYSIS_ID
        GROUP BY a.$COL_ID
    """
        return db.rawQuery(query, null)
    }

    fun getAnalysisResultById(analysisId: Int): Cursor {
        val db = this.readableDatabase
        val selection = "$COL_ANALYSIS_ID = ?"
        val selectionArgs = arrayOf(analysisId.toString())
        return db.query(TABLE_NAME1, null, selection, selectionArgs, null, null, null)
    }



}