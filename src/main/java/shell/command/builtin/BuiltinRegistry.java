package shell.command.builtin;

import shell.env.PathResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class BuiltinRegistry {

    /** Handled directly by the read loop rather than a {@link Builtin} — it breaks the loop. */
    public static final String EXIT = "exit";

    private final Map<String, Builtin> commandsMap = new HashMap<>();

    public BuiltinRegistry(PathResolver pathResolver) {
        List.of(new EchoBuiltin(), new PwdBuiltin(),
                        new TypeBuiltin(this, pathResolver), new CdBuiltin())
                .forEach(b -> commandsMap.put(b.name(), b));
    }

    public boolean isBuiltin(String command) {
        return commandsMap.containsKey(command);
    }

    public Optional<Builtin> getBuiltin(String command) {
        return Optional.ofNullable(commandsMap.get(command));
    }

    public List<String> listAvailableCommands() {
        Stream<String> names = Stream.concat(commandsMap.keySet().stream(), Stream.of(EXIT));
        return names.toList();
    }
}
