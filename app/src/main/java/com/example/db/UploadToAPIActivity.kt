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
            val apiHandler = APIHandler(this)

            var uploadURL: String

            btnNoSSLReq.setOnClickListener {
                apiHandler.NoSSLRequest()
            }

            btnGetSecret.setOnClickListener {
                if (getSecretURL.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter Url of VM to Get the Secret from",
                        Toast.LENGTH_SHORT
                    ).show()
                    getSecretURL.requestFocus()
                }else {
                    apiHandler.getSecret(getSecretURL.text.toString())
                }
            }

            btnUpload.setOnClickListener {
                if (UploadURL.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter Url of VM to upload infection data to",
                        Toast.LENGTH_SHORT
                    ).show()
                    UploadURL.requestFocus()
                }else {
                    apiHandler.postJSON(UploadURL.text.toString())
                }

            }
        }
}