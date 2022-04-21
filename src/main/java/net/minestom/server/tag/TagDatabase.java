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
    Query QUERY_ALL = new TagDatabaseImpl.Query(List.of(), List.of(), -1);

    static @NotNull Query.Builder query() {
        return new TagDatabaseImpl.QueryBuilder();
    }

    static @NotNull Sorter sort(@NotNull Tag<?> tag, SortOrder order) {
        return new TagDatabaseImpl.Sorter(tag, order);
    }

    void insert(@NotNull TagHandler... handler);

    void update(@NotNull Query query, @NotNull TagHandler handler);

    @NotNull List<@NotNull NBTCompound> find(@NotNull Query query);

    <T> void replace(@NotNull Query query, @NotNull Tag<T> tag, @NotNull UnaryOperator<T> operator);

    void delete(@NotNull Query query);

    default <T> void replaceConstant(@NotNull Query query, @NotNull Tag<T> tag, @Nullable T value) {
        replace(query, tag, t -> value);
    }

    default <T> void updateSingle(@NotNull Tag<T> tag, @NotNull T value, @NotNull TagHandler handler) {
        final Query query = new TagDatabaseImpl.Query(List.of(Filter.eq(tag, value)), List.of(), 1);
        update(query, handler);
    }

    default @NotNull List<@NotNull NBTCompound> find(@NotNull List<@NotNull Filter> filters) {
        final Query query = new TagDatabaseImpl.Query(filters, List.of(), -1);
        return find(query);
    }

    default Optional<NBTCompound> findFirst(@NotNull List<@NotNull Filter> filters) {
        final Query query = new TagDatabaseImpl.Query(filters, List.of(), 1);
        return find(query).stream().findFirst();
    }

    default <T> @NotNull Optional<@NotNull NBTCompound> findFirst(@NotNull Tag<T> tag, @NotNull T value) {
        return findFirst(List.of(Filter.eq(tag, value)));
    }

    sealed interface Query permits TagDatabaseImpl.Query {
        @Unmodifiable
        @NotNull List<@NotNull Filter> filters();

        @Unmodifiable
        @NotNull List<@NotNull Sorter> sorters();

        int limit();

        sealed interface Builder permits TagDatabaseImpl.QueryBuilder {
            @NotNull Builder filters(@NotNull List<@NotNull Filter> filters);

            @NotNull Builder filter(@NotNull Filter filter);

            @NotNull Builder sorters(@NotNull List<@NotNull Sorter> sorters);

            @NotNull Builder sorter(@NotNull Sorter sorter);

            @NotNull Builder limit(int limit);

            @NotNull Query build();
        }
    }

    sealed interface Filter permits Filter.Eq {
        static <T> Filter eq(@NotNull Tag<T> tag, @NotNull T value) {
            return new TagDatabaseImpl.Filter.Eq<>(tag, value);
        }

        sealed interface Eq<T> extends Filter permits TagDatabaseImpl.Filter.Eq {
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
