package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.completer.CompleterRegistry;
import shell.io.ExecContext;

import java.util.List;

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
        List<String> args = line.args();
        if (args.isEmpty()) {
            return 0;
        }
        String option = args.getFirst();
        String command = args.getLast();
        switch (option) {
            case "-p" -> executeDisplayOption(context, command);
            case "-C" -> registry.registerCompletion(command, args.get(1));
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
