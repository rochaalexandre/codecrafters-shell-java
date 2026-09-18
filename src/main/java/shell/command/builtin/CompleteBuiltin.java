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
        String[] tokens = line.args().strip().split("\\s+");
        String command = tokens[tokens.length - 1];
        switch (tokens[0]) {
            case "-p" -> executeDisplayOption(context, command);
            case "-C" -> registry.registerCompletion(command, tokens[1]);
            case "-r" -> registry.removeCompletion(command);
        }
        return 0;
    }

    private void executeDisplayOption(ExecContext context, String command) {
        if (registry.containsCompletion(command)) {
            context.out().printf("complete -C '%s' %s\n", registry.getCompletion(command), command);
        }
        else {
            context.out().printf("complete: %s: no completion specification\n", command);
        }
    }
}
