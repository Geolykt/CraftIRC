/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 * * Copyright (C) 2020 Emeric Werner https://geolykt.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.craftirc.minestom;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.filter.Filter;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Load;
import org.kitteh.craftirc.util.loadable.Loadable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A filter by permission node.
 */
@Loadable.Type(name = "permission")
public final class PermissionFilter extends Filter {
    @Load
    private String permission;

    public PermissionFilter(@NotNull CraftIRC plugin) {}

    /**
     * Gets the permission node being monitored.
     *
     * @return the permission node monitored
     */
    @NotNull
    public String getPermission() {
        return permission;
    }

    @Override
    public void processMessage(@NotNull TargetedMessage message) {
        if (message.getCustomData().containsKey(ChatEndpoint.RECIPIENT_NAMES)) {
            @SuppressWarnings("unchecked")
            List<MinecraftPlayer> players = (List<MinecraftPlayer>) message.getCustomData().get(ChatEndpoint.RECIPIENT_NAMES);
            Iterator<MinecraftPlayer> iterator = players.iterator();
            while (iterator.hasNext()) {
                MinecraftPlayer minecraftPlayer = iterator.next();
                Optional<Player> player = Optional.ofNullable(MinecraftServer.getConnectionManager().getPlayer(minecraftPlayer.getName()));
                if (!player.isPresent() || !player.get().hasPermission(this.getPermission())) {
                    iterator.remove();
                }
            }
        }
    }
}