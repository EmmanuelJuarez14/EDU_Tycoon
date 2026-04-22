package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align

class PropertyWindow(
    val propiedad: Propiedad,
    skin: Skin,
    private val onAction: (Propiedad) -> Unit
) : Dialog("", skin) {

    init {
        val guindaIPN = Color.valueOf("660000") // Color Guinda institucional
        val negroPuro = Color.BLACK
        val blancoPuro = Color.WHITE

        // --- DISEÑO DE LA VENTANA (GUINDA CON NEGRO) ---
        // Aumentamos el tamaño para que quepa toda la info
        val pixmap = Pixmap(450, 400, Pixmap.Format.RGBA8888)

        // Fondo y Borde Negro
        pixmap.setColor(negroPuro)
        pixmap.fill()

        // Centro Guinda
        pixmap.setColor(guindaIPN)
        pixmap.fillRectangle(6, 6, 438, 388)

        // Franja negra superior para el Título
        pixmap.setColor(negroPuro)
        pixmap.fillRectangle(6, 330, 438, 64)

        val bgTexture = Texture(pixmap)
        pixmap.dispose()

        val style = WindowStyle(skin.get(WindowStyle::class.java))
        style.background = TextureRegionDrawable(TextureRegion(bgTexture))
        this.style = style

        // --- CONTENIDO DE LA PESTAÑA ---

        // Nombre del Edificio (en la franja negra)
        val titleLabel = Label(propiedad.nombre.uppercase(), skin).apply {
            color = blancoPuro
            setFontScale(1.6f)
            setAlignment(Align.center)
        }
        contentTable.add(titleLabel).expandX().fillX().padTop(10f).padBottom(20f).row()

        val detailsTable = Table(skin)
        detailsTable.defaults().left().pad(5f)

        // Capacidad
        detailsTable.add(Label("CAPACIDAD:", skin).apply { color = Color.LIGHT_GRAY })
        detailsTable.add(Label("${propiedad.capacidad} ALUMNOS", skin).apply { color = blancoPuro }).row()

        // Base Alumnos
        detailsTable.add(Label("BASE:", skin).apply { color = Color.LIGHT_GRAY })
        detailsTable.add(Label("${propiedad.baseAlumnos} ALUMNOS", skin).apply { color = blancoPuro }).row()

        // Mejora Max / Nivel
        detailsTable.add(Label("MEJORA:", skin).apply { color = Color.LIGHT_GRAY })
        detailsTable.add(Label("NIVEL ${propiedad.nivel} / ${propiedad.mejoraMax}", skin).apply { color = blancoPuro }).row()

        // Precio en Burrodolares
        val precioLabel = Label("PRECIO:", skin).apply { color = Color.YELLOW }
        val montoLabel = Label("${propiedad.precio} BURRODOLARES", skin).apply {
            color = Color.YELLOW
            setFontScale(1.1f)
        }
        detailsTable.add(precioLabel)
        detailsTable.add(montoLabel).row()

        contentTable.add(detailsTable).expand().fill().pad(15f).row()

        // Descripción (opcional, en la parte inferior)
        val descLabel = Label(propiedad.descripcion, skin).apply {
            wrap = true
            setAlignment(Align.center)
            color = Color.LIGHT_GRAY
            setFontScale(0.9f)
        }
        contentTable.add(descLabel).width(400f).padBottom(15f).row()

        // --- BOTONES ---
        val btnLabel = if (propiedad.comprada) "MEJORAR" else "COMPRAR"

        val actionBtn = TextButton(btnLabel, skin).apply {
            color = Color.BLACK
        }

        val closeBtn = TextButton("CERRAR", skin).apply {
            color = Color.valueOf("444444")
        }

        button(actionBtn, true)
        button(closeBtn, false)

        padBottom(20f)
    }

    override fun result(obj: Any?) {
        if (obj == true) {
            onAction(propiedad)
        }
        hide()
    }
}
