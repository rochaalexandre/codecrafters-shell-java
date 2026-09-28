package shell.command;

import shell.command.builtin.Builtin;

/** What {@link CommandResolver} found: exactly one of three kinds, so a caller can act on the kind. */
public sealed interface Resolved {
    record BuiltinCommand(Builtin builtin) implements Resolved {}
    record ExternalCommand() implements Resolved {}
    record NotFound() implements Resolved {}
}
