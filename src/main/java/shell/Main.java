package shell;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.CommandDispatch;
import shell.command.builtin.BuiltinRegistry;
import shell.command.completer.CompleterRegistry;
import shell.command.job.JobRegistry;
import shell.completer.BashStyleCompleter;
import shell.completer.CompleterFactory;
import shell.env.PathResolver;

import java.io.IOError;
import java.io.IOException;

public class Main {

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final JobRegistry JOB_REGISTRY = new JobRegistry();
    private static final CompleterRegistry COMPLETER_REGISTRY = new CompleterRegistry();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry(PATH_RESOLVER, COMPLETER_REGISTRY, JOB_REGISTRY);
    private static final CommandDispatch DISPATCH = new CommandDispatch(PATH_RESOLVER, BUILTIN_REGISTRY, JOB_REGISTRY);
    private static final CompleterFactory COMPLETER_FACTORY = new CompleterFactory(PATH_RESOLVER, BUILTIN_REGISTRY, COMPLETER_REGISTRY);

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
        Completer commandCompleter = COMPLETER_FACTORY.buildCommandCompleter();
        Completer dirAndFileCompleter = COMPLETER_FACTORY.buildDirAndFileCompleter();
        Completer customCommandCompleter = COMPLETER_FACTORY.buildCustomCommandCompleter();

        return LineReaderBuilder
                .builder()
                .parser(getDefaultParser())
                .completer(new BashStyleCompleter(commandCompleter, dirAndFileCompleter, customCommandCompleter))
                .option(LineReader.Option.AUTO_REMOVE_SLASH,  false)
                .terminal(terminal).build();
    }

    private static DefaultParser getDefaultParser() {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(null);
        return parser;
    }
}
