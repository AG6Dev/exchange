package dev.ag6.exchange.init

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.ExchangeTerminalMenu
import dev.ag6.exchange.menu.TradeMenu
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

object MenuTypeInit {
    val TRADE: MenuType<TradeMenu> = register("trade", ::TradeMenu)
    val EXCHANGE_TERMINAL: MenuType<ExchangeTerminalMenu> = register("exchange_terminal", ::ExchangeTerminalMenu, BlockPos.STREAM_CODEC)

    private fun <T : AbstractContainerMenu, D : Any> register(
        id: String,
        factory: ExtendedScreenHandlerType.ExtendedFactory<T, D>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, D>
    ): ExtendedScreenHandlerType<T, D> {
        return Registry.register(BuiltInRegistries.MENU, Exchange.id(id), ExtendedScreenHandlerType(factory, codec))
    }

    private fun <T : AbstractContainerMenu> register(id: String, factory: MenuType.MenuSupplier<T>): MenuType<T> {
        return Registry.register(BuiltInRegistries.MENU, Exchange.id(id), MenuType(factory, FeatureFlagSet.of()))
    }

    fun init() {
        // No-op, just to trigger the static initializer
    }
}
