package com.example.db

import android.util.Log
import com.android.volley.Response
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray


class APIHandler {
    fun postJSON() {
        val url = "http://my-json-feed"

        //TODO input DBname etc into function
        val jsonArray  = JSONHandler().getResults("/data/user/0/com.example.db/databases/","PandemiaRisk.db", "Contacts")

        val jsonObjectRequest = JsonArrayRequest(Request.Method.POST, url, jsonArray,
            Response.Listener { response ->
                Log.d("HttpPOSTResponse", "Response: %s".format(response.toString()))
                //TODO display answers somehow
                //textView.text = "Response: %s".format(response.toString())
            },
            Response.ErrorListener { error ->
                Log.e("HttpPOSTError", "Error: %s".format(error.toString()))
            }
        )

        //TODO do smth about queue?
        // Access the RequestQueue through your singleton class.
        //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }
}