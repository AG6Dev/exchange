package dev.ag6.exchange.init

import com.mojang.blaze3d.platform.InputConstants
import dev.ag6.exchange.Exchange
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object KeyMappingInit {
    val category: KeyMapping.Category = KeyMapping.Category(Exchange.id("key_category"))

    val tradeKey: KeyMapping = register(
        KeyMapping(
            "key.${Exchange.MOD_ID}.trade", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, category
        )
    )

    private fun register(keyMapping: KeyMapping): KeyMapping {
        return KeyBindingHelper.registerKeyBinding(keyMapping)
    }

    fun init() {
        // No-op, just to trigger the static initializer
    }
}
