package com.saboon.defter.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class AddNewMomentFragmentViewModel(application: Application): AndroidViewModel(application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = Firebase.storage
    private var firestore: FirebaseFirestore = Firebase.firestore

    var dailyMoments = MutableLiveData<List<String>?>()
    //var dailyRemainingMoment = MutableLiveData<String>()
    var progress = MutableLiveData<Int>()
    var loading = MutableLiveData<Boolean>()
    var error = MutableLiveData<String>()

    fun getUser():FirebaseUser{
        return auth.currentUser!!
    }


    fun getDailyMoments(){
        firestore.collection(COLLECTION_DAILY_MOMENTS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it.exists()){
                    val momentList = arrayListOf<String>()
                    val momentMap = it.data

                    if(momentMap != null){
                        for(entry in momentMap.entries){
                            if (entry.key.toString() != "dailyMoments" && entry.value.toString() != "null"){
                                momentList.add(entry.value.toString())
                            }
                        }
                    }
                    dailyMoments.value = momentList
                }
            }.addOnFailureListener {
                error.value = it.localizedMessage
            }
    }
    fun getDailyRemainingMoments(result: (String) -> Unit){
        firestore.collection(COLLECTION_DAILY_MOMENTS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it != null){
                    //dailyRemainingMoment.value = it.data?.get("dailyMoments").toString()
                    result(it.data?.get("dailyMoments").toString())
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

        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
            mapOf(
                dailyRemainingIDString to resizedDownloadURL,
                "dailyRemaining" to dailyRemainingNumber
            )
        )

        //dailyRemainingMoment.value = dailyRemainingNumber
    }

    private fun getUserMomentsNumber(moments: (Long) -> Unit){
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it != null){
                    moments(it.getLong("moments")!!)
                }
            }
            .addOnFailureListener { e->
                error.value = e.localizedMessage
            }
    }

    fun updateUserMomentsNumber(){
        getUserMomentsNumber {
            val moments = it + 1
            firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
                mapOf(
                    "moments" to moments
                )
            )
        }
    }

    fun getUserName(): String{
        var userName = "null"
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if (it!= null){
                    userName = it.data!!["userName"] as String
                }
            }.addOnFailureListener {
                userName = "null"
            }
        return userName
    }


}