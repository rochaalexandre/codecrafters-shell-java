package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.completer.CompleterRegistry;
import shell.io.ExecContext;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompleteBuiltin implements Builtin {
    private final CompleterRegistry registry;

    public CompleteBuiltin(CompleterRegistry completerRegistry) {
        registry = completerRegistry;
    }

    @Override
    public String name() {
        return "complete";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        if (line.args() == null) {
            return 0;
        }
        String args = line.args().strip();
        if (args.trim().startsWith("-p ")) {
            executeDisplayOption(context, args);
        } else if (args.trim().startsWith("-C ")) {
            registerCompleter(args);
        }
        return 0;
    }

    private void registerCompleter(String args) {
        String[] split = args.trim().split("\\s+");
        String command = split[split.length-1].trim();
        String path = split[1].trim();
        registry.registerCompletion(command, path);
    }

    private void executeDisplayOption(ExecContext context, String args) {
        String command = args.replace("-p", "").trim();
        if (registry.containsCompletion(command)) {
            context.out().printf("complete -C '%s' %s\n", registry.getCompletion(command), command);
        } else {
            context.out().printf("complete: %s: no completion specification\n", command);
        }
    }
}
