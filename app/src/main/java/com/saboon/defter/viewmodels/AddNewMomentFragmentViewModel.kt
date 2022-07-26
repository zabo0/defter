package com.saboon.defter.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.*
import java.util.*
import kotlin.concurrent.schedule

class AddNewMomentFragmentViewModel(application: Application): AndroidViewModel(application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = Firebase.storage
    private var firestore: FirebaseFirestore = Firebase.firestore

    var dailyMoment_1 = MutableLiveData<String>()
    var dailyMoment_2 = MutableLiveData<String>()
    var dailyMoment_3 = MutableLiveData<String>()
    var dailyRemainingMoment = MutableLiveData<String>()
    var progress = MutableLiveData<Int>()
    var loading = MutableLiveData<Boolean>()
    var error = MutableLiveData<String>()

    fun getUser():FirebaseUser{
        return auth.currentUser!!
    }


    fun getDailyMoments(){
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it != null){
                    if(it.data?.get("dailyMomentIDFirst") != "null"){
                        dailyMoment_1.value = it.data?.get("dailyMomentIDFirst").toString()
                    }
                    if(it.data?.get("dailyMomentIDSecond") != "null"){
                        dailyMoment_2.value = it.data?.get("dailyMomentIDSecond").toString()
                    }
                    if(it.data?.get("dailyMomentIDThird") != "null"){
                        dailyMoment_3.value = it.data?.get("dailyMomentIDThird").toString()
                    }
                }
            }
    }
    fun getDailyRemainingMoments(){
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it != null){
                    dailyRemainingMoment.value = it.data?.get("dailyRemaining").toString()
                }
            }
    }

    fun addPhotoToStorage(selectedPhoto: Uri, dayMoment: Long, downloadURLs: (String, String) -> Unit){
        loading.value = true
        val photoID = IDGenerator().generateMomentPhotoID(dayMoment)
        val photoRef = storage.reference.child(PATH_TO_MOMENTS_PHOTOS).child(photoID + ".jpg")
        val resizedPhotoRef = storage.reference.child(PATH_TO_MOMENTS__RESIZED_PHOTOS).child(photoID+"_200x200.jpg")
        photoRef.putFile(selectedPhoto).addOnProgressListener {taskSnapshot->
            val prog: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            progress.value = prog.toInt()
        }.addOnSuccessListener{
            photoRef.downloadUrl.addOnSuccessListener {downloadURL->
                Timer().schedule(5000) {
                    resizedPhotoRef.downloadUrl.addOnSuccessListener {resizedDownloadURL->
                        downloadURLs(downloadURL.toString(),resizedDownloadURL.toString())
                        loading.value = false
                    }
                }
            }.addOnFailureListener {e->
                error.value = e.localizedMessage
            }
        }.addOnFailureListener {e->
            error.value = e.localizedMessage
        }
    }

    fun addMoment(moment: ModelMoments, result: (Boolean) -> Unit){
        firestore.collection(COLLECTION_MOMENTS).document(IDGenerator().generateMomentID(moment.date,moment.sender))
            .set(moment).addOnSuccessListener {
                result(true)
            }.addOnFailureListener { e->
                result(false)
                error.value = e.localizedMessage
            }

    }

    fun updateUserRemaining(resizedDownloadURL: String, dailyRemainingMoments: String){
        var dailyRemainingIDString = "0"
        var dailyRemainingNumber = "0"

        when(dailyRemainingMoments){
            "3" -> {
                dailyRemainingIDString = "dailyMomentIDFirst"
                dailyRemainingNumber = "2"
                dailyMoment_1.value = resizedDownloadURL
            }
            "2" -> {
                dailyRemainingIDString = "dailyMomentIDSecond"
                dailyRemainingNumber = "1"
                dailyMoment_2.value = resizedDownloadURL
            }
            "1" -> {

                dailyRemainingIDString = "dailyMomentIDThird"
                dailyRemainingNumber = "0"
                dailyMoment_3.value = resizedDownloadURL
            }
        }

        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
            mapOf(
                dailyRemainingIDString to resizedDownloadURL,
                "dailyRemaining" to dailyRemainingNumber
            ))

        dailyRemainingMoment.value = dailyRemainingNumber
    }


}