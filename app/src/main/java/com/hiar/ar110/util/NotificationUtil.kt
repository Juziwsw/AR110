package com.hiar.ar110.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.sendEvent

/**
 *
 * @author tangxucheng
 * @date 2021/7/1
 * Email: xucheng.tang@hiscene.com
 */
private var notificationManager: NotificationManager? = null
private const val NOTIFICATION_CHANNEL_ID = "new_police_item)"
var NOTIFICATION_ID = 999 //自己随便设置

fun sendNotification(cjdbh: String?, context: Context) {
    notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = buildNotification("您有新的警单要处理", "请尽快处理处警单:$cjdbh", cjdbh,context)
    notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
    notificationManager!!.notify(NOTIFICATION_ID, notification)
    CommEvent(CommEventTag.DATA_UPDATE).sendEvent()
}


fun buildNotification(title: String, msg: String, cjdbh2: String?,context: Context): Notification {
    val intent = Intent(context, AR110MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    //PendingIntent为一个特殊的Intent,通过getBroadcast或者getActivity或者getService得到.
    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)
    val builder = Notification.Builder(context).setContentTitle(title).setContentText(msg)
            .setPriority(Notification.PRIORITY_MIN).setSmallIcon(R.mipmap.ic_launcher) // .setColor(0xFF000000)
            .setOngoing(false)
            .setShowWhen(false).setContentIntent(pendingIntent).setAutoCancel(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        setupNotificationChannel(context)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)
    }

    //final int messageId = mVisibleWindow ? R.string.toggle_hide : R.string.toggle_show;
    //builder.addAction(android.R.drawable.ic_menu_preferences, res.getString(messageId),
    // PendingIntent.getService(this, 0, actionIntent, 0));
    return builder.build()
}

@RequiresApi(api = Build.VERSION_CODES.O)
fun setupNotificationChannel(context: Context) {
    val channelName = "Termux_Backer"
    val channelDescription = "Notifications from Termux_Backer"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            channelName, importance)
    channel.description = channelDescription
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}