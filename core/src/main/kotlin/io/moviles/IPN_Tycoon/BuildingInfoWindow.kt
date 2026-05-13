package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*

class BuildingInfoWindow(
    private val data: Propiedad,
    private val onBuildingChanged: () -> Unit
) : VisWindow("Gestión del Plantel") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = false

        val costo: Long
        val btnTexto: String
        val puedeMejorar: Boolean

        when {
            !data.comprada -> {
                costo        = data.precio
                btnTexto     = "COMPRAR  \$${fmt(costo)}"
                puedeMejorar = true
            }
            data.nivel < data.mejoraMax -> {
                costo        = GameState.costoMejora(data)
                btnTexto     = "MEJORAR LVL ${data.nivel + 1}  \$${fmt(costo)}"
                puedeMejorar = true
            }
            else -> {
                costo        = 0L
                btnTexto     = "NIVEL MÁXIMO"
                puedeMejorar = false
            }
        }

        add(scene2d.table {

            // ── Nombre ────────────────────────────────────────────────
            label(data.nombre) {
                color = Color.GOLD
            }.cell(padBottom = 6f)
            row()

            // ── Descripción ───────────────────────────────────────────
            label(data.descripcion) {
                setWrap(true)
                color = Color.LIGHT_GRAY
            }.cell(width = 280f, padBottom = 10f)
            row()

            // ── Stats ─────────────────────────────────────────────────
            label("Capacidad: ${data.capacidad} alumnos"); row()
            label("Ingresos base: ${data.baseAlumnos} alumnos/ciclo"); row()

            if (data.comprada) {
                label("Nivel actual: ${data.nivel} / ${data.mejoraMax}") {
                    color = Color.CYAN
                }
                row()
            }

            // ── Saldo ─────────────────────────────────────────────────
            label("Tu saldo: \$${fmt(GameState.dinero)}") {
                color = Color.LIGHT_GRAY
            }.cell(padTop = 6f, padBottom = 4f)
            row()

            // ── Botón acción ──────────────────────────────────────────
            textButton(btnTexto) {
                isDisabled = !puedeMejorar

                onChange {
                    if (!puedeMejorar) return@onChange

                    if (!GameState.puedeComprar(costo)) {
                        setText("¡Saldo insuficiente!")
                        color = Color.RED
                        isDisabled = true
                        return@onChange
                    }

                    GameState.gastar(costo)

                    if (!data.comprada) {
                        data.comprada = true
                        data.nivel    = 1
                    } else {
                        data.nivel++
                    }

                    onBuildingChanged()
                    this@BuildingInfoWindow.remove()
                }
            }.cell(padTop = 14f, expandX = true, fillX = true)
        }).pad(16f)

        pack()
        centerWindow()
    }

    fun show(stage: Stage) { stage.addActor(this) }

    private fun fmt(v: Long) = when {
        v >= 1_000_000L -> "${"%.1f".format(v / 1_000_000.0)}M"
        v >= 1_000L     -> "${"%.0f".format(v / 1_000.0)}K"
        else            -> v.toString()
    }
}
