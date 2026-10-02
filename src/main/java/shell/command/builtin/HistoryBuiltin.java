package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class HistoryBuiltin implements Builtin {
    private final HistoryManager historyManager;

    public HistoryBuiltin(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        if (line.args() == null) {
            return 0;
        }

        String[] tokens = line.args().strip().split("\\s+");
        String file = tokens[tokens.length - 1];
        if (tokens[0].equals("-r")) {
            appHistoryFromFile(file);
        } else {
            printEntries(line, context);
        }
        return 0;
    }

    private void appHistoryFromFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            historyManager.record(lines);
        } catch (IOException ignore) {}
    }

    private void printEntries(ParsedLine line, ExecContext context) {
        Integer limit = isInteger(line.args()) ? Integer.valueOf(line.args()) : null;
        Map<Integer, String> entries = historyManager.getEntries(limit);
        entries.forEach((k, v) -> context.out().printf("    %s %s\n", k, v));
    }

    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.matches("-?\\d+");
    }
}
