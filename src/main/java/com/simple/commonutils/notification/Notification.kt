package com.simple.commonutils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf

/**
 * 让用户有选择可以在app info中具体禁止某一类通道的通知
 *
 * [channel]全局唯一，[channelName]用于在通知管理中展示给用户看的
 */
@RequiresApi(Build.VERSION_CODES.N)
enum class Channel(val channel: String, val channelName: String, val importance: Int) {
    DefaultChannel("Default", "Message", NotificationManager.IMPORTANCE_DEFAULT),
}

/**
 * Created on 2023/6/27
 * @author jinlei
 */
interface Notification {
    /**
     * 显示[NotificationCompat.CATEGORY_MESSAGE]类型的BigTextStyle样式，可以点击展开显示全部的文字，点
     * 击通知，根据常见做法通知自动取消
     *
     * @param notificationId 由客户端管理
     * @param activityClass 默认没有行为，是否点击通知，启动我们的对应activity，默认配置了[notificationId]
     * 和[title]作为回传bundle，可以在对应activity的intent中区分app由通知启动
     */
    fun showBigTextStyleMessageNotification(
        notificationId: Int,
        channel: Channel,
        title: String,
        message: String,
        notificationSmallIcon: Int,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        activityClass: Class<*>? = null,
    )

    /**
     * 杀死进程后，相同的[notificationId]依然可以取消之前存在的通知
     *
     * @param notificationId
     */
    fun cancel(notificationId: Int)
}

class NotificationImpl(val context: Context) : Notification {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun showBigTextStyleMessageNotification(
        notificationId: Int,
        channel: Channel,
        title: String,
        message: String,
        notificationSmallIcon: Int,
        priority: Int,
        activityClass: Class<*>?,
    ) {
        createChannel(context, channel.channel, channel.channelName, channel.importance)
        val notification = NotificationCompat.Builder(context, channel.channel).apply {
            // 必须设置，在状态栏（主要）和下拉通知（api28）显示的小图标,有的版本在没有bigIcon时替代bigIcon
            this.setSmallIcon(notificationSmallIcon)
            // 通知右边会显示一个大的图片，比如社交类的通知，optional
            // this.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.model_1))
            // BigTextStyle共享这个title
            this.setContentTitle(title)
            // 仅仅显示省略的一行预览文字
            this.setContentText(message)
            this.setStyle(
                // 显示展开后的全部文字
                NotificationCompat.BigTextStyle().bigText(message)
            )
            // Setting this flag will make it so the notification is automatically
            // canceled when the user clicks it in the panel.
            this.setAutoCancel(true)
            this.priority = priority
            this.setCategory(NotificationCompat.CATEGORY_MESSAGE)
            if (activityClass != null) this.setContentIntent(
                createNormalPendentIntent(
                    context, activityClass, bundleOf(
                        "notificationId" to notificationId, "title" to title
                    )
                )
            )
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification.build())
    }

    override fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun createNormalPendentIntent(
        context: Context, clazz: Class<*>, bundle: Bundle?
    ): PendingIntent {
        return PendingIntent.getActivity(
            context, 12392004, Intent(context, clazz).apply { // todo request code
                bundle?.also {
                    this.putExtras(bundle)
                }
            }, PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(
        context: Context, notificationChannel: String, name: String, importance: Int
    ) {
        /*val importance = NotificationManager.IMPORTANCE_NONE//状态栏和下拉通知无任何信息（相当于设置中其实是关闭了通知）
        val importance =
            NotificationManager.IMPORTANCE_MIN//8.0默认没有声音，没有状态栏icon显示，同时下拉不显示title和context
        val importance = NotificationManager.IMPORTANCE_LOW//8.0默认没有声音，两条折叠
        val importance = NotificationManager.IMPORTANCE_DEFAULT//8.0默认有声音，五条折叠,在设置中为优先通知
        val importance = NotificationManager.IMPORTANCE_HIGH//8.0有声音，并且附带横幅通知 五条折叠*/
        val channel = NotificationChannel(notificationChannel, name, importance)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}