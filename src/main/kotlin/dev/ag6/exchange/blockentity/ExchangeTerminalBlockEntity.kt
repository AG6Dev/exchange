package dev.ag6.exchange.blockentity

import dev.ag6.exchange.Exchange
import dev.ag6.exchange.init.BlockEntityInit
import dev.ag6.exchange.init.NetworkInit
import dev.ag6.exchange.menu.ExchangeTerminalMenu
import dev.ag6.exchange.world.ExchangeOffersSavedData
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
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class ExchangeTerminalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityInit.EXCHANGE_TERMINAL, blockPos, blockState), ExtendedScreenHandlerFactory<BlockPos> {

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(provider: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(provider)
    }

    override fun loadAdditional(valueInput: ValueInput) = Unit

    override fun saveAdditional(valueOutput: ValueOutput) = Unit

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos {
        return this.worldPosition
    }

    override fun preRemoveSideEffects(blockPos: BlockPos, blockState: BlockState) {
        level?.let {
            ExchangeOffersSavedData.getSavedData(it)?.removeOffersAt(blockPos)
            it.server?.playerList?.players?.let(NetworkInit::syncTerminalOffersToOpenMenus)
        }
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

    companion object {
        val TITLE = Exchange.translatable("container", "exchange_terminal")
    }
}
