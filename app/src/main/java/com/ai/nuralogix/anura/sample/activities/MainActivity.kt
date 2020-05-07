/*
 *              Copyright (c) 2016-2019, Nuralogix Corp.
 *                      All Rights reserved
 *
 *      THIS SOFTWARE IS LICENSED BY AND IS THE CONFIDENTIAL AND
 *      PROPRIETARY PROPERTY OF NURALOGIX CORP. IT IS
 *      PROTECTED UNDER THE COPYRIGHT LAWS OF THE USA, CANADA
 *      AND OTHER FOREIGN COUNTRIES. THIS SOFTWARE OR ANY
 *      PART THEREOF, SHALL NOT, WITHOUT THE PRIOR WRITTEN CONSENT
 *      OF NURALOGIX CORP, BE USED, COPIED, DISCLOSED,
 *      DECOMPILED, DISASSEMBLED, MODIFIED OR OTHERWISE TRANSFERRED
 *      EXCEPT IN ACCORDANCE WITH THE TERMS AND CONDITIONS OF A
 *      NURALOGIX CORP SOFTWARE LICENSE AGREEMENT.
 */

package com.ai.nuralogix.anura.sample.activities

import com.ai.nuralogix.anura.sample.activities.MeasurementActivity.Companion.FACE_ENGINE_KEY
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import ai.nuralogix.anurasdk.utils.CameraCapacityCheckResult
import ai.nuralogix.anurasdk.utils.CameraCapacityCheckUtil
import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.alibaba.android.mnnkit.monitor.MNNMonitor
import com.smartCarSeatProject.BuildConfig
import com.smartCarSeatProject.R
import java.io.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ANURA_MainActivity"
        const val MY_PERMISSIONS_REQUEST_CAMERA = 1
        val FACE_ENGINES = arrayOf("MNNFaceDetector", "SeetaFace2", "Visage")
    }

    private var faceIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_n)

        val goMeasurementBtn = findViewById<Button>(R.id.go_measuremnt_btn)
        goMeasurementBtn.setOnClickListener {
            if (CameraCapacityCheckResult.GOOD != CameraCapacityCheckUtil.isSupportDFXSDK(baseContext)) {
                Toast.makeText(baseContext, "Camera does not support", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity, MeasurementActivity::class.java)
            startActivity(intent)
        }

        val goConfigBtn = findViewById<Button>(R.id.go_config_btn)
        goConfigBtn.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }

        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
        MNNMonitor.setMonitorEnable(false)
        copyFileOrDir("r21r23h-8.dat")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_CAMERA)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.device_info_menus, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_device_info -> {
                val intent = Intent(this@MainActivity, DeviceInfoActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun copyFileOrDir(path: String) {
        val assetManager = this.assets
        val assets: Array<String>?
        try {
            assets = assetManager.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = filesDir.absolutePath + File.separator + path
                val dir = File(fullPath)
                if (!dir.exists())
                    dir.mkdir()
                for (asset in assets) {
                    copyFileOrDir("$path/$asset")
                }
            }
        } catch (ex: IOException) {
            AnuLogUtil.e(TAG, "copyFileOrDir exception: ${ex.message}")
        } catch (e: Exception) {
            AnuLogUtil.e(TAG, "copyFileOrDir exception: ${e.message}")
        }

    }

    private fun copyFile(filename: String) {
        val assetManager = this.assets

        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = assetManager.open(filename)
            val newFileName = filesDir.absolutePath + File.separator + filename

            out = FileOutputStream(newFileName)
            val buffer = ByteArray(1024)
            var read = `in`.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = `in`.read(buffer)
            }
            `in`.close()
            out.flush()
            out.close()

        } catch (e: Exception) {
            AnuLogUtil.e(TAG, "copyFile exception: ${e.message}")
        }

    }

    private fun showFaceEngineChoiceDialog() {
        val builder: AlertDialog.Builder? = let {
            AlertDialog.Builder(it)
        }
        builder?.apply {
            setTitle("Choose Face Engine")
            setItems(FACE_ENGINES, DialogInterface.OnClickListener { dialog, which ->
                faceIndex = which
                if (faceIndex == 2) {
                    showAlertDialog()
                } else {
                    val intent = Intent(this@MainActivity, MeasurementActivity::class.java)
                    intent.putExtra(FACE_ENGINE_KEY, faceIndex)
                    startActivity(intent)
                }
            })
            setNegativeButton("Cancel") { _, _ -> }
        }

        val dialog: AlertDialog? = builder?.create()
        dialog?.show()
    }

    private fun showAlertDialog() {
        val builder: AlertDialog.Builder? = let {
            AlertDialog.Builder(it)
        }
        builder?.apply {
            setPositiveButton("OK") { _, _ ->
                val intent = Intent(this@MainActivity, MeasurementActivity::class.java)
                intent.putExtra(FACE_ENGINE_KEY, faceIndex)
                startActivity(intent)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setMessage("Please note that the sample is using Visage for face tracking with temporary license. \n" +
                    "Please contact Visage for permanent license to be able to use visage in your product.")
        }

        val dialog: AlertDialog? = builder?.create()
        dialog?.show()
    }
}
