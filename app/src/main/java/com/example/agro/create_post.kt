package com.example.agro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class create_post : AppCompatActivity() {

    private lateinit var postInput: EditText
    private lateinit var uploadPhotoButton: Button
    private lateinit var postActionButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var tvImageStatus: TextView
    private lateinit var progressBarPost: ProgressBar
    private lateinit var ivImagePreview: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null
    private var currentUserName: String? = null

    companion object {
        private const val REQ_PICK_IMAGE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_post)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        postInput = findViewById(R.id.post_input)
        uploadPhotoButton = findViewById(R.id.upload_photo_button)
        postActionButton = findViewById(R.id.post_action_button)
        backButton = findViewById(R.id.back_button)
        tvImageStatus = findViewById(R.id.tvImageStatus)
        progressBarPost = findViewById(R.id.progressBarPost)
        ivImagePreview = findViewById(R.id.ivImagePreview)

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        uploadPhotoButton.setOnClickListener { openGallery() }
        postActionButton.setOnClickListener { submitPost() }

        loadCurrentUserName()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQ_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                ivImagePreview.visibility = View.VISIBLE
                ivImagePreview.setImageURI(uri)

                tvImageStatus.text = "Image selected, uploading..."
                uploadImageToCloudinary(uri)
            }
        }
    }

    /**
     * UNSAFE OkHttp client: trusts ALL SSL certs.
     * Only for dev / college project. Not for production.
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        progressBarPost.visibility = View.VISIBLE
        postActionButton.isEnabled = false

        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            tvImageStatus.text = "Cannot read image"
            progressBarPost.visibility = View.GONE
            postActionButton.isEnabled = true
            return
        }

        val bytes = inputStream.readBytes()
        inputStream.close()

        val client = getUnsafeOkHttpClient()

        val cloudName = "doij5izb5"          // your Cloudinary cloud name
        val uploadPreset = "agro_unsigned"   // your unsigned preset

        val imageRequestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                imageRequestBody
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val url = json.getString("secure_url")

                    runOnUiThread {
                        uploadedImageUrl = url
                        tvImageStatus.text = "Image uploaded ✔"
                        progressBarPost.visibility = View.GONE
                        postActionButton.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        tvImageStatus.text = "Failed to upload image"
                        Toast.makeText(
                            this,
                            "Upload error: ${response.code} - ${responseBody ?: "No body"}",
                            Toast.LENGTH_LONG
                        ).show()
                        uploadedImageUrl = null
                        progressBarPost.visibility = View.GONE
                        postActionButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvImageStatus.text = "Error uploading image"
                    Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                    uploadedImageUrl = null
                    progressBarPost.visibility = View.GONE
                    postActionButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun submitPost() {
        val text = postInput.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        postActionButton.isEnabled = false
        progressBarPost.visibility = View.VISIBLE

        val userNameToStore = currentUserName
            ?: currentUser.email
            ?: "Farmer"

        val postData = hashMapOf(
            "userId" to currentUser.uid,
            "userName" to userNameToStore,
            "description" to text,
            "imageUrl" to uploadedImageUrl, // may be null
            "createdAt" to Timestamp.now(),
            "likeCount" to 0L,
            "commentCount" to 0L,
            "likedBy" to emptyList<String>()
        )

        db.collection("community_posts")
            .add(postData)
            .addOnSuccessListener {
                Toast.makeText(this, "Post added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                postActionButton.isEnabled = true
                progressBarPost.visibility = View.GONE
            }
    }

    private fun loadCurrentUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    currentUserName = doc.getString("fullName")
                }
            }
    }
}
