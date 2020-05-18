package com.example.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject


class JSONHandler {
    public fun getResults(DB_PATH:String, DB_NAME:String, TABLE_NAME:String): JSONArray? {
        val myPath: String = DB_PATH + DB_NAME// Set path to your database
        val myTable: String = TABLE_NAME //Set name of your table

        //or you can use `context.getDatabasePath("my_db_test.db")`
        val myDataBase =
            SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
        val searchQuery = "SELECT  * FROM $myTable"
        val cursor: Cursor = myDataBase.rawQuery(searchQuery, null)
        val resultSet = JSONArray()
        cursor.moveToFirst()
        while (cursor.isAfterLast() === false) {
            val totalColumn: Int = cursor.getColumnCount()
            val rowObject = JSONObject()
            for (i in 0 until totalColumn) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i))
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i))
                        } else {
                            rowObject.put(cursor.getColumnName(i), "")
                        }
                    } catch (e: Exception) {
                        Log.d("TAG_NAME", e.message)
                    }
                }
            }
            resultSet.put(rowObject)
            cursor.moveToNext()
        }
        cursor.close()
        Log.d("TAG_NAME", resultSet.toString())
        return resultSet
    }
}