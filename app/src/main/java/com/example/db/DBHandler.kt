package com.example.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler (context: Context, name:String, factory :SQLiteDatabase.CursorFactory, version :Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){

    companion object{
        private val DATABASE_NAME = "PandemiaRisk.db"
        private val DATABASE_VERSION = 1

        val CONTACTED_PERSONS_TABLE_NAME = "Contacts"
        val COLUMN_PERSONID = "personid"
        val COLUMN_PERSONNAME = "personname"
        val COLUMN_INFECTIONRISK = "infectionrisk"
    }

    //will get executed upon first installation of app
    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PERSONS_TABLE = ("CREATE TABLE $CONTACTED_PERSONS_TABLE_NAME (" +
               "$COLUMN_PERSONID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_PERSONNAME TEXT," +
                "$COLUMN_INFECTIONRISK DOUBLE DEFAULT 0)")
        db?.execSQL(CREATE_PERSONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}