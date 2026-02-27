# Go Profile Configuration

<!--
Purpose: Go-specific overrides for this project.
Note: Core configuration is loaded via CLAUDE.md → AGENTS.md
-->

# Go-Specific Guidelines

## Testing
- Use table-driven tests
- Test files should be in the same package with `_test.go` suffix

## Code Style
- Use `gofumpt` for formatting
- Use `goimports` for import management
- No `else` after return in early-return pattern
