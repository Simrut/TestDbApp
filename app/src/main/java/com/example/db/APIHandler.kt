package com.example.db

import android.R
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


class RequestHandler constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RequestHandler? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestHandler(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    fun startRequestQueue() {
        requestQueue.start()
    }

    private fun newSslSocketFactory(context: Context): SSLSocketFactory? {
        return try {
            // Get an instance of the Bouncy Castle KeyStore format
            val trusted: KeyStore = KeyStore.getInstance("BKS")
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            val `in`: InputStream =
                context.applicationContext.getResources()
                    .openRawResource(R.raw.myKestore)
            try {
                // Initialize the keystore with the provided trusted certificates
                // Provide the password of the keystore
                trusted.load(`in`, "mysecret".toCharArray())
            } finally {
                `in`.close()
            }
            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(trusted)
            val context: SSLContext = SSLContext.getInstance("TLS")
            context.init(null, tmf.getTrustManagers(), null)
            context.getSocketFactory()
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }
}

class APIHandler {

    fun postJSON(mCtx: Context, url: String) {
        val jsonArray = JSONHandler().getResults(
            "/data/user/0/com.example.db/databases/",
            "PandemiaRisk.db",
            "Contacts"
        )
        //TODO find good request, JSONArray might only be suitable for receiving JSON Arrays
        val jsonObjectRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("HttpPOSTResponse", "Response: %s".format(response.toString()))
                Toast.makeText(mCtx, "Response: %s".format(response.toString()), Toast.LENGTH_SHORT)
                    .show()
                //textView.text = "Response: %s".format(response.toString())
            },
            Response.ErrorListener { error ->
                Toast.makeText(mCtx, "An error occured", Toast.LENGTH_SHORT).show()
                Log.e("HttpPOSTError", "Error during exec of request: %s".format(error.toString()))
            }
        )

        // Access the RequestQueue through your singleton class.
        val requestHandler = RequestHandler.getInstance(mCtx)
        requestHandler.addToRequestQueue(jsonObjectRequest)
        requestHandler.startRequestQueue()

    }
}