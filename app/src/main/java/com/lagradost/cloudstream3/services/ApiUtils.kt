package com.lagradost.cloudstream3.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class ApiUtils {
     var httpClient: OkHttpClient.Builder? = null

//    static Gson gson = new GsonBuilder()
//            .setLenient()
//            .create();

    //    static Gson gson = new GsonBuilder()
    //            .setLenient()
    //            .create();
     val builder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl("http://phimhd.xyz:8888/")
        .addConverterFactory(ScalarsConverterFactory.create()) //            .addConverterFactory(GsonConverterFactory.create(gson));
        .addConverterFactory(GsonConverterFactory.create())
    companion object {

    }
      fun  <S> createService(serviceClass: Class<S>?): S {
       httpClient = OkHttpClient.Builder()
        //        CertificatePinner certificatePinner = new CertificatePinner.Builder()
//                .add("elapi.vinpearl.com", "sha256/b6rSxpFSRgP19XJTbJKUSCO0b2wpa2lhxpGFAMS7nak=")
//                .add("elapi.vinpearl.com", "sha256/IQBnNBEiFuhj+8x6X8XLgh01V9Ic5/V3IRQLNFFc7v4=")
//                .add("elapi.vinpearl.com", "sha256/K87oWBWM9UZfyddvDfoxL+8lpNyoUB2ptGtn0fv6G2Q=")
//                .build();
//        httpClient.certificatePinner(certificatePinner);
        httpClient?.readTimeout(3, TimeUnit.MINUTES)
        httpClient?.connectTimeout(3, TimeUnit.MINUTES)
        httpClient?.addInterceptor(Interceptor { chain ->
            val original = chain.request()
            // Request customization: add request headers
//                LogUtils.e("Token request -----> " + new SPUtils(AppConstant.KEY_SETTING).getString(AppConstant.KEY_TOKEN,"1b9fb7ce043e2a6ca01e8e5d7203df25"));
            val requestBuilder = original.newBuilder()
                .addHeader(
                    "Content-Type",
                    "application/json"
                ) //                        .addHeader("Authorization","bearer "+
                //                               new SPUtils(AppConstant.KEY_SETTING).getString(AppConstant.KEY_TOKEN,"1b9fb7ce043e2a6ca01e8e5d7203df25"))
                .method(original.method(), original.body())
            val request = requestBuilder.build()
            chain.proceed(request)
        })
        val client: OkHttpClient = httpClient!!.build()
        val retrofit: Retrofit = builder.client(client).build()
        return retrofit.create(serviceClass)
    }
    fun createApi(): ApiService {
        return createService(ApiService::class.java)
    }
}