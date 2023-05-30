package com.simple.commonutils.intent

import android.app.Activity
import android.content.Intent
import android.net.Uri

object SimpleIntentFactory {
    fun goHomeScreen(activity: Activity) {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(startMain)
    }

    fun createPickPhotoIntent(): Intent {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        return photoPickerIntent
    }

    fun createPickPhotoIntentByContent(): Intent {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        return photoPickerIntent
    }

    fun createShareImageIntent(providerUri: Uri, writePermission: Boolean = false): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, providerUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (writePermission) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        return intent
    }

    fun createShareVideoIntent(providerUri: Uri, writePermission: Boolean = false): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_STREAM, providerUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (writePermission) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        return intent
    }

}