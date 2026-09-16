package shell.completer;

import org.jline.reader.Completer;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import shell.command.builtin.BuiltinRegistry;
import shell.command.completer.CompleterRegistry;
import shell.env.PathResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.jline.builtins.Completers.DirectoriesCompleter;
import static org.jline.builtins.Completers.FilesCompleter;

public class CompleterFactory {
    private final PathResolver pathResolver;
    private final BuiltinRegistry builtinRegistry;
    private final CompleterRegistry completerRegistry;

    public CompleterFactory(PathResolver pathResolver, BuiltinRegistry builtinRegistry, CompleterRegistry completerRegistry) {
        this.pathResolver = pathResolver;
        this.builtinRegistry = builtinRegistry;
        this.completerRegistry = completerRegistry;
    }
    public AggregateCompleter buildCommandCompleter() {
        Collection<String> builtinCommands = this.builtinRegistry.listAvailableCommands();
        Collection<String> externalCommand = this.pathResolver.listAvailableCommands();
        return new AggregateCompleter(new StringsCompleter(builtinCommands), new StringsCompleter(externalCommand));
    }

    public AggregateCompleter buildDirAndFileCompleter() {
        Path currentDir = Paths.get(".");
        return new AggregateCompleter(new FilesCompleter(currentDir), new DirectoriesCompleter(currentDir));
    }

    public Completer buildCustomCommandCompleter() {
        return new CustomCommandCompleter(completerRegistry);
    }
}
