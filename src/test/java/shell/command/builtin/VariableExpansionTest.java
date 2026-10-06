package shell.command.builtin;

import org.junit.jupiter.api.Test;
import shell.cli.InputParser;
import shell.command.env.VariablesManager;
import shell.io.ExecContext;
import shell.io.Stream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableExpansionTest {
    @Test
    void declarationAndEchoShareVariablesWithoutExpandingLiteralDollarSignsAgain() {
        VariablesManager variables = new VariablesManager();
        InputParser parser = new InputParser(variables);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        try (ExecContext context = new ExecContext(Stream.console(new PrintStream(output)),
                Stream.console(new PrintStream(errors)))) {
            DeclareBuiltin declare = new DeclareBuiltin(variables);
            declare.run(parser.parse("declare NAME='alex freire'").stages().getFirst(), context);
            declare.run(parser.parse("declare MESSAGE=\"hello ${NAME}\"").stages().getFirst(), context);
            declare.run(parser.parse("declare LITERAL='$NAME'").stages().getFirst(), context);
            new EchoBuiltin().run(parser.parse("echo \"$MESSAGE\" '$NAME' \\$NAME \"$LITERAL\"")
                    .stages().getFirst(), context);
        }

        assertEquals("hello alex freire $NAME $NAME $NAME" + System.lineSeparator(), output.toString());
        assertEquals("", errors.toString());
    }
}
