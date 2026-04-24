package io.moviles.IPN_Tycoon

import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onChange
import ktx.scene2d.*

class BuildingInfoWindow(
    val data: Propiedad,
    val onUpgrade: () -> Unit
) : VisWindow("Gestión del Plantel") {

    init {
        addCloseButton()
        closeOnEscape()
        isModal = false

        add(scene2d.table {
            label("[GOLD]${data.nombre}[]").cell(padBottom = 10f)
            row()
            label(data.descripcion).apply { setWrap(true) }.cell(width = 280f, padBottom = 10f)
            row()
            label("Capacidad: ${data.capacidad} alumnos")
            row()
            label("Costo Mejora: $${data.precio}")
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
