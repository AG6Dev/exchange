package dev.ag6.exchange.blockentity

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.init.BlockEntityInit
import dev.ag6.exchange.menu.ExchangeTerminalMenu
import dev.ag6.exchange.world.TerminalPositionsSavedData
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ExchangeTerminalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityInit.EXCHANGE_TERMINAL, blockPos, blockState), ExtendedScreenHandlerFactory<BlockPos> {

    val offers: MutableList<ExchangeOffer> = mutableListOf()
    var owner: UUID? = null

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(provider: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(provider)
    }

    override fun loadAdditional(valueInput: ValueInput) {
        val uuidString = valueInput.getString("Owner").getOrNull()
        if (!uuidString.isNullOrEmpty()) {
            owner = UUID.fromString(uuidString)
        }

        this.offers.clear()
        for (offer in valueInput.listOrEmpty("Offers", ExchangeOffer.CODEC)) {
            this.offers.add(offer)
        }
    }

    override fun saveAdditional(valueOutput: ValueOutput) {
        valueOutput.putString("Owner", owner?.toString() ?: "")

        if (offers.isNotEmpty()) {
            val offers = valueOutput.list("Offers", ExchangeOffer.CODEC)
            for (offer in this.offers) {
                offers.add(offer)
            }
        }
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos {
        return this.worldPosition
    }

    fun addOffer(offeredItems: List<ItemStack>, receivingItems: List<ItemStack>) {
        if (owner == null) {
            Exchange.LOGGER.error("Could not add offer to terminal as the owner is null")
            return
        }

        if (offeredItems.isEmpty() || receivingItems.isEmpty()) {
            Exchange.LOGGER.error("Could not add offer to terminal as the offered or receiving items are empty")
            return
        }

        val offer = ExchangeOffer(owner!!, this.worldPosition, offeredItems, receivingItems)
        offers.add(offer)

        this.update()
    }


    override fun preRemoveSideEffects(blockPos: BlockPos, blockState: BlockState) {
        level?.let { TerminalPositionsSavedData.getSavedData(it)?.removeTerminal(blockPos) }
        super.preRemoveSideEffects(blockPos, blockState)
    }

    override fun getDisplayName(): Component = TITLE

    override fun createMenu(
        i: Int,
        inventory: Inventory,
        player: Player
    ): AbstractContainerMenu {
        return ExchangeTerminalMenu(i, inventory, this.worldPosition)
    }

    private fun update() {
        this.level?.sendBlockUpdated(this.worldPosition, this.blockState, this.blockState, 3)
        this.setChanged()
    }

    companion object {
        val TITLE = Exchange.translatable("container", "exchange_terminal")
    }
}
