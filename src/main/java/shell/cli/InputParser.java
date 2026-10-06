package shell.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import shell.command.env.VariablesManager;

/**
 * Turns a raw input line into a {@link Pipeline} of {@link ParsedLine} stages.
 * The tokenizer handles quoting, escaping, and expansion; this class interprets grammar.
 */
public final class InputParser {
    private final VariablesManager variablesManager;

    /** Creates a parser with an empty variable store. */
    public InputParser() {
        this(new VariablesManager());
    }

    /** Creates a parser using the shell's variable store. */
    public InputParser(VariablesManager variablesManager) {
        this.variablesManager = Objects.requireNonNull(variablesManager);
    }

    /** Converts raw input into a pipeline. */
    public Pipeline parse(String input) {
        List<Token> tokens = new ShellTokenizer(input, variablesManager::getVariable).tokenize();
        List<ParsedLine> stages = convertTokensToStages(tokens);
        return new Pipeline(stages);
    }

    /** Splits tokens at pipes and parses each stage. */
    private List<ParsedLine> convertTokensToStages(List<Token> tokens) {
        List<ParsedLine> parsedLines = new ArrayList<>();
        List<Token> currentStage = new ArrayList<>();

        for (Token token : tokens) {
            if (token.type() == TokenType.PIPE) {
                parsedLines.add(parseStage(currentStage));
                currentStage.clear();
            } else {
                currentStage.add(token);
            }
        }
        parsedLines.add(parseStage(currentStage));
        return parsedLines;
    }

    /** Builds a command with arguments, redirects, and background status. */
    private ParsedLine parseStage(List<Token> tokens) {
        Redirect stdout = null;
        Redirect stderr = null;
        boolean background = false;
        List<String> words = new ArrayList<>();
        ListIterator<Token> remaining = tokens.listIterator();

        while (remaining.hasNext()) {
            Token token = remaining.next();
            switch (token.type()) {
                case WORD -> words.add(token.value());
                case STDOUT, STDOUT_APPEND -> stdout = readRedirect(token, remaining);
                case STDERR, STDERR_APPEND -> stderr = readRedirect(token, remaining);
                case BACKGROUND -> {
                    requireEndOfStage(remaining);
                    background = true;
                }
                case PIPE -> throw new IllegalArgumentException("Unexpected pipe in command stage");
            }
        }

        if (words.isEmpty()) {
            throw new IllegalArgumentException("Expected command");
        }
        return new ParsedLine(words.getFirst(), words.subList(1, words.size()), stdout, stderr, background);
    }

    /** Consumes the next word as the redirect target. */
    private Redirect readRedirect(Token operator, ListIterator<Token> remaining) {
        if (!remaining.hasNext()) {
            throw new IllegalArgumentException("Expected filename after " + operator.value());
        }
        Token target = remaining.next();
        if (target.type() != TokenType.WORD) {
            throw new IllegalArgumentException("Expected filename after " + operator.value());
        }
        boolean append = operator.type() == TokenType.STDOUT_APPEND
                || operator.type() == TokenType.STDERR_APPEND;
        return new Redirect(target.value(), append);
    }

    /** Rejects tokens after a background operator. */
    private void requireEndOfStage(ListIterator<Token> remaining) {
        if (remaining.hasNext()) {
            throw new IllegalArgumentException("Background operator must end the command stage");
        }
    }
}
