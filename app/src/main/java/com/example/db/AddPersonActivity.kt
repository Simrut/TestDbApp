package com.example.db

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_person.*

class AddPersonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_person)

        btnSave.setOnClickListener {
            val person = Person()
            var allValuesFilled = false
            if (editPersonName.text.isEmpty()) {
                allValuesFilled = false
                Toast.makeText(this, "Enter Name of Person", Toast.LENGTH_SHORT).show()
                editPersonName.requestFocus()
            } else {
                allValuesFilled = true
                person.personName = editPersonName.text.toString()
            }
            if (editInfectionRisk.text.isEmpty()) {
                allValuesFilled = false
                Toast.makeText(this, "Enter Infection Risk", Toast.LENGTH_SHORT).show()
                editInfectionRisk.requestFocus()
            } else {
                allValuesFilled = true
                person.infectionRisk = editInfectionRisk.text.toString().toDouble()
            }
            if (allValuesFilled) {
                MainActivity.dbHandler.addPerson(this, person)
                clearEdits()
                editPersonName.requestFocus()
            }
        }

        btnCancel.setOnClickListener {
            clearEdits()
            finish()
        }
    }

    private fun clearEdits() {
        editPersonName.text.clear()
        editInfectionRisk.text.clear()
    }
}
