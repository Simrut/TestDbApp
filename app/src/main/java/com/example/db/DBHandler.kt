package com.example.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DBHandler(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
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

    fun getPersons(mCtx: Context): ArrayList<Person> {
        val query = "Select * From $CONTACTED_PERSONS_TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        val persons = ArrayList<Person>()

        if (cursor.count == 0)
            Toast.makeText(mCtx, "No Records found", Toast.LENGTH_SHORT).show() else {
            while (cursor.moveToNext()) {
                val person = Person()
                person.personID = cursor.getInt(cursor.getColumnIndex(COLUMN_PERSONID))
                person.personName = cursor.getString(cursor.getColumnIndex(COLUMN_PERSONNAME))
                person.infectionRisk = cursor.getDouble(cursor.getColumnIndex(COLUMN_INFECTIONRISK))
                persons.add(person)
            }
            Toast.makeText(mCtx, "${cursor.count.toString()} Records Found", Toast.LENGTH_SHORT)
                .show()
        }
        cursor.close()
        //TODO delete? val dbpath = db.path.toString()
        db.close()
        return persons
    }

    fun addPerson(mCtx: Context, person: Person) {
        val values = ContentValues()
        values.put(COLUMN_PERSONNAME, person.personName)
        values.put(COLUMN_INFECTIONRISK, person.infectionRisk)

        val db = this.writableDatabase
        try {
            db.insert(CONTACTED_PERSONS_TABLE_NAME, null, values)
            Toast.makeText(mCtx, "Customer Added", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(mCtx, e.message, Toast.LENGTH_SHORT).show()
        }
        db.close()
    }
}
