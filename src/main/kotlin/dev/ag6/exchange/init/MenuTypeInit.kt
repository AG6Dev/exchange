package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.TradeMenu
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

object MenuTypeInit {
    val TRADE_CONTAINER_SCREEN: MenuType<TradeMenu> = register("trade", ::TradeMenu)

    private fun <T : AbstractContainerMenu> register(id: String, screen: MenuType.MenuSupplier<T>): MenuType<T> {
        return Registry.register(BuiltInRegistries.MENU, Exchange.id(id), MenuType(screen, FeatureFlagSet.of()))
    }

    fun init() {
        // No-op, just to trigger the static initializer
    }
}
