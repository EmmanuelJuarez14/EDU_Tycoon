# Plan de Aceleración Económica

Para mejorar la velocidad de generación de dinero, aplicaremos cambios en dos frentes: aumentar la rentabilidad de los edificios y reducir el tiempo de espera entre cobros.

## Cambios Propuestos

### 1. Aumento de Ingresos por Alumno
En `EconomyEngine.kt`, aumentaremos el multiplicador base de dinero.
- **Actual:** $50 por alumno.
- **Nuevo:** $100 por alumno (Doble de ingresos).

### 2. Reducción de Duración del Ciclo
En `GameScreen.kt`, acortaremos el tiempo que tarda un ciclo en completarse.
- **Actual:** 60 segundos.
- **Nuevo:** 30 segundos (Cobros el doble de rápido).

> [!TIP]
> Con estos cambios combinados, la velocidad de generación de dinero será **4 veces mayor** que la actual.

## Archivos a Modificar

#### [EconomyEngine.kt](file:///C:/Users/kevin/Downloads/TycoonIPN/IPN_Tycoon/core/src/main/kotlin/io/moviles/IPN_Tycoon/engine/EconomyEngine.kt)
- Cambiar `INGRESO_POR_ALUMNO` de `50L` a `100L`.

#### [GameScreen.kt](file:///C:/Users/kevin/Downloads/TycoonIPN/IPN_Tycoon/core/src/main/kotlin/io/moviles/IPN_Tycoon/GameScreen.kt)
- Cambiar `cycleDuration` de `60f` a `30f`.
