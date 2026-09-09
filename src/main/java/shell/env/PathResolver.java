package shell.env;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Locates executables on the {@code PATH}.
 *
 * <p>Isolated so that future concerns (caching the PATH scan, platform-specific
 * extensions, respecting {@code PATHEXT}) have one home and can be tested without
 * touching the shell loop.
 */
public final class PathResolver {

    private final Map<String, Path> cache = new ConcurrentHashMap<>();

    public PathResolver() {
        this.init();
    }

    private void init() {
        String envPath = System.getenv("PATH");
        if (envPath == null) {
            return;
        }
        for (String dir : envPath.split(File.pathSeparator)) {
            cachedDirectoryFiles(dir);
        }
    }

    private void cachedDirectoryFiles(String dir) {
        Path path = Paths.get(dir);
        if (!Files.isDirectory(path)) {
            return;
        }

        try (Stream<Path> entries = Files.list(path)) {
            entries.filter(Files::isExecutable)
                    .forEach(f -> cache.putIfAbsent(f.getFileName().toString(), f));
        } catch (IOException e) {
            // unreadable dir — skip, don't kill the scan
        }
    }

    public List<String> listAvailableCommands() {
        return cache.keySet().stream().toList();
    }

    public Optional<Path> findExecutable(String name) {
        if (cache.containsKey(name)) {
            return Optional.of(cache.get(name));
        }
        return Optional.empty();
    }
}
