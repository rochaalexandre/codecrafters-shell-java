import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Locates executables on the {@code PATH}.
 *
 * <p>Isolated so that future concerns (caching the PATH scan, platform-specific
 * extensions, respecting {@code PATHEXT}) have one home and can be tested without
 * touching the shell loop.
 */
public final class PathResolver {

    public Optional<Path> findExecutable(String name) {
        String envPath = System.getenv("PATH");
        if (envPath == null) {
            return Optional.empty();
        }
        for (String dir : envPath.split(File.pathSeparator)) {
            Path file = Paths.get(dir, name);
            if (Files.exists(file) && Files.isExecutable(file)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }
}
