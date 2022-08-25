package com.saboon.defter.viewmodels

import android.app.Application
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

class AddNewMomentFragmentViewModel(application: Application): AndroidViewModel(application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = Firebase.storage
    private var firestore: FirebaseFirestore = Firebase.firestore

    var dailyMoments = MutableLiveData<ArrayList<String>?>()
    var dailyMoment = MutableLiveData<String?>()
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
                    val momentMap = it.data
                    val urlList: ArrayList<String> = arrayListOf()

                    if(momentMap != null){
                        for(entry in momentMap.entries){
                            if (entry.key.toString() != "dailyMoments" && entry.key.toString() != "totalMoments" && entry.value.toString() != "null"){
                                val moment = entry.value.toString()
                                urlList.add(getURL(moment))
                            }
                        }
                        dailyMoments.value = urlList
                    }
                }
            }.addOnFailureListener {
                error.value = it.localizedMessage
            }
    }


    fun addPhotosToStorage(selectedPhotoByteArray: ByteArray, resizedPhotoByteArray: ByteArray, dayMoment: Long, photoURLs: (String, String) -> Unit){
        loading.value = true
        val photoID = IDGenerator().generateMomentPhotoID(dayMoment)
        val photoRef = storage.reference.child(PATH_TO_MOMENTS_PHOTOS).child(photoID + ".jpg")
        val resizedPhotoRef = storage.reference.child(PATH_TO_MOMENTS_RESIZED_PHOTOS).child(photoID+"_200x200.jpg")

        photoRef.putBytes(selectedPhotoByteArray).addOnProgressListener { taskSnapshot->
            val prog: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            progress.value = prog.toInt()
        }.addOnSuccessListener{
            resizedPhotoRef.putBytes(resizedPhotoByteArray).addOnProgressListener { taskSnapshot->
            }.addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { photoURL->
                        resizedPhotoRef.downloadUrl.addOnSuccessListener {resizedPhotoURL ->
                                photoURLs(photoURL.toString(),resizedPhotoURL.toString())
                            }
                    }
            }
        }.addOnFailureListener {e->
            error.value = e.localizedMessage
        }
    }

    fun addMoment(moment: ModelMoments, result: (Boolean) -> Unit){
        firestore.collection(COLLECTION_MOMENTS).document(moment.id)
            .set(moment).addOnSuccessListener {
                result(true)
            }.addOnFailureListener { e->
                result(false)
                error.value = e.localizedMessage
            }

    }

    fun getDailySentMomentsNumber(result: (Long) -> Unit){
        firestore.collection(COLLECTION_DAILY_MOMENTS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it != null){
                    result(it.getLong("dailyMoments")!!)
                }
            }
    }

    fun updateDailySentMoments(momentID: String, resizedPhotoURL: String, dailyMomentsNumber: Long, totalMomentsNumber: Long){

        getDailySentMomentsNumber {
            firestore.collection(COLLECTION_DAILY_MOMENTS).document(auth.currentUser!!.uid).update(
                mapOf(
                    "dailyMomentID_URL_0$it" to "$momentID+$resizedPhotoURL",
                    "dailyMoments" to dailyMomentsNumber+1,
                    "totalMoments" to totalMomentsNumber+1
                )
            ).addOnFailureListener { e->
                error.value = e.localizedMessage
            }
        }
    }

    fun getTotalMomentsNumber(result: (Long) -> Unit){
        firestore.collection(COLLECTION_DAILY_MOMENTS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it != null){
                    result(it.getLong("totalMoments")!!)
                }
            }
    }

    private fun getURL(value:String):String{
        val stringArray = value.split("+")
        return stringArray[1]
    }

    private fun getID(value:String):String{
        val stringArray = value.split("+")
        return stringArray[0]
    }

    fun getUserName(): String{
        var userName = "null"
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if (it!= null){
                    userName = it.get("userName") as String
                }
            }.addOnFailureListener {
                userName = "null"
            }
        return userName
    }


}