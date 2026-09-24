package shell.cli;

import java.util.List;

/**
 * A parsed input line: one or more commands joined by {@code |}. A plain command is a
 * pipeline with a single stage, so the rest of the shell only has to handle one shape.
 *
 * <p>Each stage is an ordinary {@link ParsedLine}; stage {@code i}'s stdout feeds stage
 * {@code i + 1}'s stdin.
 */
public record Pipeline(List<ParsedLine> stages) {

    public Pipeline {
        stages = List.copyOf(stages);
    }

    public static Pipeline of(ParsedLine single) {
        return new Pipeline(List.of(single));
    }

    /** True when this is a plain command (one stage) named {@code name}. */
    public boolean isCommand(String name) {
        return isSingleStage() && stages.getFirst().isCommand(name);
    }

    public boolean isSingleStage() {
        return stages.size() == 1;
    }
}
