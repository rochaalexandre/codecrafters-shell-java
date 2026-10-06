# Expressing software design concerns

A reference from our pipeline and parser discussions. Start with what makes the code
hard to understand, then use the term that accurately describes that difficulty.
Examples below are illustrative Java fragments, not proposed repository changes.

## Vocabulary table

| What I notice | Useful vocabulary | How to express it |
|---|---|---|
| I cannot tell what happens first. | Execution flow | Make the execution flow explicit. |
| I have to remember what these indexes mean. | Cognitive load | These index checks add mental bookkeeping. Name the relationships. |
| I have to jump between methods to understand this. | Local reasoning | Keep enough context together so I can understand the operation locally. |
| This method figures things out and also runs them. | Separation of concerns | Separate command resolution, routing, and execution. |
| I want clearer code with the same results. | Behavior-preserving refactor | Improve readability while preserving observable behavior. |
| The important decision is hidden. | Implicit logic | Make the routing decisions explicit. |
| What does this sublist represent? | Intent-revealing naming; clarity of intent | Name the token slice according to what it represents. |
| I have to trace indexes to understand command boundaries. | Cognitive load | Make stage boundaries understandable without mentally executing the loop. |
| The method exposes list operations instead of the parsing story. | Level of abstraction | Express pipeline parsing in terms of stages and separators. |
| What do true and false mean in this call? | Boolean blindness | The positional boolean arguments hide the parsing rules at the call site. |
| Make whether we are inside quotes obvious. | Explicit parsing state | Represent and name the quote state explicitly. |
| Prepare for future features without building too much now. | Incremental design; avoid premature abstraction | Implement today's rules with a design we can extend when the next requirement arrives. |
| I must track several interacting fields across methods. | State-management complexity | Reduce the state I must remember to understand each parsing step. |
| Some complexity is unavoidable, but some comes from the design. | Essential vs. accidental complexity | Distinguish required parsing rules from avoidable implementation machinery. |
| A pipe saves this command and starts the next one. | Stage lifecycle | Keep collecting, finishing, and starting a stage visible. |

These terms describe concerns, not automatic diagnoses. Smaller methods, more names,
and more abstractions help only when they reduce the actual reading difficulty.

## 1. Execution flow

**Issue:** sequencing is buried among implementation details.

```java
List<ProcessBuilder> commands = new ArrayList<>();
// Resolve commands and configure streams here...
List<Process> processes = ProcessBuilder.startPipeline(commands);
// Locate built-ins and execute them here...
for (Process process : processes) {
    process.waitFor();
}
```

**Clearer expression of the sequence:**

```java
List<Stage> stages = resolveStages(pipeline);
List<ProcessBuilder> commands = configureExternalCommands(stages);
List<Process> processes = ProcessBuilder.startPipeline(commands);
runLeadingBuiltin(stages.getFirst(), processes);
runTrailingBuiltin(stages.getLast());
waitForAll(processes);
```

Say: “I want the entry point to expose the execution sequence before its details.”

## 2. Cognitive load and meaningful relationships

**Issue:** the reader translates positions into relationships.

```java
if (index > 0) {
    process.redirectInput(ProcessBuilder.Redirect.PIPE);
}
if (index < stages.size() - 1) {
    // Configure output to the next stage.
}
```

**Name those relationships:**

```java
boolean hasPreviousStage = index > 0;
boolean hasNextStage = index + 1 < stages.size();

if (hasPreviousStage) {
    process.redirectInput(ProcessBuilder.Redirect.PIPE);
}
if (hasNextStage) {
    Stage nextStage = stages.get(index + 1);
    // Configure output to nextStage.
}
```

Say: “The index arithmetic is simple, but interpreting its meaning adds cognitive load.”

## 3. Local reasoning

**Issue:** a small decision is scattered across helpers that add little meaning.

```java
Redirect redirect = makeRedirect(operator, target);
applyRedirect(stage, operator, redirect);
finishRedirect(stage);
```

**Keep the decision visible, extracting only distracting validation:**

```java
Token target = requireRedirectionTarget(tokens);
Redirect redirect = new Redirect(target.text(), operator.text().endsWith(">>"));
if (operator.text().startsWith("2")) {
    currentStage.stderr = redirect;
} else {
    currentStage.stdout = redirect;
}
currentStage.argumentsFinished = true;
```

Say: “Extract target validation, but keep destination selection here so I can reason locally.”

A helper can also improve local reasoning if its name and contract let readers safely
ignore its details. The concern is unnecessary navigation, not method count alone.

## 4. Separation of concerns

**Issue:** one loop resolves commands, configures routing, and starts processes.

```java
for (ParsedLine line : pipeline.stages()) {
    Resolved command = resolver.resolve(line);
    // Decide input and output destinations.
    // Start the command here.
}
```

**Separate the decisions and execution:**

```java
List<Stage> stages = resolveStages(pipeline);
List<ProcessBuilder> commands = configureExternalCommands(stages);
List<Process> processes = ProcessBuilder.startPipeline(commands);
```

Say: “I want to inspect routing independently of starting processes.”

For pipelines, starting a producer and waiting for it before starting its consumer can
block. This separation must preserve concurrent startup, not merely rearrange methods.

## 5. Behavior-preserving refactor

**Issue:** a readability change accidentally changes a parsing rule.

```java
// Existing rule: argument collection stops after the first redirect.
if (!argumentsFinished) {
    arguments.add(word);
}
```

Replacing that block with `arguments.add(word)` changes behavior; it is not just cleanup.
A behavior-preserving rename would be:

```java
if (!argumentCollectionFinished) {
    arguments.add(word);
}
```

Say: “Preserve the current argument-collection rule during this refactor. Discuss changing
that rule separately.” Preserving behavior does not imply that every existing rule is ideal.

## 6. Implicit logic

**Issue:** the destination is compressed into an expression.

```java
process.redirectOutput(stages.get(index + 1).isBuiltin()
        ? ProcessBuilder.Redirect.DISCARD
        : ProcessBuilder.Redirect.PIPE);
```

**Expose the decision:**

```java
Stage nextStage = stages.get(index + 1);
// Current built-ins have no stdin, so incoming output is discarded.
ProcessBuilder.Redirect output = nextStage.isBuiltin()
        ? ProcessBuilder.Redirect.DISCARD
        : ProcessBuilder.Redirect.PIPE;
process.redirectOutput(output);
```

Say: “Make the next-stage dependency and the reason for discarding output explicit.”
This rule describes our current implementation, not all shell built-ins in general.

## 7. Intent-revealing naming

**Issue:** a slice has a mechanical meaning but no visible parsing meaning.

```java
stages.add(parseStage(tokens.subList(stageStart, index), true, false));
```

**Name what the slice represents:**

```java
List<Token> currentStageTokens = tokens.subList(stageStart, index);
stages.add(parseStage(currentStageTokens, true, false));
```

Say: “I understand subList, but I have to reconstruct what this slice represents.”
The name helps with intent; it does not resolve the unexplained booleans.

## 8. Level of abstraction

**Issue:** a pipeline method mixes the parsing story with token slicing mechanics.

```java
if (tokens.get(index).kind() == TokenKind.PIPE) {
    stages.add(parseStage(tokens.subList(stageStart, index)));
    stageStart = index + 1;
}
```

**An alternative makes the stage boundary explicit:**

```java
case PIPE -> {
    stages.add(currentStage.build());
    currentStage = new StageBuilder();
}
```

Say: “At this level, I want to read 'finish a stage and begin another' rather than reconstruct
that operation from list slicing.” This alternative still has state and needs to be evaluated
against the rest of the parser.

## 9. Boolean blindness

**Issue:** literals do not reveal their roles.

```java
parseStage(stageTokens, true, false);
```

**A small clarification can be enough:**

```java
boolean commandRequired = true;
boolean finalStage = false;
parseStage(stageTokens, commandRequired, finalStage);
```

Say: “I cannot understand the flags without opening the method declaration.”
Named variables clarify the call; use richer types only if the rules justify them.

## 10. Explicit parsing state

**Issue:** whether spaces belong to an argument is hidden behind a vague flag.

```java
if (flag) {
    currentWord.append(character);
}
```

**Name the state and its transition:**

```java
if (character == '\'') {
    insideSingleQuotes = !insideSingleQuotes;
} else if (insideSingleQuotes) {
    currentWord.append(character);
}
```

This fragment only illustrates the quoted branch, not a complete tokenizer.
Say: “Make entering and leaving quotes explicit; closing quotes does not finish an argument.”

## 11. Incremental design and premature abstraction

**Issue:** future quoting features become machinery before their rules are needed.

```java
interface CharacterRule {
    boolean matches(char character, ParsingContext context);
    void apply(char character, ParsingContext context);
}
// A registry of rules, factories, and strategies follows.
```

**A smaller current model may suffice:**

```java
private enum QuoteState {
    UNQUOTED,
    SINGLE_QUOTED
}
```

Say: “Implement single quotes now. Consider future double quotes and escaping, but don't
build a general rule framework without a demonstrated need.” An enum alone does not guarantee
extensibility; future rules must still be evaluated when introduced.

## 12. State-management complexity

**Issue:** recognizing a redirect requires remembering unfinished work across characters.

```java
private String pendingRedirect;

// On one character:
pendingRedirect = ">";
// On a later character:
if (pendingRedirect != null) {
    // Decide whether to extend it, emit it, or process the next character.
}
```

**Lookahead can make that decision local:**

```java
// The first > has already been consumed.
String operator = characters.consumeIf('>') ? ">>" : ">";
tokens.add(new Token(TokenKind.REDIRECT, descriptor + operator));
```

Say: “Can we decide the operator in one place rather than carry unfinished state?”
A cursor also adds structure; compare the complexity it introduces with the state it removes.

## 13. Essential versus accidental complexity

**Required distinction:** spaces inside quotes are content, outside quotes are separators.

```java
if (insideSingleQuotes) {
    currentWord.append(character);
} else if (Character.isWhitespace(character)) {
    finishWord();
}
```

**Additional compatibility rule:** the old implementation removed unquoted ampersands.

```java
if (character == '&') {
    // Preserve removal; separately determine whether background execution applies.
}
```

Say: “Which complexity comes from required quoting behavior, and which comes from preserving
legacy rules?” Compatibility may be a real requirement. It is not safe to remove that behavior
just because it complicates the implementation.

## 14. Stage lifecycle

**Issue:** replacing a builder looks like an unexplained reset.

```java
stages.add(stage.build());
stage = new StageBuilder();
```

**Name the active stage and retain the transition:**

```java
StageBuilder currentStage = new StageBuilder();
// Inside the token loop:
case PIPE -> {
    stages.add(currentStage.build());
    currentStage = new StageBuilder();
}
// After the loop:
stages.add(currentStage.build());
```

Say: “The loop collects the current command. A pipe saves it and starts the next one.
After the loop, save the final command.” The loop continues; only the collected stage restarts.

## Explaining and defending a change

Describe a concern using:

**where I get stuck → what I must mentally reconstruct → what would help**

> In parsePipeline, I have to reconstruct command boundaries from indexes and subList calls.
> Naming the slices and making the stage transitions visible would make the flow easier to follow.

Explain a decision using:

**problem → constraints → alternatives → decision → trade-offs → evidence**

> Redirection validation interrupts the token loop's flow. We need to preserve how the target
> token is consumed and how stdout and stderr are selected. We could extract the entire branch
> or only validation. I chose to extract validation so the destination decision remains visible.
> This adds one helper, but reduces detail in the loop without hiding the routing rule. Existing
> parsing tests should verify that behavior remains unchanged.
