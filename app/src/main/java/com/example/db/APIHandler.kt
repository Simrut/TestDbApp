package com.example.db

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.io.*
import java.security.KeyStore
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


class InfectionDatabase :Serializable{
    var databaseContent : String

    constructor(dbContent : String) {
        this.databaseContent = dbContent
    }
}

/** Write the object to a Base64 String.  */
@Throws(IOException::class)
private fun toBase64(o: Serializable): String? {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(o)
    oos.close()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    } else {
        return android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
    }
}

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

    val NoSSLRequestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(
            context.applicationContext,
            HurlStack(null)
        )
    }

    fun <T> addToNoSSLRequestQueue(req: Request<T>) {
        NoSSLRequestQueue.add(req)
    }

    fun startNoSSLRequestQueue() {
        NoSSLRequestQueue.start()
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

    private fun newSslSocketFactory(context: Context): SSLSocketFactory? {//gets invoked in newRequestQueue
        return try {
            // Get an instance of the Bouncy Castle KeyStore format
            val trusted: KeyStore = KeyStore.getInstance("BKS")
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            val `in`: InputStream =
                context.applicationContext.getResources()
                    .openRawResource(com.example.db.R.raw.cert_290920)
            try {
                // Initialize the keystore with the provided trusted certificates
                // Provide the password of the keystore
                trusted.load(`in`, "mypassword".toCharArray())
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

    fun NoSSLRequest(){
        val url = "http://example.com/index.html"
        val noSSLRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Toast.makeText(context, "Simple request executed", Toast.LENGTH_SHORT)
                    .show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show()
            }
        )
        noSSLRequest.setShouldCache(false);
        requestHandler.addToNoSSLRequestQueue(noSSLRequest)
        requestHandler.startNoSSLRequestQueue()
    }

    fun getSecret() {//TODO set up with appropriate certs to run ssl
        //val url = "http://example.com/index.html"
        val url = "https://10.0.2.2:8443"//TODO does it get sent via https?
        //TODO do this? https://stackoverflow.com/questions/34823724/golang-tls-handshake-error
        val requestSecret = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("GetSecretResponse", "Response: %s".format(response.toString()))
                Toast.makeText(context, "Secret pulled from server", Toast.LENGTH_SHORT)
                    .show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show()
                Log.e("HttpSecretError", error.message)
            }
        )
        Toast.makeText(context, "Request is sent to: %s".format(url), Toast.LENGTH_LONG)
            .show()

        // Access the RequestQueue through your singleton class.
        requestSecret.setShouldCache(false);
        requestHandler.addToRequestQueue(requestSecret)
        requestHandler.startRequestQueue()
    }

    fun postJSON() {

        val url = "http://10.0.2.2:6789";//TODO set correct URL for server later on
        val jsonArray = JSONHandler().getResults(
            "/data/user/0/com.example.db/databases/",
            "PandemiaRisk.db",
            "Contacts"
        )
        val infectionDatabase = InfectionDatabase(jsonArray.toString())

        try {
            val requestBody = toBase64(infectionDatabase)
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener { response -> Log.i("VOLLEY", response) },
                Response.ErrorListener { error -> Log.e("VOLLEY", error.toString()) }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    return try {
                        requestBody?.toByteArray(charset("utf-8"))!!
                    } catch (uee: UnsupportedEncodingException) {
                        VolleyLog.wtf(
                            "Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody,
                            "utf-8"
                        )
                        null
                    }!!
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
            stringRequest.setShouldCache(false);
            requestHandler.addToRequestQueue(stringRequest)
            requestHandler.startRequestQueue()//TODO dont invoke every time but once (in constructor?)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}