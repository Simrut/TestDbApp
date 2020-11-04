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
import java.net.URL
import java.security.KeyStore
import java.util.*
import javax.net.ssl.*


class InfectionDatabase :Serializable{
    var databaseContent : String

    constructor(dbContent: String) {
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

    // Let's assume your server app is hosting inside a server machine
    // which has a server certificate in which "Issued to" is "localhost",for example.
    // Then, inside verify method you can verify "localhost".
    // If not, you can temporarily return true
    private fun getHostnameVerifier(): HostnameVerifier {//TODO replace by nullhostnameverf?
        return object: HostnameVerifier {
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                //val hv: HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
                //return hv.verify("10.0.2.2", session)
            }
        }
    }

    var hurlStack: HurlStack = object : HurlStack() {
        @Throws(IOException::class)
        override fun createConnection(url: URL?): HttpsURLConnection {
            val httpsURLConnection: HttpsURLConnection =
                super.createConnection(url) as HttpsURLConnection
            try {
                httpsURLConnection.setSSLSocketFactory(newSslSocketFactory(context))
                httpsURLConnection.setHostnameVerifier(getHostnameVerifier())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return httpsURLConnection
        }
    }

    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(
            context.applicationContext,
            this.hurlStack
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
                    .openRawResource(com.example.db.R.raw.certwithsan)
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

    fun getSecret(url:String) {
        //val url = "https://10.0.2.2:8443"
        val requestSecret = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("GetSecretResponse", "Response: %s".format(response.toString()))
                Toast.makeText(context, "Secret pulled from server", Toast.LENGTH_SHORT)
                    .show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show()
                try{
                    Log.e("HttpSecretError", error.message)}
                catch (e:Exception)
                {
                    Log.e("HttpSecretError", "failed getting secret")}
            }
        )

        // Access the RequestQueue through your singleton class.
        requestSecret.setShouldCache(false);
        requestHandler.addToRequestQueue(requestSecret)
        requestHandler.startRequestQueue()
    }

    fun postJSON(url: String) {

        //val url = "http://10.0.2.2:6789";
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
            requestHandler.addToNoSSLRequestQueue(stringRequest)
            requestHandler.startNoSSLRequestQueue()//TODO dont invoke every time but once (in constructor?)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}