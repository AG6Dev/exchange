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
    private val firstOfferContainer = OfferContainer(firstPlayer.uuid) { onOfferChanged(firstPlayer) }
    private val secondOfferContainer = OfferContainer(secondPlayer.uuid) { onOfferChanged(secondPlayer) }
    private val openMenus = mutableMapOf<UUID, TradeMenu>()

    private var state = State.PENDING
    private var firstAccepted = false
    private var secondAccepted = false

    init {
        Exchange.LOGGER.info("Created trade session between ${firstPlayer.name.string} and ${secondPlayer.name.string}")
    }

    fun openMenus() {
        if (state != State.PENDING) {
            return
        }

        state = State.ACTIVE
        openTradeMenu(firstPlayer, secondPlayer)
        openTradeMenu(secondPlayer, firstPlayer)
        syncMenus()
    }

    fun offerContainerFor(player: ServerPlayer): SimpleContainer {
        return getParticipantData(player)?.offerContainer
            ?: throw IllegalArgumentException("Player ${player.uuid} is not part of this trade session")
    }

    fun partnerOfferContainerFor(player: ServerPlayer): SimpleContainer {
        return getPartnerData(player)?.offerContainer
            ?: throw IllegalArgumentException("Player ${player.uuid} is not part of this trade session")
    }

    fun attachMenu(player: ServerPlayer, menu: TradeMenu) {
        if (!isParticipant(player)) {
            return
        }

        openMenus[player.uuid] = menu
        syncMenu(menu, player)
    }

    fun isParticipant(player: Player): Boolean {
        return player.uuid == firstPlayer.uuid || player.uuid == secondPlayer.uuid
    }

    fun isActiveFor(player: Player): Boolean {
        return state == State.ACTIVE && isParticipant(player)
    }

    fun onOfferChanged(player: ServerPlayer) {
        if (state != State.ACTIVE || !isParticipant(player)) {
            return
        }

        if (firstAccepted || secondAccepted) {
            firstAccepted = false
            secondAccepted = false
        }

        syncMenus()
    }

    fun toggleAccepted(player: ServerPlayer) {
        if (state != State.ACTIVE) {
            return
        }

        when (getParticipantRole(player)) {
            ParticipantRole.FIRST -> firstAccepted = !firstAccepted
            ParticipantRole.SECOND -> secondAccepted = !secondAccepted
            null -> return
        }

        syncMenus()

        if (firstAccepted && secondAccepted) {
            finishTrade()
        }
    }

    fun onMenuClosed(player: ServerPlayer) {
        openMenus.remove(player.uuid)

        if (state != State.ACTIVE || !isParticipant(player)) {
            return
        }

        cancel(player, EndReason.MENU_CLOSED)
    }

    fun cancel(sourcePlayer: ServerPlayer?, reason: EndReason = EndReason.CANCELLED) {
        if (state != State.ACTIVE) {
            return
        }

        state = State.CANCELING
        returnOffersToOriginalPlayers()
        finishSession()

        Exchange.LOGGER.info("Cancelled trade session between ${firstPlayer.name.string} and ${secondPlayer.name.string}")

        when (reason) {
            EndReason.CANCELLED -> {
                val partner = sourcePlayer?.let(::getPartner)
                sourcePlayer?.sendSystemMessage(Exchange.translatable("container", "trade.cancelled"))
                partner?.sendSystemMessage(Exchange.translatable("container", "trade.cancelled_by_partner"))
            }

            EndReason.MENU_CLOSED -> {
                val partner = sourcePlayer?.let(::getPartner)
                sourcePlayer?.sendSystemMessage(Exchange.translatable("container", "trade.cancelled"))
                partner?.sendSystemMessage(Exchange.translatable("container", "trade.partner_left"))
            }

            EndReason.COMPLETED -> Unit
        }
    }

    fun getPartner(currentPlayer: ServerPlayer): ServerPlayer? {
        return getPartnerData(currentPlayer)?.player
    }

    private fun finishTrade() {
        if (state != State.ACTIVE) {
            return
        }

        state = State.COMPLETING
        transferOffersToPlayer(firstOfferContainer, secondPlayer)
        transferOffersToPlayer(secondOfferContainer, firstPlayer)
        clearOffers()
        finishSession(State.COMPLETED)

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

    private fun finishSession(finalState: State = State.CANCELLED) {
        TradeManager.removeSession(this)
        closePlayerMenu(firstPlayer)
        closePlayerMenu(secondPlayer)
        state = finalState
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

    private fun getParticipantRole(player: ServerPlayer): ParticipantRole? {
        return when (player.uuid) {
            firstPlayer.uuid -> ParticipantRole.FIRST
            secondPlayer.uuid -> ParticipantRole.SECOND
            else -> null
        }
    }

    private fun getParticipantData(player: ServerPlayer): ParticipantData? {
        return when (getParticipantRole(player)) {
            ParticipantRole.FIRST -> ParticipantData(firstPlayer, firstOfferContainer)
            ParticipantRole.SECOND -> ParticipantData(secondPlayer, secondOfferContainer)
            null -> null
        }
    }

    private fun getPartnerData(player: ServerPlayer): ParticipantData? {
        return when (getParticipantRole(player)) {
            ParticipantRole.FIRST -> ParticipantData(secondPlayer, secondOfferContainer)
            ParticipantRole.SECOND -> ParticipantData(firstPlayer, firstOfferContainer)
            null -> null
        }
    }

    private class OfferContainer(
        private val ownerId: UUID,
        private val onChanged: () -> Unit
    ) : SimpleContainer(TradeMenu.OFFER_SLOT_COUNT) {
        override fun setChanged() {
            super.setChanged()
            onChanged()
        }

        override fun stillValid(player: Player): Boolean = player.uuid == ownerId
    }

    private data class ParticipantData(val player: ServerPlayer, val offerContainer: SimpleContainer)

    enum class EndReason {
        CANCELLED,
        MENU_CLOSED,
        COMPLETED
    }

    private enum class State {
        PENDING,
        ACTIVE,
        CANCELING,
        COMPLETING,
        CANCELLED,
        COMPLETED
    }

    private enum class ParticipantRole {
        FIRST,
        SECOND
    }
}
