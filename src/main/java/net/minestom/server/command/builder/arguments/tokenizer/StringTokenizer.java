package net.minestom.server.command.builder.arguments.tokenizer;

import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes strings on whitespace
 */
public class StringTokenizer {
    private final String string;
    private int cursor;

    public StringTokenizer(@NotNull String string) {
        this.string = string;
    }

    public String[] tokenize(boolean omitEmptyStringAtEnd) {
        List<String> output = new ArrayList<>();
        while (hasNext()) {
            output.add(readString());
        }
        if (!omitEmptyStringAtEnd && this.cursor > 0 && isWhitespace(peek(-1))) {
            output.add("");
        }
        return output.toArray(new String[0]);
    }

    private boolean isWhitespace(char c) {
        return c == StringUtils.SPACE_CHAR;
    }

    private @NotNull String readString() {
        final int start = this.cursor;
        while (this.hasNext() && !this.isWhitespace(peek())) {
            skip();
        }
        final int end = this.cursor;

        if (this.hasNext()) {
            skip(); // skip whitespace
        }

        return this.string.substring(start, end);
    }

    private boolean hasNext() {
        return this.cursor + 1 <= this.string.length();
    }

    private char peek() {
        return this.string.charAt(this.cursor);
    }

    private char peek(int offset) {
        return this.string.charAt(this.cursor + offset);
    }

    private void skip() {
        this.cursor++;
    }

}
