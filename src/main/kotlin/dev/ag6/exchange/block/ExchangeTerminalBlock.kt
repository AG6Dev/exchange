package dev.ag6.exchange.block

import com.mojang.serialization.MapCodec
import dev.ag6.exchange.blockentity.ExchangeTerminalBlockEntity
import dev.ag6.exchange.world.TerminalPositionsSavedData
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

//TODO: a fun loading animation when opening the block menu, can be toggleable in config
class ExchangeTerminalBlock(properties: Properties) : BaseEntityBlock(properties) {
    override fun useItemOn(
        itemStack: ItemStack,
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is ExchangeTerminalBlockEntity) {
            player.openMenu(blockEntity)
        }

        return InteractionResult.SUCCESS
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun newBlockEntity(
        blockPos: BlockPos, blockState: BlockState
    ): BlockEntity {
        return ExchangeTerminalBlockEntity(blockPos, blockState)
    }

    override fun onPlace(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        oldBlockState: BlockState,
        movedByPiston: Boolean
    ) {
        super.onPlace(blockState, level, blockPos, oldBlockState, movedByPiston)
        if (oldBlockState.`is`(this)) return

        TerminalPositionsSavedData.getSavedData(level)?.addTerminal(blockPos)
    }

    companion object {
        private val CODEC: MapCodec<ExchangeTerminalBlock> = simpleCodec(::ExchangeTerminalBlock)
    }
}
