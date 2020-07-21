package com.example.db

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectOutputStream
import java.io.UnsupportedEncodingException
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
        Volley.newRequestQueue(
            context.applicationContext,
            HurlStack(null, newSslSocketFactory(context))
        )
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    fun startRequestQueue() {
        requestQueue.start()
    }

    private fun newSslSocketFactory(context: Context): SSLSocketFactory? {//gets involved in newRequestQueue
        return try {
            // Get an instance of the Bouncy Castle KeyStore format
            val trusted: KeyStore = KeyStore.getInstance("BKS")
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            val `in`: InputStream =
                context.applicationContext.getResources()
                    .openRawResource(com.example.db.R.raw.mykeystore)
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

class APIHandler constructor(context: Context) {
    val context = context
    // Access the RequestQueue through your singleton class.
    val requestHandler = RequestHandler.getInstance(context)


    fun getSecret() {
        val url = "https://127.0.0.1:8443/"
        val requestSecret = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("SecretResponse", "Secret is %s".format(response.toString()))
            },
            Response.ErrorListener { error ->
                Log.e("HttpSecretError", "Could not get secret, please check connection")
            }
        )
        requestHandler.addToRequestQueue(requestSecret)
        requestHandler.startRequestQueue()//TODO dont invoke this multiple times but once
        Toast.makeText(context, "Secret pulled from server", Toast.LENGTH_SHORT)
            .show()
    }

    fun postJSON(url: String) {
        val jsonDataObject = JSONHandler().getResults(
            "/data/user/0/com.example.db/databases/",
            "PandemiaRisk.db",
            "Contacts"
        )

        try {
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener { response -> Log.i("VOLLEY", response) },
                Response.ErrorListener { error -> Log.e("VOLLEY", error.toString()) }) {
                override fun getBodyContentType(): String {
                    return "application/xml; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    return SerializeObject(jsonDataObject)//Object as ByteArray
                }

                override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                    var responseString = ""
                    if (response != null) {
                        responseString = response.statusCode.toString()
                        // can get more details such as response.headers
                    }
                    return Response.success(
                        responseString,
                        HttpHeaderParser.parseCacheHeaders(response)
                    )
                }
            }
            requestHandler.addToRequestQueue(stringRequest)
            requestHandler.startRequestQueue()//TODO dont invoke every time but once (in constructor?)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun SerializeObject(jsonDataObject: JsonData): ByteArray {
        val bos = ByteArrayOutputStream()
        val os = ObjectOutputStream(bos)
        os.writeObject(jsonDataObject)
        val serializedObject: ByteArray = bos.toByteArray()
        return serializedObject
        os.close()
    }
}