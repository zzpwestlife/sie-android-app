# React Native Project Initialization Design

**Date:** 2026-02-27
**Author:** Claude Sonnet 4.5
**Status:** Approved

---

## 1. Overview

This document describes the design for initializing a React Native project with TypeScript, ESLint, Prettier, React Navigation, and Zustand state management. The approach is incremental configuration rather than full project regeneration.

---

## 2. Design Rationale

### 2.1 Approach Selection
**Chosen Approach:** Incremental Configuration (Approach A)

**Reasons:**
1. **Preserve existing structure** — Current project has `.claude/` configuration and Git history.
2. **Minimize dependencies** — Aligns with Constitution Article 1.2 (Minimal Dependencies).
3. **Avoid over-engineering** — Native code can be auto-generated on first run (React Native CLI feature).
4. **Minimal changes principle** — Complies with Constitution Article 5.1 (Minimal Changes).

**Rejected Approach:** Full Reinitialization (Approach B)
- Violates Constitution Article 1.1 (YAGNI) — Current project doesn't need full rebuild.
- Requires manual migration of `.claude/` configuration.

---

## 3. Architecture

### 3.1 TypeScript Configuration
**Goal:** Enable Strict Mode for type safety (Constitution Article 3).

**Key Settings:**
```json
{
  "strict": true,
  "noImplicitAny": true,
  "strictNullChecks": true,
  "noUnusedLocals": true,
  "noUnusedParameters": true,
  "noImplicitReturns": true,
  "paths": {
    "@/*": ["src/*"]
  }
}
```

**Impact:**
- All code must have explicit types.
- Existing `App.tsx` may require type fixes.

---

### 3.2 ESLint Configuration
**Goal:** Enforce type safety and code standards.

**Key Rules:**
- `@typescript-eslint/no-explicit-any`: error (Constitution requirement)
- `@typescript-eslint/no-unused-vars`: error (with `_` prefix exception)
- `import/order`: error (auto-sort imports)
- `no-console`: warn (allow `warn` and `error`)

**Required Dependencies:**
```json
{
  "devDependencies": {
    "@typescript-eslint/eslint-plugin": "^6.12.0",
    "@typescript-eslint/parser": "^6.12.0",
    "eslint-plugin-import": "^2.29.0",
    "eslint-import-resolver-typescript": "^3.6.1"
  }
}
```

---

### 3.3 Runtime Dependencies
**Goal:** Add minimal dependencies for navigation and state management.

**Dependencies:**
```json
{
  "dependencies": {
    "@react-navigation/native": "^6.1.9",
    "@react-navigation/stack": "^6.3.20",
    "react-native-screens": "^3.27.0",
    "react-native-safe-area-context": "^4.7.4",
    "react-native-gesture-handler": "^2.13.4",
    "zustand": "^4.4.7"
  }
}
```

**Rationale:**
- **React Navigation v6**: Industry-standard navigation library.
- **Zustand**: Lightweight state management (zero boilerplate, simple API).

---

### 3.4 Directory Structure
**Goal:** Follow React Native best practices and Constitution Article 5.2 (file size control).

```
src/
├── navigation/
│   ├── AppNavigator.tsx        # Main navigator
│   └── types.ts                # Navigation type definitions
├── screens/
│   ├── HomeScreen.tsx          # Example home screen
│   └── index.ts                # Export all screens
├── stores/
│   ├── useCounterStore.ts      # Example Zustand store
│   └── index.ts                # Export all stores
├── types/
│   └── index.ts                # Global type definitions
└── utils/
    └── index.ts                # Utility functions
```

**Root File Modifications:**
- `App.tsx`: Update to use `AppNavigator`
- `babel.config.js`: Add `react-native-reanimated/plugin`
- `.gitignore`: Add `.claude/tmp/`

---

## 4. Component Design

### 4.1 AppNavigator (`src/navigation/AppNavigator.tsx`)
**Purpose:** Type-safe navigation container.

**Key Features:**
- Uses `createStackNavigator<RootStackParamList>()`
- Wraps with `<NavigationContainer>`
- Initial route: `Home`

**Type Safety:**
```typescript
export type RootStackParamList = {
  Home: undefined;
  // Future screens...
};
```

---

### 4.2 Zustand Store (`src/stores/useCounterStore.ts`)
**Purpose:** Example state management with type safety.

**Interface:**
```typescript
interface CounterState {
  count: number;
  increment: () => void;
  decrement: () => void;
  reset: () => void;
}
```

**Implementation Pattern:**
```typescript
export const useCounterStore = create<CounterState>((set) => ({
  count: 0,
  increment: () => set((state) => ({ count: state.count + 1 })),
  decrement: () => set((state) => ({ count: state.count - 1 })),
  reset: () => set({ count: 0 }),
}));
```

---

### 4.3 HomeScreen (`src/screens/HomeScreen.tsx`)
**Purpose:** Demonstrate navigation and state integration.

**Features:**
- Uses `useCounterStore` hook
- Displays current count
- Provides increment/decrement/reset buttons

**File Header (Constitution Article 5.6):**
```typescript
// INPUT: React, react-native, useCounterStore
// OUTPUT: Home screen component
// POS: Main application interface
```

---

## 5. Data Flow

```
User Action (Button Press)
  ↓
useCounterStore.increment()
  ↓
Zustand State Update
  ↓
HomeScreen Re-render
  ↓
Display Updated Count
```

---

## 6. Error Handling

### 6.1 Type Safety
- All functions have explicit return types
- No `any` types allowed (ESLint enforced)
- Strict null checks enabled

### 6.2 Runtime Errors
- Native module linking failures handled in documentation
- Import errors prevented by TypeScript path resolution

---

## 7. Testing Strategy

### 7.1 Verification Steps
**Phase 4 Validation:**
1. Run `npm run lint` → Verify ESLint configuration
2. Run `npx tsc --noEmit` → Verify TypeScript configuration
3. Run `npm run format` → Verify Prettier configuration
4. Run `npm run android` → Verify native integration

### 7.2 Success Criteria
- No ESLint errors
- No TypeScript errors
- App launches successfully
- Counter buttons work correctly

---

## 8. Risk Assessment

### 8.1 Potential Issues

**Risk 1: Native Dependency Auto-linking Failure**
- **Impact:** `react-native-gesture-handler` may require manual configuration
- **Mitigation:** Add manual Android configuration steps to documentation

**Risk 2: Existing `App.tsx` Overwrite**
- **Impact:** User's custom code may be lost
- **Mitigation:** Read existing `App.tsx` and preserve useful code

**Risk 3: TypeScript Strict Mode Errors**
- **Impact:** Existing code may not pass strict type checks
- **Mitigation:** Fix type errors in Phase 4 validation

---

## 9. Implementation Plan

### Phase 1: Configuration Files Update
1. Update `tsconfig.json` (enable Strict Mode)
2. Update `.eslintrc.js` (add TypeScript rules)
3. Update `babel.config.js` (add reanimated plugin)
4. Update `.gitignore` (add `.claude/tmp/`)

### Phase 2: Dependency Installation
5. Install runtime dependencies (`npm install react-navigation zustand ...`)
6. Install dev dependencies (`npm install -D @typescript-eslint/...`)

### Phase 3: Code Generation
7. Create `src/navigation/` directory and files
8. Create `src/stores/` directory and files
9. Create `src/screens/` directory and files
10. Update `App.tsx` (integrate AppNavigator)

### Phase 4: Validation
11. Run `npm run lint` (verify ESLint)
12. Run `npm run format` (verify Prettier)
13. Run `npx tsc --noEmit` (verify TypeScript)
14. Run `npm run android` (verify native integration)

---

## 10. Constitution Compliance Checklist

- [x] **Article 1.1 (YAGNI):** Only implementing explicitly requested features
- [x] **Article 1.2 (Minimal Dependencies):** Using mature libraries (React Navigation, Zustand)
- [x] **Article 1.3 (Anti-over-engineering):** Incremental approach, not full rebuild
- [x] **Article 2.1 (Testing Strategy):** Validation plan in Phase 4
- [x] **Article 3.1 (Error Handling):** TypeScript Strict Mode enforces explicit error handling
- [x] **Article 3.2 (Explicit Dependencies):** No global state, all dependencies passed explicitly
- [x] **Article 5.1 (Minimal Changes):** Preserving existing structure
- [x] **Article 5.6 (Module Metadata):** All files include INPUT/OUTPUT/POS headers
- [x] **Article 10.3 (TUI Interaction):** Used `AskUserQuestion` for all decision points
- [x] **Article 11.1 (Completeness):** No TODOs in deliverable code

---

## 11. Next Steps

1. Generate implementation plan in `docs/plans/2026-02-27-react-native-initialization.md`
2. Execute plan using `/execute-plan` command
3. Perform validation in Phase 4
4. Generate changelog with `/changelog-generator`

---

## 12. References

- [React Navigation Documentation](https://reactnavigation.org/docs/getting-started)
- [Zustand Documentation](https://github.com/pmndrs/zustand)
- [TypeScript Strict Mode Guide](https://www.typescriptlang.org/tsconfig#strict)
- [Project Constitution](/.claude/constitution/constitution.md)
