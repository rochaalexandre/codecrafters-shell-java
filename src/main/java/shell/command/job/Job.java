package shell.command.job;

public record Job(int number, Process process, String commandLine)
{
    public long pid() {
        return process.pid();
    }
}
