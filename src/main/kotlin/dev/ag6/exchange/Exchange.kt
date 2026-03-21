package dev.ag6.exchange

import dev.ag6.exchange.init.BlockEntityInit
import dev.ag6.exchange.init.BlockInit
import dev.ag6.exchange.init.MenuTypeInit
import dev.ag6.exchange.init.NetworkInit
import net.fabricmc.api.ModInitializer
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//TODO: Add remote delivery via drones and drone station
//TODO: Consider adding a chat during trading so you can talk to the other player
//TODO: Change homepage in fabric.mod.json
object Exchange : ModInitializer {
    const val MOD_ID = "exchange"
    val LOGGER: Logger = LoggerFactory.getLogger("exchange")

    override fun onInitialize() {
        LOGGER.info("Exchange Mod initializing...")

        BlockInit.init()
        BlockEntityInit.init()
        MenuTypeInit.init()
        NetworkInit.init()

        LOGGER.info("Exchange Mod initialized.")
    }

    fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

    fun translatable(prefix: String, path: String): Component = Component.translatable("${prefix}.${MOD_ID}.$path")

    fun translatable(prefix: String, path: String, vararg args: Any): Component =
        Component.translatable("${prefix}.${MOD_ID}.$path", *args)
}