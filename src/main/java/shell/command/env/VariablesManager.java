package shell.command.env;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VariablesManager {

    private final Map<String, String> variables;

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
}
