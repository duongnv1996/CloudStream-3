package com.lagradost.cloudstream3.ui

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.ZipUtils
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.mvvm.normalSafeApiCall
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.services.ApiUtils
import com.lagradost.cloudstream3.utils.ExtractorLink
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class APIRepository(val api: MainAPI) {
    companion object {
        var providersActive = HashSet<String>()
        var typesActive = HashSet<TvType>()
        var dubStatusActive = HashSet<DubStatus>()

        val noneApi = object : MainAPI() {
            override val name: String
                get() = "None"
        }
        val randomApi = object : MainAPI() {
            override val name: String
                get() = "Random"
        }

        val noneRepo = APIRepository(noneApi)
    }

    val hasMainPage: Boolean get() = api.hasMainPage
    val name: String get() = api.name
    val mainUrl: String get() = api.mainUrl
    val hasQuickSearch: Boolean get() = api.hasQuickSearch

    suspend fun load(url: String): Resource<LoadResponse> {
        return safeApiCall {
            api.load(api.fixUrl(url)) ?: throw ErrorLoadingException()
        }
    }

    suspend fun search(query: String): Resource<List<SearchResponse>> {
        return safeApiCall {
            return@safeApiCall (api.search(query)
                ?: throw ErrorLoadingException()).filter { typesActive.contains(it.type) }.toList()
        }
    }

    suspend fun quickSearch(query: String): Resource<List<SearchResponse>> {
        return safeApiCall {
            api.quickSearch(query) ?: throw ErrorLoadingException()
        }
    }

    suspend fun getMainPage(): Resource<HomePageResponse> {
        return safeApiCall {
            api.getMainPage() ?: throw ErrorLoadingException()
        }
    }

    fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return normalSafeApiCall { api.loadLinks(data, isCasting, subtitleCallback, callback) } ?: false
    }
}