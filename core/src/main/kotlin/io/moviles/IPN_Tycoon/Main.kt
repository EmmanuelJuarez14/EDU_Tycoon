package io.moviles.IPN_Tycoon

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kotcrab.vis.ui.VisUI
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use
import ktx.scene2d.Scene2DSkin

class Main : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()
        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        // --- SCREEN REGISTRATION ---
        addScreen(Bienvenida(this))
        addScreen(SeleccionPartida(this))
        addScreen(GameScreen(this))
        addScreen(FirstScreen())

        setScreen<Bienvenida>()
    }

    override fun dispose() {
        super.dispose()
        VisUI.dispose()
    }
}

class FirstScreen : KtxScreen {
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(Linear, Linear) }
    private val batch = SpriteBatch()

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        batch.use {
            it.draw(image, 100f, 160f)
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
    }
}
