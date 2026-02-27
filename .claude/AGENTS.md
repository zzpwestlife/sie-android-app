# AI Agents & Commands
This file defines the AI capabilities available to this project.

## Agents
The following specialized agents are installed in `.claude/agents/`:

- **Code Reviewer**: `@.claude/agents/code-reviewer.md` - Reviews code against constitution.
- **Code Scribe**: `@.claude/agents/code-scribe.md` - Manages documentation.
- **Security Auditor**: `@.claude/agents/security-auditor.md` - Checks for security issues.

## Commands
Common project commands for the `Makefile`.

- **Build**: `make build` (dev: `make build-dev`)
- **Test**: `make test` (gotestsum), `make race` (race detection)
- **Lint**: `make lint` (staticcheck + nilaway)
- **Format**: `make fmt` (gofumpt)
- **Deps**: `make dep` (go mod tidy)
- **Tools**: `make tools` (install dev tools)
- **All**: `make all` (dep+lint+test)

> *Note: If `Makefile` is missing, check `README.md` for project-specific commands.*

## Guidelines

> **⚠️ Constitution**: This project strictly follows [constitution.md](.claude/constitution/constitution.md).
> All code modifications must comply with its **11 core principles** and relevant **language annexes** (e.g., [Go Annex](.claude/constitution/go_annex.md)).

### Git & Version Control
- **Commit Messages**: **[STRICT]** Conventional Commits (type(scope): subject).
- **Atomic Commits**: Each commit contains only one feature or fix.

### Workflow
1. **Research**: Analyze context and patterns.
2. **Plan**: Use the **Plan Template** below.
3. **Implement**: Write code and tests.
4. **Verify**: Run tests and lint.

## Plan Template (Mandatory)
When creating plans, you **MUST** include the following "Constitution Check" section:

```markdown
## Constitution Check
*GATE: Must pass before technical design.*

- [ ] **Simplicity (Art. 1):** Is the standard library used? Is over-abstraction avoided?
- [ ] **Test First (Art. 2):** Does the plan include writing tests *before* implementation?
- [ ] **Clarity (Art. 3):** Are errors explicitly handled? No global state?
- [ ] **Core Logic (Art. 4):** Is business logic decoupled from HTTP/CLI interfaces?
- [ ] **Security (Art. 11):** Are inputs validated? No sensitive data leakage?

*If any check fails, provide a strong justification.*
```
