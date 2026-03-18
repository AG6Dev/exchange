package dev.ag6.exchange.trade

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.menu.TradeMenu
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import java.util.*

class TradeSession(
    private val firstPlayer: ServerPlayer, private val secondPlayer: ServerPlayer
) {
    private val firstOfferContainer = SimpleContainer(TradeMenu.OFFER_SLOT_COUNT)
    private val secondOfferContainer = SimpleContainer(TradeMenu.OFFER_SLOT_COUNT)
    private val openMenus = mutableMapOf<UUID, TradeMenu>()

    private var firstAccepted = false
    private var secondAccepted = false
    private var finished = false
    private var canceling = false

    init {
        Exchange.LOGGER.info("Created trade session between ${firstPlayer.name.string} and ${secondPlayer.name.string}")
    }

    fun openMenus() {
        openTradeMenu(firstPlayer, secondPlayer)
        openTradeMenu(secondPlayer, firstPlayer)
        syncMenus()
    }

    fun offerContainerFor(player: ServerPlayer): SimpleContainer {
        return if (player.uuid == firstPlayer.uuid) firstOfferContainer else secondOfferContainer
    }

    fun partnerOfferContainerFor(player: ServerPlayer): SimpleContainer {
        return if (player.uuid == firstPlayer.uuid) secondOfferContainer else firstOfferContainer
    }

    fun attachMenu(player: ServerPlayer, menu: TradeMenu) {
        openMenus[player.uuid] = menu
        syncMenu(menu, player)
    }

    fun isParticipant(player: Player): Boolean {
        return player.uuid == firstPlayer.uuid || player.uuid == secondPlayer.uuid
    }

    fun onOffersChanged() {
        if (finished || canceling) {
            return
        }

        if (firstAccepted || secondAccepted) {
            firstAccepted = false
            secondAccepted = false
        }

        syncMenus()
    }

    fun toggleAccepted(player: ServerPlayer) {
        if (finished || canceling) {
            return
        }

        when (player.uuid) {
            firstPlayer.uuid -> firstAccepted = !firstAccepted
            secondPlayer.uuid -> secondAccepted = !secondAccepted
            else -> return
        }

        syncMenus()

        if (firstAccepted && secondAccepted) {
            completeTrade()
        }
    }

    fun onMenuClosed(player: ServerPlayer) {
        openMenus.remove(player.uuid)

        if (finished || canceling) {
            return
        }

        cancelTrade(player)
    }

    fun cancelTrade(sourcePlayer: ServerPlayer?) {
        if (finished || canceling) {
            return
        }

        canceling = true
        returnOffersToOriginalPlayers()
        TradeManager.removeSession(this)

        closePlayerMenu(firstPlayer)
        closePlayerMenu(secondPlayer)

        Exchange.LOGGER.info("Cancelled trade session between ${firstPlayer.name.string} and ${secondPlayer.name.string}")

        sourcePlayer?.sendSystemMessage(Exchange.translatable("container", "trade.cancelled"))
    }

    fun getPartner(currentPlayer: ServerPlayer): ServerPlayer? {
        return when (currentPlayer.uuid) {
            firstPlayer.uuid -> secondPlayer
            secondPlayer.uuid -> firstPlayer
            else -> null
        }
    }

    private fun completeTrade() {
        if (finished || canceling) {
            return
        }

        finished = true
        transferOffersToPlayer(firstOfferContainer, secondPlayer)
        transferOffersToPlayer(secondOfferContainer, firstPlayer)
        clearOffers()
        TradeManager.removeSession(this)

        closePlayerMenu(firstPlayer)
        closePlayerMenu(secondPlayer)

        Exchange.LOGGER.info("Trade completed between ${firstPlayer.name.string} and ${secondPlayer.name.string}")

        firstPlayer.sendSystemMessage(Exchange.translatable("container", "trade.complete"))
        secondPlayer.sendSystemMessage(Exchange.translatable("container", "trade.complete"))
    }

    private fun returnOffersToOriginalPlayers() {
        transferOffersToPlayer(firstOfferContainer, firstPlayer)
        transferOffersToPlayer(secondOfferContainer, secondPlayer)
        clearOffers()
    }

    private fun transferOffersToPlayer(container: SimpleContainer, recipient: ServerPlayer) {
        repeat(container.containerSize) { slot ->
            val stack = container.removeItemNoUpdate(slot)
            if (stack.isEmpty) {
                return@repeat
            }

            val remaining = stack.copy()
            if (!recipient.inventory.add(remaining)) {
                recipient.drop(remaining, false)
            } else if (!remaining.isEmpty) {
                recipient.drop(remaining, false)
            }
        }
    }

    private fun clearOffers() {
        repeat(firstOfferContainer.containerSize) { slot ->
            firstOfferContainer.setItem(slot, ItemStack.EMPTY)
            secondOfferContainer.setItem(slot, ItemStack.EMPTY)
        }
    }

    private fun closePlayerMenu(player: ServerPlayer) {
        val menu = openMenus[player.uuid] ?: return
        if (player.containerMenu === menu) {
            player.closeContainer()
        }
    }

    private fun openTradeMenu(player: ServerPlayer, otherPlayer: ServerPlayer) {
        player.openMenu(object : MenuProvider {
            override fun getDisplayName(): Component {
                return Exchange.translatable("container", "trade", otherPlayer.name.string)
            }

            override fun createMenu(syncId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
                return TradeMenu(syncId, inventory, this@TradeSession, player as ServerPlayer)
            }
        })
    }

    private fun syncMenus() {
        syncMenu(openMenus[firstPlayer.uuid], firstPlayer)
        syncMenu(openMenus[secondPlayer.uuid], secondPlayer)
        openMenus.values.forEach(TradeMenu::broadcastChanges)
    }

    private fun syncMenu(menu: TradeMenu?, viewer: ServerPlayer) {
        menu ?: return
        if (viewer.uuid == firstPlayer.uuid) {
            menu.setAcceptStatus(firstAccepted, secondAccepted)
        } else {
            menu.setAcceptStatus(secondAccepted, firstAccepted)
        }
    }
}
