# T-Observer GitHub + Subagent-Driven Workflow

## Purpose

This document defines the recommended solo development workflow for executing the MVP plan in this repository.

Reference plan:

- `doc/superpowers/plans/2026-04-19-t-observer-mvp-implementation.md`

## Default Rules

- Use `main` only as the stable integration branch.
- Do not implement MVP tasks directly on `main`.
- Use one GitHub issue per MVP task.
- Use one implementation branch per issue.
- Use one isolated worktree per implementation branch.
- Open one draft PR per implementation branch.
- Complete implementation review in this order:
  1. Implementer pass
  2. Spec-compliance review
  3. Code-quality review

## Issue To Branch Mapping

### Backend

- `#1` `MVP Task 1: Bootstrap Schema, Shared Types, and Test Harness`
  - Branch: `codex/issue-1-bootstrap-schema`
  - Worktree: `.worktrees/codex-issue-1-bootstrap-schema`
- `#2` `MVP Task 2: Implement Local Login Backend and Request Interception`
  - Branch: `codex/issue-2-auth-backend`
  - Worktree: `.worktrees/codex-issue-2-auth-backend`
- `#4` `MVP Task 3: Implement Task Management Backend and Audit Logging`
  - Branch: `codex/issue-4-task-backend`
  - Worktree: `.worktrees/codex-issue-4-task-backend`
- `#7` `MVP Task 4: Implement Observation Record Draft and Submit Backend`
  - Branch: `codex/issue-7-record-backend`
  - Worktree: `.worktrees/codex-issue-7-record-backend`
- `#3` `MVP Task 5: Implement Review and Analytics Backend`
  - Branch: `codex/issue-3-review-analytics-backend`
  - Worktree: `.worktrees/codex-issue-3-review-analytics-backend`

### Frontend

- `#6` `MVP Task 6: Build Frontend Foundation, HTTP Client, and Login Flow`
  - Branch: `codex/issue-6-frontend-auth`
  - Worktree: `.worktrees/codex-issue-6-frontend-auth`
- `#8` `MVP Task 7: Build Task List and Record Form Frontend`
  - Branch: `codex/issue-8-task-record-frontend`
  - Worktree: `.worktrees/codex-issue-8-task-record-frontend`
- `#5` `MVP Task 8: Build Review and Analytics Frontend and Final Verification`
  - Branch: `codex/issue-5-review-analytics-frontend`
  - Worktree: `.worktrees/codex-issue-5-review-analytics-frontend`

## Recommended Execution Order

Follow the plan order because later tasks depend on earlier contracts:

1. `#1` Bootstrap schema and shared types
2. `#2` Local login backend
3. `#4` Task management backend
4. `#7` Observation record backend
5. `#3` Review and analytics backend
6. `#6` Frontend foundation and login
7. `#8` Task list and record frontend
8. `#5` Review and analytics frontend and final verification

## Per-Issue Working Agreement

For each issue:

1. Start from the latest `origin/main`.
2. Create the mapped branch and worktree.
3. Re-read the exact task section in the MVP plan.
4. Implement only the files and behavior needed for that task.
5. Run the task-specific verification listed in the plan.
6. Do a spec-compliance review before a code-quality review.
7. Commit with a message that references the issue when practical.
8. Push the branch and open a draft PR linked to the issue.

## Commit And PR Conventions

- Branch: use the mapped `codex/...` name
- Commit message style:
  - `feat: bootstrap schema and shared enums (#1)`
  - `feat: add local login endpoints (#2)`
- PR title style:
  - `[codex] Task 1 bootstrap schema and shared types`

PR descriptions should include:

- which MVP task and issue they implement
- what changed
- what checks were run
- any follow-up work left for later tasks

## Verification Baseline

### Backend examples

- `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=BootstrapDataTest test`
- `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=AuthControllerTest test`
- `.\mvnw.cmd test`

### Frontend examples

Run from `t-observer-web`:

- `npx vitest run src/stores/auth.spec.ts`
- `npx vitest run src/views/member/MemberTaskListView.spec.ts`
- `npx vitest run src/views/leader/AnalyticsView.spec.ts`
- `npm run type-check`
- `npm run build`

## Notes

- Issue numbers are based on the issues created on April 19, 2026.
- If issue titles or numbers change later, update this document before creating new branches.
