package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode

class SaveAppLayoutModeUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(mode: AppLayoutMode) {
        preferencesRepository.setAppLayoutMode(mode)
    }
}
