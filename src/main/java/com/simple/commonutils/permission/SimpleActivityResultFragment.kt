package com.simple.commonutils.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

class SimpleActivityResultFragment : androidx.fragment.app.Fragment() {

    interface ActivityResultCallback {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("SimpleARFragment", "onAttach")
        if (activityResultCallback == null) {
            removeFragment(requireActivity())
        } else {
            arguments?.getParcelable<Intent>(EXTRA_INTENT)?.let { t ->
                startActivityForResult(t, 693)
            } ?: kotlin.run {
                activity?.let { removeFragment(it) }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("SimpleARFragment", "onActivityResult")
        setActivityResult(requestCode, resultCode, data)
        activity?.let { removeFragment(it) }
    }

    private fun setActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultCallback?.onActivityResult(requestCode, resultCode, data)
        activityResultCallback = null
    }

    private fun removeFragment(a: FragmentActivity) {
        if (a.supportFragmentManager.findFragmentByTag(ACTIVITY_RESULT_FRAGMENT_TAG) != null && this.isAdded) {
            a.supportFragmentManager.beginTransaction().apply {
                this.remove(this@SimpleActivityResultFragment)
                this.commitAllowingStateLoss()
                activityResultCallback = null
            }
        }
    }

    companion object {
        private const val ACTIVITY_RESULT_FRAGMENT_TAG = "SimpleActivityResultFragment"
        private const val EXTRA_INTENT = "fintent"

        private var activityResultCallback: ActivityResultCallback? = null

        fun requestActivityResult(
            fragmentActivity: FragmentActivity,
            intent: Intent,
            permissionCallback: ActivityResultCallback
        ) {
            this.activityResultCallback = permissionCallback
            fragmentActivity.supportFragmentManager.beginTransaction().apply {
                val fragment = SimpleActivityResultFragment()
                val b = Bundle()
                b.putParcelable(EXTRA_INTENT, intent)
                fragment.arguments = b
                add(fragment, ACTIVITY_RESULT_FRAGMENT_TAG)
                commitAllowingStateLoss()
            }
        }
    }
}
