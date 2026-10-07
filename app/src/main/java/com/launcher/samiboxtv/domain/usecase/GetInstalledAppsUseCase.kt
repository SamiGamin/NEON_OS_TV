package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.repository.AppRepository
import com.launcher.samiboxtv.domain.repository.PreferencesRepository

data class AppsGroup(
    val visibleApps: List<AppItem>,
    val hiddenApps: List<AppItem>,
    val allInstalledApps: List<AppItem>
)

/**
 * Caso de uso para obtener las aplicaciones instaladas organizadas por visibilidad,
 * favoritos y orden personalizado.
 */
class GetInstalledAppsUseCase(
    private val appRepository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): AppsGroup {
        val installedApps = appRepository.getInstalledApps()
        val hiddenSet = preferencesRepository.getHiddenPackages()
        val favoriteSet = preferencesRepository.getFavoritePackages()
        val customOrder = preferencesRepository.getCustomOrder()

        val allMapped = installedApps.map { app ->
            app.copy(
                isHidden = app.packageName in hiddenSet,
                isFavorite = app.packageName in favoriteSet
            )
        }

        val (hiddenRaw, visibleRaw) = allMapped.partition { it.isHidden }

        val orderedVisible = visibleRaw.sortedWith { a, b ->
            val indexA = customOrder.indexOf(a.packageName)
            val indexB = customOrder.indexOf(b.packageName)

            when {
                indexA != -1 && indexB != -1 -> indexA.compareTo(indexB)
                indexA != -1 -> -1
                indexB != -1 -> 1
                else -> a.name.lowercase().compareTo(b.name.lowercase())
            }
        }.mapIndexed { index, app ->
            app.copy(orderIndex = index)
        }

        val hiddenSorted = hiddenRaw.sortedBy { it.name.lowercase() }

        return AppsGroup(
            visibleApps = orderedVisible,
            hiddenApps = hiddenSorted,
            allInstalledApps = allMapped.sortedBy { it.name.lowercase() }
        )
    }
}
