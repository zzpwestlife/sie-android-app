# Prompt Engineering Annex

## 1. Core Philosophy
Prompt engineering is not just about writing instructions; it is about **designing interactions** and **structuring outputs**. We treat prompts as code: they must be modular, testable, and robust.

## 2. Structured Prompt Standard
For all implementation tasks, especially complex ones, Agent MUST use the following XML-based structure to ensure clarity and constraint compliance.

### 2.1 Standard Template
```xml
<task_definition>
  <objective>[Clear, single-sentence objective]</objective>
  <context>[Necessary background information]</context>
</task_definition>

<requirements>
  <requirement id="1">[Must-have feature 1]</requirement>
  <requirement id="2">[Must-have feature 2]</requirement>
  <!-- ... -->
</requirements>

<constraints>
  <constraint type="negative">[What NOT to do]</constraint>
  <constraint type="tech_stack">[Library/Version constraints]</constraint>
  <constraint type="performance">[Performance limits]</constraint>
</constraints>

<examples>
  <example>
    <input>[Example input]</input>
    <output>[Example output]</output>
    <explanation>[Why this output is correct]</explanation>
  </example>
</examples>

<output_format>
  [Specific file structure, code style, or JSON schema required]
</output_format>

<validation_criteria>
  [How to verify success - e.g., "Run npm test", "Check UI responsiveness"]
</validation_criteria>
```

## 3. Workflow Integration
1.  **Optimization Phase**: Before execution, offer to optimize the user's raw prompt using the standard template above.
2.  **Verification**: Ensure the generated plan/code explicitly addresses each `<requirement>` and adheres to all `<constraints>`.

## 4. Best Practices
-   **Negative Constraints**: Explicitly state what should be avoided (e.g., "Do not use `any` type", "Do not introduce new dependencies").
-   **Chain of Thought**: Encourage the model to use `<thinking>` tags to plan before acting.
-   **Few-Shot Learning**: Always provide at least one concrete example in the `<examples>` section.
