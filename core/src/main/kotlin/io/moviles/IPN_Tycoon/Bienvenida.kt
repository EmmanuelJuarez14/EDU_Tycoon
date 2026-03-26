package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.actors.onChange
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class Bienvenida(private val game: Main) : KtxScreen {
    private val stage = Stage(ScreenViewport())
    private var pixelFont: BitmapFont? = null

    private val backgroundTexture by lazy {
        val file = "back_bienvenida.png".toInternalFile()
        if (file.exists()) {
            Texture(file).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
        } else null
    }

    // Textura refinada para simular el estilo de la imagen (Pixel Art redondeado)
    private val pixelTexture: Texture by lazy {
        val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.None

        // 1. Limpiar (Transparente)
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()

        // 2. Borde Exterior (Color Gris-Azulado de la imagen: #3e3e54)
        pixmap.setColor(Color.valueOf("3e3e54"))
        pixmap.fillRectangle(2, 0, 8, 12) // Cuerpo vertical
        pixmap.fillRectangle(0, 2, 12, 8) // Cuerpo horizontal
        pixmap.fillRectangle(1, 1, 10, 10) // Rellenar esquinas internas

        // 3. Cuerpo Verde Principal (#8cbd5c)
        pixmap.setColor(Color.valueOf("8cbd5c"))
        pixmap.fillRectangle(1, 1, 10, 10)

        // 4. Sombra Inferior Interna (#5b8c3f) - Más gruesa para dar volumen
        pixmap.setColor(Color.valueOf("5b8c3f"))
        pixmap.fillRectangle(1, 6, 10, 5)

        // 5. Brillo Superior Interno (#c8e6a1) - Solo una línea fina
        pixmap.setColor(Color.valueOf("c8e6a1"))
        pixmap.fillRectangle(2, 1, 8, 1)

        Texture(pixmap).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
    }

    private fun generatePixelFont(): BitmapFont {
        val fontFile = "font.ttf".toInternalFile()
        return if (fontFile.exists()) {
            val generator = FreeTypeFontGenerator(fontFile)
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 44
                color = Color.WHITE
                // Sombra de la fuente más gruesa para estilo retro
                shadowColor = Color.valueOf("3e3e54")
                shadowOffsetX = 3
                shadowOffsetY = 3
                minFilter = TextureFilter.Nearest
                magFilter = TextureFilter.Nearest
            }
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } else {
            Scene2DSkin.defaultSkin.getFont("default-font").apply {
                region.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)
            }
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        pixelFont = generatePixelFont()

        // Definimos el NinePatch con un margen de 5 píxeles para proteger la curvatura
        val pixelDrawable = NinePatchDrawable(NinePatch(pixelTexture, 5, 5, 5, 5))

        val pixelButtonStyle = TextButtonStyle().apply {
            font = pixelFont
            up = pixelDrawable
            over = pixelDrawable.tint(Color.valueOf("d1e8b2")) // Tinte verdoso claro al pasar el mouse
            down = pixelDrawable.tint(Color.valueOf("a0a0a0")) // Oscurece al presionar
        }

        stage.actors {
            stack {
                setFillParent(true)

                backgroundTexture?.let {
                    image(it) { setScaling(Scaling.fill) }
                }

                table {
                    setFillParent(true)
                    // DESPLAZAR HACIA ABAJO
                    bottom()

                    textButton("START") {
                        style = pixelButtonStyle
                        onChange { game.setScreen<FirstScreen>() }
                    }.cell(width = 340f, height = 120f, padBottom = 100f)
                }
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        pixelTexture.dispose()
        backgroundTexture?.dispose()
        pixelFont?.dispose()
    }
}
