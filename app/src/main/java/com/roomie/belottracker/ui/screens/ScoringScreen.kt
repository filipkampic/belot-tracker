package com.roomie.belottracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.ui.viewmodel.ScoringViewModel

@Composable
fun ScoringScreen(
    gameId: Long,
    onBack: () -> Unit,
    onWinner: (Long, String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    
}
