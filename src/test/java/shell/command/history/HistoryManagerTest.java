package shell.command.history;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    @TempDir
    Path directory;

    @Test
    void appendsOnlyNewCommandsAfterLoadingAndAcrossRepeatedCalls() throws Exception {
        Path file = directory.resolve("history");
        Files.write(file, List.of("old"));
        HistoryManager manager = new HistoryManager();
        manager.load(file);
        manager.record("pwd");
        assertEquals(0, manager.append(file, System.err));
        manager.record("pwd");
        assertEquals(0, manager.append(file, System.err));
        assertEquals(List.of("old", "pwd", "pwd"), Files.readAllLines(file));
    }

    @Test
    void failedAppendCanBeRetriedAndSaveReplacesFileContents() throws Exception {
        HistoryManager manager = new HistoryManager();
        manager.record("echo hello");
        assertEquals(1, manager.append(directory, System.err));
        Path file = directory.resolve("history");
        assertEquals(0, manager.append(file, System.err));
        assertEquals(List.of("echo hello"), Files.readAllLines(file));
        Files.writeString(file, "stale content that is longer than the saved history\n");
        assertEquals(0, manager.save(file, System.err));
        assertEquals(List.of("echo hello"), Files.readAllLines(file));
    }
}
