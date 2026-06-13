# Nombres flotantes para edificios no comprados

Implementar etiquetas de texto que floten sobre las ubicaciones de los edificios en el mapa cuando aún no han sido comprados. Esto permitirá al jugador identificar visualmente qué edificio puede construir en cada zona. Una vez comprada la propiedad, la etiqueta desaparecerá para dar lugar al renderizado del edificio.

## User Review Required

> [!IMPORTANT]
> Se utilizará la fuente por defecto del skin para el renderizado. El texto estará centrado horizontalmente respecto a la posición del edificio.

## Proposed Changes

### Componente de Pantalla

#### [GameScreen.kt](file:///C:/Users/kevin/Downloads/TycoonIPN/IPN_Tycoon/core/src/main/kotlin/io/moviles/IPN_Tycoon/GameScreen.kt)

- Cargar una fuente para el renderizado en el mundo.
- Modificar `renderizarMundo` para dibujar el nombre del edificio si `!info.propiedad.comprada`.

```kotlin
// En la clase GameScreen:
private val worldFont: BitmapFont by lazy {
    Scene2DSkin.defaultSkin.getFont("default-font").apply {
        // Opcional: ajustar escala si es muy pequeña en el mundo
        data.setScale(3.5f) 
    }
}

// En renderizarMundo, dentro del batch.begin():
for (info in buildingsToRender) {
    if (info.propiedad.comprada) {
        val texture = getBuildingTexture(info.propiedad) ?: continue
        r.batch.draw(
            texture,
            info.worldX - (info.propiedad.renderW / 2f),
            info.worldY,
            info.propiedad.renderW,
            info.propiedad.renderH
        )
    } else {
        // Dibujar nombre si no está comprado
        val texto = "Edificio de ${info.propiedad.nombre}"
        val layout = GlyphLayout(worldFont, texto)
        worldFont.draw(
            r.batch, 
            texto, 
            info.worldX - (layout.width / 2f), 
            info.worldY + 100f // Elevado un poco del suelo
        )
    }
}
```

## Verification Plan

### Manual Verification
- Iniciar una nueva partida.
- Navegar por el mapa y verificar que sobre las zonas vacías aparezca el texto "Edificio de [Nombre]".
- Comprar un edificio (ej. la ESCOM).
- Verificar que el texto desaparezca y aparezca el sprite del edificio en su lugar.
