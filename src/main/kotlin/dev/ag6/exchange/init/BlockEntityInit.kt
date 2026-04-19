package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object BlockEntityInit {

    private fun <T : BlockEntityType<*>> register(id: String, blockEntityType: T): T {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Exchange.id(id), blockEntityType)
    }

    fun init() {
        //No-op
    }
}