package shell.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BuiltinRegistry {
    private final Map<String, Builtin> commandsMap = new HashMap<>();

    public BuiltinRegistry() {
        List.of(new EchoBuiltin(), new PwdBuiltin(), new TypeBuiltin(this), new CdBuiltin())
                .forEach(b -> commandsMap.put(b.name(), b));
    }

    public boolean isBuiltin(String command) {
        return commandsMap.containsKey(command);
    }

    public Optional<Builtin> getBuiltin(String command) {
        return Optional.ofNullable(commandsMap.get(command));
    }
}
