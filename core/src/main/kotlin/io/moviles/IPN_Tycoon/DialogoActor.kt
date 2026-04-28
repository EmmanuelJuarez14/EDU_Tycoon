package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2DSkin

class DialogoActor(
    private val font: BitmapFont,
    private val textureManager: (String) -> TextureRegion
) : Group() {

    private val labelTexto: Label
    private val labelNombre: Label
    private val imagenPersonaje = Image()
    private val fondoBordeGuinda: Image
    private val fondoBlancoInterno: Image
    private val inputNombre: TextField

    var alTerminarNombre: ((String) -> Unit)? = null
    var alTerminarEscuela: ((String) -> Unit)? = null
    val variables = mutableMapOf<String, String>()

    private var listaDialogos = listOf<Dialogo>()
    private var indiceActual = 0
    private var contadorInputs = 0
    private var textoCompleto = ""
    private var caracteresVisibles = 0
    private var tiempoAcumulado = 0f
    private val velocidadTexto = 0.03f

    // --- TEXTURES ---
    private val whiteRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        TextureRegion(texture)
    }

    init {
        val guindaIPN = Color.valueOf("660000")
        val blancoPuro = Color.WHITE
        val negroPuro = Color.BLACK

        // --- STYLES ---
        val estiloTexto = Label.LabelStyle(font, negroPuro)
        labelTexto = Label("", estiloTexto).apply {
            wrap = true
            setAlignment(Align.topLeft)
            setFontScale(1.2f)
        }

        val estiloNombre = Label.LabelStyle(font, negroPuro)
        labelNombre = Label("", estiloNombre).apply {
            setAlignment(Align.left)
            setFontScale(1.3f)
        }

        // --- BACKGROUNDS ---
        fondoBordeGuinda = Image(TextureRegionDrawable(whiteRegion)).apply {
            color = guindaIPN
        }

        fondoBlancoInterno = Image(TextureRegionDrawable(whiteRegion)).apply {
            color = blancoPuro
        }

        // --- LAYOUT ---
        fondoBordeGuinda.setSize(760f, 160f)
        fondoBordeGuinda.setPosition(20f, 20f)

        fondoBlancoInterno.setSize(752f, 152f)
        fondoBlancoInterno.setPosition(24f, 24f)

        labelNombre.setPosition(50f, 135f)
        labelTexto.setSize(700f, 90f)
        labelTexto.setPosition(50f, 40f)

        inputNombre = TextField("", Scene2DSkin.defaultSkin).apply {
            isVisible = false
            setSize(300f, 50f)
            setPosition(50f, 50f)
            messageText = "Escribe aquí..."

            // Listener para la tecla ENTER
            addListener(object : com.badlogic.gdx.scenes.scene2d.InputListener() {
                override fun keyDown(event: com.badlogic.gdx.scenes.scene2d.InputEvent?, keycode: Int): Boolean {
                    if (keycode == com.badlogic.gdx.Input.Keys.ENTER) {
                        avanzar()
                        return true
                    }
                    return false
                }
            })
        }

        imagenPersonaje.setSize(280f, 280f)
        imagenPersonaje.setPosition(500f, 170f)

        // --- HIERARCHY ---
        addActor(imagenPersonaje)
        addActor(fondoBordeGuinda)
        addActor(fondoBlancoInterno)
        addActor(labelNombre)
        addActor(labelTexto)
        addActor(inputNombre)

        this.isVisible = false
    }

    // --- DIALOGUE LOGIC ---
    fun mostrarConversacion(dialogos: List<Dialogo>) {
        this.listaDialogos = dialogos
        this.indiceActual = 0
        this.contadorInputs = 0
        this.isVisible = true
        actualizarContenido()
    }

    private fun actualizarContenido() {
        if (indiceActual >= listaDialogos.size) return
        val actual = listaDialogos[indiceActual]

        try {
            val region = textureManager(actual.spritePath)
            imagenPersonaje.drawable = TextureRegionDrawable(region)
        } catch (e: Exception) {}

        labelNombre.setText(actual.nombre)

        // Reemplazo de variables en el texto
        var textoFinal = actual.texto
        variables.forEach { (clave, valor) ->
            textoFinal = textoFinal.replace("{$clave}", valor)
        }

        textoCompleto = textoFinal
        caracteresVisibles = 0
        tiempoAcumulado = 0f
        labelTexto.setText("")

        // Lógica para mostrar input si es tipo INPUT
        if (actual.tipo == TipoDialogo.INPUT) {
            inputNombre.isVisible = true
            inputNombre.text = ""
            stage?.keyboardFocus = inputNombre
        } else {
            inputNombre.isVisible = false
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (!isVisible) return

        if (caracteresVisibles < textoCompleto.length) {
            tiempoAcumulado += delta
            if (tiempoAcumulado >= velocidadTexto) {
                caracteresVisibles++
                labelTexto.setText(textoCompleto.substring(0, caracteresVisibles))
                tiempoAcumulado = 0f
            }
        }
    }

    fun avanzar() {
        if (caracteresVisibles < textoCompleto.length) {
            caracteresVisibles = textoCompleto.length
            labelTexto.setText(textoCompleto)
        } else {
            val actual = listaDialogos.getOrNull(indiceActual)
            if (actual?.tipo == TipoDialogo.INPUT) {
                if (inputNombre.text.isNotBlank()) {
                    if (contadorInputs == 0) {
                        alTerminarNombre?.invoke(inputNombre.text)
                    } else {
                        alTerminarEscuela?.invoke(inputNombre.text)
                    }
                    contadorInputs++

                    inputNombre.isVisible = false
                    stage?.keyboardFocus = null
                } else {
                    return // No avanzar si no hay texto
                }
            }

            indiceActual++
            if (indiceActual < listaDialogos.size) {
                actualizarContenido()
            } else {
                this.isVisible = false
            }
        }
    }

    /**
     * Retrocede al diálogo anterior.
     * Retorna true si retrocedió, false si ya estaba en el primer diálogo.
     */
    fun retroceder(): Boolean {
        if (indiceActual > 0) {
            indiceActual--
            actualizarContenido()
            // Al retroceder, mostramos el texto completo de una vez
            caracteresVisibles = textoCompleto.length
            labelTexto.setText(textoCompleto)
            return true
        }
        return false
    }
}
