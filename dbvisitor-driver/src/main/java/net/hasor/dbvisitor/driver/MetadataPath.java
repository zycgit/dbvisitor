/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A requested object kind and its ordered, exact parent names. */
public record MetadataPath(MetadataType type, List<Level> levels) {
    public MetadataPath {
        Objects.requireNonNull(type, "type");
        levels = List.copyOf(levels);
    }

    public MetadataPath(MetadataType type) {
        this(type, List.of());
    }

    public String name(MetadataType type) {
        for (Level level : levels) {
            if (level.type() == type) {
                return level.name();
            }
        }
        return null;
    }

    public MetadataPath child(MetadataNode parent, MetadataType childType) {
        List<Level> parents = new ArrayList<>(levels);
        parents.add(new Level(parent.type(), parent.name()));
        return new MetadataPath(childType, parents);
    }

    public MetadataPath target(MetadataType targetType) {
        return new MetadataPath(targetType, levels);
    }

    public record Level(MetadataType type, String name) {
        public Level {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(name, "name");
        }
    }
}
