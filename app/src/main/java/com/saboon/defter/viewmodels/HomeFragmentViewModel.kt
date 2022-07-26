package com.saboon.defter.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.saboon.defter.models.ModelMoments

class HomeFragmentViewModel(application: Application): AndroidViewModel(application) {
    var moments = MutableLiveData<List<ModelMoments>?>()





}