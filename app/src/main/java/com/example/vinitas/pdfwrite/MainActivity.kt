package com.example.vinitas.pdfwrite

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.print.*
import android.print.PdfView.openPdfFile
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var myWebView: WebView? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            STORAGE_PERMISSION_CODE)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    view.evaluateJavascript("loadMsg('loadMsg by javascript')", null)
                } else {
                    view.loadUrl("javascript:loadMsg('loadMsg by javascript')")
                }
            }
        }
        webView.loadUrl("file:///android_asset/html/index.html")
        myWebView = webView


        button.setOnClickListener {
            val path =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Stock Transfer"
            val fileName = "Test.pdf"
            val dir = File(path);
            if (!dir.exists())
                dir.mkdirs()

            val file = File(dir, fileName)
            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Please wait")
            progressDialog.show()
            PdfView.createWebPdfJob(
                this@MainActivity,
                webView,
                file,
                fileName,
                object : PdfView.Callback {
                    override fun success(path: String) {
                        progressDialog.dismiss()
                        val builder = AlertDialog.Builder(this@MainActivity)
                        with(builder) {
                            setTitle("File Exported")
                            setMessage("Do you want to Share the file?")
                            setPositiveButton("Share") { dialog, whichButton ->
//                                openPdfFile(this@MainActivity, path)
                                sendMail(path)
                            }
                            setNegativeButton("Cancel") { dialog, whichButton ->
                                dialog.dismiss()
                            }

                            // Dialog
                            val dialog = builder.create()

                            dialog.show()
                        }
                    }

                    override fun failure() {
                        progressDialog.dismiss()

                    }
                })
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun sendMail(path: String) {  //Send this pdf to desired path.
        val emailIntent = Intent(Intent.ACTION_SEND)

        val file = File(path)
        val uri =
            FileProvider.getUriForFile(this@MainActivity, "com.package.name.fileproviders", file)
        emailIntent.type = "application/pdf"
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(emailIntent, "Share to other application"))
        finish()
    }


    /*@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createWebPrintJob(webView: WebView) {

        val printManager = this
                .getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = webView.createPrintDocumentAdapter("MyDocument")

        val jobName = getString(R.string.app_name) + " Print Test"

        printManager.print(jobName, printAdapter,
                PrintAttributes.Builder().build())
    }*/


    /*  private fun createWebPrintJob(webView: WebView) {
          val jobName = getString(R.string.app_name) + " Document"
          val attributes = PrintAttributes.Builder()
                  .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                  .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                  .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
          val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/PDFTest/")
          val pdfPrint = PdfPrint(attributes)

          pdfPrint.print(webView.createPrintDocumentAdapter(jobName), path, "output_" + System.currentTimeMillis() + ".pdf")
      }*/
    /* @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     private fun createWebPrintJob(webView: WebView) {

         val printManager = this
                 .getSystemService(Context.PRINT_SERVICE) as PrintManager

         val printAdapter = webView.createPrintDocumentAdapter("MyDocument")

         val jobName = getString(R.string.app_name) + " Print Test"

         PrintAttributes attributes = new PrintAttributes.Builder()
         printManager.print(jobName, printAdapter,
                 PrintAttributes.Builder()
                         .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                         .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                 .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build())
     }*/
}
