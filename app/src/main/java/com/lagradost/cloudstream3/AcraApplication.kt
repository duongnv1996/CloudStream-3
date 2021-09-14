package com.lagradost.cloudstream3

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.blankj.utilcode.util.Utils
import com.google.auto.service.AutoService
import com.lagradost.cloudstream3.mvvm.normalSafeApiCall
import com.lagradost.cloudstream3.utils.Coroutines.runOnMainThread
import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import kotlin.concurrent.thread

class CustomReportSender : ReportSender {
    // Sends all your crashes to google forms
    override fun send(context: Context, errorContent: CrashReportData) {
        println("Sending report")
        val url =
            "https://docs.google.com/forms/u/0/d/e/1FAIpQLSeFmyBChi6HF3IkhTVWPiDXJtxt8W0Hf4Agljm_0-0_QuEYFg/formResponse"
        val data = mapOf(
            "entry.134906550" to errorContent.toJSON()
        )

        thread { // to not run it on main thread
            normalSafeApiCall {
                val post = khttp.post(url, data = data)
                println("Report response: $post")
            }
        }

        runOnMainThread { // to run it on main looper
            normalSafeApiCall {
                Toast.makeText(context, R.string.acra_report_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@AutoService(ReportSenderFactory::class)
class CustomSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return CustomReportSender()
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}

class AcraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            reportContent = arrayOf(
                ReportField.BUILD_CONFIG, ReportField.USER_CRASH_DATE,
                ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE
            )

            // removed this due to bug when starting the app, moved it to when it actually crashes
            //each plugin you chose above can be configured in a block like this:
            /*toast {
                text = getString(R.string.acra_report_toast)
                //opening this block automatically enables the plugin.
            }*/
        }
    }
}