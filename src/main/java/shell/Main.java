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
import shell.command.CommandDispatch;
import shell.command.builtin.BuiltinRegistry;
import shell.env.PathResolver;

import java.io.IOException;
import java.util.Collection;

public class Main {

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry(PATH_RESOLVER);
    private static final CommandDispatch DISPATCH = new CommandDispatch(PATH_RESOLVER, BUILTIN_REGISTRY);

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
            replLoop(reader, terminal);

            terminal.close();
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }
    }

    private static void replLoop(LineReader reader, Terminal terminal) throws IOException {
        while (true) {
            String input = reader.readLine("$ ");
            ParsedLine line = PARSER.parse(input);

            if (line.isCommand(BuiltinRegistry.EXIT)) {
                break;
            }

            DISPATCH.dispatch(line);
            terminal.flush();
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
}
