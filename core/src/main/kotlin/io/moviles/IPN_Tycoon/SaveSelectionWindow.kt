package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*
import java.text.SimpleDateFormat
import java.util.*

class SaveSelectionWindow(
    private val game: Main,
    private val onSaveComplete: (Boolean) -> Unit
) : VisWindow("Seleccionar Slot para Guardar") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = true

        loadSlotsAndBuild()
    }

    private fun loadSlotsAndBuild() {
        game.saveManager.cargarSlots { s1, s2 ->
            buildUI(s1, s2)
        }
    }

    private fun buildUI(s1: GameSaveData?, s2: GameSaveData?) {
        clearChildren()
        addCloseButton()

        add(scene2d.table {
            defaults().pad(10f)

            // Slot 1
            addSlotUI(this, 1, s1)
            // Slot 2
            addSlotUI(this, 2, s2)

        }).pad(10f)

        pack()
        centerWindow()
    }

    private fun addSlotUI(table: KTableWidget, slotNum: Int, data: GameSaveData?) {
        table.table {
            val isOccupied = data != null

            label("SLOT $slotNum") { color = Color.GOLD }.cell(padBottom = 5f)
            row()

            if (isOccupied) {
                val fecha = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                    .format(Date(data!!.fechaGuardado))
                label("${data.nombreEscuela}\n${data.nombreJugador}\n$fecha") {
                    setAlignment(com.badlogic.gdx.utils.Align.center)
                }.cell(width = 180f, height = 80f)
            } else {
                label("VACÍO") {
                    color = Color.LIGHT_GRAY
                }.cell(width = 180f, height = 80f)
            }
            row()

            textButton(if (isOccupied) "OCUPADO" else "GUARDAR AQUÍ") {
                isDisabled = isOccupied
                if (!isOccupied) {
                    onChange {
                        isDisabled = true
                        setText("Guardando...")
                        game.saveManager.guardar(slotNum) { ok ->
                            if (ok) {
                                GameState.slotActual = slotNum
                                onSaveComplete(true)
                                this@SaveSelectionWindow.remove()
                            } else {
                                setText("Error")
                                isDisabled = false
                            }
                        }
                    }
                }
            }.cell(fillX = true, padTop = 10f)
        }
    }

    fun show(stage: Stage) { stage.addActor(this) }
}
