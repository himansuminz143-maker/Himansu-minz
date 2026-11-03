package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class JarvisHomeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateJarvisAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateJarvisAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.jarvis_home_widget)

            val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            val dateStr = SimpleDateFormat("EEE, MMM dd", Locale.US).format(Date()).uppercase(Locale.US)

            val batteryStatus = context.applicationContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 94

            views.setTextViewText(R.id.widget_title_text, "J.A.R.V.I.S. // $dateStr")
            views.setTextViewText(R.id.widget_clock_text, "$timeStr • ARC CORE 3.20 GW")
            views.setTextViewText(R.id.widget_telemetry_text, "CELLS: $batteryPct% • TAP FOR FULL HUD")

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
