package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {
    val recentGames: StateFlow<List<Game>> = gameRepository.getAllGames()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
