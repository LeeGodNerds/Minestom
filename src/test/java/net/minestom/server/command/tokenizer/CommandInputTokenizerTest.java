package net.minestom.server.command.tokenizer;

import net.minestom.server.command.builder.arguments.tokenizer.CommandInputTokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandInputTokenizerTest {

    @Test
    public void dispatchTokenizeInput() {
        String stringToTest = "this is a spaced string";
        String[] expected = new String[] {
                "this",
                "is",
                "a",
                "spaced",
                "string"
        };

        Assertions.assertArrayEquals(expected, CommandInputTokenizer.DISPATCH.tokenizeInput(stringToTest));
    }

    @Test
    public void suggestionTokenizeInput() {
        String stringToTest = "another string to test ";
        String[] expected = new String[] {
                "another",
                "string",
                "to",
                "test",
                ""
        };

        Assertions.assertArrayEquals(expected, CommandInputTokenizer.SUGGESTION.tokenizeInput(stringToTest));
    }

}
