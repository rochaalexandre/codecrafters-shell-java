package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.job.Job;
import shell.command.job.JobManager;
import shell.io.ExecContext;

import java.util.List;

public class JobBuiltin implements Builtin {
    private final JobManager registry;

    public JobBuiltin(JobManager registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "jobs";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        registry.checkAndReapJobs(context);
        return 0;
    }
}
