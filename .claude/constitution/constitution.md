# Project Constitution (Universal Core Version)

**Version: 2.5, Approved Date: 2026-02-25**

This document defines the unshakeable core development principles of this project. All code modifications must strictly adhere to these principles.
This constitution applies to all technology stacks under this project (Go, Frontend, DevOps, etc.). For language-specific implementation details, please refer to the [Appendices](#appendices).

---

## Article 1: Simplicity Principle (Simplicity First)

**Core:** "Less is More". Never create unnecessary abstractions, never introduce non-essential dependencies.

- **1.1 (YAGNI):** Only implement explicitly requested features, don't speculate on future needs.
- **1.2 (Minimal Dependencies):** Prioritize language standard libraries or mature community standard libraries. Introducing new dependencies requires rigorous evaluation.
- **1.3 (Anti-over-engineering):** Simple functions and data structures are better than complex abstractions. Avoid over-design.
- **1.4 (No Laziness):** Find root causes. No temporary fixes. Senior developer standards. If a fix feels hacky: "Knowing everything I know now, implement the elegant solution".
- **1.5 (First Principles):** Solve problems by breaking them down to basic truths, not by analogy. Question "why" until the root constraint is found.

---

## Article 2: Testing Quality Principle (Testing Quality) - Non-Negotiable

**Core:** All new features or bug fixes must have test coverage.

- **2.1 (Testing Strategy):** Stratify by complexity: complex business logic insists on test-first; simple logic can be supplemented after implementation.
- **2.2 (Test Coverage):** Tests must cover happy paths, error paths, and edge cases.
- **2.3 (Testability):** Code design must be easy to test (using dependency injection and other techniques).

---

## Article 3: Clarity Principle (Clarity and Explicitness) - Non-Negotiable

**Core:** The primary purpose of code is to be understandable by humans, secondarily for machines to execute.

- **3.1 (Error Handling):** **Non-Negotiable**: All errors must be handled explicitly, silent failures are absolutely prohibited.
- **3.2 (Explicit Dependencies):** No implicit global state allowed. All dependencies must be explicitly defined and passed.
- **3.3 (Self-Documenting):** Naming should be self-documenting. Comments explain "why" (Why), not "what" (What).

---

## Article 4: Architecture & Design Principles

**Core:** Follow SOLID principles, ensure high cohesion, low coupling, and core logic isolation.

- **4.1 (Core Logic Isolation):** **"Library First" spirit**. Core business logic must be decoupled from external interfaces (HTTP, CLI, GUI), existing as an independent, testable "library".
- **4.2 (Single Responsibility):** Each module, class, and function should do one thing well (Single Responsibility).
- **4.3 (Interface Segregation):** Define small, focused interfaces.
- **4.4 (Dependency Inversion):** Depend on abstractions rather than concrete implementations.

---

## Article 5: Modification & Structure Principles

**Core:** Keep changes minimal and strictly control code size to ensure maintainability.

- **5.1 (Minimal Changes):** When modifying code, follow the principle of minimal changes. Only modify necessary parts, avoid irrelevant changes, reduce regression risk.
- **5.2 (File Size):** Single file line count **should** be under 200 lines. Significant exceedance requires justification or refactoring plan.
- **5.3 (Function Size):** Single function or method line count **should** be under 20 lines.
- **5.4 (Line Width Limit):** Single line code **should** not exceed 80 characters, with a hard limit of 120 characters.
- **5.5 (Code Evolution):** Delete old code directly, strictly prohibit keeping commented-out obsolete code.
- **5.6 (Module Metadata):** Each module directory must include a README describing Role, Logic, Constraints, and a submodule index. Each source file should start with three header lines: INPUT (dependencies), OUTPUT (provided capabilities), POS (position in the system). When code changes, these metadata must be updated accordingly.

---

## Article 6: Copywriting & Typography Principles

**Core:** Unify copywriting typography in multilingual environments, improve documentation professionalism.

- **6.1 (CJK-ASCII Spacing):** Must add space between Chinese and English, and between Chinese and numbers.
- **6.2 (Punctuation Usage):** Use full-width punctuation in Chinese contexts, half-width for complete English sentences.
- **6.3 (Proper Nouns):** Must use correct official case (e.g., GitHub, iPhone).

---

## Article 7: Continuous Improvement Principle

**Core:** Establish a closed-loop mechanism for learning from mistakes.

- **7.1 (Self-Improvement Loop):** After ANY correction from the user, convert the mistake into a rule and append to `.claude/lessons.md`.
- **7.2 (Pre-load Knowledge):** Always read `.claude/lessons.md` at the start of a session to prevent recurring mistakes.
- **7.3 (Ruthless Iteration):** Ruthlessly iterate on these lessons until mistake rate drops.

---

## Article 8: Plan-First Principle - Non-Negotiable

**Core:** Think before acting.

- **8.1 (Adaptive Planning):** Planning is mandatory but scalable. Complex tasks require detailed plans; trivial tasks require clear intent.
- **8.2 (Strategic Planning):** For non-trivial tasks (3+ steps), must generate a plan in `docs/plans/` (e.g. `docs/plans/YYYY-MM-DD-feature.md`).
- **8.3 (Stop on Deviation):** If execution deviates from the plan, **STOP IMMEDIATELY** and re-plan. No blind trial-and-error.
- **8.4 (User Confirmation):** Plans must include objectives, steps, verification methods, and be confirmed.
- **8.5 (Constitution Check):** Before generating any plan, self-check against this constitution (Constitution Check).

---

## Article 9: Evidence-Based Principle

**Core:** Facts over assumptions (Evidence > Assumptions).

- **9.1 (Log Analysis):** Always base debugging on actual logs and error messages, not guesses.
- **9.2 (Reproduction):** Ensure bugs are reproducible before fixing.
- **9.3 (Verification):** Verify fixes with tests or reproduction scripts.
- **9.4 (Validation-Driven):** Technical decisions must be based on test results or monitoring data.
- **9.5 (Source Verification):** When citing third-party libraries or documentation, must verify credibility.

---

## Article 10: Zero-Friction & TUI-First Principle

**Core:** User interaction must be frictionless. Minimize typing; maximize selection.

- **10.1 (TUI Preference):** Whenever user input is required (in scripts or Agent interactions), provide a TUI (Text User Interface) menu navigable with arrow keys and Enter. Avoid free-text input unless absolutely necessary.
- **10.2 (Smart Defaults):** Always provide a sensible default option that can be selected with a single Enter.
- **10.3 (Option-Based Interaction):** Agents must use multiple-choice options (AskUserQuestion with options) rather than open-ended questions whenever possible.
- **10.4 (Unified Entry):** Complex workflows should be encapsulated in TUI-driven scripts to reduce command memorization.

---

## Article 11: Delivery Standards Principle

**Core:** Start with planning, end with completion.

- **10.1 (Completeness):** Delivered code must be production-ready (Anti-Hackathon). Strictly prohibit leaving `TODO`s.
- **10.2 (Environmental Hygiene):** Must clean up temporary files after operations end.
- **10.3 (Branch Standards):** Strictly prohibit leaking sensitive information (PII, Credentials) in logs, error messages, or test data.
- **10.4 (Edge Case Handling):** Handle errors gracefully. No "Happy Path" only assumptions.
- **10.5 (Staff Engineer Standard):** Ask yourself: "Would a staff engineer approve this?" Verify behavior changes, check logs, and demonstrate correctness before marking as done.

---

## Article 12: Security & Privacy Principle - Non-Negotiable

**Core:** Security is not an add-on feature, it is a fundamental attribute.

- **11.1 (Least Privilege):** Components and accounts should only have the minimum permissions needed to perform their functions.
- **11.2 (Input Validation):** Never trust external input. Must validate and sanitize all input at boundaries.
- **11.3 (Data Privacy):** Strictly prohibit leaking sensitive information (PII, Credentials) in logs, error messages, or test data.
- **11.4 (Dependency Security):** Regularly check and fix dependency library security vulnerabilities.

---

## Article 13: Workflow Control & Handoff Principle - Non-Negotiable

**Core:** Human-in-the-loop control is absolute. AI must pause for confirmation at critical boundaries.

- **12.1 (Atomic Execution):** Tasks must be broken down into atomic steps (Optimization, Planning, Execution Phase, Review, Changelog). AI is strictly prohibited from executing more than one step/phase per turn.
- **12.2 (Mandatory Handoff):** Upon completing ANY workflow step or task phase, AI **must** stop execution and present a TUI (Text User Interface) menu using `AskUserQuestion`.
- **12.3 (Explicit Continuation):** AI cannot proceed to the next step/phase without explicit user selection (e.g., "Continue Execution" or "Next Step").
- **12.4 (Bilingual Options):** All handoff menus must provide bilingual (English/Chinese) descriptions to ensure clarity.

---

## Article 14: Cognitive Architecture Principle

**Core:** Apply mental models (Occam's Razor, Feynman Technique, Socratic Method) to reduce complexity, increase clarity, and validate assumptions.

- **13.1 (Occam's Razor):** "Entities should not be multiplied beyond necessity." Rigorously question the existence of every new Agent, Skill, or Dependency.
- **13.2 (Feynman Technique):** "If you can't explain it simply, you don't understand it." Spec and Code must be self-explanatory to a novice.
- **13.3 (Socratic Method):** Use probing questions ("Why?", "What is the evidence?", "What are the alternatives?") to challenge assumptions and validate First Principles (see Art. 1.5).
- **13.4 (Cognitive Review):** Use the [Mental Model Checklist](../checklists/mental_model_checklist.md) during planning and review phases.

---

## Governance

This constitution has the highest priority. Before implementation, all code modifications must undergo "Constitution Check".
Any plans or code that violate "Non-Negotiable" clauses should be deemed "unconstitutional" and rejected.

### Appendices

- [Go Language Implementation Details](./go_annex.md)
- [Mental Model Whitepaper](../docs/mental_model_whitepaper.md)
- [Mental Model Checklist](../checklists/mental_model_checklist.md)
- (To be added: Frontend implementation details)

---

## Changelog
