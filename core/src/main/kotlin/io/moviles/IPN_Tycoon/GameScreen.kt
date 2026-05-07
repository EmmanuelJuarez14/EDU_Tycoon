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
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.Scene2DSkin

class GameScreen(game: Main) : BaseScreen(game) {
    private companion object {
        const val MAX_FALLBACK_LEVEL = 2
        const val FALLBACK_TEXTURE_PREFIX = "escom"
    }

    // ── Mapa ──────────────────────────────────────────────────────────
    private val map: TiledMap? by lazy {
        try {
            TmxMapLoader().load("Mapa/Mapa_General.tmx")
        } catch (e: Exception) {
            Gdx.app.error("MAP_ERROR", "Error cargando Mapa_General.tmx: ${e.message}")
            null
        }
    }
    private val renderer by lazy { map?.let { IsometricTiledMapRenderer(it) } }

    // ── Cámara ────────────────────────────────────────────────────────
    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 480f)
        zoom = 8f
        position.set(-436f, 1360f, 0f)
    }
    private var initialZoom = 8f

    // ── Caché de texturas de edificios ────────────────────────────────
    /**
     * Clave: "${texturePrefix}lvl${nivel}"
     * Ej.: "escomlvl1", "escomlvl2"
     * El valor puede ser null si el archivo no existe todavía.
     */
    private val buildingTextureCache = mutableMapOf<String, Texture?>()

    private fun loadBuildingTexture(key: String): Texture? {
        return buildingTextureCache.getOrPut(key) {
            val path = "Mapa/Edificios/$key.png"
            val file = path.toInternalFile()
            if (file.exists()) {
                Texture(file)
            } else {
                null
            }
        }
    }

    private fun getBuildingTexture(propiedad: Propiedad): Texture? {
        if (propiedad.nivel <= 0) return null
        val level = propiedad.nivel
        propiedad.texturePrefix?.let { prefix ->
            for (candidateLevel in level downTo 1) {
                loadBuildingTexture("${prefix}lvl$candidateLevel")?.let { return it }
            }
        }
        val fallbackLevel = level.coerceIn(1, MAX_FALLBACK_LEVEL)
        val fallbackTexture = loadBuildingTexture("${FALLBACK_TEXTURE_PREFIX}lvl$fallbackLevel")
        if (fallbackTexture == null) {
            Gdx.app.error("TEXTURE", "No se encontró textura para ${propiedad.id} (nivel $level), ni fallback ${FALLBACK_TEXTURE_PREFIX}lvl$fallbackLevel")
        }
        return fallbackTexture
    }

    // ── Diálogo de intro ──────────────────────────────────────────────
    private var dialogoActor: DialogoActor? = null
    private val backgroundTexture: Texture by lazy {
        Texture("background.png".toInternalFile()).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private var backgroundImage: Image? = null
    private var nombreJugador: String  = ""
    private var nombreEscuela: String  = ""

    // ── HUD ───────────────────────────────────────────────────────────
    private var moneyLabel: Label? = null

    var modoCarga: Boolean = false

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
        stage.clearListeners()

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
            dialogoActor?.let {
                stage.addActor(it)
                it.width  = stage.width
                it.height = stage.height

                it.alTerminarNombre = { nombre ->
                    nombreJugador = nombre
                    it.variables["nombre"] = nombre
                    Gdx.app.log("GAME", "Nombre guardado: $nombreJugador")
                }

                it.alTerminarEscuela = { escuela ->
                    nombreEscuela = escuela
                    it.variables["escuela"] = escuela
                    Gdx.app.log("GAME", "Escuela guardada: $nombreEscuela")
                }

                it.mostrarConversacion(listOf(
                    Dialogo("?????",          "¡Hola! Soy el Ing. Lázaro Cárdenas.",                                          "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas",  "Bienvenido a EDU-TYCOON. Aquí podrás crear tu propia institución educativa.",    "sprite_apenado.png"),
                    Dialogo("Ing. Cárdenas",  "¿Y por qué no? Llegar a construir un ¡¡IMPERIO EDUCATIVO!!",                    "sprite_explicando.png"),
                    Dialogo("Ing. Cárdenas",  "Pero antes que nada, empecemos por lo básico....",                              "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas",  "¿Cuál es tu nombre?",                                                           "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas",  "¡Un gusto conocerte {nombre}! Prepárate para el resto.",                        "sprite_saludando.png"),
                    Dialogo("Ing. Cárdenas",  "Tener un buen nombre para tu institución lo es todo.",                          "sprite_serio.png"),
                    Dialogo("Ing. Cárdenas",  "Así que dime, ¿qué nombre llevará?",                                            "sprite_hablando.png", TipoDialogo.INPUT),
                    Dialogo("Ing. Cárdenas",  "¿{escuela}? Suena genial",                                                      "sprite_explicando.png")
                ))
            }

            stage.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    dialogoActor?.let {
                        if (it.isVisible) it.avanzar()
                        else {
                            Gdx.app.postRunnable { stage.removeListener(this) }
                            modoCarga = true
                            backgroundImage?.remove()
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

    // ── Controles del mapa + HUD ──────────────────────────────────────
    private fun configurarControlesMapa() {
        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)

        val gestureDetector = GestureDetector(object : GestureAdapter() {

            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                if (dialogoActor?.isVisible == true) return false
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
                            val propiedad = PropiedadRepository.getPropiedad(obj.name ?: "") ?: return false
                            BuildingInfoWindow(propiedad) {
                                // Callback: el edificio cambió de estado → nada más que hacer,
                                // render() ya leerá propiedad.nivel en el siguiente frame.
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

        // ── HUD de dinero ─────────────────────────────────────────────
        setupHUD()
    }

    /**
     * Crea la etiqueta de dinero en la esquina superior izquierda.
     * Se llama una sola vez al entrar al mapa.
     */
    private fun setupHUD() {
        val skin = Scene2DSkin.defaultSkin

        val labelStyle = Label.LabelStyle(skin.getFont("default-font"), Color.GOLD)
        moneyLabel = Label(formatMoney(GameState.dinero), labelStyle).apply {
            setFontScale(1.1f)
        }

        val hudTable = Table().apply {
            setFillParent(true)
            top().left()
            pad(12f)
            // Fondo semitransparente para legibilidad
            background = skin.newDrawable("white", Color(0f, 0f, 0f, 0.45f))
            add(Label("$  ", labelStyle))   // ícono moneda (texto)
            add(moneyLabel)
        }

        stage.addActor(hudTable)
    }

    // ── Render ────────────────────────────────────────────────────────
    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)

        if (modoCarga) {
            val r = renderer ?: return
            camera.update()
            r.setView(camera)
            r.render()

            r.batch.begin()
            try {
                val m = map ?: return
                m.layers["Puntos_origen"]
                    ?.objects
                    ?.filterIsInstance<PointMapObject>()
                    ?.forEach { obj ->
                        // Sólo renderizamos si el edificio fue comprado
                        val propiedad = PropiedadRepository.getPropiedad(obj.name ?: "") ?: return@forEach
                        if (!propiedad.comprada) return@forEach

                        val texture = getBuildingTexture(propiedad) ?: return@forEach

                        // Conversión de coordenadas isométricas → mundo
                        val worldX = obj.point.x + obj.point.y
                        val worldY = (obj.point.y - obj.point.x) * 0.5f

                        r.batch.draw(
                            texture,
                            worldX - (propiedad.renderW / 2f),
                            worldY,
                            propiedad.renderW,
                            propiedad.renderH
                        )
                    }
            } catch (_: Exception) {}
            r.batch.end()

            // Actualizar HUD de dinero en cada frame
            moneyLabel?.setText(formatMoney(GameState.dinero))
        }

        stage.act(delta)
        stage.draw()
    }

    // ── Resize ────────────────────────────────────────────────────────
    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        camera.viewportWidth  = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    // ── Dispose ───────────────────────────────────────────────────────
    override fun dispose() {
        super.dispose()
        backgroundTexture.dispose()
        buildingTextureCache.values.forEach { it?.dispose() }
        buildingTextureCache.clear()
        map?.dispose()
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private fun formatMoney(amount: Long): String = when {
        amount >= 1_000_000L -> "${"%.1f".format(amount / 1_000_000.0)}M"
        amount >= 1_000L     -> "${"%.0f".format(amount / 1_000.0)}K"
        else                 -> amount.toString()
    }
}
