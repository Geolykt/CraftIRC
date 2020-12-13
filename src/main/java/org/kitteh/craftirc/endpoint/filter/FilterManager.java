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
package org.kitteh.craftirc.endpoint.filter;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.filter.defaults.AntiHighlight;
import org.kitteh.craftirc.endpoint.filter.defaults.Colors;
import org.kitteh.craftirc.endpoint.filter.defaults.DataMapper;
import org.kitteh.craftirc.endpoint.filter.defaults.RegexFilter;
import org.kitteh.craftirc.endpoint.link.Link;
import org.kitteh.craftirc.util.loadable.LoadableTypeManager;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Filters.
 */
public final class FilterManager extends LoadableTypeManager<Filter> {
    @Deprecated(forRemoval = false)
    enum Target {
        EndpointLoader
    }

    private final Map<String, ConfigurationNode> repeatableObjects = new ConcurrentHashMap<>();

    public FilterManager(@NotNull CraftIRC plugin, @NotNull ConfigurationNode repeatables) {
        super(plugin, Filter.class);
        // Register filter types here
        this.registerType(AntiHighlight.class);
        this.registerType(Colors.class);
        this.registerType(DataMapper.class);
        this.registerType(RegexFilter.class);
        if (!repeatables.virtual() && repeatables.isMap()) {
            this.loadRepeatables(repeatables);
        }
    }

    @Override
    protected void loadList(@NotNull List<? extends ConfigurationNode> list) {
        throw new UnsupportedOperationException("Must provide Endpoint when loading filters!");
    }

    public void loadList(@NotNull List<? extends ConfigurationNode> list, @NotNull Link.LinkFilterLoader link) {
        List<ConfigurationNode> updatedList = new ArrayList<>(list);
        for (int i = 0; i < updatedList.size(); i++) {
            ConfigurationNode node = updatedList.get(i);
            if (!node.isMap()) {
                if (this.repeatableObjects.containsKey(node.getString())) {
                    node = this.repeatableObjects.get(node.getString());
                    updatedList.set(i, node);
                } else {
                    continue;
                }
            }
            // FIXME reimplement this
            /*
            try {
                node.node(Target.EndpointLoader).set(link); // FIXME fix
            } catch (SerializationException e) {
                e.printStackTrace();
            }*/
        }
        super.loadList(updatedList);
    }

    private void loadRepeatables(@NotNull ConfigurationNode repeatables) {
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : repeatables.childrenMap().entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                // TODO log
                continue;
            }
            this.repeatableObjects.put((String) entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void processCompleted(@NotNull Filter loaded) {
        Link.LinkFilterLoader loader = loaded.getLoader();
        if (loader != null) {
            loader.addFilter(loaded);
        }
    }

    @Override
    protected void processFailedLoad(@NotNull Exception exception, @NotNull ConfigurationNode data) {
        CraftIRC.log().warn("Failed to load Filter", exception);
    }

    @Override
    protected void processInvalid(@NotNull String reason, @NotNull ConfigurationNode data) {
        CraftIRC.log().warn("Encountered invalid Filter: " + reason);
    }
}
