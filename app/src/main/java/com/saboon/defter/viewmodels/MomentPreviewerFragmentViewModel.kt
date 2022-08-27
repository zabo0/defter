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
import com.saboon.defter.utils.COLLECTION_MOMENTS
import com.saboon.defter.utils.COLLECTION_USERS

class MomentPreviewerFragmentViewModel(application: Application): AndroidViewModel(application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = Firebase.storage
    private var firestore: FirebaseFirestore = Firebase.firestore


    val moment = MutableLiveData<ModelMoments>()

    fun getMoment(momentID: String){

        firestore.collection(COLLECTION_MOMENTS).document(momentID)
            .get()
            .addOnSuccessListener {
                if (it!=null){
                    val id = it.data!!["id"] as String
                    val dateAdded = it.data!!["dateAdded"] as Long
                    val date = it.data!!["date"] as Long
                    val photoUrl = it.data!!["photoURL"] as String
                    val resizedPhotoURL = it.data!!["resizedPhotoURL"] as String
                    val senderUID = it.data!!["senderUID"] as String
                    val text = it.data!!["text"] as String

                    moment.value = ModelMoments(id,senderUID,date,dateAdded,photoUrl,resizedPhotoURL,text)
                }
            }.addOnFailureListener {

            }
    }

    fun getUser(): FirebaseUser {
        return auth.currentUser!!
    }

    fun getUserName(uid: String, result:(String)->Unit){
        firestore.collection(COLLECTION_USERS).document(uid)
            .get()
            .addOnSuccessListener {
                if(it!=null){
                    result(it.data!!["userName"] as String)
                }
            }
    }

}