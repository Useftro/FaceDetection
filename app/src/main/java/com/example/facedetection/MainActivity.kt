package com.example.facedetection

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.widget.Toast
import com.example.facedetection.Utils.Common
import com.example.facedetection.Utils.IUploadCallback
import com.example.facedetection.Utils.ProgressRequestBody
import com.example.facedetection.retrofit.IUploadAPI
import com.example.facedetection.retrofit.RetrofitClient
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Callback
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.lang.StringBuilder
import java.net.URISyntaxException

class MainActivity : AppCompatActivity(), IUploadCallback {

    lateinit var mService: IUploadAPI
    var selectedUri: Uri? = null
    lateinit var dialog: ProgressDialog

    companion object{
        private val PICK_FILE_REQUEST: Int = 1000
    }

    private val apiUpload: IUploadAPI
        get() = RetrofitClient.client.create(IUploadAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this).withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity, "Accept permission first!", Toast.LENGTH_LONG).show()
                }

            }).check()

        mService = apiUpload

        imageView.setOnClickListener{ chooseFile() }
        button.setOnClickListener{ uploadFile() }
    }

    private fun uploadFile() {
        if(selectedUri != null)
            dialog = ProgressDialog(this@MainActivity)
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                dialog.setMessage("Wait while uploading")
        dialog.isIndeterminate = false
        dialog.max = 100
        dialog.setCancelable(false)
        dialog.show()

        var file: File? = null
        try{
            file = File(Common.getFilePath(this, selectedUri!!))
        } catch (e: URISyntaxException) { e.printStackTrace() }
        if(file != null){
            val requestBody = ProgressRequestBody(file, this)
            val body = MultipartBody.Part.createFormData("image", file.name,
                                                                          requestBody)
            Thread(Runnable {
                mService.uploadFile(body).enqueue(object:Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        dialog.dismiss()
                        val image_processed_link = StringBuilder("http://d833d6f301eb.ngrok.io").append(response
                            .body()!!.replace("\"", "")).toString()
                        Picasso.get().load(image_processed_link).into(imageView)
                        Toast.makeText(this@MainActivity, "Detected!", Toast.LENGTH_SHORT).show()
                    }

                })
            }).start()
        }
        else{
            Toast.makeText(this@MainActivity, "Can't upload this file!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                if(data != null){
                    selectedUri = data.data
                    if(selectedUri != null && selectedUri!!.path!!.isEmpty())
                        imageView.setImageURI(selectedUri)
                }
            }
        }
    }

    private fun chooseFile() {
        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type="image/*"
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onProgressUpdate(percent: Int) {

    }
}