package shell;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.AggregateCompleter;
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
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()){
            // Create a line reader
            LineReader reader = buildReader(terminal);
            replLoop(reader, terminal);
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

    private static LineReader buildReader(Terminal terminal) {
        Completer aggregateCompleter = new AggregateCompleter(getBuiltinCompleter(), getExtermaCompleter());

        return LineReaderBuilder
                .builder()
                .parser(getDefaultParser())
                .completer(aggregateCompleter)
                .terminal(terminal).build();
    }

    private static DefaultParser getDefaultParser() {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(null);
        return parser;
    }

    private static Completer getBuiltinCompleter() {
        Collection<String> dynamicStrings = Main.BUILTIN_REGISTRY.listAvailableCommands();
        return new StringsCompleter(dynamicStrings);
    }

    private static Completer getExtermaCompleter() {
        // Complete with dynamic strings
        Collection<String> dynamicStrings = Main.PATH_RESOLVER.listAvailableCommands();
        return new StringsCompleter(dynamicStrings);
    }
}
