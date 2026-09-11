package shell.completer;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.*;

public final class BashStyleCompleter implements Completer {
    private final Completer commandCompleter;
    private final Completer fileAndDirectoryCompleter;
    private String pendingWord;   // the word we already beeped once for

    public BashStyleCompleter(Completer delegate, Completer fileAndDirectoryCompleter) {
        this.commandCompleter = delegate;
        this.fileAndDirectoryCompleter = fileAndDirectoryCompleter;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<Candidate> matched = getCandidateList(reader, line);
        Optional<String> commonPrefix = longestCommonPrefix(line, matched);

        if (matched.size() <= 1) {          // unique or none: normal behaviour
            candidates.addAll(matched);
            pendingWord = null;
        } else if (commonPrefix.isPresent()) {   // ambiguous, but LCP extends past what's typed: insert it
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

        //only run one set of completer at time
        boolean isFileOrDirComplete = line.wordIndex() > 0;
        if (isFileOrDirComplete) {
            fileAndDirectoryCompleter.complete(reader, line,  matched);
        } else  {
            commandCompleter.complete(reader, line, matched);
        }
        // filter to what actually matches line.word() — StringsCompleter returns ALL of them
        matched.removeIf(c -> !c.value().startsWith(line.word()));
        // filter duplicate values like  Builtin echo + /usr/bin/echo on PATH.
        return matched.stream().distinct().toList();
    }


    /**
     * Longest common prefix of the candidates' values, if it extends past what's already typed.
     * Empty if there's no overlap, or the overlap is just the typed word itself (fully ambiguous).
     *
     * @see <a href="https://algomaster.io/learn/dsa/longest-common-prefix">Longest Common Prefix</a>
     */
    public Optional<String> longestCommonPrefix(ParsedLine line, List<Candidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        // Sort so first and last strings are most different
        ArrayList<Candidate> shortedList = new ArrayList<>(candidates);
        shortedList.sort(Comparator.comparing(Candidate::value));

        Candidate first = shortedList.getFirst();
        Candidate last = shortedList.getLast();

        int i = 0;
        int limit = Math.min(first.value().length(), last.value().length());
        // sorted first/last are the most different pair; walk while they still agree char-by-char
        while (i < limit && first.value().charAt(i) == last.value().charAt(i)) {
            i++;
        }

        String commonPrefix = first.value().substring(0, i);
        boolean isNotTheTypedWord = commonPrefix.isEmpty() || commonPrefix.equals(line.word());

        return isNotTheTypedWord ? Optional.empty() : Optional.of(commonPrefix);
     }

}
