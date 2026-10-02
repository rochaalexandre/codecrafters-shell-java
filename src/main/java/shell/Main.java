package shell;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import shell.cli.InputParser;
import shell.cli.Pipeline;
import shell.command.CommandDispatch;
import shell.command.builtin.BuiltinRegistry;
import shell.command.completer.CompleterRegistry;
import shell.command.history.HistoryManager;
import shell.command.job.JobManager;
import shell.completer.BashStyleCompleter;
import shell.completer.CompleterFactory;
import shell.completer.TerminalHistory;
import shell.env.PathResolver;
import shell.io.ExecContext;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Main {

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final JobManager JOB_MANAGER = new JobManager();
    private static final HistoryManager HISTORY_MANAGER = new HistoryManager();
    private static final CompleterRegistry COMPLETER_REGISTRY = new CompleterRegistry();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry(PATH_RESOLVER, COMPLETER_REGISTRY, JOB_MANAGER, HISTORY_MANAGER);
    private static final CommandDispatch DISPATCH = new CommandDispatch(PATH_RESOLVER, BUILTIN_REGISTRY, JOB_MANAGER);
    private static final CompleterFactory COMPLETER_FACTORY = new CompleterFactory(PATH_RESOLVER, BUILTIN_REGISTRY, COMPLETER_REGISTRY);

    public static void main(String[] args) {
        initHistory();
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
            try (ExecContext context = ExecContext.defaultContext()) {
                JOB_MANAGER.checkCompletedJobs(context);

                String input = reader.readLine("$ ");
                Pipeline pipeline = PARSER.parse(input);

                if (pipeline.isCommand(BuiltinRegistry.EXIT)) {
                    break;
                }

                DISPATCH.dispatch(pipeline, context);
                terminal.flush();
            } catch (UserInterruptException | EndOfFileException | IOError e) {
                break;
            } finally {
                saveHistory();
            }
        }
    }

    private static void initHistory() {
        String historyFile = System.getenv("HISTFILE");
        if (historyFile == null || historyFile.isEmpty()) {
            return;
        }

        try {
            HISTORY_MANAGER.load(Path.of(historyFile));
        } catch (InvalidPathException e) {
            System.err.printf("history: cannot read '%s': %s%n", historyFile, e.getMessage());
        }
    }

    private static void saveHistory() {
        String historyFile = System.getenv("HISTFILE");
        if (historyFile == null || historyFile.isEmpty()) {
            return;
        }

        try {
            HISTORY_MANAGER.save(Path.of(historyFile));
        } catch (InvalidPathException e) {
            System.err.printf("history: cannot read '%s': %s%n", historyFile, e.getMessage());
        }
    }

    private static LineReader buildReader(Terminal terminal) {
        Completer commandCompleter = COMPLETER_FACTORY.buildCommandCompleter();
        Completer dirAndFileCompleter = COMPLETER_FACTORY.buildDirAndFileCompleter();
        Completer customCommandCompleter = COMPLETER_FACTORY.buildCustomCommandCompleter();

        return LineReaderBuilder
                .builder()
                .parser(getDefaultParser())
                .history(new TerminalHistory(HISTORY_MANAGER))
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
