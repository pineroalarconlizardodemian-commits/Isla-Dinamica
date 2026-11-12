package com.tuapp.isla

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class IslaService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var islaView: IslaView
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        crearNotificacionForeground()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        crearIsla()
    }

    private fun crearNotificacionForeground() {
        val canalId = "isla_canal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId, "Isla Dinámica",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(canal)
        }
        val notif = NotificationCompat.Builder(this, canalId)
            .setContentTitle("Isla Dinámica activa")
            .setContentText("Toca para configurar")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
        startForeground(1, notif)
    }

    private fun crearIsla() {
        islaView = IslaView(this) { accion ->
            when (accion) {
                IslaView.Accion.SUBIR_BRILLO -> ajustarBrillo(true)
                IslaView.Accion.BAJAR_BRILLO -> ajustarBrillo(false)
                IslaView.Accion.SUBIR_VOLUMEN -> ajustarVolumen(true)
                IslaView.Accion.BAJAR_VOLUMEN -> ajustarVolumen(false)
                IslaView.Accion.ABRIR_AJUSTES -> {
                    val i = Intent(this, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                }
            }
        }

        val tipoVentana = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            200, 80,
            tipoVentana,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 100
        }

        windowManager.addView(islaView, params)
    }

    private fun ajustarBrillo(subir: Boolean) {
        if (!Settings.System.canWrite(this)) return
        val cr = contentResolver
        var actual = Settings.System.getInt(
            cr, Settings.System.SCREEN_BRIGHTNESS, 128
        )
        actual = if (subir) (actual + 15).coerceAtMost(255)
                 else (actual - 15).coerceAtLeast(0)
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, actual)
    }

    private fun ajustarVolumen(subir: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val dir = if (subir) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::islaView.isInitialized) {
            windowManager.removeView(islaView)
        }
    }
}
