package shell;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.builtin.Builtin;
import shell.command.builtin.BuiltinRegistry;
import shell.io.ExecContext;
import shell.env.PathResolver;
import shell.command.exec.ExternalCommandRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public class Main {

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry(PATH_RESOLVER);
    private static final ExternalCommandRunner EXTERNAL_COMMAND_RUNNER = new ExternalCommandRunner();

    public static void main(String[] args) {
        try {
            // Create a terminal
            Terminal terminal = TerminalBuilder.builder().system(true).build();

            // Create a line reader
            LineReader reader = LineReaderBuilder
                    .builder()
                    .parser(getDefaultParser())
                    .completer(getBuiltinCompleter(BUILTIN_REGISTRY))
                    .terminal(terminal).build();

            while (true) {
                String input = reader.readLine("$ ");
                ParsedLine line = PARSER.parse(input);

                if (line.isCommand(BuiltinRegistry.EXIT)) {
                    break;
                }

                dispatch(line);

                terminal.flush();
            }
            terminal.close();
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }
    }

    private static DefaultParser getDefaultParser() {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(null);
        return parser;
    }

    public static Completer getBuiltinCompleter(BuiltinRegistry builtinRegistry) {
        // Complete with dynamic strings
        Collection<String> dynamicStrings = builtinRegistry.listAvailableCommands();
        return new StringsCompleter(dynamicStrings);
    }


    private static void dispatch(ParsedLine line) throws IOException {
        Optional<Builtin> builtin = BUILTIN_REGISTRY.getBuiltin(line.command());
        if (builtin.isPresent()) {
            try (ExecContext context = ExecContext.from(line)) {
                builtin.get().run(line, context);
            }
        }
        else if (PATH_RESOLVER.findExecutable(line.command()).isPresent()) {
            EXTERNAL_COMMAND_RUNNER.run(line);
        }
        else {
            System.out.println(line.command() + ": command not found");
        }
    }
}
