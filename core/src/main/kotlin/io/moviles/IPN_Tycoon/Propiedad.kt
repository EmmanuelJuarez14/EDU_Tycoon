package io.moviles.IPN_Tycoon

data class Propiedad(
    val id: String,
    val nombre: String,
    val precio: Long,
    val descripcion: String,
    val capacidad: Int,
    val baseAlumnos: Int,
    val mejoraMax: Int,
    var nivel: Int = 0,
    var comprada: Boolean = false
)

object PropiedadRepository {
    val propiedades = mapOf(
        "escom_hitbox" to Propiedad(
            id = "escom_hitbox",
            nombre = "ESCOM",
            precio = 1300000L,
            descripcion = "Escuela Superior de Cómputo",
            capacidad = 3200,
            baseAlumnos = 600,
            mejoraMax = 5
        ),
        "Direccion" to Propiedad(
            id = "Direccion",
            nombre = "Dirección General",
            precio = 5000000L,
            descripcion = "Oficinas administrativas del IPN",
            capacidad = 1000,
            baseAlumnos = 200,
            mejoraMax = 3
        ),
        "Mac_and_cheese" to Propiedad(
            id = "Mac_and_cheese",
            nombre = "Cafetería",
            precio = 150000L,
            descripcion = "Lugar para comer rico",
            capacidad = 500,
            baseAlumnos = 100,
            mejoraMax = 10
        ),
        "auditorio" to Propiedad(
            id = "auditorio",
            nombre = "Auditorio Alejo Peralta",
            precio = 800000L,
            descripcion = "Eventos y conferencias",
            capacidad = 2000,
            baseAlumnos = 400,
            mejoraMax = 4
        ),
        "Arquitectura" to Propiedad(
            id = "Arquitectura",
            nombre = "ESIA Arquitectura",
            precio = 1100000L,
            descripcion = "Escuela de construcción y diseño",
            capacidad = 2500,
            baseAlumnos = 500,
            mejoraMax = 6
        ),
        "Biologicas" to Propiedad(
            id = "Biologicas",
            nombre = "ENCB Biológicas",
            precio = 1250000L,
            descripcion = "Ciencias biológicas",
            capacidad = 2800,
            baseAlumnos = 550,
            mejoraMax = 7
        ),
        "Bioquimica" to Propiedad(
            id = "Bioquimica",
            nombre = "ENCB Bioquímica",
            precio = 1250000L,
            descripcion = "Ciencias bioquímicas",
            capacidad = 2800,
            baseAlumnos = 550,
            mejoraMax = 7
        ),
        "Matematicas" to Propiedad(
            id = "Matematicas",
            nombre = "ESFM Matemáticas",
            precio = 1000000L,
            descripcion = "Física y Matemáticas",
            capacidad = 1500,
            baseAlumnos = 300,
            mejoraMax = 5
        ),
        "Edificio1" to Propiedad(
            id = "Edificio1",
            nombre = "Edificio 1",
            precio = 400000L,
            descripcion = "Salones de clases generales",
            capacidad = 1200,
            baseAlumnos = 250,
            mejoraMax = 8
        ),
        "Edificio2" to Propiedad(
            id = "Edificio2",
            nombre = "Edificio 2",
            precio = 400000L,
            descripcion = "Laboratorios de computación",
            capacidad = 1200,
            baseAlumnos = 250,
            mejoraMax = 8
        ),
        "Museo" to Propiedad(
            id = "Museo",
            nombre = "Museo Tezozómoc",
            precio = 600000L,
            descripcion = "Ciencia y tecnología",
            capacidad = 800,
            baseAlumnos = 150,
            mejoraMax = 4
        ),
        "Turismo" to Propiedad(
            id = "Turismo",
            nombre = "EST Turismo",
            precio = 900000L,
            descripcion = "Escuela de Turismo",
            capacidad = 2200,
            baseAlumnos = 450,
            mejoraMax = 6
        )
    )

    fun getPropiedad(id: String): Propiedad? = propiedades[id]
}
