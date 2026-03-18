package com.example.fgclockwidget

import android.app.AlarmManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.github.lalyos.jfiglet.FigletFont

//拡張プロパティ　datastore をインスタンス化　by デリゲートによってシングルトン化
val Context.dataStore by preferencesDataStore(name = "widget_settings")
suspend fun setColor(context: Context, color: Color) {
    context.dataStore.edit { prefs ->
        prefs[WidgetSettings.TEXT_COLOR] = color.toArgb()

        val now =
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        prefs[WidgetSettings.LAST_UPDATE_TIME] = now
    }
    FgClockWidget().updateAll(context)
}
suspend fun setFont(context: Context, font: String) {

    context.dataStore.edit { prefs ->
        prefs[WidgetSettings.FONT] = font
        //一応時刻も更新
        val now =
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        prefs[WidgetSettings.LAST_UPDATE_TIME] = now
    }
    FgClockWidget().updateAll(context)
}

fun isExactAlarmPermissionGranted(context: Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

object WidgetSettings {
    const val DEFAULT_FONT = "alligator2.flf"
    const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF.toInt()
    const val EMPTY_TIME = "--:--"
    val FONT = stringPreferencesKey("font")
    val LAST_UPDATE_TIME = stringPreferencesKey("last_update_time")
    val TEXT_COLOR = intPreferencesKey("text_color")

    val fontList = listOf(
        "alligator.flf",
        "alligator2.flf",
        "banner3d.flf",
        "graffiti.flf",
        "Rebel.flf",
        "BlurVisionASCII.flf",
        "ANSIShadow.flf",
        "Nipples.flf"
    )
}
object FigletCache {
    //hashmap (高速なmapみたいなもん)
    private val fontDataMap = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()

    fun render(context: Context, font: String, text: String): String {
        //あるなら取り出すないなら追加
        val data = fontDataMap.getOrPut(font) {
            context.assets.open(font).readBytes()
        }
        return FigletFont.convertOneLine(data.inputStream(), text)
            .replace("\r", "")
    }
}