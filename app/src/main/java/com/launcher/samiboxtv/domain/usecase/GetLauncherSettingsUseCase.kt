package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.repository.PreferencesRepository

data class LauncherSettingsData(
    val cardStyle: AppCardStyle,
    val categories: List<String>,
    val appCategoryMap: Map<String, String>,
    val showAppNames: Boolean = true
)

class GetLauncherSettingsUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): LauncherSettingsData {
        return LauncherSettingsData(
            cardStyle = preferencesRepository.getCardStyle(),
            categories = preferencesRepository.getCustomCategories(),
            appCategoryMap = preferencesRepository.getAppCategoryMap(),
            showAppNames = preferencesRepository.getShowAppNames()
        )
    }
}
