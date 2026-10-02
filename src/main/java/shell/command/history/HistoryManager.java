package shell.command.history;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class HistoryManager {

    private final List<String> entries = new ArrayList<>();
    private int appendPosition;

    public void load(Path filePath) {
        try {
            record(Files.readAllLines(filePath));
            appendPosition = size();
        } catch (NoSuchFileException e) {
            // A new history file starts with empty history.
        } catch (IOException e) {
            System.err.printf("history: cannot read '%s': %s%n", filePath, e.getMessage());
        }
    }

    public int read(Path filePath, PrintStream errors) {
        try {
            record(Files.readAllLines(filePath));
            return 0;
        } catch (IOException e) {
            errors.printf("history: cannot read '%s': %s%n", filePath, e.getMessage());
            return 1;
        }
    }

    public int save(Path filePath) {
        return save(filePath, System.err);
    }

    public int save(Path filePath, PrintStream errors) {
        return writeLines(filePath, entries, errors,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public int append(Path filePath, PrintStream errors) {
        int status = writeLines(filePath, entries.subList(appendPosition, size()), errors,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        if (status == 0) {
            appendPosition = size();
        }
        return status;
    }

    private static int writeLines(Path filePath, List<String> lines, PrintStream errors,
                                  StandardOpenOption... options) {
        try {
            Files.write(filePath, lines, options);
            return 0;
        } catch (IOException e) {
            errors.printf("history: cannot write '%s': %s%n", filePath, e.getMessage());
            return 1;
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
        appendPosition = 0;
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
