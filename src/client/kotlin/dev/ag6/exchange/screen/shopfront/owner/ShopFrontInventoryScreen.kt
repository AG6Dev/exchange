package dev.ag6.exchange.screen.shopfront.owner

import dev.ag6.exchange.menu.shopfront.owner.ShopFrontInventoryMenu
import dev.ag6.exchange.screen.shopfront.ShopFrontScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ShopFrontInventoryScreen(menu: ShopFrontInventoryMenu, inventory: Inventory, title: Component) :
    ShopFrontScreen<ShopFrontInventoryMenu>(
        menu,
        inventory,
        title,
        texture = TEXTURE,
        guiWidth = 176,
        guiHeight = 166,
        textureWidth = 256,
        textureHeight = 256,
        showWidgets = false
    ) {

    override fun init() {
        super.init()
        this.inventoryLabelY++
    }

    companion object {
        private val TEXTURE: Identifier = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png")
    }
}