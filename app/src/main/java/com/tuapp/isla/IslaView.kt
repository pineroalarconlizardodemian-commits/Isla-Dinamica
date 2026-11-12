package com.tuapp.isla

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView

@SuppressLint("ViewConstructor")
class IslaView(
    context: Context,
    private val onAccion: (Accion) -> Unit
) : FrameLayout(context) {

    enum class Accion {
        SUBIR_BRILLO, BAJAR_BRILLO,
        SUBIR_VOLUMEN, BAJAR_VOLUMEN,
        ABRIR_AJUSTES
    }

    private var modoGestoActivo = false

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {

            override fun onDoubleTap(e: MotionEvent): Boolean {
                modoGestoActivo = true
                mostrarFeedback("Modo gesto activo")
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!modoGestoActivo) {
                    onAccion(Accion.ABRIR_AJUSTES)
                }
                return true
            }

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent,
                distanceX: Float, distanceY: Float
            ): Boolean {
                if (!modoGestoActivo || e1 == null) return false

                val mitad = width / 2f
                val enIzquierda = e1.x < mitad

                if (enIzquierda) {
                    if (distanceX > 0) onAccion(Accion.SUBIR_BRILLO)
                    else onAccion(Accion.BAJAR_BRILLO)
                } else {
                    if (distanceX > 0) onAccion(Accion.SUBIR_VOLUMEN)
                    else onAccion(Accion.BAJAR_VOLUMEN)
                }
                return true
            }
        })

    private val texto: TextView = TextView(context).apply {
        text = "●"
        setTextColor(Color.WHITE)
        textSize = 22f
        gravity = android.view.Gravity.CENTER
    }

    init {
        setBackgroundColor(Color.parseColor("#CC000000"))
        addView(texto, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun mostrarFeedback(msg: String) {
        texto.text = msg
        postDelayed({ texto.text = "●" }, 1200)
    }
}
