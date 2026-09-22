package shell.command.job;

import java.util.ArrayList;
import java.util.List;

public class JobRegistry {
    private final List<Job> jobs = new ArrayList<>();

    public Job add(Process process, String commandLine) {
        int number = jobs.size() + 1;
        Job job = new Job(number, process, commandLine);
        jobs.add(job);
        return job;
    }

    public List<Job> list() {
        return jobs;
    }

    public void remove(Job job) {
        jobs.remove(job);
    }
}

