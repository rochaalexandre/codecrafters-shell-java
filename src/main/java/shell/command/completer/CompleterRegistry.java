package shell.command.completer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompleterRegistry {

    private final Map<String, String> completions;

    public CompleterRegistry() {
        completions = new ConcurrentHashMap<>();
    }

    public void registerCompletion(String targetCommand, String completion) {
        completions.put(targetCommand, completion);
    }

    public boolean containsCompletion(String targetCommand) {
        return completions.containsKey(targetCommand);
    }

    public String getCompletion(String targetCommand) {
        return completions.get(targetCommand);
    }

}
