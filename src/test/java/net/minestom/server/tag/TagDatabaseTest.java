package net.minestom.server.tag;

import net.minestom.server.api.RandomUtils;
import net.minestom.server.tag.TagDatabase.Condition;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagDatabaseTest {

    @Test
    public void insert() {
        TagDatabase db = createDB();
        var compound = NBT.Compound(Map.of("key", NBT.Int(1)));
        db.insert(TagHandler.fromCompound(compound));
    }

    @Test
    public void insertNested() {
        TagDatabase db = createDB();
        var compound = NBT.Compound(Map.of("key",
                NBT.Compound(Map.of("value", NBT.Int(1)))));
        db.insert(TagHandler.fromCompound(compound));
    }

    @Test
    public void empty() {
        TagDatabase db = createDB();
        var select = TagDatabase.select().where(Condition.eq(Tag.String("key"), "value")).build();
        var result = db.execute(select);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findFilterEq() {
        TagDatabase db = createDB();
        var tag = Tag.String("key");
        var compound1 = NBT.Compound(Map.of("key", NBT.String("value"),
                "other", NBT.String("otherValue")));
        var compound2 = NBT.Compound(Map.of("key", NBT.String("value2"),
                "other", NBT.String("otherValue")));

        db.updateSingle(tag, "value", TagHandler.fromCompound(compound1));
        db.updateSingle(tag, "value2", TagHandler.fromCompound(compound2));

        var query = TagDatabase.select()
                .where(Condition.eq(tag, "value")).build();
        assertListEqualsIgnoreOrder(List.of(compound1), db.execute(query));
    }

    @Test
    public void findFilterCompoundEq() {
        TagDatabase db = createDB();
        var child = NBT.Compound(Map.of("something", NBT.String("something")));
        var compound = NBT.Compound(Map.of("key", NBT.String("value2"),
                "other", child));

        db.insert(TagHandler.fromCompound(compound));

        var query = TagDatabase.select()
                .where(Condition.eq(Tag.NBT("other"), child)).build();
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(query));
    }

    @Test
    public void findTagMismatch() {
        TagDatabase db = createDB();
        var tagInteger = Tag.Integer("key");
        var tagDouble = Tag.Double("key");
        var compound1 = NBT.Compound(Map.of("key", NBT.Int(1)));
        var compound2 = NBT.Compound(Map.of("key", NBT.Double(1)));

        db.insert(TagHandler.fromCompound(compound1), TagHandler.fromCompound(compound2));

        var queryInteger = TagDatabase.select()
                .where(Condition.eq(tagInteger, 1)).build();
        assertListEqualsIgnoreOrder(List.of(compound1), db.execute(queryInteger));

        var queryDouble = TagDatabase.select()
                .where(Condition.eq(tagDouble, 1D)).build();
        assertListEqualsIgnoreOrder(List.of(compound2), db.execute(queryDouble));
    }

    @Test
    public void findArray() {
        TagDatabase db = createDB();
        var tag = Tag.NBT("key");
        var nbt = NBT.IntArray(1, 2, 3);
        var compound = NBT.Compound(Map.of("key", nbt));

        db.insert(TagHandler.fromCompound(compound));

        var query = TagDatabase.select()
                .where(Condition.eq(tag, nbt)).build();
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(query));
    }

    @Test
    public void handlerCopy() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("key");

        var handler = TagHandler.newHandler();
        handler.setTag(tag, 5);
        var compound = handler.asCompound();
        // Must call TagHandler#copy to avoid side effects and invalidate the potential cache
        db.insert(handler);

        handler.setTag(tag, 1);
        assertListEqualsIgnoreOrder(List.of(), db.execute(TagDatabase.select()
                .where(Condition.eq(tag, 1)).build()));
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag, 5)).build()));
    }

    @Test
    public void findMultiEq() {
        TagDatabase db = createDB();
        var tag = Tag.String("key");
        var compound1 = NBT.Compound(Map.of("key", NBT.String("value"),
                "other", NBT.String("otherValue")));
        var compound2 = NBT.Compound(Map.of("key", NBT.String("value2"),
                "other", NBT.String("otherValue")));

        db.updateSingle(tag, "value", TagHandler.fromCompound(compound1));
        db.updateSingle(tag, "value2", TagHandler.fromCompound(compound2));

        var query = TagDatabase.select()
                .where(Condition.eq(Tag.String("other"), "otherValue")).build();
        assertListEqualsIgnoreOrder(List.of(compound1, compound2), db.execute(query));
    }

    @Test
    public void findMultiTreeEq() {
        TagDatabase db = createDB();
        var tag = Tag.String("key");
        var compound1 = NBT.Compound(Map.of("key", NBT.String("value"),
                "path", NBT.Compound(Map.of("other", NBT.String("otherValue")))));
        var compound2 = NBT.Compound(Map.of("key", NBT.String("value2"),
                "path", NBT.Compound(Map.of("other", NBT.String("otherValue")))));

        db.updateSingle(tag, "value", TagHandler.fromCompound(compound1));
        db.updateSingle(tag, "value2", TagHandler.fromCompound(compound2));

        var query = TagDatabase.select()
                .where(Condition.eq(Tag.String("other").path("path"), "otherValue")).build();
        assertListEqualsIgnoreOrder(List.of(compound1, compound2), db.execute(query));
    }

    @Test
    public void findNestedTag() {
        TagDatabase db = createDB();
        var handler = TagHandler.newHandler();

        var tag = Tag.String("key");
        var tag2 = Tag.String("key2").path("path");
        var tag3 = Tag.String("key3").path("path", "path2");
        var tag4 = Tag.String("key4").path("path", "path2");
        var tag5 = Tag.String("key4").path("path", "path2", "path3", "path4", "path5");

        handler.setTag(tag, "value");
        handler.setTag(tag2, "value2");
        handler.setTag(tag3, "value3");
        handler.setTag(tag4, "value4");
        handler.setTag(tag5, "value5");

        var compound = handler.asCompound();

        db.insert(handler);

        // Check query based on nested tag
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag, "value")).build()));
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag2, "value2")).build()));
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag3, "value3")).build()));
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag4, "value4")).build()));
        assertListEqualsIgnoreOrder(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(tag5, "value5")).build()));
    }

    @Test
    public void findFirst() {
        TagDatabase db = createDB();
        var tag = Tag.String("key");
        var compound = NBT.Compound(Map.of("key", NBT.String("value"),
                "key2", NBT.String("value2")));

        db.updateSingle(tag, "value", TagHandler.fromCompound(compound));

        var result = db.findFirst(tag, "value");
        assertEquals(compound, result.get());
    }

    @Test
    public void replaceConstant() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));

        db.insert(TagHandler.fromCompound(compound));
        db.execute(TagDatabase.update().set(tag, 10).build());

        var result = db.execute(TagDatabase.SELECT_ALL);
        assertEquals(1, result.size());
        assertEquals(NBT.Compound(Map.of("number", NBT.Int(10))), result.get(0));
    }

    @Test
    public void replaceNull() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));

        db.insert(TagHandler.fromCompound(compound));
        db.execute(TagDatabase.update().set(tag, null).build());
        // Empty handlers must be removed
        var result = db.execute(TagDatabase.SELECT_ALL);
        assertTrue(result.isEmpty());
    }

    // FIXME
    //@Test
    public void replaceOperator() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));

        db.insert(TagHandler.fromCompound(compound));
        //db.replace(TagDatabase.QUERY_ALL, tag, integer -> integer * 2);

        var result = db.execute(TagDatabase.SELECT_ALL);
        assertEquals(1, result.size());
        assertEquals(NBT.Compound(Map.of("number", NBT.Int(10))), result.get(0));
    }

    @Test
    public void delete() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));
        var condition = Condition.eq(tag, 5);
        var query = TagDatabase.delete().where(condition).build();

        db.insert(TagHandler.fromCompound(compound));
        db.execute(query);

        var result = db.execute(TagDatabase.select().where(condition).build());
        assertTrue(result.isEmpty());
    }

    @Test
    public void intSort() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number");
        var compound1 = NBT.Compound(Map.of("number", NBT.Int(1)));
        var compound2 = NBT.Compound(Map.of("number", NBT.Int(2)));
        var compound3 = NBT.Compound(Map.of("number", NBT.Int(3)));
        db.insert(TagHandler.fromCompound(compound2), TagHandler.fromCompound(compound3), TagHandler.fromCompound(compound1));

        var ascending = TagDatabase.select().orderByAsc(tag).build();
        assertEquals(List.of(compound1, compound2, compound3), db.execute(ascending));

        var descending = TagDatabase.select().orderByDesc(tag).build();
        assertEquals(List.of(compound3, compound2, compound1), db.execute(descending));
    }

    @Test
    public void nestedSort() {
        TagDatabase db = createDB();
        var tag = Tag.Integer("number").path("path", "path2");

        var handler = TagHandler.newHandler();
        var handler2 = TagHandler.newHandler();
        var handler3 = TagHandler.newHandler();
        var handler4 = TagHandler.newHandler();

        handler.setTag(tag, 1);
        handler2.setTag(tag, 2);
        handler3.setTag(tag, 3);
        handler4.setTag(tag, 4);

        db.insert(handler, handler2, handler3, handler4);

        var compound = handler.asCompound();
        var compound2 = handler2.asCompound();
        var compound3 = handler3.asCompound();
        var compound4 = handler4.asCompound();

        var ascending = TagDatabase.select().orderByAsc(tag).build();
        assertEquals(List.of(compound, compound2, compound3, compound4), db.execute(ascending));

        var descending = TagDatabase.select().orderByDesc(tag).build();
        assertEquals(List.of(compound4, compound3, compound2, compound), db.execute(descending));
    }

    @Test
    public void tableResize() {
        TagDatabase db = createDB();

        var handler = TagHandler.newHandler();
        handler.setTag(Tag.Integer("number"), 1);

        db.insert(handler);

        var condition = Condition.eq(Tag.Integer("number"), 1);

        var selectQuery = TagDatabase.select().where(condition).build();

        var compound = handler.asCompound();
        assertEquals(List.of(compound), db.execute(selectQuery));

        handler.setTag(Tag.Integer("number2"), 2);
        db.update(condition, handler);
        compound = handler.asCompound();
        assertEquals(List.of(compound), db.execute(selectQuery));
        assertEquals(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(Tag.Integer("number2"), 2)).build()));

        handler.setTag(Tag.String("string"), "value");
        db.update(condition, handler);
        compound = handler.asCompound();
        assertEquals(List.of(compound), db.execute(selectQuery));
        assertEquals(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(Tag.Integer("number2"), 2)).build()));
        assertEquals(List.of(compound), db.execute(TagDatabase.select()
                .where(Condition.eq(Tag.String("string"), "value")).build()));
    }

    @Test
    public void tableDownsize() {
        TagDatabase db = createDB();

        var tag = Tag.Integer("number");
        var condition = Condition.eq(tag, 1);
        var selectQuery = TagDatabase.select().where(condition).build();

        var handler = TagHandler.newHandler();
        handler.setTag(tag, 1);

        db.insert(handler);
        assertEquals(List.of(handler.asCompound()), db.execute(selectQuery));

        handler.removeTag(tag);
        db.update(condition, handler);
        assertEquals(List.of(), db.execute(selectQuery));
    }

    @Test
    public void complexCompound() {
        NBTCompound compound = RandomUtils.randomizeCompound(100);
        TagDatabase db = createDB();
        db.insert(TagHandler.fromCompound(compound));
        var result = db.execute(TagDatabase.SELECT_ALL);
        assertEquals(1, result.size());
        assertEquals(compound, result.get(0));
    }

    @Test
    public void singleSelector() {
        TagDatabase db = createDB();

        Tag<Integer> tag = Tag.Integer("key");
        TagDatabase.SelectQuery<Integer> basicQuery = TagDatabase.select(tag)
                .where(Condition.eq(tag, 5)).build();

        NBTCompound compound = NBT.Compound(Map.of("key", NBT.Int(5)));

        db.insert(TagHandler.fromCompound(compound));

        Integer result = db.execute(basicQuery).get(0);
        assertEquals(5, result);
    }

    @Test
    public void childNestedSelector() {
        TagDatabase db = createDB();

        Tag<Integer> tag = Tag.Integer("key");
        Tag<Integer> tag2 = Tag.Integer("key").path("child");
        TagDatabase.SelectQuery<Integer> basicQuery = TagDatabase.select(tag)
                .where(Condition.eq(tag2, 2)).build();

        var handler = TagHandler.newHandler();
        handler.setTag(tag, 1);
        handler.setTag(tag2, 2);

        db.insert(handler);

        Integer result = db.execute(basicQuery).get(0);
        assertEquals(1, result);
    }

    public static void assertListEqualsIgnoreOrder(List<?> expected, List<?> actual) {
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
    }

    private TagDatabase createDB() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
