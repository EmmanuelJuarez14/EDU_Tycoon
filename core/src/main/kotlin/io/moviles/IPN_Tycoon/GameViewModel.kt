package io.moviles.IPN_Tycoon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel {
    private val _selectedBuilding = MutableStateFlow<Propiedad?>(null)
    val selectedBuilding = _selectedBuilding.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu = _showMenu.asStateFlow()

    fun onBuildingClicked(propiedad: Propiedad) {
        _selectedBuilding.value = propiedad
        _showMenu.value = true
    }

    fun closeMenu() {
        _showMenu.value = false
        _selectedBuilding.value = null
    }
}
