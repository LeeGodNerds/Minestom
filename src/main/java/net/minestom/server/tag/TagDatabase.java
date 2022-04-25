package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Optional;

@ApiStatus.Experimental
public interface TagDatabase {
    SelectQuery<NBTCompound> SELECT_ALL = select().build();

    void insert(@NotNull TagHandler... handler);

    int execute(@NotNull UpdateQuery query);

    int execute(@NotNull DeleteQuery query);

    <T> List<T> execute(@NotNull SelectQuery<T> query);

    default void update(@NotNull Condition condition, @NotNull TagHandler handler) {
        final int count = execute(delete().where(condition).build());
        for (int i = 0; i < count; i++) {
            insert(handler);
        }
    }

    default <T> void updateSingle(@NotNull Tag<T> tag, @NotNull T value, @NotNull TagHandler handler) {
        var delete = delete().where(Condition.eq(tag, value)).build();
        execute(delete);
        insert(handler);
    }

    default <T> @NotNull Optional<@NotNull NBTCompound> findFirst(@NotNull Tag<T> tag, @NotNull T value) {
        final SelectQuery<NBTCompound> query = select().where(Condition.eq(tag, value)).limit(1).build();
        final List<NBTCompound> res = execute(query);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    static <T> @NotNull SelectBuilder<T> select(@NotNull Tag<T> tag) {
        return new TagDatabaseImpl.SelectBuilder<>(tag);
    }

    static @NotNull SelectBuilder<NBTCompound> select() {
        return select(Tag.View(TagSerializer.COMPOUND));
    }

    static @NotNull UpdateBuilder update() {
        return new TagDatabaseImpl.UpdateBuilder();
    }

    static @NotNull DeleteBuilder delete() {
        return new TagDatabaseImpl.DeleteBuilder();
    }

    interface SelectQuery<T> {
        @NotNull Tag<T> selector();

        @NotNull Condition condition();

        @Unmodifiable
        @NotNull List<@NotNull Sorter> sorters();

        int limit();
    }

    interface SelectBuilder<T> {
        @NotNull SelectBuilder<T> where(@NotNull Condition condition);

        @NotNull SelectBuilder<T> orderByAsc(@NotNull Tag<?> tag);

        @NotNull SelectBuilder<T> orderByDesc(@NotNull Tag<?> tag);

        @NotNull SelectBuilder<T> limit(int limit);

        @NotNull SelectQuery<T> build();
    }

    interface UpdateQuery {
        @NotNull Condition condition();
    }

    interface UpdateBuilder {
        @NotNull UpdateBuilder where(@NotNull Condition condition);

        <T> @NotNull UpdateBuilder set(@NotNull Tag<T> tag, @NotNull T value);

        @NotNull UpdateQuery build();
    }

    interface DeleteQuery {
        @NotNull Condition condition();
    }

    interface DeleteBuilder {
        @NotNull DeleteBuilder where(@NotNull Condition condition);

        @NotNull DeleteQuery build();
    }

    sealed interface Condition permits Condition.Eq {
        static <T> Condition eq(@NotNull Tag<T> tag, @NotNull T value) {
            return new TagDatabaseImpl.ConditionEq<>(tag, value);
        }

        sealed interface Eq<T> extends Condition permits TagDatabaseImpl.ConditionEq {
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
