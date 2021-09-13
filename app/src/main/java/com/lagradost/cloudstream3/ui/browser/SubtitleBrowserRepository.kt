package com.lagradost.cloudstream3.ui.browser

import android.util.Log
import com.blankj.utilcode.util.ZipUtils
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.services.ApiUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class SubtitleBrowserRepository {

    fun downloadSubtitle(url: String, fileDir: String, callback: (List<String>) -> Unit) {
        ApiUtils().createApi().downloadZipSubtitleFile(url)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    response.body()?.let {
                        val filePath = extractZipSubtitle(it, fileDir)
                        Log.d("DuongKK","File $filePath" )
                        filePath?.let{
                            callback.invoke(it)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("DuongKK", "onFailure ${t.message}")
                }
            })

    }

    fun extractZipSubtitle(responseBody: ResponseBody, fileDir: String): List<String>? {
        val fileName = "file" + System.currentTimeMillis() + ".srt"
        val filePath: List<String>? = writeResponseBodyToDisk(fileDir, responseBody, fileName)
        Log.d("DuongKK", if ("file download was a success? $filePath" != null) "true" else "false")
        return filePath
    }


    private fun writeResponseBodyToDisk(
        fileDir: String,
        body: ResponseBody,
        filename: String
    ): List<String>? {
        return try {
            val extension = filename.substring(filename.lastIndexOf("."))
            var nameZipFile = filename
            nameZipFile = nameZipFile.replace(extension, ".zip")
            // todo change the file location/name according to your needs
            val futureStudioIconFile =
                File(fileDir + File.separator + nameZipFile)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d("DuongKK", "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                val unzipSuccess =
                    ZipUtils.unzipFile(futureStudioIconFile, futureStudioIconFile.parentFile)
                Log.d("DuongKK", "unzipSuccess: $unzipSuccess")
                if (unzipSuccess) {
                    val listFileInZip = ZipUtils.getFilesPath(futureStudioIconFile)
                    val result = arrayListOf<String>()
                    for ( fileName in listFileInZip){
                        val filePath = File(fileDir + File.separator + fileName).absolutePath
                        result.add(filePath)
                    }
                    result
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}