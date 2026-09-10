package shell;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.*;

public final class BashStyleCompleter implements Completer {
    private final Completer delegate;
    private String pendingWord;   // the word we already beeped once for

    public BashStyleCompleter(Completer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<Candidate> matched = getCandidateList(reader, line);

        if (matched.size() <= 1) {          // unique or none: normal behaviour
            candidates.addAll(matched);
            pendingWord = null;
        }
        else if (!line.word().equals(pendingWord)) {
            pendingWord = line.word();       // first TAB: nothing -> JLine beeps
        }
        else {
            // second TAB: list
            String names = matched.stream().map(Candidate::value).sorted()
                    .collect(java.util.stream.Collectors.joining("  "));
            reader.printAbove("$ " + line.word());
            reader.printAbove(names);
            pendingWord = null;
        }
    }

    private List<Candidate> getCandidateList(LineReader reader, ParsedLine line) {
        List<Candidate> matched = new ArrayList<>();
        delegate.complete(reader, line, matched);
        // filter to what actually matches line.word() — StringsCompleter returns ALL of them
        matched.removeIf(c -> !c.value().startsWith(line.word()));
        // filter duplicate values like  Builtin echo + /usr/bin/echo on PATH.
        return matched.stream().distinct().toList();
    }
}
