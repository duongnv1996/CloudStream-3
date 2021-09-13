package com.lagradost.cloudstream3.ui.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.ui.APIRepository
import kotlinx.coroutines.launch

class ShareViewModel : ViewModel() {
    private val _notifyData: MutableLiveData<Resource<Any?>> = MutableLiveData()
    val notifyData: LiveData<Resource<Any?>> get() = _notifyData
    fun notifyToPlayer(listSubPath :List<String>){
        _notifyData.postValue(Resource.Success<List<String>>(listSubPath))
    }
    fun refreshData(){
        _notifyData.postValue(null)
    }
}