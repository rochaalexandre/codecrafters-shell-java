package shell.cli;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputParserTest {

    private final InputParser parser = new InputParser();

    private ParsedLine parseSingle(String input) {
        return parser.parse(input).stages().getFirst();
    }

    @Test
    void splitsRepeatedWhitespaceWithoutEmptyArguments() {
        ParsedLine line = parseSingle("echo\t apple   orange  > out.txt");
        assertEquals("echo", line.command());
        assertEquals(List.of("apple", "orange"), line.args());
        assertEquals("out.txt", line.stdout().target());
    }

    // --- no redirect ---

    @Test
    void splitsCommandFromArgs() {
        ParsedLine line = parseSingle("echo hello world");

        assertEquals("echo", line.command());
        assertEquals(List.of("hello", "world"), line.args());
    }

    @Test
    void stripsDoubleQuotesFromArgs() {
        ParsedLine line = parseSingle("grep \"f-46\"");

        assertEquals(List.of("f-46"), line.args());
    }

    @Test
    void commandWithNoArgs() {
        ParsedLine line = parseSingle("pwd");

        assertEquals("pwd", line.command());
        assertEquals(List.of(), line.args());
    }

    @Test
    void noRedirectLeavesStreamsNull() {
        ParsedLine line = parseSingle("echo hello");

        assertNull(line.stdout());
        assertNull(line.stderr());
        assertFalse(line.hasStdoutRedirect());
        assertFalse(line.hasStderrRedirect());
    }

    // --- stdout redirect: `>` / `1>` (truncate) ---

    @Test
    void parsesStdoutRedirectWithGtToken() {
        ParsedLine line = parseSingle("echo hello > output.txt");

        assertEquals("echo", line.command());
        assertEquals(List.of("hello"), line.args());
        assertEquals("output.txt", line.stdout().target());
        assertFalse(line.stdout().append());
        assertTrue(line.hasStdoutRedirect());
    }

    @Test
    void parsesStdoutRedirectWith1GtToken() {
        ParsedLine line = parseSingle("echo hello 1> output.txt");

        assertEquals("echo", line.command());
        assertEquals(List.of("hello"), line.args());
        assertEquals("output.txt", line.stdout().target());
    }

    @Test
    void redirectWithNoArgsBeforeIt() {
        ParsedLine line = parseSingle("ls > out.txt");

        assertEquals("ls", line.command());
        assertEquals(List.of(), line.args());
        assertEquals("out.txt", line.stdout().target());
    }

    @Test
    void externalCommandWithArgsAndRedirect() {
        ParsedLine line = parseSingle("ls -1 /tmp > out.txt");

        assertEquals("ls", line.command());
        assertEquals(List.of("-1", "/tmp"), line.args());
        assertEquals("out.txt", line.stdout().target());
    }

    // --- stderr redirect: `2>` (truncate) --- bit 2, currently RED ---

    @Test
    void parsesStderrRedirect() {
        ParsedLine line = parseSingle("ls /nope 2> err.txt");

        assertEquals("ls", line.command());
        assertEquals(List.of("/nope"), line.args());
        assertEquals("err.txt", line.stderr().target());
        assertFalse(line.stderr().append());
        assertTrue(line.hasStderrRedirect());
        assertNull(line.stdout());
    }

    @Test
    void stdoutAndStderrRedirectOnSameLine() {
        ParsedLine line = parseSingle("ls /nope > out.txt 2> err.txt");

        assertEquals("ls", line.command());
        assertEquals(List.of("/nope"), line.args());
        assertEquals("out.txt", line.stdout().target());
        assertEquals("err.txt", line.stderr().target());
    }

    // --- append: `>>` / `2>>` --- later bit, currently RED ---

    @Test
    void parsesStdoutAppend() {
        ParsedLine line = parseSingle("echo hi >> log.txt");

        assertEquals("echo", line.command());
        assertEquals(List.of("hi"), line.args());
        assertEquals("log.txt", line.stdout().target());
        assertTrue(line.stdout().append());
    }

    @Test
    void parsesStderrAppend() {
        ParsedLine line = parseSingle("ls /nope 2>> log.txt");

        assertEquals("log.txt", line.stderr().target());
        assertTrue(line.stderr().append());
    }

    // --- pipelines ---

    @Test
    void singleCommandIsOneStagePipeline() {
        Pipeline pipeline = parser.parse("echo hello");

        assertEquals(1, pipeline.stages().size());
    }

    @Test
    void splitsOnPipeIntoTrimmedStages() {
        Pipeline pipeline = parser.parse("tail -f /tmp/foo/file-1 | head -n 5");

        assertEquals(2, pipeline.stages().size());
        ParsedLine first = pipeline.stages().get(0);
        ParsedLine second = pipeline.stages().get(1);
        assertEquals("tail", first.command());
        assertEquals(List.of("-f", "/tmp/foo/file-1"), first.args());
        assertEquals("head", second.command());
        assertEquals(List.of("-n", "5"), second.args());
    }
}
