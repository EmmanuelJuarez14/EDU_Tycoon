# Plan de Optimización para Multi-Dispositivo

Para asegurar que el juego funcione fluidamente en teléfonos de gama baja, media y alta, implementaremos estrategias que reduzcan el consumo de memoria (RAM) y la carga del procesador gráfico (GPU).

## Estrategias Principales

### 1. Gestión de Texturas y Memoria
Actualmente, cada edificio carga su propia textura. En dispositivos de gama baja, esto puede agotar la memoria de video.
- **Implementación:** Mejorar la caché de texturas y asegurar el `dispose()` correcto de recursos.
- **Textura Compartida:** Usar el `labelBgTexture` de 1x1 píxeles para el fondo de las etiquetas es una excelente práctica que ya estamos usando.

### 2. Optimización de Renderizado (Culling)
El mapa es grande (500x500 tiles). Renderizar todo el tiempo consume mucha GPU.
- **Implementación:** Implementar "Frustum Culling". Solo dibujar los edificios y etiquetas que están dentro de la vista actual de la cámara.

### 3. Reducción de Draw Calls
Cada vez que el motor cambia entre dibujar una textura de edificio y un texto, se genera una "Draw Call". Muchas draw calls ralentizan el juego.
- **Implementación:** Agrupar el dibujado de fondos de etiquetas y luego el dibujado de textos para minimizar los cambios de estado en el Batch.

### 4. Ajustes de Calidad (Opcional)
- **Implementación:** Podríamos añadir un menú de ajustes para desactivar efectos visuales costosos o reducir la resolución interna en dispositivos muy antiguos.

## Cambios Propuestos en Código

### [GameScreen.kt](file:///C:/Users/kevin/Downloads/TycoonIPN/IPN_Tycoon/core/src/main/kotlin/io/moviles/IPN_Tycoon/GameScreen.kt)

#### Optimización de `renderizarMundo`
Añadiremos una comprobación para no dibujar lo que está fuera de pantalla:

```kotlin
// Ejemplo de Culling
val viewBounds = camera.frustum
if (!viewBounds.boundsInFrustum(info.worldX, info.worldY, 0f, sizeW, sizeH)) continue
```

## Próximos Pasos Recomendados

1.  **Uso de Texture Atlas:** Agrupar todas las imágenes de edificios en un solo archivo grande (Atlas). Esto es la optimización número 1 en juegos 2D.
2.  **Optimización de Tiled:** Asegurarse de que el renderizador de mapas de LibGDX esté configurado para usar culling automático (ya lo hace por defecto, pero podemos ajustarlo).
3.  **Gestión de Ciclos:** Mover la lógica pesada a hilos secundarios si el ciclo de economía se vuelve muy complejo.

---


