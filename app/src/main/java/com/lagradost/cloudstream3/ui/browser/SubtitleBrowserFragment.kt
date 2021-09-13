package com.lagradost.cloudstream3.ui.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.mvvm.observe
import com.lagradost.cloudstream3.ui.result.ResultViewModel
import com.lagradost.cloudstream3.utils.UIHelper.popCurrentPage
import kotlinx.android.synthetic.main.fragment_subtitle_browser.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.w3c.dom.Element

class SubtitleBrowserFragment : Fragment() {
    companion object {
        fun newInstance(): SubtitleBrowserFragment {
            val args = Bundle()
            val fragment = SubtitleBrowserFragment()
            fragment.arguments = args
            return fragment
        }

        const val DOMAIN_SUBSCENE = "https://subscene.com"
    }

    private lateinit var viewModel: SubtitleBrowserViewModel
    private lateinit var shareViewModel: ShareViewModel
    private var elementDownloadLink: org.jsoup.nodes.Element? = null
    private val iCallback = ICallback<String> { html ->
        val doc = Jsoup.parse(html);
        val element = doc.body().getElementById("downloadButton");
        if (element != null) {
            Log.d("DuongKK", "element ${element.attr("href")}")
            CoroutineScope(Dispatchers.Main).launch {
                tvDownload.visibility = View.VISIBLE
            }
            elementDownloadLink = element
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                tvDownload.visibility = View.GONE
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subtitle_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(SubtitleBrowserViewModel::class.java)
        shareViewModel =
            ViewModelProvider(activity ?: this).get(ShareViewModel::class.java)
        btnBack.setOnClickListener {
            activity?.popCurrentPage()
        }
        tvDownload.setOnClickListener {
            tvDownload?.visibility = View.GONE
            downloadSub()
        }
        btnMore.setOnClickListener {
            tvDownload?.visibility = View.VISIBLE
        }
        webview.settings.javaScriptEnabled = true
        webview.addJavascriptInterface(
            JavaScriptInterface(iCallback),
            "HTMLOUT"
        )
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                Log.d("DuongKK", " onPageFinished ${request.url.toString()}")
                val urlRequest = request.url.toString()
                elementDownloadLink?.let {
                    val urlDownload = DOMAIN_SUBSCENE + it.attr("href");
                    if (urlDownload == urlRequest)
                        downloadSub()
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.d("DuongKK", "onPageFinished $url")
                loading?.visibility = View.GONE
                tvTitle?.text = url
                parseHtmlAndDownload()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loading?.visibility = View.VISIBLE
                tvTitle?.text = url
            }
        }
        webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                loading?.let { loading ->
                    loading.isIndeterminate = false
                    loading.max = 100
                    loading.progress = newProgress
                    if (loading.progress == 100) {
                        loading.visibility = View.GONE
                    } else {
                        loading.visibility = View.VISIBLE
                    }
                }

            }
        }
        webview.loadUrl(DOMAIN_SUBSCENE)

        observe(viewModel.resultResponse) { data ->
            when (data) {
                is Resource.Success -> {
                    loading.visibility = View.GONE
                    val data = data.value as List<String>
                    activity?.popCurrentPage()
                    shareViewModel.notifyToPlayer(data)
                }
                is Resource.Loading -> {
                    loading?.isIndeterminate = true
                    loading?.visibility = View.VISIBLE
                }
                is Resource.Failure -> {
                    loading?.visibility = View.GONE
                    Toast.makeText(context, "Error ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseHtmlAndDownload() {
        webview?.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
    }

    private fun downloadSub() {
        elementDownloadLink?.let { elementDownloadLink ->
            val urlDownload = DOMAIN_SUBSCENE + elementDownloadLink.attr("href")
            val fileDir = context?.getExternalFilesDir(null)?.absolutePath
            fileDir?.let {
                Log.d("DuongKK", "URL SUB $urlDownload to $it")
                viewModel.downloadSubtitle(urlDownload, it)
            }
        }

    }

    override fun onDestroyView() {
        viewModel.resultResponse.removeObservers(this)
        super.onDestroyView()
    }

}