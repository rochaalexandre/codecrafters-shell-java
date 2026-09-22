package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.command.job.Job;
import shell.command.job.JobRegistry;
import shell.io.ExecContext;

import java.util.List;

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
        List<Job> jobList = registry.list();
        for (int i = 0; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            String marker = getMarker(i, jobList.size());
            context.out().printf("[%s]%s  %s                 %s &%n", job.number(), marker, job.status(), job.commandLine().trim());
        }

        return 0;
    }

    /**
     * When multiple commands run in the background, the jobs command lists them in the order they were started.
     *
     * Job numbers are assigned sequentially: the first background job is [1], the next is [2], and so on.
     *
     * The shell uses markers to indicate special jobs:
     *
     * + - The most recently started job (the "current" job)
     * - - The second most recently started job (the "previous" job)
     * Space () - All other jobs
     * @param i
     * @param listSize
     * @return
     */
    private static String getMarker(int i, int listSize) {
        if (i == listSize - 1) {
           return "+";
        } else  if (i == listSize - 2) {
            return  "-";
        }
        return "";
    }
}
