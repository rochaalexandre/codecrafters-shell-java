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
            printConsoleMessage(context, job, jobList.size());
            checkFinishedJobs(job);
        }

        return 0;
    }

    private void checkFinishedJobs(Job job) {
        if (job.isFinished()) {
            registry.remove(job);
        }
    }

    private static void printConsoleMessage(ExecContext context, Job job, int listSize) {
        String marker = getMarker(job.number(), listSize);
        String backgroundMarker = job.isFinished() ? "" :"&";
        context.out().printf("[%s]%s  %s                 %s %s", job.number(), marker, job.status(), job.commandLine().trim(), backgroundMarker);
    }

    /**
     * Returns marker for job at index {@code jobIndex} in jobs listing.
     * <p>
     * Jobs run in the order started, numbered sequentially ([1], [2], ...).
     * Markers flag special jobs:
     * <ul>
     *     <li>{@code +} - most recently started job (the "current" job)</li>
     *     <li>{@code -} - second most recently started job (the "previous" job)</li>
     *     <li>{@code ""} - all other jobs</li>
     * </ul>
     *
     * @param jobIndex        index of job in list
     * @param listSize total number of jobs in list
     * @return marker string for job at index {@code jobIndex}
     */
    private static String getMarker(int jobIndex, int listSize) {
        if (jobIndex == listSize) {
            return "+";
        }
        else if (jobIndex == listSize - 1) {
            return "-";
        }
        return "";
    }
}
