package shell.command.exec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.env.VariablesManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ExternalCommandRunnerTest {
    @TempDir Path directory;

    @Test
    void receivesExpandedArgumentsAndPreservesTheirBoundaries() {
        VariablesManager variables = new VariablesManager();
        variables.setVariable("NAME", "expanded");
        ParsedLine line = new InputParser(variables).parse("\"my executable\" '' \"two words\" '$NAME' \"$NAME\"")
                .stages().getFirst();

        variables.setVariable("NAME", "changed after parsing");
        assertEquals(List.of("my executable", "", "two words", "$NAME", "expanded"),
                ExternalCommandRunner.getProcessBuilder(line).command());
    }

    @Test
    void startsQuotedExecutableAndPreservesItsArgumentBoundaries() throws Exception {
        Path executable = directory.resolve("my executable");
        Files.writeString(executable, "#!/bin/sh\nprintf '%s\\n' \"$#\" \"$@\"\n");
        Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
        Path output = directory.resolve("output");
        Path errors = directory.resolve("errors");
        VariablesManager variables = new VariablesManager();
        variables.setVariable("NAME", "expanded value");
        ParsedLine line = new InputParser(variables).parse("\"" + executable + "\" '' \"$NAME\" '$NAME'")
                .stages().getFirst();
        ProcessBuilder builder = ExternalCommandRunner.getProcessBuilder(line)
                .redirectOutput(output.toFile()).redirectError(errors.toFile());

        try (Process process = builder.start()) {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Executable should finish promptly");
            assertEquals(0, process.exitValue(), Files.readString(errors));
        }
        assertEquals("3\n\nexpanded value\n$NAME\n", Files.readString(output));
    }
}
