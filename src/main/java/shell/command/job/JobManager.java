package shell.command.job;

import shell.io.ExecContext;

import java.util.ArrayList;
import java.util.List;

public class JobManager {
    private final List<Job> jobs = new ArrayList<>();

    public Job add(Process process, String commandLine) {
        int number = jobs.size() + 1;
        Job job = new Job(number, process, commandLine);
        jobs.add(job);
        return job;
    }

    public List<Job> list() {
        return jobs.stream().toList();
    }

    public void remove(Job job) {
        jobs.remove(job);
    }

    public void checkAndReapJobs(ExecContext context) {
        List<Job> jobList = this.list();
        for (int i = 0; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            printConsoleMessage(context, job, i, jobList.size());
            checkFinishedJobs(job);
        }
    }

    public void checkCompletedJobs(ExecContext context) {
        List<Job> jobList = this.list().stream().filter(Job::isFinished).toList();
        for (int i = 0; i < jobList.size(); i++) {
            Job job = jobList.get(i);
            printConsoleMessage(context, job, i, jobList.size());
            checkFinishedJobs(job);
        }
    }

    private void checkFinishedJobs(Job job) {
        if (job.isFinished()) {
            this.remove(job);
        }
    }

    private static void printConsoleMessage(ExecContext context, Job job, int index, int listSize) {
        String marker = getMarker(index, listSize);
        String backgroundMarker = job.isFinished() ? "" :"&";
        context.out().printf("[%s]%s  %s                 %s %s%n", job.number(), marker, job.status(), job.commandLine().trim(), backgroundMarker);
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
        if (jobIndex == listSize - 1) {
            return "+";
        }
        else if (jobIndex == listSize - 2) {
            return "-";
        }
        return "";
    }

}

