package com.bluepanda22.blockgameresourcepackshelper.util

import net.minecraft.item.equipment.EquipmentAsset
import net.minecraft.item.equipment.EquipmentAssetKeys
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object TextureUtil {

	fun getEquipmentAssetKeyFromTextureId(textureId: Identifier): RegistryKey<EquipmentAsset>? {
		val sanitisedTextureId = getSanitisedTextureIdFromTextureId(textureId)
			?: return null
		return RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, sanitisedTextureId)
	}

	fun getSanitisedTextureIdFromTextureId(textureId: Identifier): Identifier? {
		val equipmentAsset = if (textureId.path.startsWith("textures/entity/equipment/humanoid/")) {
			textureId.path.substring("textures/entity/equipment/humanoid/".length).substringBeforeLast(".")
		} else if (textureId.path.startsWith("textures/entity/equipment/humanoid_leggings/")) {
			textureId.path.substring("textures/entity/equipment/humanoid_leggings/".length).substringBeforeLast(".")
		} else {
			return null
		}
		return Identifier.of(textureId.namespace, equipmentAsset)
	}

	fun isSpecial(registryKey: RegistryKey<EquipmentAsset>): Boolean {
		return registryKey.value.path.contains("/special/")
	}

	fun makeNotSpecial(registryKey: RegistryKey<EquipmentAsset>): RegistryKey<EquipmentAsset>? {
		if (isSpecial(registryKey)) {
			return RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(registryKey.value.namespace, registryKey.value.path.replaceFirst("special/", "")))
		}
		return null
	}

}
