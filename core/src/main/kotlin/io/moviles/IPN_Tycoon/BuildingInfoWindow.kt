package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*

class BuildingInfoWindow(
    val data: BuildingInfo,
    val onUpgrade: () -> Unit
) : VisWindow("Gestión del Plantel") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = false

        add(scene2d.table {
            label("[GOLD]${data.name}[]").cell(padBottom = 10f)
            row()
            label(data.description).apply { setWrap(true) }.cell(width = 280f, padBottom = 10f)
            row()
            label("Capacidad: ${data.capacity} alumnos")
            row()
            label("Costo Mejora: $${data.upgradePrice}")
            row()
            textButton("MEJORAR") {
                onChange {
                    onUpgrade()
                    this@BuildingInfoWindow.remove()
                }
            }.cell(padTop = 15f, expandX = true, fillX = true)
        }).pad(15f)

        pack()
        centerWindow()
    }

    fun show(stage: Stage) {
        stage.addActor(this)
    }
}
