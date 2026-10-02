package shell.command.history;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HistoryManager {

    private final List<String> entries = new ArrayList<>();

    public void record(String input) {
        entries.add(input);
    }

    public List<String> getEntries(Integer limit) {
        Stream<String> entriesStream = entries.stream();
        if (limit != null) {
            return entriesStream.skip(Math.max(0, entries.size() - limit)).toList();
        }
        return entriesStream.toList();
    }
}
