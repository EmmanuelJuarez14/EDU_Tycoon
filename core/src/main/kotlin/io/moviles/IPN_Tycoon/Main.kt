package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

class Main : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()
        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        addScreen(Bienvenida(this))
        addScreen(FirstScreen())
        setScreen<Bienvenida>()
    }

    override fun dispose() {
        super.dispose()
        VisUI.dispose()
    }
}

class FirstScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = ScreenViewport(camera)

    // Cargamos el mapa desde assets
    private val map: TiledMap by lazy { TmxMapLoader().load("ESCOM.tmx") }

    // Cambiado a IsometricTiledMapRenderer para soportar el formato de tu mapa
    private val renderer by lazy { IsometricTiledMapRenderer(map) }

    // Para el movimiento del mapa
    private val lastTouch = Vector3()

    override fun show() {
        // Ajustamos el zoom inicial para ver mejor el mapa isométrico
        camera.zoom = 1.5f
        // Centramos la cámara al inicio
        camera.position.set(0f, 0f, 0f)
        camera.update()
    }

    override fun render(delta: Float) {
        // Limpiamos la pantalla
        clearScreen(0f, 0f, 0f, 1f)

        // Lógica de movimiento (Drag/Arrastrar)
        handleInput()

        // Actualizamos la cámara y renderizamos el mapa
        camera.update()
        renderer.setView(camera)
        renderer.render()
    }

    private fun handleInput() {
        if (Gdx.input.isTouched) {
            val currentTouch = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)

            if (Gdx.input.justTouched()) {
                lastTouch.set(currentTouch)
            } else {
                // Calculamos cuánto se movió el dedo/mouse multiplicado por el zoom para que sea preciso
                val deltaX = (currentTouch.x - lastTouch.x) * camera.zoom
                val deltaY = (currentTouch.y - lastTouch.y) * camera.zoom

                // Movemos la cámara
                camera.translate(-deltaX, deltaY)
                lastTouch.set(currentTouch)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        // Usamos false en centercamera para que no reinicie la posición al redimensionar
        viewport.update(width, height, false)
    }

    override fun dispose() {
        map.disposeSafely()
        renderer.disposeSafely()
    }
}
