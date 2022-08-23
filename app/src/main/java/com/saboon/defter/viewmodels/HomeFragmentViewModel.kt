package com.saboon.defter.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.saboon.defter.models.ModelHomeSection
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.COLLECTION_MOMENTS
import com.saboon.defter.utils.COLLECTION_USERS

class HomeFragmentViewModel(application: Application): AndroidViewModel(application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = Firebase.storage
    private var firestore: FirebaseFirestore = Firebase.firestore

    var moments = MutableLiveData<List<ModelMoments>?>()

    var loading = MutableLiveData<Boolean>()
    var error = MutableLiveData<String>()
    var errorLayout = MutableLiveData<Boolean>()

    fun getUserProfilePhotoURL(result:(String) -> Unit){
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if(it!=null){
                    result( it.data!!["profilePhotoURL"] as String)
                }
            }.addOnFailureListener { e->
                error.value = e.localizedMessage
            }
    }

    fun getAllMoments(){
        loading.value = true
        firestore.collection(COLLECTION_MOMENTS)
            .get()
            .addOnSuccessListener {
                if(it!=null){
                    moments.value = it.toObjects(ModelMoments::class.java)
                    loading.value = false
                }
            }
            .addOnFailureListener {e->
                error.value = e.localizedMessage
                errorLayout.value = true
            }
    }


}