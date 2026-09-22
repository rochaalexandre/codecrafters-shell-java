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
     * Returns marker for job at index {@code i} in jobs listing.
     * <p>
     * Jobs run in the order started, numbered sequentially ([1], [2], ...).
     * Markers flag special jobs:
     * <ul>
     *     <li>{@code +} - most recently started job (the "current" job)</li>
     *     <li>{@code -} - second most recently started job (the "previous" job)</li>
     *     <li>{@code ""} - all other jobs</li>
     * </ul>
     *
     * @param i index of job in list
     * @param listSize total number of jobs in list
     * @return marker string for job at index {@code i}
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
