package dev.ag6.exchange.blockentity

import dev.ag6.exchange.init.BlockEntityInit
import dev.ag6.exchange.menu.shopfront.ShopFrontCustomerMenu
import dev.ag6.exchange.menu.shopfront.owner.ShopFrontOwnerMenu
import dev.ag6.exchange.network.BlockPosPayload
import dev.ag6.exchange.network.CommonNetworking
import dev.ag6.exchange.offer.ExchangeOffer
import dev.ag6.exchange.offer.ExchangeOffersSavedData
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.Containers
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.ContainerUser
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*

class ShopFrontBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntityInit.SHOP_FRONT, pos, state),
    ExtendedScreenHandlerFactory<BlockPosPayload> {

    val inventory: SimpleContainer = object : SimpleContainer(27) {
        override fun startOpen(user: ContainerUser) {
            super.startOpen(user)
            update()
        }

        override fun stopOpen(user: ContainerUser) {
            super.stopOpen(user)
            update()
        }

        override fun setChanged() {
            super.setChanged()
            update()
        }
    }
    var isOpen: Boolean = true
        set(value) {
            field = value
            this.update()
        }
    var owner: UUID? = null

    override fun getScreenOpeningData(player: ServerPlayer): BlockPosPayload {
        return BlockPosPayload(worldPosition)
    }

    override fun getDisplayName(): Component = TITLE

    override fun createMenu(
        i: Int, inventory: Inventory, player: Player
    ): AbstractContainerMenu {
        if (player.uuid == owner) {
            return ShopFrontOwnerMenu(i, inventory, this)
        }
        return ShopFrontCustomerMenu(i, inventory, this)
    }

    override fun preRemoveSideEffects(pos: BlockPos, state: BlockState) {
        super.preRemoveSideEffects(pos, state)

        val revision = ExchangeOffersSavedData.getSavedData(level)?.removeOffersAt(worldPosition)
        if (revision != null) {
            CommonNetworking.broadcastShopOffersCleared(level, worldPosition, revision)
        }
        level?.let { Containers.dropContents(it, worldPosition, inventory) }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output, inventory.items)

        if (owner != null) {
            output.putString("Owner", owner.toString())
        }

        output.putBoolean("IsOpen", isOpen)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        ContainerHelper.loadAllItems(input, inventory.items)
        input.getString("Owner").ifPresent {
            owner = UUID.fromString(it)
        }
        isOpen = input.getBooleanOr("IsOpen", true)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(registries)
    }

    fun isOwner(player: Player): Boolean {
        return owner != null && player.uuid == owner
    }

    fun getOffers(): List<ExchangeOffer> {
        return ExchangeOffersSavedData.getSavedData(this.level)?.getOffersAt(this.worldPosition) ?: emptyList()
    }

    private fun update() {
        setChanged()
        level?.let {
            if (!it.isClientSide)
                it.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)
        }
    }

    companion object {
        private val TITLE = Component.translatable("container.exchange.shop_front")
    }
}
