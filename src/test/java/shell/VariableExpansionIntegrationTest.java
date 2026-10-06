package shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class VariableExpansionIntegrationTest {
    @TempDir Path directory;

    @Test
    void omitsMissingVariablesAndPreservesSuffixThroughTheRunningShell() throws Exception {
        Path executable = directory.resolve("custom_exe_7056");
        Files.writeString(executable, """
                #!/bin/sh
                printf 'Program was passed %s args (including program name).\\n' "$(( $# + 1 ))"
                printf 'Arg: <%s>\\n' "$@"
                """);
        Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
        Path output = directory.resolve("output");
        ProcessBuilder builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "--enable-native-access=ALL-UNNAMED", "--enable-preview",
                "-cp", System.getProperty("java.class.path"), Main.class.getName())
                .redirectErrorStream(true).redirectOutput(output.toFile());
        builder.environment().put("PATH", directory + ":" + System.getenv("PATH"));
        builder.environment().put("HISTFILE", directory.resolve("history").toString());

        Process process = builder.start();
        try {
            try (var input = process.getOutputStream()) {
                input.write("""
                        declare pear=raspberry
                        custom_exe_7056 ${missing_var_2}_suffix ${pear} ${missing_var_5} $missing_var_6
                        exit
                        """.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Shell should exit promptly");
            String transcript = Files.readString(output);
            assertEquals(0, process.exitValue(), transcript);
            assertTrue(transcript.contains("Program was passed 3 args (including program name)."), transcript);
            assertTrue(transcript.contains("Arg: <_suffix>"), transcript);
            assertTrue(transcript.contains("Arg: <raspberry>"), transcript);
            assertFalse(transcript.contains("Arg: <>"), transcript);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
