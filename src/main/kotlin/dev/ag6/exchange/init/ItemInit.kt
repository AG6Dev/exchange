package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item

object ItemInit {

    // Block items
    val SHOP_FRONT_BLOCK_ITEM = register(
        "shop_front",
        BlockItem(
            BlockInit.SHOP_FRONT,
            Item.Properties().setId(
                ResourceKey.create(Registries.ITEM, Exchange.id("shop_front"))
            )
        )
    )

    private fun <T : Item> register(id: String, item: T): T {
        return Registry.register(BuiltInRegistries.ITEM, Exchange.id(id), item)
    }

    fun init() {}
}