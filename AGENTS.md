# AGENTS.md

## Purpose

This file defines how Codex and other agents should work in this repository.
Follow these project rules before making code, test, branch, or GitHub changes.

## Project Map

- Root project: Spring Boot backend
- Backend source: `src/main/java/com/edu/tobserver`
- Backend resources: `src/main/resources`
- Backend tests: `src/test/java`
- Frontend project: `t-observer-web`
- Frontend source: `t-observer-web/src`
- MVP implementation plan: `doc/superpowers/plans/2026-04-19-t-observer-mvp-implementation.md`

## Source Of Truth

Before implementing MVP work, read:

1. `doc/superpowers/plans/2026-04-19-t-observer-mvp-implementation.md`
2. `pom.xml`
3. `t-observer-web/package.json`

The current MVP plan is the default execution guide unless the user explicitly changes scope.
Do not silently change architecture, stack, role model, API shape, or data contracts defined there.

## Architecture Constraints

### Backend

- Keep backend code in the root Spring Boot project under `src/main/java`.
- Use Java 17 with Spring Boot 4.x conventions already present in `pom.xml`.
- Use MyBatis for persistence.
- Use Lombok where the project already relies on it for boilerplate reduction.
- Do not introduce Java `record` types for backend DTO, VO, entity, or response objects unless the user explicitly asks for them.
- Prefer lightweight interceptor-based auth for MVP instead of introducing full Spring Security unless the user requests it.
- Use H2-backed integration tests for backend verification where the plan calls for them.
- Keep shared enums, DTOs, VOs, and response wrappers aligned with the plan.
- Keep backend code style aligned with the existing stack in `pom.xml`, including MySQL, H2, and the currently declared Redis support.

### Frontend

- Keep frontend work inside `t-observer-web`.
- Use Vue 3, TypeScript, Pinia, Vue Router, and Vite.
- Follow the role-based page structure in the MVP plan.
- Keep frontend DTO and VO types aligned with backend contracts.

### Cross-Cutting Contracts

Unless the user explicitly changes them, preserve these codes:

- Roles: `LEADER`, `MEMBER`, `ADMIN`
- Task statuses: `PENDING`, `IN_PROGRESS`, `COMPLETED`
- Record statuses: `DRAFT`, `SUBMITTED`, `RETURNED`, `APPROVED`
- Dimension codes:
  - `TEACHING_DESIGN`
  - `CLASSROOM_ORGANIZATION`
  - `TEACHING_CONTENT`
  - `INTERACTION_FEEDBACK`
  - `TEACHING_EFFECTIVENESS`

## Default Execution Workflow

For plan-driven implementation work, use this order:

1. Read the relevant plan task and identify its exact file list and acceptance checks.
2. Prefer isolated branch-based work instead of coding directly on `main`.
3. Implement task-by-task instead of mixing multiple unrelated plan tasks in one change.
4. Write or run the failing test first when the plan provides a concrete test-first step.
5. Implement the smallest change that makes the task pass.
6. Run the verification commands named in the plan before claiming success.
7. Report what changed, what was verified, and what remains.

## Subagent-Driven Development

When executing the MVP plan, prefer a Subagent-Driven workflow:

1. One plan task at a time.
2. One implementation branch or worktree per task when practical.
3. Implementer step first.
4. Spec-compliance review second.
5. Code-quality review third.
6. Do not move to the next task while review issues remain open.

Do not dispatch multiple implementation agents against overlapping files at the same time.

## Git And Branch Rules

- Do not develop directly on `main` unless the user explicitly asks for it.
- Prefer branch names under `codex/`.
- Keep commits scoped to one coherent task.
- Reference the related GitHub issue in commit messages or PR descriptions when applicable.
- Do not rewrite history, force-push, or reset user work unless explicitly requested.
- Do not revert unrelated local changes.

## GitHub Workflow

For single-developer feature execution, prefer:

- `1 plan task = 1 GitHub issue`
- `1 issue = 1 branch`
- `1 branch = 1 draft PR`

If the user asks for a lighter workflow, confirm the simplified scope before collapsing tasks together.

## Editing Guardrails

- Do not perform unrelated refactors while implementing MVP tasks.
- Do not rename files, move packages, or replace libraries unless the task requires it.
- Keep backend and frontend field names consistent.
- If a schema or API contract changes, update all dependent layers in the same task or clearly report the gap.
- Add brief comments only where the logic is non-obvious.

## Verification Requirements

Do not claim work is complete without fresh verification evidence.

### Backend checks

Use the smallest command that proves the claim, for example:

- `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=BootstrapDataTest test`
- `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=AuthControllerTest test`
- `.\mvnw.cmd test`

### Frontend checks

Run from `t-observer-web` as needed:

- `npx vitest run src/stores/auth.spec.ts`
- `npx vitest run src/views/member/MemberTaskListView.spec.ts`
- `npx vitest run`
- `npm run type-check`
- `npm run build`

Only report a check as passing if you ran it in the current task and confirmed the result.

## Communication Expectations

When reporting progress or completion:

- State the task or file area being changed.
- Mention any assumptions you made.
- Report exactly which verification commands were run.
- Call out blockers, skipped checks, or unresolved risks plainly.

## When Unsure

- Ask before making changes with hidden product or workflow consequences.
- Prefer the plan over guesswork.
- Prefer small, reviewable changes over broad edits.
