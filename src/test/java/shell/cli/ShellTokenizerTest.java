package shell.cli;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static shell.cli.TokenType.*;

class ShellTokenizerTest {
    private List<Token> tokenize(String input) {
        return new ShellTokenizer(input).tokenize();
    }

    private Token word(String value) {
        return new Token(WORD, value);
    }

    @Test
    void splitsUnquotedWhitespaceAndIgnoresBlankInput() {
        assertEquals(List.of(), tokenize(" \t "));
        assertEquals(List.of(word("echo"), word("apple"), word("orange")),
                tokenize("  echo\t apple   orange  "));
    }

    @Test
    void preservesQuotedWhitespaceAndEmptyWords() {
        assertEquals(List.of(word("echo"), word(" a  b\t"), word("c  d"), word(""), word("")),
                tokenize("echo ' a  b\t' \"c  d\" '' \"\""));
    }

    @Test
    void joinsAdjacentFragmentsIntoOneWord() {
        assertEquals(List.of(word("ab cdef"), word("xy")),
                tokenize("ab\" cd\"'ef' x''y"));
    }

    @Test
    void treatsBackslashesAndDoubleQuotesLiterallyInsideSingleQuotes() {
        assertEquals(List.of(word("a\\b\\\"$NAME")), tokenize("'a\\b\\\"$NAME'"));
    }

    @Test
    void escapesCharactersOutsideQuotes() {
        assertEquals(List.of(word("two words"), word("'"), word("\""), word("\\"), word("|>&")),
                tokenize("two\\ words \\' \\\" \\\\ \\|\\>\\&"));
    }

    @Test
    void selectivelyEscapesInsideDoubleQuotes() {
        assertEquals(List.of(word("a\"b\\c$d`e\\q\\'\\ ")),
                tokenize("\"a\\\"b\\\\c\\$d\\`e\\q\\'\\ \""));
    }

    @Test
    void removesEscapedNewlinesButPreservesSingleQuotedOnes() {
        assertEquals(List.of(word("ab"), word("cd"), word("e\\\nf")),
                tokenize("a\\\nb \"c\\\nd\" 'e\\\nf'"));
        assertEquals(List.of(), tokenize("\\\n"));
    }

    @Test
    void decodesQuotedExecutableLikeAnyOtherWord() {
        assertEquals(List.of(word("my executable"), word("arg")),
                tokenize("\"my executable\" arg"));
        assertEquals(List.of(word("exe'cutable")), tokenize("\"exe'cutable\""));
    }

    @Test
    void distinguishesQuotedOperatorsFromSyntax() {
        assertEquals(List.of(word("echo"), word("|>&"), word(">"), new Token(PIPE, "|"), word("cat")),
                tokenize("echo '|>&' \\>|cat"));
    }

    @Test
    void recognizesSupportedOperatorsWithoutRequiringSpaces() {
        assertEquals(List.of(word("echo"), word("hi"), new Token(STDOUT, ">"), word("out file"),
                        new Token(STDERR_APPEND, "2>>"), word("err"), new Token(PIPE, "|"),
                        word("cat"), new Token(BACKGROUND, "&")),
                tokenize("echo hi>\"out file\" 2>>err|cat&"));
        assertEquals(List.of(new Token(STDOUT, "1>"), new Token(STDOUT_APPEND, ">>"),
                        new Token(STDOUT_APPEND, "1>>"), new Token(STDERR, "2>")),
                tokenize("1> >> 1>> 2>"));
    }

    @Test
    void redirectDescriptorsMustBePlainAndAdjacent() {
        for (String input : List.of("'2'>", "\\2>", "2\"\">", "2 >")) {
            assertEquals(List.of(word("2"), new Token(STDOUT, ">")), tokenize(input), input);
        }
        assertEquals(List.of(word("file2"), new Token(STDOUT, ">")), tokenize("file2>"));
    }

    @Test
    void expandsVariablesOnlyOutsideSingleQuotes() {
        assertEquals(List.of(word("alex"), word("$NAME"), word("alex")),
                new ShellTokenizer("$NAME '$NAME' \"$NAME\"", name -> "alex").tokenize());
    }

    @Test
    void preservesEscapedDollarSigns() {
        assertEquals(List.of(word("$NAME"), word("$NAME"), word("${NAME}")),
                new ShellTokenizer("\\$NAME \"\\$NAME\" \\${NAME}", name -> "alex").tokenize());
    }

    @Test
    void expandsBracedNamesAndAdjacentFragments() {
        assertEquals(List.of(word("prealexpost"), word("alex/alex"), word("user")),
                new ShellTokenizer("pre${NAME}post \"$NAME/${NAME}\" $_USER2",
                        name -> name.equals("_USER2") ? "user" : "alex").tokenize());
    }

    @Test
    void retainsQuotedEmptyExpansionsButOmitsUnquotedEmptyOnes() {
        assertEquals(List.of(word("echo"), word(""), word(""), word("prefix")),
                tokenize("echo $MISSING \"$MISSING\" \"${MISSING}\" prefix${MISSING}"));
    }

    @Test
    void expandedValuesAreDataRatherThanShellSyntax() {
        String value = "2 > | & '\" \\ $OTHER";
        assertEquals(List.of(word(value), word(value)),
                new ShellTokenizer("$NAME \"$NAME\"", name -> value).tokenize());
        assertEquals(List.of(word("2"), new Token(STDOUT, ">"), word("out")),
                new ShellTokenizer("$FD>out", name -> "2").tokenize());
    }

    @Test
    void preservesUnsupportedDollarFormsAndRejectsMalformedBraces() {
        assertEquals(List.of(word("$"), word("$?"), word("$1")), tokenize("$ $? $1"));
        for (String input : List.of("${NAME", "${}", "${1}", "${NAME:-default}")) {
            assertThrows(IllegalArgumentException.class, () -> tokenize(input), input);
        }
    }

    @Test
    void rejectsUnmatchedQuotesAndTrailingEscapes() {
        for (String input : List.of("'unfinished", "\"unfinished", "word\\", "\"word\\")) {
            assertThrows(IllegalArgumentException.class, () -> tokenize(input), input);
        }
    }

    @Test
    void eachInputHasIndependentState() {
        assertThrows(IllegalArgumentException.class, () -> tokenize("'unfinished"));
        assertEquals(List.of(word("ok")), tokenize("ok"));
    }
}
