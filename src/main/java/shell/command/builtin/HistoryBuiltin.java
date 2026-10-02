package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.history.HistoryManager;
import shell.io.ExecContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HistoryBuiltin implements Builtin {
    private final HistoryManager historyManager;
    private int appendPosition = 0;

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

        String filePath = args.getLast();
        List<String> lines = readFileLines(context, filePath);
        historyManager.record(lines);
        return 0;
    }


    private int writeHistoryToFile(List<String> args, ExecContext context) {
        if (isMissingFileName(args, "-w", context)) {
            return 1;
        }

        return writeLines(Paths.get(args.getLast()), historyManager.getEntries().values(), context,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private int appendHistoryToFile(List<String> args, ExecContext context) {
        if (isMissingFileName(args, "-a", context)) {
            return 1;
        }

        int newEntryCount = historyManager.size() - appendPosition;
        var lines = historyManager.getEntries(newEntryCount).values();
        int status = writeLines(Paths.get(args.getLast()), lines, context,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        if (status == 0) {
            appendPosition = historyManager.size();
        }
        return status;
    }

    private static boolean isMissingFileName(List<String> args, String option, ExecContext context) {
        if (args.size() != 2) {
            context.err().printf("history: %s requires a filename%n", option);
            return true;
        }
        return false;
    }

    private static int writeLines(Path filePath, Collection<String> lines, ExecContext context,
                                  StandardOpenOption... options) {
        try {
            Files.write(filePath, lines, options);
        } catch (IOException e) {
            context.err().printf("history: cannot write '%s': %s%n", filePath, e.getMessage());
            return 1;
        }
        return 0;
    }

    private static List<String> readFileLines(ExecContext context, String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            context.err().printf("history: cannot write '%s': %s%n", filePath, e.getMessage());
        }
        return lines;
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
