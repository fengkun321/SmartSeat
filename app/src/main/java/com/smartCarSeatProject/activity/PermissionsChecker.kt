package com.smartCarSeatProject.activity

import android.content.Context
import android.content.pm.PackageManager

/**
 * 妫?鏌ユ潈闄愮殑宸ュ叿绫?
 *
 *
 * Created by wangchenlong on 16/1/26.
 */
class PermissionsChecker(context: Context) {

    private val mContext: Context
    internal var pm: PackageManager? = null

    init {
        mContext = context.applicationContext
        pm = mContext.packageManager
    }

    // 鍒ゆ柇鏉冮檺闆嗗悎
    fun lacksPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (lacksPermission(permission)) {
                return true
            }
        }
        return false
    }

    // 鍒ゆ柇鏄惁缂哄皯鏉冮檺
    private fun lacksPermission(permission: String): Boolean {
        return PackageManager.PERMISSION_DENIED == pm!!.checkPermission(permission, mContext.packageName)
    }


}
