package com.lagradost.cloudstream3.ui.browser;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.blankj.utilcode.util.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;

public class DownloadSubtitleTask extends AsyncTask<ResponseBody, Void, String> {
    WeakReference<Context> contextWeakReference;
    WeakReference<String> fileNameWeakReference;
    ICallback<String> iCallback;

    public DownloadSubtitleTask(Context context, String fileName, ICallback<String> iCallback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.fileNameWeakReference = new WeakReference<>(fileName);
        this.iCallback = iCallback;
    }

    @Override
    protected String doInBackground(ResponseBody... response) {
        if (contextWeakReference.get() != null) {
            Context context = contextWeakReference.get();
            String fileName = fileNameWeakReference.get() != null ? fileNameWeakReference.get() : "file" + System.currentTimeMillis() + ".srt";
            String filePath = writeResponseBodyToDisk(context, response[0], fileName);
            Log.d("DuongKK", "file download was a success? " + filePath != null ? "true" : "false");
            return filePath;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (iCallback != null) {
            iCallback.onCallback(s);
        }
    }

    private String writeResponseBodyToDisk(Context context, ResponseBody body, String filename) {
        try {
            String extension = filename.substring(filename.lastIndexOf("."));
            String nameZipFile = filename;
            nameZipFile = nameZipFile.replace(extension, ".zip");
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(context.getExternalFilesDir(null) + File.separator + nameZipFile);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("DuongKK", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                boolean unzipSuccess = ZipUtils.unzipFile(futureStudioIconFile, futureStudioIconFile.getParentFile());
                Log.d("DuongKK", "unzipSuccess: " + unzipSuccess);
                if (unzipSuccess) {
                    return new File(context.getExternalFilesDir(null) + File.separator + filename).getAbsolutePath();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
