/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
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
package org.kitteh.craftirc.endpoint.link;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.filter.Filter;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Endpoints are the origin and destination of messages tracked by CraftIRC.
 */
public class Link {
    /**
     * Helper class for loading filters.
     */
    public class LinkFilterLoader {
        private LinkFilterLoader() {
        }

        @NotNull
        public Link getLink() {
            return Link.this;
        }

        public void addFilter(@NotNull Filter filter) {
            Link.this.addFilter(filter);
        }
    }

    private final String source;
    private final String target;
    private final List<Filter> filters = new CopyOnWriteArrayList<>();

    public Link(@NotNull CraftIRC plugin, @NotNull String source, @NotNull String target, @Nullable List<? extends ConfigurationNode> filters) {
        this.source = source;
        this.target = target;
        if (filters != null) {
            plugin.getFilterManager().loadList(filters, new LinkFilterLoader());
        }
    }

    /**
     * Gets the source of this Link.
     *
     * @return the source endpoint name
     */
    @NotNull
    public String getSource() {
        return this.source;
    }

    /**
     * Gets the target of this Link.
     *
     * @return the target endpoint name
     */
    @NotNull
    public String getTarget() {
        return this.target;
    }

    private void addFilter(@NotNull Filter filter) {
        this.filters.add(filter);
    }

    /**
     * Executes filters.
     *
     * @param message the message sent by the source
     */
    public void filterMessage(@NotNull TargetedMessage message) {
        for (Filter filter : this.filters) {
            try {
                filter.processMessage(message);
                if (message.isRejected()) {
                    return;
                }
            } catch (Throwable thrown) {
                CraftIRC.log().warn("Unable to process a received message", thrown);
            }
        }
    }
}
