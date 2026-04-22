package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.Scene2DSkin
import ktx.tiled.layer

class GameScreen(game: Main) : BaseScreen(game) {
    private val map by lazy { TmxMapLoader().load("Mapa/Mapa_General.tmx") }
    private val renderer by lazy { IsometricTiledMapRenderer(map) }
    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 480f)
        zoom = 4f
        position.set(2200f, 3000f, 0f)
    }
    private var initialZoom = 4f

    private var dialogoActor: DialogoActor? = null
    private val backgroundTexture: Texture by lazy {
        Texture("background.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private var backgroundImage: Image? = null

    var modoCarga: Boolean = false

    override fun show() {
        super.show()

        val skin = Scene2DSkin.defaultSkin
        val fuente = skin.getFont("default-font")

        if (dialogoActor == null) {
            dialogoActor = DialogoActor(fuente) { path ->
                TextureRegion(Texture(path.toInternalFile()))
            }
        }

        if (backgroundImage == null) {
            backgroundImage = Image(backgroundTexture).apply {
                setScaling(Scaling.fit)
                setAlign(Align.center)
            }
            backgroundImage?.setFillParent(true)
        }

        stage.clear()

        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    if (!modoCarga && dialogoActor?.isVisible == true) {
                        val pudoRetroceder = dialogoActor?.retroceder() ?: false
                        if (!pudoRetroceder) {
                            game.setScreen<SeleccionPartida>()
                        }
                        return true
                    } else if (modoCarga) {
                        game.setScreen<SeleccionPartida>()
                        return true
                    }
                }
                return false
            }
        })

        if (!modoCarga) {
            backgroundImage?.let { stage.addActor(it) }
            dialogoActor?.let {
                stage.addActor(it)
                it.width = stage.width
                it.height = stage.height

                val charla = listOf(
                    Dialogo("?????", "¡Hola, qué tal! Soy el Ing. Lázaro Cárdenas, gracias por jugar Edu-Tycoon.", "sprite_saludando.png"),
                    Dialogo("Ing. Lázaro", "Aquí podrás construir tu propio instituto educativo, tal como yo lo hice.", "sprite_apenado.png"),
                    Dialogo("Ing. Lázaro", "Y por qué no, convertirlo en un ¡ IMPERIO EDUCATIVO !", "sprite_explicando.png"),
                    Dialogo("Ing. Lázaro", "Pero no nos adelantemos. Para comenzar ¿cuál es tu nombre?", "sprite_hablando.png")
                )
                it.mostrarConversacion(charla)
            }

            stage.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    dialogoActor?.let { actor ->
                        if (actor.isVisible) {
                            actor.avanzar()
                        } else {
                            modoCarga = true
                            configurarControlesMapa()
                        }
                    }
                }
            })
            Gdx.input.inputProcessor = stage
            Gdx.input.setCatchKey(Input.Keys.BACK, true)
        } else {
            configurarControlesMapa()
            Gdx.input.setCatchKey(Input.Keys.BACK, true)
        }
    }

    private fun configurarControlesMapa() {
        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)

        val gestureDetector = GestureDetector(object : GestureAdapter() {
            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                if (dialogoActor?.isVisible == true) return false

                val worldCoords = camera.unproject(Vector3(x, y, 0f))

                // --- FÓRMULA DE CONVERSIÓN ISOMÉTRICA EXACTA PARA TILED (64x32) ---
                // mapX = (worldX / 32 + worldY / 16) / 2
                // mapY = (worldY / 16 - worldX / 32) / 2
                // Multiplicado por 64/32 para obtener píxeles de mapa Tiled
                val mapPixelX = (worldCoords.x / 32f + worldCoords.y / 16f) * 32f
                val mapPixelY = (worldCoords.y / 16f - worldCoords.x / 32f) * 16f

                val logicaLayer = map.layer("Logica_Clics")
                logicaLayer.objects.filterIsInstance<RectangleMapObject>().forEach { obj ->
                    // Verificamos si las coordenadas transformadas caen dentro del rectángulo
                    if (obj.rectangle.contains(mapPixelX, mapPixelY)) {
                        val propiedad = PropiedadRepository.getPropiedad(obj.name ?: "")
                        if (propiedad != null) {
                            PropertyWindow(propiedad, Scene2DSkin.defaultSkin) { p ->
                                if (!p.comprada) {
                                    p.comprada = true
                                    println("Compraste ${p.nombre}")
                                } else {
                                    if (p.nivel < p.mejoraMax) {
                                        p.nivel++
                                        println("Mejoraste ${p.nombre} al nivel ${p.nivel}")
                                    }
                                }
                            }.show(stage)
                            return true
                        }
                    }
                }
                return false
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                if (modoCarga) {
                    camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom)
                    return true
                }
                return false
            }

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                if (modoCarga) {
                    val ratio = if (distance > 0) initialDistance / distance else 1f
                    camera.zoom = initialZoom * ratio
                    if (camera.zoom < 1f) camera.zoom = 1f
                    if (camera.zoom > 10f) camera.zoom = 10f
                    return true
                }
                return false
            }

            override fun pinchStop() {
                initialZoom = camera.zoom
            }
        })

        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        if (modoCarga) {
            camera.update()
            renderer.setView(camera)
            renderer.render()
        }
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        camera.viewportWidth = viewport.worldWidth
        camera.viewportHeight = viewport.worldHeight
        camera.update()
    }

    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
    }
}
