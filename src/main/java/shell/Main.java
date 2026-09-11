package shell;

import org.jline.builtins.Completers;
import org.jline.reader.*;
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

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Paths;
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
            try {
                String input = reader.readLine("$ ");
                ParsedLine line = PARSER.parse(input);

                if (line.isCommand(BuiltinRegistry.EXIT)) {
                    break;
                }

                DISPATCH.dispatch(line);
                terminal.flush();
            } catch (UserInterruptException | EndOfFileException | IOError e) {
                break;
            }
        }
    }

    private static LineReader buildReader(Terminal terminal) {
        Completer commandCompleter = buildCommandCompleter();
        Completer dirAndFileCompleter = buildDirAndFileCompleter();

        return LineReaderBuilder
                .builder()
                .parser(getDefaultParser())
                .completer(new BashStyleCompleter(commandCompleter, dirAndFileCompleter))
                .terminal(terminal).build();
    }


    private static DefaultParser getDefaultParser() {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(null);
        return parser;
    }
    private static AggregateCompleter buildCommandCompleter() {
        return new AggregateCompleter(getBuiltinCompleter(), getExternalCommandCompleter());
    }

    private static AggregateCompleter buildDirAndFileCompleter() {
        return new AggregateCompleter(getFileCompleter(), getDirectoryCompleter());
    }

    private static Completer getBuiltinCompleter() {
        Collection<String> dynamicStrings = Main.BUILTIN_REGISTRY.listAvailableCommands();
        return new StringsCompleter(dynamicStrings);
    }

    private static Completer getExternalCommandCompleter() {
        // Complete with dynamic strings
        Collection<String> dynamicStrings = Main.PATH_RESOLVER.listAvailableCommands();
        return new StringsCompleter(dynamicStrings);
    }

    private static Completer getFileCompleter() {
        return new Completers.FilesCompleter(Paths.get("."));
    }

    private static Completer getDirectoryCompleter() {
        return new Completers.DirectoriesCompleter(Paths.get("."));
    }

}
