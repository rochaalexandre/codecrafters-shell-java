package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.job.JobRegistry;
import shell.io.ExecContext;

public class JobBuiltin implements Builtin {
    private final JobRegistry registry;

    public JobBuiltin(JobRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "jobs";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        registry.list().forEach(b -> {
            context.out().printf("[%s]+  %s                 %s &%n", b.number(), b.status(), b.commandLine().trim());
        });
        return 0;
    }
}
