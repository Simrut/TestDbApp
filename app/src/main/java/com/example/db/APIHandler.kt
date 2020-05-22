package com.example.db

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*

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
}

class APIHandler {

    fun postJSON(mCtx: Context, url: String) {
        val jsonArray = JSONHandler().getResults(
            "/data/user/0/com.example.db/databases/",
            "PandemiaRisk.db",
            "Contacts"
        )

        val jsonObjectRequest = JsonArrayRequest(Request.Method.POST, url, jsonArray,
            Response.Listener { response ->
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