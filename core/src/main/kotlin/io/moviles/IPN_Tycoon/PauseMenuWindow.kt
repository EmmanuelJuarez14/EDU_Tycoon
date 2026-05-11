package io.moviles.IPN_Tycoon

import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*

class PauseMenuWindow(
    private val game: Main,
    private val onGoToMainMenu: () -> Unit
) : VisWindow("⚙  MENÚ") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = true

        add(scene2d.table {
            defaults().expandX().fillX().pad(6f)

            // ── Guardar partida ───────────────────────────────────────
            val saveBtn = textButton("💾  Guardar partida") {}
            saveBtn.onChange {
                // Solo guarda si hay un slot real asignado (no testing)
                if (GameState.slotActual == 3 || GameState.slotActual == 0) {
                    saveBtn.setText("ℹ Usa slot 1 o 2 para guardar")
                    return@onChange
                }
                saveBtn.isDisabled = true
                saveBtn.setText("Guardando…")
                game.saveManager.guardar(GameState.slotActual) { ok ->
                    saveBtn.setText(if (ok) "✔ Guardado" else "✘ Error al guardar")
                    saveBtn.isDisabled = false
                }
            }
            row()

            // ── Estadísticas ──────────────────────────────────────────
            textButton("📊  Estadísticas") {
                onChange { StatsWindow().show(stage) }
            }
            row()

            // ── Audio ─────────────────────────────────────────────────
            val audioBtn = textButton(audioLabel()) {}
            audioBtn.onChange {
                GameState.musicaActiva = !GameState.musicaActiva
                audioBtn.setText(audioLabel())
                // TODO: conectar con MusicManager cuando esté listo
            }
            row()

            // ── Menú principal ────────────────────────────────────────
            textButton("🏠  Menú principal") {
                onChange {
                    this@PauseMenuWindow.remove()
                    onGoToMainMenu()
                }
            }
        }).pad(20f).minWidth(280f)

        pack()
        centerWindow()
    }

    private fun audioLabel() =
        if (GameState.musicaActiva) "🔊  Audio: ON" else "🔇  Audio: OFF"

    fun show(stage: Stage) { stage.addActor(this) }
}

// ── Ventana de estadísticas ───────────────────────────────────────────
class StatsWindow : VisWindow("📊  ${GameState.nombreEscuela.ifBlank { "Estadísticas" }}") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = false

        val edificios      = PropiedadRepository.propiedades.values.count { it.comprada }
        val alumnosTotales = PropiedadRepository.propiedades.values
            .filter { it.comprada }.sumOf { it.baseAlumnos * it.nivel }
        val ingresosCiclo  = alumnosTotales * 100L

        add(scene2d.table {
            defaults().left().padBottom(8f)
            label("[GOLD]💰  Dinero:[]  \$${fmt(GameState.dinero)}"); row()
            label("[CYAN]🏫  Edificios:[]  $edificios comprados"); row()
            label("[GREEN]👨‍🎓  Alumnos:[]  $alumnosTotales"); row()
            label("[YELLOW]📈  Ingresos/ciclo:[]  \$${fmt(ingresosCiclo)}"); row()
            label("[LIGHT_GRAY]🔄  Ciclos:[]  ${GameState.ciclosJugados}"); row()
            label("[LIGHT_GRAY]👤  Jugador:[]  ${GameState.nombreJugador}"); row()
        }).pad(20f).minWidth(260f)

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
