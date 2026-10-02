package shell.completer;

import org.junit.jupiter.api.Test;
import shell.command.history.HistoryManager;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TerminalHistoryTest {
    @Test
    void navigatesFromPromptThroughCommandsAndBack() {
        HistoryManager manager = new HistoryManager();
        TerminalHistory history = new TerminalHistory(manager);
        assertFalse(history.previous());
        assertFalse(history.next());
        assertFalse(history.moveToFirst());
        assertFalse(history.moveToLast());

        history.add("echo first");
        history.add("echo second");
        assertEquals(2, manager.size());
        assertEquals("", history.current());
        assertTrue(history.previous());
        assertEquals("echo second", history.current());
        assertTrue(history.previous());
        assertEquals("echo first", history.current());
        assertFalse(history.previous());
        assertTrue(history.next());
        assertTrue(history.next());
        assertEquals("", history.current());
        assertFalse(history.next());
        assertFalse(history.moveTo(-1));
        assertFalse(history.moveTo(2));
        assertTrue(history.moveToLast());
        assertTrue(history.moveToFirst());
    }

    @Test
    void retainsDuplicatesAndExposesTimestampedReadOnlyEntries() {
        TerminalHistory history = new TerminalHistory(new HistoryManager());
        Instant time = Instant.parse("2026-10-02T00:00:00Z");
        history.add(time, "pwd");
        history.add(time, "pwd");
        var iterator = history.iterator(1);
        var entry = iterator.previous();
        assertEquals(0, entry.index());
        assertEquals(time, entry.time());
        assertEquals("pwd", entry.line());
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertEquals(1, history.reverseIterator().next().index());
        history.purge();
        assertEquals(0, history.size());
        assertEquals(0, history.index());
        assertEquals("", history.current());
    }
}
