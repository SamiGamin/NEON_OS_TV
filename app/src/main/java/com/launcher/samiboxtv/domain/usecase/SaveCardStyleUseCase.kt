package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.repository.PreferencesRepository

class SaveCardStyleUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(style: AppCardStyle) {
        preferencesRepository.setCardStyle(style)
    }
}
