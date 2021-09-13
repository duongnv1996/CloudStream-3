package com.lagradost.cloudstream3.services

import com.lagradost.cloudstream3.ui.player.PlayerFragment
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("uploadfile")
    fun upload( @Part  myFile : MultipartBody.Part): Call<ResponseSubtitle>
    @GET
    fun downloadZipSubtitleFile(@Url url: String): Call<ResponseBody>
}