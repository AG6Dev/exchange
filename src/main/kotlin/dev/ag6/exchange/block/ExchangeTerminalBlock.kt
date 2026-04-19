package dev.ag6.exchange.block

import dev.ag6.exchange.init.NetworkInit
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

//TODO: a fun loading animation when opening the block menu, can be toggleable in config
class ExchangeTerminalBlock(properties: Properties) : Block(properties) {
    override fun useItemOn(
        itemStack: ItemStack,
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            NetworkInit.syncTerminalOffersToPlayer(player)
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult)
    }
}
