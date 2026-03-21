package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.block.ExchangeTerminalBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

object BlockInit {
    val EXCHANGE_TERMINAL = register(
        "exchange_terminal", ExchangeTerminalBlock(
            BlockBehaviour.Properties.of().setId(
                ResourceKey.create(Registries.BLOCK, Exchange.id("exchange_terminal"))
            )
        )
    )

    private fun <T : Block> register(id: String, block: T): Block {
        return Registry.register(BuiltInRegistries.BLOCK, Exchange.id(id), block)
    }

    fun init() {
        //No-op
    }
}