package io.moviles.IPN_Tycoon

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class GameScreen(game: Main) : BaseScreen(game) {

    // ── Mapa ──────────────────────────────────────────────────────────
    private val map: TiledMap? by lazy {
        try {
            TmxMapLoader().load("Mapa/Mapa_General.tmx")
        } catch (e: Exception) {
            Gdx.app.error("MAP_ERROR", "Error cargando mapa: ${e.message}")
            null
        }
    }
    private val renderer: IsometricTiledMapRenderer? by lazy {
        map?.let { IsometricTiledMapRenderer(it) }
    }

    /**
     * Índices de capas a renderizar — excluye "Edificios" porque
     * nosotros dibujamos los edificios dinámicamente según nivel/compra.
     */
    private val layerIndicesToRender: IntArray by lazy {
        val m = map ?: return@lazy intArrayOf()
        (0 until m.layers.count)
            .filter { m.layers[it].name != "Edificios" }
            .toIntArray()
    }

    // ── Cámara ────────────────────────────────────────────────────────
    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 480f)
        zoom = 8f
        position.set(-436f, 1360f, 0f)
    }
    private var initialZoom = 8f

    // ── Caché de texturas de edificios ────────────────────────────────
    private val buildingTextureCache = mutableMapOf<String, Texture?>()

    private fun getBuildingTexture(propiedad: Propiedad): Texture? {
        val prefix = propiedad.texturePrefix ?: return null
        val key    = "${prefix}lvl${propiedad.nivel}"
        return buildingTextureCache.getOrPut(key) {
            val file = "Mapa/Edificios/$key.png".toInternalFile()
            if (file.exists()) Texture(file)
            else { Gdx.app.error("TEXTURE", "No encontrado: $key.png"); null }
        }
    }

    // ── Diálogo ───────────────────────────────────────────────────────
    private var dialogoActor: DialogoActor? = null
    private val backgroundTexture: Texture by lazy {
        Texture("background.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private var backgroundImage: Image? = null

    // ── Icono Menú ────────────────────────────────────────────────────
    private val menuIconTexture: Texture by lazy {
        Texture("menuicon.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    // ── HUD ───────────────────────────────────────────────────────────
    private var moneyLabel: Label? = null

    // ── Estado ────────────────────────────────────────────────────────
    var modoCarga: Boolean = false

    // ── Mapa auxiliar punto → propiedad ───────────────────────────────
    private val puntosAPropiedad = mapOf(
        "escom"          to "escom_hitbox",
        "escom_hitbox"   to "escom_hitbox",
        "Direccion"      to "Direccion",
        "direccion"      to "Direccion",
        "Mac_and_cheese" to "Mac_and_cheese",
        "cafeteria"      to "cafeteria",
        "Cafeteria"      to "cafeteria",
        "auditorio"      to "auditorio",
        "Auditorio"      to "auditorio",
        "Arquitectura"   to "Arquitectura",
        "arquitectura"   to "Arquitectura",
        "Biologicas"     to "Biologicas",
        "biologicas"     to "Biologicas",
        "Bioquimica"     to "Bioquimica",
        "bioquimica"     to "Bioquimica",
        "Matematicas"    to "Matematicas",
        "matematicas"    to "Matematicas",
        "Edificio1"      to "Edificio1",
        "edificio1"      to "Edificio1",
        "Edificio2"      to "Edificio2",
        "edificio2"      to "Edificio2",
        "Museo"          to "Museo",
        "museo"          to "Museo",
        "Turismo"        to "Turismo",
        "turismo"        to "Turismo",
        "Palapas"        to "Palapas",
        "palapas"        to "Palapas"
    )

    // ─────────────────────────────────────────────────────────────────
    override fun show() {
        super.show()
        val skin   = Scene2DSkin.defaultSkin
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
                        if (!(dialogoActor?.retroceder() ?: false)) game.setScreen<SeleccionPartida>()
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
            dialogoActor?.let { actor ->
                stage.addActor(actor)
                actor.width  = stage.width
                actor.height = stage.height

                actor.alTerminarNombre = { nombre ->
                    GameState.nombreJugador = nombre
                    actor.variables["nombre"] = nombre
                }
                actor.alTerminarEscuela = { escuela ->
                    GameState.nombreEscuela = escuela
                    actor.variables["escuela"] = escuela
                }

                actor.mostrarConversacion(listOf(
                    Dialogo("?????",         "¡Hola! Soy el Ing. Lázaro Cárdenas.",                                       "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas", "Bienvenido a EDU-TYCOON. Aquí podrás crear tu propia institución educativa.", "sprite_apenado.png"),
                    Dialogo("Ing. Cárdenas", "¿Y por qué no? Llegar a construir un ¡¡IMPERIO EDUCATIVO!!",                 "sprite_explicando.png"),
                    Dialogo("Ing. Cárdenas", "Pero antes que nada, empecemos por lo básico....",                           "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas", "¿Cuál es tu nombre?",                                                        "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas", "¡Un gusto conocerte {nombre}! Prepárate para el resto.",                     "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas", "Tener un buen nombre para tu institución lo es todo.",                       "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas", "Así que dime, ¿qué nombre llevará?",                                         "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas", "¿{escuela}? Suena genial",                                                   "sprite_explicando.png")
                ))
            }

            stage.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    dialogoActor?.let { actor ->
                        if (actor.isVisible) {
                            actor.avanzar()
                        } else if (!modoCarga) {
                            modoCarga = true
                            backgroundImage?.remove()
                            stage.root.clearListeners()
                            reAgregarListenerBack()
                            configurarControlesMapa()
                        }
                    }
                }
            })
            Gdx.input.inputProcessor = stage
        } else {
            configurarControlesMapa()
        }

        Gdx.input.setCatchKey(Input.Keys.BACK, true)
    }

    private fun reAgregarListenerBack() {
        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    game.setScreen<SeleccionPartida>()
                    return true
                }
                return false
            }
        })
    }

    // ── Controles ────────────────────────────────────────────────────
    private fun configurarControlesMapa() {
        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)

        val gestureDetector = GestureDetector(object : GestureAdapter() {

            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                val m = map ?: return false
                val worldTouch = Vector3(x, y, 0f)
                camera.unproject(worldTouch)

                val tileWidth  = 64f
                val tileHeight = 32f
                val tiledX = (worldTouch.x / (tileWidth  / 2f) - worldTouch.y / (tileHeight / 2f)) / 2f * tileHeight
                val tiledY = (worldTouch.y / (tileHeight / 2f) + worldTouch.x / (tileWidth  / 2f)) / 2f * tileHeight

                try {
                    val logicaLayer = m.layers["Logica_Clics"] ?: return false
                    logicaLayer.objects.filterIsInstance<RectangleMapObject>().forEach { obj ->
                        if (obj.rectangle.contains(tiledX, tiledY)) {
                            val rawName   = obj.name ?: ""
                            val propId    = puntosAPropiedad[rawName] ?: rawName
                            val propiedad = PropiedadRepository.getPropiedad(propId) ?: return false
                            BuildingInfoWindow(propiedad) {
                                Gdx.app.log("GAME", "${propiedad.nombre} → nivel ${propiedad.nivel}")
                            }.show(stage)
                            return true
                        }
                    }
                } catch (_: Exception) {}
                return false
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom)
                return true
            }

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                val ratio = if (distance > 0) initialDistance / distance else 1f
                camera.zoom = (initialZoom * ratio).coerceIn(1f, 15f)
                return true
            }

            override fun pinchStop() { initialZoom = camera.zoom }
        })

        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer

        setupHUD()
    }

    // ── HUD ───────────────────────────────────────────────────────────
    private fun setupHUD() {
        val skin = Scene2DSkin.defaultSkin
        val labelStyle = Label.LabelStyle(skin.getFont("default-font"), Color.GOLD)

        moneyLabel = Label(formatMoney(GameState.dinero), labelStyle).apply {
            setFontScale(1.1f)
        }

        val hudTable = Table().apply {
            setFillParent(true)
            top()
            add(Table().apply {
                background = skin.newDrawable("white", Color(0f, 0f, 0f, 0.45f))
                pad(8f)
                add(Label("$  ", labelStyle))
                add(moneyLabel)
            }).left().expandX().pad(10f)

            // Icono de menú puro (solo imagen, sin fondo, 38x38)
            add(scene2d.image(menuIconTexture) {
                setScaling(Scaling.fit)
                setAlign(Align.center)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        PauseMenuWindow(game) {
                            modoCarga = false
                            game.setScreen<SeleccionPartida>()
                        }.show(stage)
                    }
                })
            }).right().pad(10f).size(80f)
        }

        stage.addActor(hudTable)
    }

    // ──Render ──────────────────────────────────────────────────────
    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)

        if (modoCarga) {
            renderer?.let { r ->
                camera.update()
                r.setView(camera)

                // Renderiza todas las capas EXCEPTO "Edificios"
                // (los edificios los dibujamos nosotros según nivel y compra)
                r.render(layerIndicesToRender)

                r.batch.begin()
                try {
                    map?.layers?.get("Puntos_origen")
                        ?.objects?.filterIsInstance<PointMapObject>()
                        ?.forEach { obj ->
                            val rawName   = obj.name ?: ""
                            val propId    = puntosAPropiedad[rawName] ?: rawName
                            val propiedad = PropiedadRepository.getPropiedad(propId) ?: return@forEach
                            if (!propiedad.comprada) return@forEach

                            val texture = getBuildingTexture(propiedad) ?: return@forEach
                            val worldX  = obj.point.x + obj.point.y
                            val worldY  = (obj.point.y - obj.point.x) * 0.5f

                            r.batch.draw(
                                texture,
                                worldX - (propiedad.renderW / 2f),
                                worldY,
                                propiedad.renderW,
                                propiedad.renderH
                            )
                        }
                } catch (e: Exception) {
                    Gdx.app.error("RENDER", "Error dibujando edificios: ${e.message}")
                }
                r.batch.end()
            }

            moneyLabel?.setText(formatMoney(GameState.dinero))
        }

        // Siempre se ejecuta
        stage.act(delta)
        stage.draw()
    }

    // ── Resize / Dispose ──────────────────────────────────────────────
    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        camera.viewportWidth  = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
        menuIconTexture.dispose()
        buildingTextureCache.values.forEach { it?.dispose() }
        buildingTextureCache.clear()
        map?.dispose()
    }

    private fun formatMoney(v: Long) = when {
        v >= 1_000_000L -> "${"%.1f".format(v / 1_000_000.0)}M"
        v >= 1_000L     -> "${"%.0f".format(v / 1_000.0)}K"
        else            -> v.toString()
    }
}
