package shell.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Scans one input, decoding words and recognizing unquoted, unescaped operators.
 * Create a new tokenizer for each input. Expands variables without field splitting.
 */
public final class ShellTokenizer {
    private enum QuoteState { UNQUOTED, SINGLE_QUOTED, DOUBLE_QUOTED }

    private final String input;
    private final Function<String, String> variableLookup;
    private final List<Token> tokens = new ArrayList<>();
    private final StringBuilder word = new StringBuilder();

    private int index;
    private QuoteState quoteState = QuoteState.UNQUOTED;
    private boolean wordStarted;
    private boolean plainWord = true;

    /** Creates a tokenizer for one input. */
    public ShellTokenizer(String input) {
        this(input, name -> "");
    }

    /** Creates a tokenizer using the supplied variable lookup. */
    public ShellTokenizer(String input, Function<String, String> variableLookup) {
        this.input = Objects.requireNonNull(input);
        this.variableLookup = Objects.requireNonNull(variableLookup);
    }

    /** Decodes tokens, rejecting unmatched quotes and trailing escapes. */
    public List<Token> tokenize() {
        while (hasNext()) {
            switch (quoteState) {
                case UNQUOTED -> readUnquoted();
                case SINGLE_QUOTED -> readSingleQuoted();
                case DOUBLE_QUOTED -> readDoubleQuoted();
            }
        }

        requireClosedQuotes();
        finishWord();
        return List.copyOf(tokens);
    }

    /** Reads the next character outside quotes. */
    private void readUnquoted() {
        char character = take();
        switch (character) {
            case '\'' -> openQuote(QuoteState.SINGLE_QUOTED);
            case '"' -> openQuote(QuoteState.DOUBLE_QUOTED);
            case '\\' -> readUnquotedEscape();
            case '$' -> readVariable();
            case '|' -> emitOperator(TokenType.PIPE, "|");
            case '&' -> emitOperator(TokenType.BACKGROUND, "&");
            case '>' -> readRedirect();
            default -> readUnquotedText(character);
        }
    }

    /** Appends text or finishes the word at whitespace. */
    private void readUnquotedText(char character) {
        if (Character.isWhitespace(character)) {
            finishWord();
        } else {
            appendToWord(character);
        }
    }

    /** Reads literal text until the closing single quote. */
    private void readSingleQuoted() {
        char character = take();
        if (character == '\'') {
            quoteState = QuoteState.UNQUOTED;
        } else {
            appendToWord(character);
        }
    }

    /** Reads text, escapes, or the closing double quote. */
    private void readDoubleQuoted() {
        char character = take();
        switch (character) {
            case '"' -> quoteState = QuoteState.UNQUOTED;
            case '\\' -> readDoubleQuotedEscape();
            case '$' -> readVariable();
            default -> appendToWord(character);
        }
    }

    /** Enters quote mode and starts a word. */
    private void openQuote(QuoteState state) {
        quoteState = state;
        wordStarted = true;
        plainWord = false;
    }

    /** Expands a variable after its dollar sign has been consumed. */
    private void readVariable() {
        if (!hasNext() || (peek() != '{' && !isVariableNameStart(peek()))) {
            appendToWord('$');
            return;
        }
        String name = peek() == '{' ? readBracedVariableName() : readVariableName();
        String value = Objects.requireNonNullElse(variableLookup.apply(name), "");
        word.append(value);
        wordStarted = wordStarted || !value.isEmpty();
        plainWord = false;
    }

    /** Reads a variable name enclosed in braces. */
    private String readBracedVariableName() {
        take(); // Opening brace.
        if (!hasNext() || !isVariableNameStart(peek())) {
            throw new IllegalArgumentException("Expected variable name after ${");
        }
        String name = readVariableName();
        if (!hasNext() || take() != '}') {
            throw new IllegalArgumentException("Expected } after variable name");
        }
        return name;
    }

    /** Consumes letters, digits, and underscores in a variable name. */
    private String readVariableName() {
        StringBuilder name = new StringBuilder();
        while (hasNext() && (isVariableNameStart(peek()) || isVariableNameDigit(peek()))) {
            name.append(take());
        }
        return name.toString();
    }

    /** Checks for an ASCII letter or underscore. */
    private boolean isVariableNameStart(char character) {
        return character == '_' || (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z');
    }

    /** Checks for an ASCII digit. */
    private boolean isVariableNameDigit(char character) {
        return character >= '0' && character <= '9';
    }

    /** Consumes the character following an unquoted backslash. */
    private void readUnquotedEscape() {
        requireEscapedCharacter();
        appendEscapedCharacter(take());
    }

    /** Decodes supported escapes; otherwise preserves the backslash. */
    private void readDoubleQuotedEscape() {
        requireEscapedCharacter();
        if (isDoubleQuoteEscape(peek())) {
            appendEscapedCharacter(take());
        } else {
            // The next character is still unread; the backslash is literal.
            appendToWord('\\');
        }
    }

    /** Checks whether a backslash escapes this character inside double quotes. */
    private boolean isDoubleQuoteEscape(char character) {
        return switch (character) {
            case '"', '\\', '$', '`', '\n' -> true;
            default -> false;
        };
    }

    /** Appends escaped text, discarding escaped newlines. */
    private void appendEscapedCharacter(char character) {
        if (character != '\n') {
            appendToWord(character);
            plainWord = false;
        }
    }

    /** Emits a redirect after its first '>' has been consumed. */
    private void readRedirect() {
        String descriptor = takeRedirectDescriptor();
        boolean append = hasNext() && peek() == '>';
        if (append) {
            take();
        }

        TokenType type = redirectType(descriptor, append);
        emitOperator(type, descriptor + (append ? ">>" : ">"));
    }

    /** Removes a plain 1 or 2 from the word, or returns an empty descriptor. */
    private String takeRedirectDescriptor() {
        // Quoted/escaped digits remain arguments, even if they decode to 1 or 2.
        if (plainWord && word.length() == 1
                && (word.charAt(0) == '1' || word.charAt(0) == '2')) {
            String descriptor = word.toString();
            resetWord();
            return descriptor;
        }
        return "";
    }

    /** Selects the redirect stream and append mode. */
    private TokenType redirectType(String descriptor, boolean append) {
        if (descriptor.equals("2")) {
            return append ? TokenType.STDERR_APPEND : TokenType.STDERR;
        }
        return append ? TokenType.STDOUT_APPEND : TokenType.STDOUT;
    }

    /** Finishes the current word and adds an operator. */
    private void emitOperator(TokenType type, String value) {
        finishWord();
        tokens.add(new Token(type, value));
    }

    /** Appends a character and marks the word as started. */
    private void appendToWord(char character) {
        word.append(character);
        wordStarted = true;
    }

    /** Emits a started word, including an empty quoted word. */
    private void finishWord() {
        // Empty quotes start a word even when its buffer is empty.
        if (wordStarted) {
            tokens.add(new Token(TokenType.WORD, word.toString()));
        }
        resetWord();
    }

    /** Clears the word buffer and its flags. */
    private void resetWord() {
        word.setLength(0);
        wordStarted = false;
        plainWord = true;
    }

    /** Rejects an unclosed quote. */
    private void requireClosedQuotes() {
        if (quoteState != QuoteState.UNQUOTED) {
            throw new IllegalArgumentException("Unmatched "
                    + (quoteState == QuoteState.SINGLE_QUOTED ? "single" : "double") + " quote");
        }
    }

    /** Rejects a backslash with no following character. */
    private void requireEscapedCharacter() {
        if (!hasNext()) {
            throw new IllegalArgumentException("Trailing backslash");
        }
    }

    /** Checks for unread characters. */
    private boolean hasNext() {
        return index < input.length();
    }

    /** Returns the next character without consuming it. */
    private char peek() {
        return input.charAt(index);
    }

    /** Returns the next character and advances the cursor. */
    private char take() {
        return input.charAt(index++);
    }
}
