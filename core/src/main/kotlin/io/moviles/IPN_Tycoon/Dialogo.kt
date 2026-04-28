package io.moviles.IPN_Tycoon

enum class TipoDialogo {
    NORMAL,
    INPUT
}

data class Dialogo(
    val nombre: String,
    val texto: String,
    val spritePath: String,
    val tipo: TipoDialogo = TipoDialogo.NORMAL
)
