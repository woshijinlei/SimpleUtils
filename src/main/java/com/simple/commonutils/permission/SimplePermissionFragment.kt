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

typealias permissionHandle<T, R> = (T, R) -> Unit

fun ArrayList<String>.changeToArray(): Array<String?> {
    val k: Array<String?> = arrayOfNulls<String>(this.size)
    forEach {
        k[indexOf(it)] = it
    }
    return k
}

fun MutableList<String>.changeToArrayList(): ArrayList<String?> {
    val k: ArrayList<String?> = arrayListOf()
    forEachIndexed { index, s ->
        k.add(s)
    }
    return k
}

//具有权限直接回调
//不具有权限，没有勾选不再提醒，onRequestPermissionsResult
//不具有权限，勾选不再提醒，onRequestPermissionsResult
class SimplePermissionFragment : androidx.fragment.app.Fragment() {
    lateinit var mPermissions: MutableList<String>
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("PermissionFragment", "onAttach")
        activity?.let { a ->
            arguments?.getStringArrayList("permissions")?.let { t ->
                mPermissions = t
                t.filter { s ->
                    ContextCompat.checkSelfPermission(a, s) != PackageManager.PERMISSION_GRANTED
                }.let { list ->
                    if (list.isNotEmpty()) {
                        requestPermissions(t.changeToArray(), 123)
                    } else {
                        mHandle?.invoke(true, false)
                        removeFragment(a)
                    }
                }
            }

        }
    }

    private fun removeFragment(a: androidx.fragment.app.FragmentActivity) {
        if (a.supportFragmentManager.findFragmentByTag("permission") != null && this.isAdded) {
            a.supportFragmentManager.beginTransaction().apply {
                this.remove(this@SimplePermissionFragment)
                this.commitAllowingStateLoss()
                mHandle = null
            }
        }
    }

    companion object {

        private var mHandle: permissionHandle<Boolean, Boolean>? = null

        fun openPermissionSetting(activity: Activity) {
            AlertDialog.Builder(activity).setMessage("We Would Like to  Access the Permissions")
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

        fun requestPermission(
            manager: androidx.fragment.app.FragmentManager,
            permissions: MutableList<String>,
            handle: permissionHandle<Boolean, Boolean>
        ) {
            mHandle = handle
            manager.beginTransaction().apply {
                val fragment =
                    SimplePermissionFragment()
                val b = Bundle()
                b.putStringArrayList("permissions", permissions.changeToArrayList())
                fragment.arguments = b
                add(fragment, "permission")
                commitAllowingStateLoss()
            }
        }

    }

    private fun setOnRequestPermissionsResult(grantResults: IntArray) {
        grantResults
            .filter { it == PackageManager.PERMISSION_DENIED }
            .let {
                if (it.isNotEmpty()) {
                    Log.d("PermissionFragment", "onDontPermissions")
                    mHandle?.invoke(
                        false,
                        !ActivityCompat.shouldShowRequestPermissionRationale(//是否显示dont ask again
                            this.requireActivity(),
                            mPermissions[0]
                        )
                    )
                } else {
                    Log.d("PermissionFragment", "onHasPermissions")
                    mHandle?.invoke(true, false)
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            Log.d("PermissionFragment", "onRequestPermissionsResult: $it")
        }
        setOnRequestPermissionsResult(grantResults)
        activity?.let { removeFragment(it) }
    }
}
