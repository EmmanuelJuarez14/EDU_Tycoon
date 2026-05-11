package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*
import java.text.SimpleDateFormat
import java.util.*

class PartidasGuardadas(game: Main) : BaseScreen(game) {

    private var pixelFont: BitmapFont? = null
    private var smallFont: BitmapFont? = null

    private val backgroundTexture: Texture by lazy {
        Texture("partidasguardadas.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    private val buttonTexture: Texture by lazy {
        val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.valueOf("3e3e54")); pixmap.fillRectangle(0, 0, 12, 12)
        pixmap.setColor(Color.valueOf("8cbd5c")); pixmap.fillRectangle(1, 1, 10, 10)
        pixmap.setColor(Color.valueOf("c8e6a1")); pixmap.fillRectangle(1, 1, 10, 2)
        pixmap.setColor(Color.valueOf("5b8c3f")); pixmap.fillRectangle(1, 9, 10, 2)
        Texture(pixmap).apply { setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest) }
    }

    // Datos de los slots — null = vacío
    private var saveSlot1: GameSaveData? = null
    private var saveSlot2: GameSaveData? = null

    // ── Fonts ─────────────────────────────────────────────────────────
    private fun generateFont(size: Int): BitmapFont {
        val fontFile = "font.ttf".toInternalFile()
        return if (fontFile.exists()) {
            val generator = FreeTypeFontGenerator(fontFile)
            val param = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size   = size
                color       = Color.WHITE
                borderWidth = 2f
                borderColor = Color.valueOf("3e3e54")
                minFilter   = Texture.TextureFilter.Nearest
                magFilter   = Texture.TextureFilter.Nearest
            }
            val font = generator.generateFont(param)
            generator.dispose()
            font
        } else BitmapFont().apply { data.setScale(size / 15f) }
    }

    // ── Show ──────────────────────────────────────────────────────────
    override fun show() {
        super.show()
        pixelFont = generateFont(22)
        smallFont = generateFont(14)
        buildUI()     // primera pasada: slots vacíos
        loadSlots()   // consulta Room → llama buildUI() de nuevo con datos reales
    }

    // ── Carga slots de Room ───────────────────────────────────────────
    private fun loadSlots() {
        game.saveManager.cargarSlots { s1, s2 ->
            saveSlot1 = s1
            saveSlot2 = s2
            buildUI()  // reconstruye con datos reales
        }
    }

    // ── Construye la UI completa (usa saveSlot1/2 directamente) ───────
    private fun buildUI() {
        stage.clear()

        val drawable = NinePatchDrawable(NinePatch(buttonTexture, 4, 4, 4, 4))
        val btnStyle = TextButtonStyle().apply {
            font = pixelFont
            up   = drawable
            over = drawable.tint(Color.valueOf("d1e8b2"))
            down = drawable.tint(Color.valueOf("a0a0a0"))
        }
        val smallStyle = Label.LabelStyle(smallFont, Color.WHITE)

        stage.actors {
            stack {
                setFillParent(true)

                image(backgroundTexture) {
                    setScaling(Scaling.fill)
                    setAlign(Align.center)
                }

                table {
                    setFillParent(true)
                    center()
                    pad(20f)

                    label("PARTIDAS GUARDADAS") {
                        setFontScale(1.1f)
                        setAlignment(Align.center)
                    }.cell(colspan = 5, padBottom = 30f)

                    row()

                    // ── Slot 1 ────────────────────────────────────────
                    addSlot(this, 1, saveSlot1, btnStyle, smallStyle)
                    label("").cell(width = 20f)

                    // ── Slot 2 ────────────────────────────────────────
                    addSlot(this, 2, saveSlot2, btnStyle, smallStyle)
                    label("").cell(width = 20f)

                    // ── Slot 3 TESTING ────────────────────────────────
                    addSlot(this, 3, null, btnStyle, smallStyle)

                    row()

                    textButton("← VOLVER") {
                        style = btnStyle
                        onChange { game.setScreen<SeleccionPartida>() }
                    }.cell(colspan = 5, padTop = 25f, width = 220f, height = 60f)
                }
            }
        }
    }

    /**
     * Añade un slot de partida al table.
     * [slotNum] 1, 2 = real  |  3 = testing
     */
    private fun addSlot(
        table: KTableWidget,
        slotNum: Int,
        data: GameSaveData?,
        btnStyle: TextButtonStyle,
        smallStyle: Label.LabelStyle
    ) {
        table.table {
            // Capturamos el slot en una val local para el lambda
            val slot = slotNum
            val saveData = data

            textButton(slotTitle(slot, saveData)) {
                style = btnStyle
                onChange { onSlotClicked(slot) }
            }.cell(width = 200f, height = 130f)

            row()

            label(slotSubtitle(slot, saveData)) {
                style = smallStyle
                setAlignment(Align.center)
                wrap = true
            }.cell(width = 200f, padTop = 6f)

        }
    }

    // ── Lógica de clic ────────────────────────────────────────────────
    private fun onSlotClicked(slotNum: Int) {
        val gameScreen = game.getScreen<GameScreen>()

        when (slotNum) {
            1 -> if (saveSlot1 != null) {
                game.saveManager.cargarPartida(1) { ok ->
                    if (ok) { gameScreen.modoCarga = true; game.setScreen<GameScreen>() }
                }
            } else {
                GameState.reset()
                GameState.slotActual = 1
                PropiedadRepository.resetProgress()
                gameScreen.modoCarga = false
                game.setScreen<GameScreen>()
            }

            2 -> if (saveSlot2 != null) {
                game.saveManager.cargarPartida(2) { ok ->
                    if (ok) { gameScreen.modoCarga = true; game.setScreen<GameScreen>() }
                }
            } else {
                GameState.reset()
                GameState.slotActual = 2
                PropiedadRepository.resetProgress()
                gameScreen.modoCarga = false
                game.setScreen<GameScreen>()
            }

            // Slot 3: testing — carga mapa directo sin Room ni diálogos
            3 -> {
                GameState.slotActual = 3
                gameScreen.modoCarga = true
                game.setScreen<GameScreen>()
            }
        }
    }

    // ── Helpers de texto ─────────────────────────────────────────────
    private fun slotTitle(slotNum: Int, data: GameSaveData?): String = when {
        slotNum == 3 -> "TEST\nMapa directo"
        data != null -> data.nombreEscuela.ifBlank { "Partida $slotNum" }
        else         -> "+\nNUEVA PARTIDA"
    }

    private fun slotSubtitle(slotNum: Int, data: GameSaveData?): String = when {
        slotNum == 3 -> "Carga el mapa\nsin diálogos"
        data == null -> "Slot $slotNum vacío"
        else -> {
            val fecha = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                .format(Date(data.fechaGuardado))
            "${data.nombreJugador}\n\$${fmt(data.dinero)}\nCiclos: ${data.ciclosJugados}\n$fecha"
        }
    }

    private fun fmt(v: Long) = when {
        v >= 1_000_000L -> "${"%.1f".format(v / 1_000_000.0)}M"
        v >= 1_000L     -> "${"%.0f".format(v / 1_000.0)}K"
        else            -> v.toString()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────
    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
        buttonTexture.dispose()
        pixelFont?.dispose()
        smallFont?.dispose()
    }
}
