package com.smartCarSeatProject.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle


class FirstActivity : BaseActivity() {

    private var mPermissionsChecker: PermissionsChecker? = null // 权限�?测器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPermissionsChecker = PermissionsChecker(this)

    }

    public override fun onResume() {
        super.onResume()
        // 缺少权限�?, 进入权限配置页面
        if (mPermissionsChecker!!.lacksPermissions(*PERMISSIONS)) {
            startPermissionsActivity()
        } else {
//            startActivity(Intent(this, WelcomeActivity::class.java))
            startActivity(Intent(this, TestActivity::class.java))
            finish()
        }

    }

    private fun startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, *PERMISSIONS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 拒绝�?, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish()
        } else {
//            startActivity(Intent(this, WelcomeActivity::class.java))
            startActivity(Intent(this, TestActivity::class.java))
            finish()
        }
    }

    companion object {

        private val REQUEST_CODE = 0 // 请求�?

        // �?�?的全部权�?
        @SuppressLint("InlinedApi")
        internal val PERMISSIONS = arrayOf(

                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
    }


}
