package dev.ag6.exchange.world

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType

class TerminalPositionsSavedData : SavedData() {
    private val terminals: MutableList<BlockPos> = mutableListOf()

    fun addTerminal(blockPos: BlockPos) {
        if (terminals.contains(blockPos)) return

        terminals.add(blockPos.immutable())
        setDirty()
    }

    fun removeTerminal(blockPos: BlockPos) {
        if (!terminals.remove(blockPos)) return

        setDirty()
    }

    fun getTerminals(): List<BlockPos> = terminals.toList()

    companion object {
        val CODEC: Codec<TerminalPositionsSavedData> = RecordCodecBuilder.create { inst ->
            inst.group(
                Codec.list(BlockPos.CODEC).fieldOf("terminals")
                    .forGetter { it.terminals })
                .apply(inst) { terminals -> TerminalPositionsSavedData().apply { this.terminals.addAll(terminals) } }
        }

        val TYPE: SavedDataType<TerminalPositionsSavedData> = SavedDataType(
            "exchange_terminals", ::TerminalPositionsSavedData, CODEC, DataFixTypes.LEVEL
        )

        fun getSavedData(server: MinecraftServer): TerminalPositionsSavedData {
            val level = server.getLevel(ServerLevel.OVERWORLD) ?: return TerminalPositionsSavedData()

            return level.dataStorage.computeIfAbsent(TYPE)
        }

        fun getSavedData(level: Level): TerminalPositionsSavedData? {
            if (level.isClientSide) return null

            return level.server?.let(::getSavedData)
        }
    }
}
