package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.blockentity.ExchangeTerminalBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object BlockEntityInit {

    val EXCHANGE_TERMINAL = register(
        "exchange_terminal",
        FabricBlockEntityTypeBuilder.create(::ExchangeTerminalBlockEntity, BlockInit.EXCHANGE_TERMINAL).build()
    )

    private fun <T : BlockEntityType<*>> register(id: String, blockEntityType: T): T {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Exchange.id(id), blockEntityType)
    }

    fun init() {
        //No-op
    }
}