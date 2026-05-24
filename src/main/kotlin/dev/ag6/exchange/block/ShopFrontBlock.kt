package dev.ag6.exchange.block

import com.mojang.serialization.MapCodec
import dev.ag6.exchange.blockentity.ShopFrontBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ShopFrontBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult
    ): InteractionResult {

        if (!level.isClientSide) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is ShopFrontBlockEntity) {
                if (blockEntity.owner == null) blockEntity.owner = player.uuid

                player.openMenu(blockEntity)
            }
        }


        return InteractionResult.SUCCESS
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun newBlockEntity(
        pos: BlockPos, state: BlockState
    ): BlockEntity {
        return ShopFrontBlockEntity(pos, state)
    }

    override fun getDestroyProgress(state: BlockState, player: Player, level: BlockGetter, pos: BlockPos): Float {
        if (level is ServerLevel) {
            if (level.server.playerList.ops.userList.contains(player.plainTextName)) {
                return super.getDestroyProgress(state, player, level, pos)
            }

            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is ShopFrontBlockEntity) {
                if (blockEntity.owner == null) return super.getDestroyProgress(state, player, level, pos)

                if (blockEntity.owner != player.uuid) {
                    return 0f
                }
            }
        }

        return 0f
    }

    companion object {
        private val CODEC = simpleCodec(::ShopFrontBlock)
    }
}
