package com.example.db

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_person.*
import kotlinx.android.synthetic.main.activity_upload.*

class UploadToAPIActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_upload)

            var uploadURL: String

            btnUpload.setOnClickListener {
                if (editUploadUrl.text.isEmpty()) {
                    Toast.makeText(this, "Enter URL", Toast.LENGTH_SHORT).show()
                    editUploadUrl.requestFocus()
                } else {
                    uploadURL = editUploadUrl.text.toString()
                    APIHandler().postJSON(this, uploadURL)
                }
            }
        }
}