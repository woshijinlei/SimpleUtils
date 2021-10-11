package com.simple.commonutils.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


fun ArrayList<String>.changeToArray(): Array<String?> {
    val k: Array<String?> = arrayOfNulls<String>(this.size)
    forEach {
        k[indexOf(it)] = it
    }
    return k
}

fun MutableList<String>.changeToArrayList(): ArrayList<String?> {
    val k: ArrayList<String?> = arrayListOf()
    forEachIndexed { _, s ->
        k.add(s)
    }
    return k
}

class SimplePermissionFragment : androidx.fragment.app.Fragment() {
    private lateinit var mPermissions: MutableList<String>

    interface PermissionCallback {

        /**
         * @param isAllPermissionsGranted 所有请求的权限都获得用户同意后，返回true
         * @param hasPermissionsNeverShowAgain 当权限用户点击了不在提示，这时我们请求这个权限，不会出现系统权限框，
         *        在onRequestPermissionsResult回调中直接会返回未授予权限，我们通过
         *        [hasPermissionsNeverShowAgain] 返回true标示这种情况，可以提示进入设置详情页面，手动赋予权限
         */
        fun onPermissionStatus(
            isAllPermissionsGranted: Boolean,
            hasPermissionsNeverShowAgain: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("PermissionFragment", "onAttach")
        arguments?.getStringArrayList(EXTRA_PERMISSION)?.let { t ->
            mPermissions = t
            requestPermissions(t.changeToArray(), 123)
        } ?: kotlin.run {
            activity?.let { removeFragment(it) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("PermissionFragment", "onRequestPermissionsResult: ${grantResults.contentToString()}")
        setOnRequestPermissionsResult(grantResults)
        activity?.let { removeFragment(it) }
    }

    private fun setOnRequestPermissionsResult(grantResults: IntArray) {
        grantResults
            .filter { it == PackageManager.PERMISSION_DENIED }
            .let {
                if (it.isNotEmpty()) {
                    Log.d("PermissionFragment", "has permission denied")
                    val hasNeverShowPermission = mPermissions.find { permission ->
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            this.requireActivity(),
                            permission
                        )
                    }
                    permissionCallback?.onPermissionStatus(
                        false,
                        hasNeverShowPermission != null
                    )
                } else {
                    Log.d("PermissionFragment", "permissions granted")
                    permissionCallback?.onPermissionStatus(
                        isAllPermissionsGranted = true,
                        hasPermissionsNeverShowAgain = false
                    )
                }
            }
    }

    private fun removeFragment(a: FragmentActivity) {
        if (a.supportFragmentManager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) != null && this.isAdded) {
            a.supportFragmentManager.beginTransaction().apply {
                this.remove(this@SimplePermissionFragment)
                this.commitAllowingStateLoss()
                permissionCallback = null
            }
        }
    }

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "SimplePermissionFragmentTag"
        private const val EXTRA_PERMISSION = "permission"

        private var permissionCallback: PermissionCallback? = null

        fun openPermissionSetting(activity: Activity) {
            AlertDialog.Builder(activity).setMessage("We Would Like to Access the Permissions.")
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                    val packageURI = Uri.parse("package:${activity.packageName}")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                    activity.startActivity(
                        intent
                    )
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .show()
        }

        /**
         * 1.点击不再提示后，再请求权限是不会出现请求页面的，直接返回false
         * 2.android 11，One-time permissions，增加了Only this time选项，如果退到后台会保留一段时间的access
         * 3.android 11，Auto-reset permissions from unused apps
         */
        fun requestPermission(
            fragmentActivity: FragmentActivity,
            permissions: MutableList<String>,
            permissionCallback: PermissionCallback
        ) {
            permissions.filter { s ->
                ContextCompat.checkSelfPermission(
                    fragmentActivity,
                    s
                ) != PackageManager.PERMISSION_GRANTED
            }.let { list ->
                //有权限我们并没有获得，简单处理，不过滤全部发起权限申请
                if (list.isNotEmpty()) {
                    this.permissionCallback = permissionCallback
                    fragmentActivity.supportFragmentManager.beginTransaction().apply {
                        val fragment =
                            SimplePermissionFragment()
                        val b = Bundle()
                        b.putStringArrayList(EXTRA_PERMISSION, permissions.changeToArrayList())
                        fragment.arguments = b
                        add(fragment, PERMISSION_FRAGMENT_TAG)
                        commitAllowingStateLoss()
                    }
                } else {
                    permissionCallback.onPermissionStatus(
                        isAllPermissionsGranted = true,
                        hasPermissionsNeverShowAgain = false
                    )
                }
            }
        }
    }
}
