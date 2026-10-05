package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.env.VariablesManager;
import shell.io.ExecContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeclareBuiltin implements Builtin {
    public static final Pattern PATTERN = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)=(.*)");
    private final VariablesManager variablesManager;

    public DeclareBuiltin(VariablesManager variablesManager) {
        this.variablesManager = variablesManager;
    }

    @Override
    public String name() {
        return "declare";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        if (!line.args().isEmpty() && line.args().getFirst().equals("-p")) {
            if (line.args().size() != 2) {
                context.err().println("declare: -p requires a variable name");
                return 1;
            }
            return printEnvVariable(line, context);
        } else {
            storeEnvVariable(line, context);
        }
        return 0;
    }

    private void storeEnvVariable(ParsedLine line, ExecContext context) {
        Matcher matcher = PATTERN.matcher(line.args().getFirst());
        if (matcher.matches()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            variablesManager.setVariable(key, value);
        } else {
            context.err().printf("declare: `%s': not a valid identifier%n", line.args().getFirst());
        }
    }

    private int printEnvVariable(ParsedLine line, ExecContext context) {
        String key = line.args().get(1);
        if (this.variablesManager.containsVariable(key)) {
            context.out().printf("declare -- %s=\"%s\"%n", key, this.variablesManager.getVariable(key));
        } else {
            context.out().printf("declare: %s: not found%n", key);
        }
        return 1;
    }
}
