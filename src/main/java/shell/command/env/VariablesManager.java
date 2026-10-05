package shell.command.env;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariablesManager {

    private final Map<String, String> variables;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)\\}|\\$([A-Za-z_][A-Za-z0-9_]*)");

    public VariablesManager() {
        variables = new ConcurrentHashMap<>();
    }

    public void setVariable(String variable, String completion) {
        variables.put(variable, completion);
    }

    public void removeVariable(String variable) {
        variables.remove(variable);
    }

    public boolean containsVariable(String variable) {
        return variables.containsKey(variable);
    }

    public String getVariable(String variable) {
        return variables.get(variable);
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String replaceVariables(String arg) {
        Matcher matcher = VARIABLE_PATTERN.matcher(arg);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String name = matcher.group(1) != null
                    ? matcher.group(1)
                    : matcher.group(2);

            String value = variables.getOrDefault(name, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
