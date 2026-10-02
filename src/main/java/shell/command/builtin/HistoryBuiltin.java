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
        List<String> args = line.args();
        if (!args.isEmpty() && args.getFirst().equals("-r")) {
            if (args.size() != 2) {
                context.err().println("history: -r requires a filename");
                return 1;
            }
            appHistoryFromFile(args.getLast());
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
        String argument = line.args().isEmpty() ? "" : line.args().getFirst();
        Integer limit = isInteger(argument) ? Integer.valueOf(argument) : null;
        Map<Integer, String> entries = historyManager.getEntries(limit);
        entries.forEach((k, v) -> context.out().printf("    %s %s\n", k, v));
    }

    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.matches("-?\\d+");
    }
}
