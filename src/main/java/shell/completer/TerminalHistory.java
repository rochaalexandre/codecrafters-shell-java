package shell.completer;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import shell.command.history.HistoryManager;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/** Adapts command storage to JLine's in-memory history and navigation contract. */
public class TerminalHistory implements History {
    private final HistoryManager manager;
    private final Map<Integer, Instant> timestamps = new HashMap<>();
    private int cursor;

    public TerminalHistory(HistoryManager manager) {
        this.manager = Objects.requireNonNull(manager);
        moveToEnd();
    }

    @Override
    public void attach(LineReader reader) {
        // No reader configuration is needed for this in-memory implementation.
    }

    @Override
    public void load() {
        // Persistence belongs to a later stage.
    }

    @Override
    public void save() {
        // Persistence belongs to a later stage.
    }

    @Override
    public void write(Path file, boolean incremental) {
        throw new UnsupportedOperationException("History persistence is not implemented");
    }

    @Override
    public void append(Path file, boolean incremental) {
        throw new UnsupportedOperationException("History persistence is not implemented");
    }

    @Override
    public void read(Path file, boolean checkDuplicates) {
        throw new UnsupportedOperationException("History persistence is not implemented");
    }

    @Override
    public void purge() {
        manager.clear();
        timestamps.clear();
        moveToEnd();
    }

    @Override
    public int size() {
        return manager.size();
    }

    @Override
    public int index() {
        return cursor;
    }

    @Override
    public int first() {
        return 0;
    }

    @Override
    public int last() {
        return size() - 1;
    }

    @Override
    public String get(int index) {
        return manager.get(index);
    }

    @Override
    public void add(Instant time, String line) {
        Objects.requireNonNull(time);
        Objects.requireNonNull(line);
        timestamps.put(size(), time);
        manager.record(line);
        moveToEnd();
    }

    @Override
    public ListIterator<Entry> iterator(int index) {
        List<Entry> snapshot = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            snapshot.add(new TerminalEntry(i, timestamps.getOrDefault(i, Instant.EPOCH), get(i)));
        }
        return Collections.unmodifiableList(snapshot).listIterator(index);
    }

    @Override
    public String current() {
        return cursor >= size() ? "" : get(cursor);
    }

    @Override
    public boolean previous() {
        if (cursor <= first()) {
            return false;
        }
        cursor--;
        return true;
    }

    @Override
    public boolean next() {
        if (cursor >= size()) {
            return false;
        }
        cursor++;
        return true;
    }

    @Override
    public boolean moveToFirst() {
        if (isEmpty() || cursor == first()) {
            return false;
        }
        cursor = first();
        return true;
    }

    @Override
    public boolean moveToLast() {
        if (isEmpty() || cursor == last()) {
            return false;
        }
        cursor = last();
        return true;
    }

    @Override
    public boolean moveTo(int index) {
        if (index < first() || index > last()) {
            return false;
        }
        cursor = index;
        return true;
    }

    @Override
    public void moveToEnd() {
        cursor = size();
    }

    @Override
    public void resetIndex() {
        cursor = Math.min(cursor, size());
    }

    private record TerminalEntry(int index, Instant time, String line) implements Entry {}
}
