package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

import java.util.List;

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

        List<String> entries = historyManager.getEntries(limit);
        for (int i = 0; i < entries.size(); i++) {
            String h = entries.get(i);
            context.out().printf("    %s %s\n", i, h);
        }
        return 0;
    }

    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.matches("-?\\d+");
    }
}
