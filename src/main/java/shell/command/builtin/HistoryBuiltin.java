package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            default -> printEntries(line, context);
        };
    }

    private int readHistoryFromFile(List<String> args, ExecContext context) {
        if (args.size() != 2) {
            context.err().println("history: -r requires a filename");
            return 1;
        }

        String filePath = args.getLast();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            historyManager.record(lines);
        } catch (IOException e) {
            context.err().printf("history: cannot write '%s': %s%n", filePath, e.getMessage());
            return 1;
        }
        return 0;
    }

    private int writeHistoryToFile(List<String> args, ExecContext context) {
        if (args.size() != 2) {
            context.err().println("history: -w requires a filename");
            return 1;
        }

        var lines = historyManager.getEntries().values();
        Path filePath = Paths.get(args.getLast());
        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            context.err().printf("history: cannot write '%s': %s%n", filePath, e.getMessage());
            return 1;
        }
        return 0;
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
