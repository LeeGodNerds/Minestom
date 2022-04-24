package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public interface TagDatabase {
    Query<NBTCompound> QUERY_ALL = query().build();

    static <T> Query.@NotNull Builder<T> query(Tag<T> selector) {
        return new TagDatabaseImpl.QueryBuilder(selector);
    }

    static Query.@NotNull Builder<NBTCompound> query() {
        return query(Tag.View(TagSerializer.COMPOUND));
    }

    static @NotNull Sorter sort(@NotNull Tag<?> tag, SortOrder order) {
        return new TagDatabaseImpl.Sorter(tag, order);
    }

    void insert(@NotNull TagHandler... handler);

    void update(@NotNull Query<?> query, @NotNull TagHandler handler);

    <T> @NotNull List<@NotNull T> find(@NotNull Query<T> query);

    <T> void replace(@NotNull Query<?> query, @NotNull Tag<T> tag, @NotNull UnaryOperator<T> operator);

    void delete(@NotNull Query<?> query);

    default <T> void replaceConstant(@NotNull Query<?> query, @NotNull Tag<T> tag, @Nullable T value) {
        replace(query, tag, t -> value);
    }

    default <T> void updateSingle(@NotNull Tag<T> tag, @NotNull T value, @NotNull TagHandler handler) {
        final Query<?> query = query().filter(Filter.eq(tag, value)).limit(1).build();
        update(query, handler);
    }

    default <T> @NotNull Optional<@NotNull NBTCompound> findFirst(@NotNull Tag<T> tag, @NotNull T value) {
        final Query<NBTCompound> query = query().filter(Filter.eq(tag, value)).limit(1).build();
        return find(query).stream().findFirst();
    }

    sealed interface Query<T> permits TagDatabaseImpl.Query {
        @Unmodifiable
        @NotNull List<@NotNull Filter> filters();

        @Unmodifiable
        @NotNull List<@NotNull Sorter> sorters();

        @NotNull Tag<T> selector();

        int limit();

        sealed interface Builder<T> permits TagDatabaseImpl.QueryBuilder {
            @NotNull Builder<T> filter(@NotNull Filter filter);

            @NotNull Builder<T> sorter(@NotNull Sorter sorter);

            @NotNull Builder<T> limit(int limit);

            @NotNull Query<T> build();
        }
    }

    sealed interface Filter permits Filter.Eq {
        static <T> Filter eq(@NotNull Tag<T> tag, @NotNull T value) {
            return new TagDatabaseImpl.FilterEq<>(tag, value);
        }

        sealed interface Eq<T> extends Filter permits TagDatabaseImpl.FilterEq {
            @NotNull Tag<T> tag();

            @NotNull T value();
        }
    }

    sealed interface Sorter permits TagDatabaseImpl.Sorter {
        @NotNull Tag<?> tag();

        @NotNull SortOrder sortOrder();
    }

    enum SortOrder {
        ASCENDING,
        DESCENDING
    }
}
