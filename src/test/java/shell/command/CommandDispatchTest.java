package shell.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shell.cli.ParsedLine;
import shell.cli.Pipeline;
import shell.cli.Redirect;
import shell.io.ExecContext;
import shell.command.builtin.EchoBuiltin;
import shell.command.env.VariablesManager;
import shell.command.exec.ExternalCommandRunner;
import shell.command.job.JobManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandDispatchTest {
    @TempDir Path directory;

    @Test
    void externalRunnerReceivesConfigurationWithoutDispatcherOpeningFiles() throws Exception {
        Path output = directory.resolve("output.txt");
        Files.writeString(output, "existing content");
        ParsedLine line = new ParsedLine("external", List.of(), new Redirect(output.toString(), false), null, false);
        boolean[] called = {false};
        ExternalCommandRunner runner = new ExternalCommandRunner(new VariablesManager()) {
            @Override public int run(ParsedLine received) {
                called[0] = true;
                assertSame(line, received);
                assertDoesNotThrow(() -> assertEquals("existing content", Files.readString(output)));
                return 0;
            }
        };
        dispatcher(new Resolved.ExternalCommand(), runner).dispatch(Pipeline.of(line), ExecContext.defaultContext());
        assertTrue(called[0]);
    }

    @Test
    void builtinWritesToConfiguredFileAndClosesIt() throws Exception {
        Path output = directory.resolve("output.txt");
        ParsedLine line = new ParsedLine("echo", List.of("hello"), new Redirect(output.toString(), false), null, false);
        dispatcher(new Resolved.BuiltinCommand(new EchoBuiltin()), new ExternalCommandRunner(new VariablesManager()))
                .dispatch(Pipeline.of(line), ExecContext.defaultContext());
        assertEquals("hello" + System.lineSeparator(), Files.readString(output));
    }

    private CommandDispatch dispatcher(Resolved result, ExternalCommandRunner runner) {
        CommandResolver resolver = new CommandResolver(null, null) {
            @Override public Resolved resolve(ParsedLine line) { return result; }
        };
        return new CommandDispatch(resolver, runner, null, new JobManager());
    }
}
