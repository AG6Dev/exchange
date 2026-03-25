package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item

object ItemInit {
    val EXCHANGE_TERMINAL_BLOCK_ITEM = register("exchange_terminal_block", BlockItem(BlockInit.EXCHANGE_TERMINAL, Item.Properties().stacksTo(1).setId(
        ResourceKey.create(Registries.ITEM, Exchange.id("exchange_terminal_block"))
    )))

    private fun <T : Item> register(id: String, item: T): T {
        return Registry.register(BuiltInRegistries.ITEM, Exchange.id(id), item)
    }

    fun init() {}
}