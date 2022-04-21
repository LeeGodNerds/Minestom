package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class TagDatabaseImpl {
    record Query(List<TagDatabase.Filter> filters,
                 List<TagDatabase.Sorter> sorters, int limit) implements TagDatabase.Query {
        Query {
            filters = List.copyOf(filters);
            sorters = List.copyOf(sorters);
        }
    }

    static final class QueryBuilder implements TagDatabase.Query.Builder {
        private final List<TagDatabase.Filter> filters = new ArrayList<>();
        private final List<TagDatabase.Sorter> sorters = new ArrayList<>();
        private int limit = -1;

        @Override
        public TagDatabase.Query.@NotNull Builder filters(@NotNull List<TagDatabase.@NotNull Filter> filters) {
            this.filters.addAll(filters);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder filter(TagDatabase.@NotNull Filter filter) {
            this.filters.add(filter);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder sorters(@NotNull List<TagDatabase.@NotNull Sorter> sorters) {
            this.sorters.addAll(sorters);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder sorter(TagDatabase.@NotNull Sorter sorter) {
            this.sorters.add(sorter);
            return this;
        }

        @Override
        public TagDatabase.Query.@NotNull Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public TagDatabase.@NotNull Query build() {
            return new Query(filters, sorters, limit);
        }
    }

    static final class Filter {
        record Eq<T>(Tag<T> tag, T value) implements TagDatabase.Filter.Eq<T> {
        }
    }

    record Sorter(Tag<?> tag, TagDatabase.SortOrder sortOrder) implements TagDatabase.Sorter {
    }
}
