package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

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
        Integer limit = isInteger(line.args()) ? Integer.valueOf(line.args()) : null;

        Map<Integer, String> entries = historyManager.getEntries(limit);
        entries.forEach((k, v) -> context.out().printf("    %s %s\n", k, v));
        return 0;
    }

    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.matches("-?\\d+");
    }
}
