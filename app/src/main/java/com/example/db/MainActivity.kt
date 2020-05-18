package com.example.db

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var dbHandler: DBHandler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DBHandler(this, null, null, 1)

        viewPersons()
        fab.setOnClickListener {
            val intent = Intent(this, AddPersonActivity::class.java)
            startActivity(intent)
        }
        APIHandler().postJSON(this)
    }

    private fun viewPersons() {
        val personsList = dbHandler.getPersons(this)
        val adapter = PersonAdapter(this, personsList)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        viewPersons()
        super.onResume()
    }
}
