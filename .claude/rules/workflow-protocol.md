# Workflow Rules & Protocols

## 1. Standard Operating Workflow

### 1.1 Strategic Planning (Non-Negotiable)

- **Trigger**: Any non-trivial task (3+ steps or architectural changes).
- **Protocol**:
  - **Plan First**: Generate a plan in `docs/plans/` (e.g. `docs/plans/2025-02-14-feature.md`) with checkable items.
  - **Stop on Deviation**: If execution deviates, **STOP IMMEDIATELY** and re-plan. No blind trial-and-error.
  - **Verify Plan**: Confirm intent with user before writing code.

### 1.2 Execution Loop

- **Track Progress**: Mark items in the active plan file (in `docs/plans/`) as `[x]` in real-time.
- **Autonomous Remediation**: Fix bugs autonomously by analyzing logs/tests.
- **Mandatory Handoff**: Upon completing a Phase, **STOP** and present a TUI menu (Continue/Review). Never auto-proceed to the next Phase.

### 1.3 Self-Improvement Loop

- **Trigger**: Any user correction or rejection.
- **Action**:
  - **Extract Lesson**: Convert the mistake into a rule.
  - **Update Knowledge**: Append to `.claude/lessons.md`.
  - **Pre-load**: Read `.claude/lessons.md` at the start of new sessions.

### 1.4 Quality Gates

- **Principal Engineer Check**: Before handoff, ask: "Is this the most elegant solution?"
- **Definition of Done**:
  - Evidence-based verification (logs, test results).
  - Comparison with `main` branch behavior.
  - No "happy path" assumptions.

# FlowState Workflow Protocols (Detailed Implementation)

## 1. 核心原则 (Core Principles - Detailed)

- **Atomic Execution (原子化执行)**: 每次交互仅执行**一个**步骤 (Step) 或任务阶段 (Phase)。严禁跨越自动执行。
- **Interactive Handoff (交互式交接)**: 每个 Step/Phase 结束后，**必须**展示 TUI 菜单并等待用户指令。
- **Interactive Navigation (交互式导航)**: 所有的 Handoff 必须使用 `AskUserQuestion` 提供方向键选择，然后自动提议下一步。对于安全的流程转换（如进入下一阶段），应设置 `RunCommand(requires_approval=False)`，确保用户只需使用 **方向键 + Enter** 即可直接执行，无需二次确认。
- **Resilient Recovery (弹性恢复)**: 即使在执行过程中遇到错误或中断（如 Code Review 发现问题），一旦修复完成，**必须**立即恢复交互式导航，通过 `AskUserQuestion` 提供下一步选项，绝不让用户退回到手动输入模式。
- **File-First (文件优先)**: 所有长内容（>10 行）必须写入文件，聊天窗口仅保留摘要。
- **Source of Truth (单一真理)**: `docs/plans/` 下的最新计划文件是任务状态的唯一真理。必须先更新文件，再宣称 Phase 完成。

## 2. 工作流规范 (Workflow Specification)

### Step 1: Optimization (Prompt Engineering)

1. **Command**: `/optimize-prompt`
2. **Action**: 交互式优化提示词 -> 生成 `prompt.md`。
3. **Handoff**: 展示 Text-Based 菜单 -> 使用 `AskUserQuestion` 提供箭头选择 -> 选择后用 `RunCommand` 提议 `/write-plan`。

### Step 2: Planning (Architecture & Task Breakdown)

1. **Command**: `/write-plan`
2. **Action**: 读取 `docs/design/` -> 生成 `docs/plans/YYYY-MM-DD-feature.md`。
3. **Constraint**: **STOP** immediately after file generation.
4. **Handoff**: 使用 `AskUserQuestion` 提供箭头选择 -> 选择后用 `RunCommand` 提议 `/execute-plan`。

### Step 3: Execution (The Loop - Task Phases)

1. **Command**: `/execute-plan`
2. **Action**: 读取最新计划文件 -> 执行当前 `in_progress` 的 **Task Phase**。
3. **Completion**:
   - 完成该 Phase 的代码与测试。
   - 更新计划文件 (Mark Phase as `[x]`).
4. **MANDATORY STOP (关键控制点)**:
   - 更新文件后，系统会触发 "STOP EXECUTION NOW" 警告。
   - **必须** 响应此警告，停止思考，展示 TUI。
5. **Handoff**:
   - 使用 `AskUserQuestion` 提供箭头选择。
   - 若选择继续，用 `RunCommand` 提议 `/execute-plan`。

## 3. TUI 交互标准 (Interaction Standards)

**Universal Rule**: 每一个工作流步骤 (Step) 结束后，**必须**展示 TUI 菜单并等待用户指令。严禁自动跳过。所有菜单必须支持**中英双语**。
**Strict Prohibition**: 严禁在文本回复中询问 "Do you want to proceed?" 或 "Which option do you prefer?"。**必须**使用 `AskUserQuestion` 工具来呈现选项。
**Visual Consistency**: 当呈现多个复杂方案时，先用 Markdown 表格或列表对比方案，然后**立即**调用 `AskUserQuestion`，将方案转化为选项。

**关键机制 (Key Mechanism): Interactive Navigation**

1. **Ask**: 使用 `AskUserQuestion` 提供**方向键 (Arrow Keys)** 选择。
2. **Execute**: 用户选择后，**必须**立即使用 `RunCommand` 执行对应操作。
3. **Zero Friction**: 对于标准工作流命令（如 `/plan`, `/execute`, `/review`），必须设置 `requires_approval: false`，实现**一键直达**。

### 3.1 Step 1: Optimization -> Planning

- **Visual**: `[> Optimize] -> [Plan] -> [Execute] -> [Review] -> [Changelog]`
- **Trigger**: `prompt.md` 生成完毕。
- **Menu Options**:
  1. **Start Planning**
     - **Label**: `Start Planning (进入规划阶段)`
     - **Action**: Call `RunCommand(command="/write-plan", requires_approval=False)`
  2. **Refine Prompt**
     - **Label**: `Refine Prompt (继续优化)`
     - **Action**: Wait for user input

### 3.2 Step 2: Planning -> Execution

- **Visual**: `[Optimize] -> [> Plan] -> [Execute] -> [Review] -> [Changelog]`
- **Trigger**: 计划文件生成完毕。
- **Menu Options**:
  1. **Execute Plan**
     - **Label**: `Execute Plan (开始执行计划)`
     - **Action**: Call `RunCommand(command="/execute-plan", requires_approval=False)`
  2. **Review Plan**
     - **Label**: `Review Plan (审查计划)`
     - **Action**: Wait for user input

### 3.3 Step 3: Execution Loop (Phase Handoff)

- **Visual**: `[Optimize] -> [Plan] -> [> Execute] -> [Review] -> [Changelog]`
- **Trigger**: 单个 Task Phase 完成 (Phase Completed)。
- **Menu Options**:
  1. **Continue Execution**
     - **Label**: `Continue Execution (Start Next Phase)`
     - **Description**: `开始 [Next Phase Title]` (Dynamic)
     - **Action**: Call `RunCommand(command="/execute-plan", requires_approval=False)`
  2. **Pause / Review**
     - **Label**: `Pause / Review`
     - **Description**: `暂停执行，审查代码`
     - **Action**: Wait for user input

### 3.4 Step 3 -> Step 4: Execution Done -> Review

- **Visual**: `[Optimize] -> [Plan] -> [Execute] -> [> Review] -> [Changelog]`
- **Trigger**: 所有 Phase 完成 (All Phases Complete)。
- **Menu Options**:
  1. **Proceed to Code Review**
     - **Label**: `Proceed to Code Review (进入代码审查)`
     - **Action**: Call `RunCommand(command="/review-code", requires_approval=False)`
  2. **Generate Changelog**
     - **Label**: `Generate Changelog (生成变更日志)`
     - **Action**: Call `RunCommand(command="/changelog-generator", requires_approval=False)`

### 3.5 Step 4: Review -> Changelog

- **Visual**: `[Optimize] -> [Plan] -> [Execute] -> [Review] -> [> Changelog]`
- **Trigger**: 代码审查报告生成完毕。
- **Menu Options**:
  1. **Generate Changelog**
     - **Label**: `Generate Changelog (生成变更日志)`
     - **Action**: Propose `/changelog-generator`
  2. **Fix Issues**
     - **Label**: `Fix Issues (修复问题)`
     - **Action**: Propose `/write-plan` (to plan fixes)

### 3.6 Error Recovery & Fix Loop (通用修复循环)

- **Visual**: `[Optimize] -> [Plan] -> [> Execute] -> [Review] -> [Changelog]`
- **Trigger**: 错误修复完成 (Fix Applied) 或 审查问题已解决 (Issues Resolved)。
- **Menu Options**:
  1. **Resume / Retry**
     - **Label**: `Resume Workflow (恢复流程)` / `Re-run Review (重新审查)`
     - **Action**: Propose previous command (e.g. `/execute-plan` or `/review-code`)
  2. **Manual Check**
     - **Label**: `Manual Check (手动检查)`
     - **Action**: Wait for user input

### 3.7 Step 5: Changelog -> Commit

- **Visual**: `[Optimize] -> [Plan] -> [Execute] -> [Review] -> [> Changelog] -> [Commit]`
- **Trigger**: CHANGELOG.md 更新完毕。
- **Menu Options**:
  1. **Generate Commit Message**
     - **Label**: `Generate Commit Message (生成提交信息)`
     - **Action**: Call `RunCommand(command="/commit-message-generator", requires_approval=False)`
  2. **Edit Changelog**
     - **Label**: `Edit Changelog (编辑日志)`
     - **Action**: Wait for user input

## 4. 验证与强制机制 (Enforcement)

- **Hook Verification**: 每次 `Write` 操作后，`check-complete.sh` 会自动运行。
- **Stop Signal**: 如果脚本检测到 Task Phase 完成，会输出 `🛑 STOP EXECUTION NOW 🛑` 并显示下一阶段名称。
- **Protocol**: 见到此信号，**必须**立即停止当前推理链，使用 `AskUserQuestion` 展示 TUI 菜单。
