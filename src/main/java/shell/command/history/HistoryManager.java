package shell.command.history;

import java.util.*;

public class HistoryManager {

    private final List<String> entries = new ArrayList<>();

    public void record(String input) {
        entries.add(input);
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
