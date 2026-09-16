package shell.completer;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import shell.command.completer.CompleterRegistry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CustomCommandCompleter implements Completer {
    private final CompleterRegistry completerRegistry;

    public CustomCommandCompleter(CompleterRegistry completerRegistry) {
        this.completerRegistry = completerRegistry;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<Candidate> matched = new ArrayList<>();
        String targetCommand = line.words().getFirst();
        if (completerRegistry.containsCompletion(targetCommand)) {
            runScript(completerRegistry.getCompletion(targetCommand), line, matched);
            matched.removeIf(c -> !c.value().startsWith(line.word()));
            candidates.addAll(matched);
        }
    }

    private void runScript(String path, ParsedLine parsedLine, List<Candidate> candidates) {
        int wordIdx = parsedLine.wordIndex();
        String command = parsedLine.words().getFirst();
        String currentWord = parsedLine.word();
        String previousWord = (wordIdx >= 2) ? parsedLine.words().get(wordIdx - 1) : "";

        System.setProperty("COMP_LINE", parsedLine.line());
        System.setProperty("COMP_POINT", String.valueOf(parsedLine.wordIndex()));

        ProcessBuilder processBuilder = new ProcessBuilder(path, command, currentWord, previousWord);
        processBuilder.redirectErrorStream(false);
        Map<String, String> env = processBuilder.environment();
        env.put("COMP_LINE", parsedLine.line());
        env.put("COMP_POINT", String.valueOf(parsedLine.wordIndex()));
        try (Process process = processBuilder.start()) {
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                candidates.add(new Candidate(line, line, null, null, null, null, true));
            }
        } catch (Exception e) {
            //silent crash
        }
    }
}
