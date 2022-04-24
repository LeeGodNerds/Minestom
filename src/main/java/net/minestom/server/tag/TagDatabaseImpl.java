package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class TagDatabaseImpl {
    record Query<T>(List<TagDatabase.Filter> filters,
                    List<TagDatabase.Sorter> sorters,
                    Tag<T> selector, int limit) implements TagDatabase.Query<T> {
        Query {
            filters = List.copyOf(filters);
            sorters = List.copyOf(sorters);
        }
    }

    static final class QueryBuilder<T> implements TagDatabase.Query.Builder<T> {
        private final Tag<T> selector;
        private final List<TagDatabase.Filter> filters = new ArrayList<>();
        private final List<TagDatabase.Sorter> sorters = new ArrayList<>();
        private int limit = -1;

        QueryBuilder(Tag<T> selector) {
            this.selector = selector;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder<T> filter(TagDatabase.@NotNull Filter filter) {
            this.filters.add(filter);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder<T> sorter(TagDatabase.@NotNull Sorter sorter) {
            this.sorters.add(sorter);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public TagDatabase.@NotNull Query<T> build() {
            return new Query<>(filters, sorters, selector, limit);
        }
    }

    record FilterEq<T>(Tag<T> tag, T value) implements TagDatabase.Filter.Eq<T> {
    }

    record Sorter(Tag<?> tag, TagDatabase.SortOrder sortOrder) implements TagDatabase.Sorter {
    }
}
