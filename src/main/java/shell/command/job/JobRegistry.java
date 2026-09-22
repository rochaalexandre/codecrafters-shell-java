package shell.command.job;

import java.util.ArrayList;
import java.util.List;

public class JobRegistry {
    private final List<Job> jobs = new ArrayList<>();
    private int nextNumber = 1;

    public Job add(Process process, String commandLine) {
        Job job = new Job(nextNumber++, process, commandLine);
        jobs.add(job);
        return job;
    }

    public List<Job> list() {
        return jobs;
    }
}

