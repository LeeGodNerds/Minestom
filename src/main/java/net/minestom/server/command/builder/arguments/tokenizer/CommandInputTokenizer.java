package net.minestom.server.command.builder.arguments.tokenizer;

import org.jetbrains.annotations.NotNull;

/**
 * Tokenizes command input into distinct "argument" tokens.
 *
 * <p>Splits on whitespace.</p>
 */
public enum CommandInputTokenizer {

    DISPATCH {
        @Override
        public @NotNull String[] tokenizeInput(@NotNull String args) {
            return new StringTokenizer(args).tokenize(true);
        }
    },
    SUGGESTION {
        @Override
        public @NotNull String[] tokenizeInput(@NotNull String args) {
            return new StringTokenizer(args).tokenize(false);
        }
    };

    public @NotNull String[] tokenizeInput(@NotNull String[] args) {
        return this.tokenizeInput(String.join(" ", args));
    }

    public abstract @NotNull String[] tokenizeInput(@NotNull String args);

}
