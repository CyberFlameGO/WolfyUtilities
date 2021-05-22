package me.wolfyscript.utilities.api.network.messages;

import io.netty.buffer.Unpooled;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.nms.network.MCByteBuf;
import me.wolfyscript.utilities.util.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Reads and Writes a single registered Packet.
 *
 * <strong>This API is not final and still WIP! It is very experimental and for testing! The API might change completely in the future, so you shouldn't use it!</strong>
 */
public class Message implements PluginMessageListener {

    private final NamespacedKey channelTag;
    private final WolfyUtilities wolfyUtils;
    private final MessageConsumer decoder;

    public Message(NamespacedKey key, WolfyUtilities wolfyUtils, @Nullable MessageConsumer decoder) {
        this.channelTag = key;
        this.wolfyUtils = wolfyUtils;
        this.decoder = decoder;
        Plugin plugin = wolfyUtils.getPlugin();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelTag.toString());
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelTag.toString(), this);
    }

    Optional<MessageConsumer> getDecoder() {
        return Optional.ofNullable(decoder);
    }

    void decode(MCByteBuf in, Player player) {
        getDecoder().ifPresent(d -> d.accept(player, wolfyUtils, in));
    }

    void send(Player player, MCByteBuf bufOut) {
        player.sendPluginMessage(wolfyUtils.getPlugin(), channelTag.toString(), bufOut.array());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals(channelTag.toString())) {
            return;
        }
        decode(wolfyUtils.getNmsUtil().getNetworkUtil().buffer(Unpooled.wrappedBuffer(message)), player);
    }
}
