@file:Suppress("MemberVisibilityCanBePrivate")

package com.simple.commonutils.share

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import com.simple.commonutils.log

object SimpleShareUtils {

    private fun shareIntentByFilter2(context: Activity, intent: Intent, packageName: String) {
        try {
            val resolveInfo =
                context.packageManager.queryIntentActivities(intent, 0)//time consume 43
            val r = resolveInfo.find {
                it.activityInfo.packageName == packageName
            }
            log(
                "instagram", " ${r?.activityInfo?.name}"
            )
            //SimpleShareUtils.instagram:  com.instagram.direct.share.handler.DirectExternalPhotoShareActivity
            //SimpleShareUtils.instagram:  com.instagram.share.handleractivity.ShareHandlerActivity
            //SimpleShareUtils.instagram:  com.instagram.share.handleractivity.StoryShareHandlerActivity
            if (r != null) {
                context.startActivity(intent.apply {
                    r.activityInfo?.name?.let {
                        this.component = ComponentName(packageName, it)
                    }
                })
            } else {
                shareIntentByChooser(context, intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareIntentByFilter(context: Activity, intent: Intent, packageName: String) {
        try {
            context.startActivity(intent.apply {
                this.`package` = packageName
            })
        } catch (e: Exception) {
            shareIntentByChooser(context, intent)
        }
    }

    fun shareIntentByChooser(context: Activity, intent: Intent) {
        val c = Intent.createChooser(intent, null)
        context.startActivity(c)
    }

}