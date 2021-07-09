package com.simple.commonutils.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.commonutils.log
import com.simple.commonutils.permission.SimpleActivityResultFragment
import com.simple.commonutils.permission.SimplePermissionFragment

/**
 * 适配android读写权限
 */
object StoragePermissionHelper {

    /**
     * api29的范围存储: 自动获得MediaStore写入文件的权限，无需任何声明
     *
     * manifest配置:
     * 1.<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"android:maxSdkVersion="28" />
     * 2.<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     *
     * 权限要求: 在大于等于api29的机器上，我们不需要写入权限，只需声明读取权限即可，小于api29，还是都需要的
     * 存储范围: api29除了私有目录，只能通过MediaStore提供的api存储在外部分享目录(注意外部公有目录存储我们是没有权限的)
     * 注意说明: 1.从api29范围存储出现就开始适配，并没有设置requestLegacyExternalStorage=true，
     *         2.另外如果我们仅仅是插入公有媒体文件到MediaStore中，api29我们不需要任何声明，自动具有写入权限
     */
    fun requestScopeStoragePermission(
        activity: FragmentActivity,
        needReadPermission: Boolean,
        granted: (granted: Boolean, neverShowAgain: Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//<23
            granted.invoke(true, false)
        } else {
            val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (needReadPermission) {
                    mutableListOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            } else {
                if (needReadPermission) {
                    mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    mutableListOf()
                }
            }
            if (permissions.isEmpty()) {//api29，仅仅插入数据到MediaStore并不读取，不需要任何权限
                granted.invoke(true, false)
                return
            }
            SimplePermissionFragment.requestPermission(activity,
                permissions, object : SimplePermissionFragment.PermissionCallback {
                    override fun onPermissionStatus(
                        isAllPermissionsGranted: Boolean,
                        hasPermissionsNeverShowAgain: Boolean
                    ) {
                        log(
                            "onPermissionStatus",
                            "$isAllPermissionsGranted $hasPermissionsNeverShowAgain"
                        )
                        granted.invoke(isAllPermissionsGranted, hasPermissionsNeverShowAgain)
                    }
                })
        }
    }

    /**
     * 外部公有目录存储权限
     *
     * manifest配置:
     * 1.<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"android:maxSdkVersion="29" />
     * 2.<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * 3.<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
     * 4.android:requestLegacyExternalStorage="true"
     *
     * 1.小于等于api29(Q)，设置requestLegacyExternalStorage=true，保持和之前一样声明和请求读写权限
     * 2.大于等于api30(R)，声明MANAGE_EXTERNAL_STORAGE权限，但是我们还想访问MediaStore的API，
     *   是否还需要声明读权限(这个地方没有真机测试，模拟器是不需要READ_EXTERNAL_STORAGE) todo
     */
    fun requestExternalStoragePermission(
        activity: FragmentActivity,
        granted: (granted: Boolean, neverShowAgain: Boolean) -> Unit
    ) {
        @Suppress("CascadeIf")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//<23
            granted.invoke(true, false)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {//<=29
            val permissions = mutableListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            SimplePermissionFragment.requestPermission(activity,
                permissions, object : SimplePermissionFragment.PermissionCallback {
                    override fun onPermissionStatus(
                        isAllPermissionsGranted: Boolean,
                        hasPermissionsNeverShowAgain: Boolean
                    ) {
                        log(
                            "onPermissionStatus",
                            "$isAllPermissionsGranted $hasPermissionsNeverShowAgain"
                        )
                        granted.invoke(isAllPermissionsGranted, hasPermissionsNeverShowAgain)
                    }
                })
        } else {//>29
            SimpleActivityResultFragment.requestActivityResult(
                activity,
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                ), object : SimpleActivityResultFragment.ActivityResultCallback {
                    override fun onActivityResult(
                        requestCode: Int,
                        resultCode: Int,
                        data: Intent?
                    ) {
                        granted.invoke(Environment.isExternalStorageManager(), false)
                    }
                }
            )
        }

    }
}