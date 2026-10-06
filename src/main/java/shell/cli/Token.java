package shell.cli;

/** A decoded word or an operator. Quoted operator text remains a WORD. */
public record Token(TokenType type, String value) {}
