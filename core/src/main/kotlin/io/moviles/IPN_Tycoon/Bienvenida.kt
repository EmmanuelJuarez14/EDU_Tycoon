package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Scaling
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class Bienvenida(game: Main) : BaseScreen(game) {
    private var pixelFont: BitmapFont? = null

    // --- ASSETS ---
    private val backgroundTexture by lazy {
        val file = "back_main.png".toInternalFile()
        if (file.exists()) {
            Texture(file).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
        } else null
    }

    private val pixelTexture: Texture by lazy {
        val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pixmap.blending = Pixmap.Blending.None

        pixmap.setColor(Color.valueOf("3e3e54"))
        pixmap.fillRectangle(2, 0, 8, 12)
        pixmap.fillRectangle(0, 2, 12, 8)
        pixmap.fillRectangle(1, 1, 10, 10)
        pixmap.setColor(Color.valueOf("8cbd5c"))
        pixmap.fillRectangle(1, 1, 10, 10)
        pixmap.setColor(Color.valueOf("5b8c3f"))
        pixmap.fillRectangle(1, 6, 10, 5)
        pixmap.setColor(Color.valueOf("c8e6a1"))
        pixmap.fillRectangle(2, 1, 8, 1)

        Texture(pixmap).apply { setFilter(TextureFilter.Nearest, TextureFilter.Nearest) }
    }

    // --- FONT GENERATION ---
    private fun generatePixelFont(): BitmapFont {
        val fontFile = "font.ttf".toInternalFile()
        return if (fontFile.exists()) {
            val generator = FreeTypeFontGenerator(fontFile)
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = 40
                color = Color.WHITE
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
            BitmapFont().apply { data.setScale(2f) }
        }
    }

    override fun show() {
        super.show()
        pixelFont = generatePixelFont()

        // --- STYLES ---
        val pixelDrawable = NinePatchDrawable(NinePatch(pixelTexture, 5, 5, 5, 5))
        val pixelButtonStyle = TextButtonStyle().apply {
            font = pixelFont
            up = pixelDrawable
            over = pixelDrawable.tint(Color.valueOf("d1e8b2"))
            down = pixelDrawable.tint(Color.valueOf("a0a0a0"))
        }

        // --- UI LAYOUT ---
        stage.actors {
            stack {
                setFillParent(true)
                backgroundTexture?.let {
                    image(it) { setScaling(Scaling.fill) }
                }
                table {
                    setFillParent(true)
                    bottom()
                    textButton("START") {
                        style = pixelButtonStyle
                        onChange {
                            game.setScreen<SeleccionPartida>()
                        }
                    }.cell(width = 300f, height = 100f, padBottom = 80f)
                }
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        pixelTexture.dispose()
        backgroundTexture?.dispose()
        pixelFont?.dispose()
    }
}
