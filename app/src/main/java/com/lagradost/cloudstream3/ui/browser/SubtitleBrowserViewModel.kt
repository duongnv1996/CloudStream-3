package com.lagradost.cloudstream3.ui.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.ui.APIRepository
import kotlinx.coroutines.launch

class SubtitleBrowserViewModel : ViewModel() {
    var repo: SubtitleBrowserRepository? = null
    private val _resultResponse: MutableLiveData<Resource<Any?>> = MutableLiveData()
    val resultResponse: LiveData<Resource<Any?>> get() = _resultResponse

    fun downloadSubtitle(url: String, fileDir: String) {
        repo = SubtitleBrowserRepository()
        viewModelScope.launch {
            _resultResponse.postValue(Resource.Loading(url))
            repo?.downloadSubtitle(url, fileDir) {
                _resultResponse.postValue(Resource.Success<List<String>>(it))
            }
        }

    }

    fun resetData() {
    }
}