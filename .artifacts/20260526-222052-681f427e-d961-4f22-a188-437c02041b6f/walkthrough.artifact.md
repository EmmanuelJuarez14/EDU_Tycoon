# Implementación de Nombres Flotantes

Se han añadido etiquetas de texto dinámicas sobre los terrenos de edificios no comprados para mejorar la orientación del jugador en el mapa.

## Cambios Realizados

### GameScreen.kt

- Se agregó una propiedad `worldFont` que utiliza la fuente por defecto del Skin pero escalada para el mundo (escala 3.5f).
- En el método `renderizarMundo`, se implementó una condición:
    - **Si está comprado**: Se dibuja el sprite del edificio.
    - **Si NO está comprado**: Se calcula el ancho del texto usando `GlyphLayout` y se dibuja centrado sobre la posición del objeto en el mapa, elevado 150 unidades para simular efecto de flotación.

```kotlin
// Fragmento de la lógica de renderizado:
} else {
    val texto = "Edificio de ${info.propiedad.nombre}"
    val layout = GlyphLayout(worldFont, texto)
    worldFont.draw(
        r.batch,
        texto,
        info.worldX - (layout.width / 2f),
        info.worldY + 150f
    )
}
```

## Verificación
- Se realizó un análisis estático del código para asegurar que no hay errores de sintaxis o referencias nulas.
- La lógica asegura la desaparición inmediata del texto al cambiar el estado `comprada` de la propiedad a `true`.
