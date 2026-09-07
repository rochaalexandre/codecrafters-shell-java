package shell.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputParserTest {

    private final InputParser parser = new InputParser();

    // --- existing behaviour: no redirect ---

    @Test
    void splitsCommandFromArgs() {
        ParsedLine line = parser.parse("echo hello world");

        assertEquals("echo", line.command());
        assertEquals("hello world", line.args());
    }

    @Test
    void commandWithNoArgs() {
        ParsedLine line = parser.parse("pwd");

        assertEquals("pwd", line.command());
        assertEquals("", line.args());
    }

    @Test
    void noRedirectLeavesStdoutTargetNull() {
        ParsedLine line = parser.parse("echo hello");

        assertNull(line.stdoutTarget());
        assertFalse(line.hasStdoutRedirect());
    }

    // --- redirection phase, stage 1: `>` / `1>` stdout to file ---

    @Test
    void parsesStdoutRedirectWithGtToken() {
        ParsedLine line = parser.parse("echo hello > output.txt");

        assertEquals("echo", line.command());
        assertEquals("hello", line.args());
        assertEquals("output.txt", line.stdoutTarget());
        assertTrue(line.hasStdoutRedirect());
    }

    @Test
    void parsesStdoutRedirectWith1GtToken() {
        ParsedLine line = parser.parse("echo hello 1> output.txt");

        assertEquals("echo", line.command());
        assertEquals("hello", line.args());
        assertEquals("output.txt", line.stdoutTarget());
    }

    @Test
    void redirectWithNoArgsBeforeIt() {
        ParsedLine line = parser.parse("ls > out.txt");

        assertEquals("ls", line.command());
        assertEquals("", line.args());
        assertEquals("out.txt", line.stdoutTarget());
    }

    @Test
    void externalCommandWithArgsAndRedirect() {
        ParsedLine line = parser.parse("ls -1 /tmp > out.txt");

        assertEquals("ls", line.command());
        assertEquals("-1 /tmp", line.args());
        assertEquals("out.txt", line.stdoutTarget());
    }
}
