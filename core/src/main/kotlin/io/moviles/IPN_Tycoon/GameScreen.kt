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
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import io.moviles.IPN_Tycoon.engine.EconomyEngine
import io.moviles.IPN_Tycoon.engine.EstudiantesEngine
import io.moviles.IPN_Tycoon.engine.EventEngine
import io.moviles.IPN_Tycoon.engine.EventoEfecto
import io.moviles.IPN_Tycoon.engine.GameCycleEngine
import io.moviles.IPN_Tycoon.engine.GameEvent
import ktx.actors.onChange
import ktx.app.clearScreen
import ktx.assets.toInternalFile
import ktx.scene2d.*

class GameScreen(game: Main) : BaseScreen(game) {

    // ── Motor de ciclos ───────────────────────────────────────────────
    private val cycleEngine       = GameCycleEngine()
    private val economyEngine     = EconomyEngine()
    private val estudiantesEngine = EstudiantesEngine()
    private val eventEngine       = EventEngine { evento -> showEventToast(evento) }
    private var cycleTimer        = 0f
    private val cycleDuration     = 60f

    init {
        cycleEngine.addListener(economyEngine)
        cycleEngine.addListener(estudiantesEngine)
        cycleEngine.addListener(eventEngine)
    }

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
    private var moneyLabel:      Label? = null
    private var alumnosLabel:    Label? = null
    private var toastLine1:      Label? = null   // ingresos del ciclo
    private var toastLine2:      Label? = null   // total alumnos
    private var toastTable:      Table? = null
    private var eventTituloLabel: Label? = null
    private var eventEfectoLabel: Label? = null
    private var eventTable:       Table? = null

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
        val skin          = Scene2DSkin.defaultSkin
        val font          = skin.getFont("default-font")
        val goldStyle     = Label.LabelStyle(font, Color.GOLD)
        val cyanStyle     = Label.LabelStyle(font, Color.CYAN)
        val whiteDrawable = skin.newDrawable("white", Color(0f, 0f, 0f, 0.45f))
        val toastBg       = skin.newDrawable("white", Color(0f, 0f, 0f, 0.72f))

        moneyLabel   = Label(formatMoney(GameState.dinero), goldStyle).apply { setFontScale(1.1f) }
        alumnosLabel = Label(formatAlumnos(GameState.alumnosTotales), cyanStyle)

        // ── Toast de ciclo (actor permanente, empieza invisible) ──────
        toastLine1 = Label("", Label.LabelStyle(font, Color.GREEN))
        toastLine2 = Label("", Label.LabelStyle(font, Color.CYAN))

        val sw = stage.width
        val sh = stage.height

        // ── Toast de ciclo ────────────────────────────────────────────
        toastTable = Table().apply {
            background = toastBg
            pad(8f, 16f, 8f, 16f)
            defaults().left().padBottom(2f)
            add(toastLine1).row()
            add(toastLine2)
            pack()
            color.a = 0f
            setPosition((sw - width) / 2f, sh * 0.08f)
        }
        stage.addActor(toastTable)

        // ── Toast de eventos (más arriba, fondo más oscuro) ───────────
        eventTituloLabel = Label("", Label.LabelStyle(font, Color.WHITE))
        eventEfectoLabel = Label("", Label.LabelStyle(font, Color.WHITE))
        val eventBg = skin.newDrawable("white", Color(0.1f, 0.05f, 0.2f, 0.85f))
        eventTable = Table().apply {
            background = eventBg
            pad(10f, 18f, 10f, 18f)
            defaults().left().padBottom(3f)
            add(eventTituloLabel).row()
            add(eventEfectoLabel)
            pack()
            color.a = 0f
            setPosition((sw - width) / 2f, sh * 0.22f)
        }
        stage.addActor(eventTable)

        val hudTable = Table().apply {
            setFillParent(true)
            top()
            add(Table().apply {
                background = whiteDrawable
                pad(8f)
                add(Label("$  ", goldStyle))
                add(moneyLabel)
                add(Label("   Alumnos: ", cyanStyle)).padLeft(12f)
                add(alumnosLabel)
            }).left().expandX().pad(10f)

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

            cycleTimer += delta
            while (cycleTimer >= cycleDuration) {
                cycleTimer -= cycleDuration
                cycleEngine.advanceCycle()
                economyEngine.lastResult?.let { showCycleToast(it) }
            }

            moneyLabel?.setText(formatMoney(GameState.dinero))
            alumnosLabel?.setText(formatAlumnos(GameState.alumnosTotales))
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

    private fun showCycleToast(result: EconomyEngine.CycleResult) {
        val toast = toastTable ?: return

        toastLine1?.setText("Ingresos:  +${formatMoney(result.ingresos)}")
        toastLine2?.setText("Alumnos:    ${formatAlumnos(GameState.alumnosTotales)}")

        toast.clearActions()
        toast.color.a = 0f
        toast.invalidateHierarchy()
        toast.pack()
        toast.setPosition((stage.width - toast.width) / 2f, stage.height * 0.08f)
        toast.addAction(Actions.sequence(
            Actions.fadeIn(0.25f),
            Actions.delay(3.5f),
            Actions.fadeOut(0.4f)
        ))
    }

    private fun showEventToast(evento: GameEvent) {
        val toast = eventTable ?: return
        val esGasto = evento.efecto is EventoEfecto.Gasto

        val cantidad = when (val e = evento.efecto) {
            is EventoEfecto.Gasto   -> e.cantidad
            is EventoEfecto.Ingreso -> e.cantidad
        }
        val signo = if (esGasto) "-" else "+"

        eventTituloLabel?.setText(evento.titulo)
        eventTituloLabel?.setColor(if (esGasto) Color.RED else Color.GREEN)
        eventEfectoLabel?.setText("${signo}\$${formatMoney(cantidad)}  —  ${evento.descripcion}")
        eventEfectoLabel?.setColor(if (esGasto) Color.RED else Color.GREEN)

        toast.clearActions()
        toast.color.a = 0f
        toast.invalidateHierarchy()
        toast.pack()
        toast.setPosition((stage.width - toast.width) / 2f, stage.height * 0.22f)
        toast.addAction(Actions.sequence(
            Actions.fadeIn(0.25f),
            Actions.delay(5f),
            Actions.fadeOut(0.4f)
        ))
    }

    private fun formatMoney(v: Long) = when {
        v >= 1_000_000L -> "${"%.2f".format(v / 1_000_000.0)}M"
        v >= 1_000L     -> "${"%.1f".format(v / 1_000.0)}K"
        else            -> v.toString()
    }

    private fun formatAlumnos(v: Int) = when {
        v >= 1_000 -> "${"%.1f".format(v / 1_000.0)}K"
        else       -> v.toString()
    }
}
