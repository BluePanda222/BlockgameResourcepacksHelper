package com.bluepanda22.blockgameresourcepackshelper.resourcepack

import com.bluepanda22.blockgameresourcepackshelper.resourcepack.bool.BgiBooleanBooleanProperty
import com.bluepanda22.blockgameresourcepackshelper.resourcepack.bool.BgiHasAttributeBooleanProperty
import com.bluepanda22.blockgameresourcepackshelper.resourcepack.numeric.BgiDoubleNumericProperty
import com.bluepanda22.blockgameresourcepackshelper.resourcepack.numeric.BgiIntegerNumericProperty
import com.bluepanda22.blockgameresourcepackshelper.resourcepack.select.*
import dev.bnjc.bglib.BGIField
import dev.bnjc.bglib.BGIType
import net.minecraft.client.render.item.property.bool.BooleanProperties
import net.minecraft.client.render.item.property.numeric.NumericProperties
import net.minecraft.client.render.item.property.select.SelectProperties
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object ResourcepackPropertiesInitializer {

	val LOGGER: Logger = LoggerFactory.getLogger("BlockgameResourcepacksHelper - ResourcepackPropertiesInitializer");

	fun initResourcepackProperties() {
		BGIField.entries.forEach { bgiField: BGIField ->
			val name = bgiField.name
			val bgiType = bgiField.type
			val id = "blockgame:bgi_" + name.lowercase(Locale.getDefault())
			val hasAttributeId = "blockgame:bgi_has_" + name.lowercase(Locale.getDefault())

			LOGGER.debug("Registering property {} of type {} as BgiHasAttributeBooleanProperty", hasAttributeId, bgiType)
			BooleanProperties.ID_MAPPER.put(Identifier.of(hasAttributeId), BgiHasAttributeBooleanProperty.ofCodec(bgiField))

			when (bgiType) {
				BGIType.INTEGER -> {
					LOGGER.debug("Registering property {} of type {} as BgiIntegerSelectProperty", id, bgiType)
					SelectProperties.ID_MAPPER.put(Identifier.of(id), BgiIntegerSelectProperty.ofType(bgiField))
					LOGGER.debug("Registering property {} of type {} as BgiIntegerNumericProperty", id, bgiType)
					NumericProperties.ID_MAPPER.put(Identifier.of(id), BgiIntegerNumericProperty.ofCodec(bgiField))
				}

				BGIType.STRING -> {
					LOGGER.debug("Registering property {} of type {} as BgiStringSelectProperty", id, bgiType)
					SelectProperties.ID_MAPPER.put(Identifier.of(id), BgiStringSelectProperty.ofType(bgiField))
				}

				BGIType.STRING_ARRAY -> {
					if (bgiField == BGIField.NAME_PRE || bgiField == BGIField.NAME_SUF) {
						LOGGER.debug("Registering property {} of type {} as BgiStringArraySelectProperty", id, bgiType)
						SelectProperties.ID_MAPPER.put(Identifier.of(id), BgiStringArraySelectProperty.ofType(bgiField))
					}
				}

				BGIType.DOUBLE -> {
					LOGGER.debug("Registering property {} of type {} as BgiDoubleSelectProperty", id, bgiType)
					SelectProperties.ID_MAPPER.put(Identifier.of(id), BgiDoubleSelectProperty.ofType(bgiField))
					LOGGER.debug("Registering property {} of type {} as BgiDoubleNumericProperty", id, bgiType)
					NumericProperties.ID_MAPPER.put(Identifier.of(id), BgiDoubleNumericProperty.ofCodec(bgiField))
				}

				BGIType.STREAM -> {
					// Stream data is very complex, so it is not handled.
					// LOGGER.warn("Couldn't register BGIField {} of BGIType {}", bgiField, bgiType)
				}

				BGIType.BOOLEAN -> {
					LOGGER.debug("Registering property {} of type {} as BgiBooleanSelectProperty", id, bgiType)
					SelectProperties.ID_MAPPER.put(Identifier.of(id), BgiBooleanSelectProperty.ofType(bgiField))
					LOGGER.debug("Registering property {} of type {} as BgiBooleanBooleanProperty", id, bgiType)
					BooleanProperties.ID_MAPPER.put(Identifier.of(id), BgiBooleanBooleanProperty.ofCodec(bgiField))
				}

				else -> LOGGER.warn("Unhandled BGIField {} of BGIType {} in registering", bgiField, bgiType)
			}
		}
	}

}
