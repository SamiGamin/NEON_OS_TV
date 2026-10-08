package com.launcher.samiboxtv.data.mapper

import com.launcher.samiboxtv.data.model.AppEntity
import com.launcher.samiboxtv.domain.model.AppItem

/**
 * Mapper que convierte entre modelos de la capa de datos y modelos del dominio.
 */
object AppMapper {
    fun toDomain(entity: AppEntity, isHidden: Boolean = false, orderIndex: Int = -1): AppItem {
        return AppItem(
            packageName = entity.packageName,
            activityName = entity.activityName,
            name = entity.name,
            iconDrawable = entity.icon,
            bannerDrawable = entity.banner,
            category = entity.category,
            isHidden = isHidden,
            orderIndex = orderIndex,
            isSystemApp = false
        )
    }

    fun toEntity(domain: AppItem): AppEntity {
        return AppEntity(
            name = domain.name,
            packageName = domain.packageName,
            activityName = domain.activityName,
            icon = domain.iconDrawable,
            banner = domain.bannerDrawable,
            category = domain.category
        )
    }
}
