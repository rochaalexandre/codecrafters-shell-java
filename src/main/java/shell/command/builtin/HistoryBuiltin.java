package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

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
        String argument = args.isEmpty() ? "" : args.getFirst();
        return switch (argument) {
            case "-r" -> readHistoryFromFile(args, context);
            case "-w" -> writeHistoryToFile(args, context);
            case "-a" -> appendHistoryToFile(args, context);
            default -> printEntries(line, context);
        };
    }

    private int readHistoryFromFile(List<String> args, ExecContext context) {
        if (args.size() != 2) {
            context.err().println("history: -r requires a filename");
            return 1;
        }

        return historyManager.read(Paths.get(args.getLast()), context.err());
    }

    private int writeHistoryToFile(List<String> args, ExecContext context) {
        if (isMissingFileName(args, "-w", context)) {
            return 1;
        }
        return historyManager.save(Paths.get(args.getLast()), context.err());
    }

    private int appendHistoryToFile(List<String> args, ExecContext context) {
        if (isMissingFileName(args, "-a", context)) {
            return 1;
        }
        return historyManager.append(Paths.get(args.getLast()), context.err());
    }

    private static boolean isMissingFileName(List<String> args, String option, ExecContext context) {
        if (args.size() != 2) {
            context.err().printf("history: %s requires a filename%n", option);
            return true;
        }
        return false;
    }

    private int printEntries(ParsedLine line, ExecContext context) {
        String argument = line.args().isEmpty() ? "" : line.args().getFirst();
        Integer limit = isInteger(argument) ? Integer.valueOf(argument) : null;
        Map<Integer, String> entries = historyManager.getEntries(limit);
        entries.forEach((k, v) -> context.out().printf("    %s %s\n", k, v));
        return 0;
    }

    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && str.matches("-?\\d+");
    }
}
