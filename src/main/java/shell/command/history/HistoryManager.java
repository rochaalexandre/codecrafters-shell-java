package shell.command.history;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public class HistoryManager {

    private final List<String> entries = new ArrayList<>();

    public void load(Path filePath) {
        try {
            record(Files.readAllLines(filePath));
        } catch (NoSuchFileException e) {
            // A new history file starts with empty history.
        } catch (IOException e) {
            System.err.printf("history: cannot read '%s': %s%n", filePath, e.getMessage());
        }
    }

    public void record(String input) {
        entries.add(input);
    }

    public void record(List<String> inputs) {
        entries.addAll(inputs);
    }

    public int size() {
        return entries.size();
    }

    public String get(int index) {
        return entries.get(index);
    }

    public void clear() {
        entries.clear();
    }

    public Map<Integer, String> getEntries() {
        return getEntries(null);
    }

    public Map<Integer, String> getEntries(Integer limit) {
        int startIndex = getSkip(limit, entries.size());
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int i = startIndex; i < entries.size(); i++) {
            result.put(i + 1, entries.get(i));
        }
        return result;
    }

    private static int getSkip(Integer limit, int size) {
        if (Objects.isNull(limit)) {
            return 0;
        }
        return Math.max(0, size - limit);
    }
}
