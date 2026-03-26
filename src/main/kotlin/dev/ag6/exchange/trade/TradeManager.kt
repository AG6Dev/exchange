package dev.ag6.exchange.trade

import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object TradeManager {
    private val sessionsByPlayer = mutableMapOf<UUID, TradeSession>()

    fun startTradeSession(firstPlayer: ServerPlayer, secondPlayer: ServerPlayer): TradeSession? {
        if (firstPlayer.uuid == secondPlayer.uuid) {
            return null
        }

        if (sessionsByPlayer.containsKey(firstPlayer.uuid) || sessionsByPlayer.containsKey(secondPlayer.uuid)) {
            return null
        }

        return TradeSession(firstPlayer, secondPlayer).also { session ->
            sessionsByPlayer[firstPlayer.uuid] = session
            sessionsByPlayer[secondPlayer.uuid] = session
        }
    }

    fun getSession(player: ServerPlayer): TradeSession? = sessionsByPlayer[player.uuid]

    fun removeSession(session: TradeSession) {
        sessionsByPlayer.entries.removeIf { (_, activeSession) -> activeSession === session }
    }
}
