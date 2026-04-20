# Task 6 Auth Flow And Chinese UI Design

## Scope

This design extends the current Task 6 frontend foundation so the app can:

- perform real local login against the backend
- validate persisted tokens through `/api/auth/me` on refresh and protected-route entry
- enter the existing main shell preview after successful login
- show Chinese UI copy consistently across the login page and shell

This pass does not add real member, leader, or admin business pages. All roles continue to land on the existing shell preview route and shared placeholder child pages.

## Goals

- Turn the current login page from UI-only into a working local login entry.
- Restore login state safely on refresh by validating tokens against the backend.
- Protect shell routes so anonymous or expired sessions return to the login page.
- Replace visible English UI copy with Chinese copy and keep that as the default going forward.
- Show the current user’s Chinese real name and Chinese role label in the main shell.

## Confirmed Product Decisions

- Login success redirects all roles to the same existing shell entry route.
- The unified post-login landing route is `/overview`.
- On application startup, refresh, or protected navigation, the frontend must call `/api/auth/me` to validate any persisted token.
- Visible UI copy should be Chinese by default from now on.
- Role label mapping:
  - `LEADER` -> `组长`
  - `MEMBER` -> `教师`
  - `ADMIN` -> `管理员`

## Backend Contract Assumptions

This design follows the current MVP plan and existing backend auth contract:

- `POST /api/auth/login`
  - accepts local username and password
  - returns `token`, `userId`, `realName`, and `roleCode`
- `GET /api/auth/me`
  - reads `X-Auth-Token`
  - returns the current user profile for a valid token
  - fails for missing or invalid tokens

If the backend response wrapper includes a top-level `data` field, the frontend auth API layer should unwrap and return the inner payload consistently.

## Auth Flow

### Login Submission

- The login page collects username and password.
- Clicking the primary login button sends a request to `POST /api/auth/login`.
- On success:
  - persist the returned token and user identity into the auth store
  - mark the session as validated for the current app lifecycle
  - navigate to `/overview`
- On failure:
  - keep the user on the login page
  - show a clear Chinese error message
  - stop loading state cleanly

### Session Restore

- If the app starts with no persisted token, the user is treated as anonymous.
- If a persisted token exists, protected-route entry triggers a validation request to `GET /api/auth/me`.
- If `/me` succeeds:
  - refresh the auth store with the current backend user identity
  - allow entry into the protected shell route
- If `/me` fails:
  - clear local auth state and persisted cache
  - redirect to `/login`

### Route Protection

- `/login` remains public.
- The shell route and its child routes are protected.
- Visiting a protected route without a token redirects to `/login`.
- Visiting `/login` while already authenticated and validated redirects to `/overview`.

### Revalidation Strategy

- The frontend should avoid calling `/me` repeatedly on every route change within the same validated session.
- A session-level validation flag in the auth store should record whether the current in-memory session has already been verified.
- Logging out or a failed `/me` call resets that validation state.

## API Layer Design

### `src/api/auth.ts`

Add a dedicated auth API module with two functions:

- `login(payload)`
  - sends username and password to `/auth/login`
  - returns the unwrapped login payload
- `fetchCurrentUser()`
  - sends `GET /auth/me`
  - returns the unwrapped current-user payload

The module should stay small and only encode the auth endpoints and response typing needed by Task 6.

### `src/types/auth.ts`

Add shared frontend auth types for:

- login request payload
- login response payload
- current user payload
- role code union

The auth API layer, store, and route guard should all use the same shared types to avoid drift.

## Auth Store Design

### State

Keep persisted auth identity in the store:

- `token`
- `roleCode`
- `realName`
- `userId`

Add one non-persisted session validation flag:

- `isValidated`

`isValidated` tracks whether the current runtime session has already completed a successful `/me` validation. It should not be the source of truth across reloads; reloads should still require `/me`.

### Actions

- `acceptLogin(payload)`
  - stores token and identity returned from login
  - persists them to `localStorage`
  - sets `isValidated = true`
- `acceptCurrentUser(payload)`
  - updates `realName`, `roleCode`, and `userId` from `/me`
  - preserves the current token
  - refreshes persisted cache
  - sets `isValidated = true`
- `logout()`
  - clears token, identity, and persisted cache
  - resets `isValidated = false`

### Derived Data

The store should expose a Chinese role label helper or computed mapping:

- `LEADER` -> `组长`
- `MEMBER` -> `教师`
- `ADMIN` -> `管理员`

That mapping should be the single source for role display in the shell.

## Router Design

### Public Route

- `/login`

### Protected Shell

Keep the current main shell route structure and placeholder children, with the shell acting as the parent route.

- `/`
  - redirects to `/overview`
- `/overview`
- `/tasks`
- `/records`

All shell routes are protected by the auth guard.

### Guard Behavior

Before entering any protected route:

1. If no token exists, redirect to `/login`.
2. If token exists and `isValidated` is already `true`, allow navigation.
3. If token exists and `isValidated` is `false`, call `/api/auth/me`.
4. On `/me` success, update store and allow navigation.
5. On `/me` failure, clear auth state and redirect to `/login`.

Before entering `/login`:

1. If no token exists, allow navigation.
2. If token exists and `isValidated` is already `true`, redirect to `/overview`.
3. If token exists and `isValidated` is `false`, call `/api/auth/me`.
4. On success, redirect to `/overview`.
5. On failure, clear auth state and allow the login page.

## Login View Design

### Behavior

Keep the approved visual layout and upgrade it into a working form.

- Add reactive form state for username and password.
- Add Chinese labels, helper copy, and button text.
- Add Element Plus form validation for required fields.
- Add loading state while the login request is in flight.
- Disable or visually lock the submit button during submission.
- Show backend error feedback in Chinese where possible.

### Chinese Copy Direction

The login page should display Chinese by default. Example copy direction:

- brand remains `T-Observer`
- page title: local account login in Chinese
- helper copy: explain entering the observation workspace in Chinese
- field labels: username and password in Chinese
- submit button: login in Chinese

Exact final phrasing can stay concise as long as the tone remains aligned with the existing clean administrative UI.

## Main Layout Design

### User Display

Replace the shell’s static preview identity with real store-backed identity:

- show the logged-in user’s `realName`
- show the Chinese role label derived from `roleCode`

### Menu Copy

Change visible shell copy to Chinese, including:

- sidebar item labels
- breadcrumb labels
- user dropdown entries
- placeholder child-page headings and descriptions

### Logout

The user dropdown logout action should:

- call `authStore.logout()`
- navigate to `/login`

### Placeholder Pages

The existing overview, tasks, and records child routes may remain lightweight placeholders for now, but their visible text should also become Chinese.

## Chinese UI Rule

This task establishes a standing frontend rule for this repository:

- visible UI text should default to Chinese unless the user explicitly asks otherwise
- technical identifiers, route names, and internal codes may remain English
- user-facing role labels must use the Chinese mapping defined above

## Error Handling

- Invalid credentials should show a user-readable Chinese message on the login page.
- Failed session restore should not leave stale user info visible.
- Logout should always succeed locally, even if no backend logout endpoint exists yet.
- Guard failures should prefer redirecting to `/login` over leaving the app in a partially authenticated shell state.

## Testing Strategy

### Auth Store

Extend `src/stores/auth.spec.ts` to cover:

- `acceptLogin` persistence
- `acceptCurrentUser` refresh behavior
- `logout()` clearing state and cache
- Chinese role label mapping

### Auth API

Add `src/api/auth.spec.ts` to cover:

- `login` calling the login endpoint
- `fetchCurrentUser` calling the me endpoint

### Router Guard

Add a focused router/auth guard test that covers:

- anonymous user hitting a protected route goes to `/login`
- persisted token plus successful `/me` goes to `/overview`
- persisted token plus failed `/me` clears auth and returns to `/login`

### View Tests

Keep and update:

- `LoginView.spec.ts`
- `MainLayout.spec.ts`

Both should assert against Chinese visible copy where the contract is user-facing.

### Build Safety

Run at least:

- `npx vitest run src/stores/auth.spec.ts`
- `npx vitest run src/api/auth.spec.ts`
- any new router guard spec
- `npx vitest run src/views/login/LoginView.spec.ts --environment jsdom`
- `npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom`
- `npm run type-check`
- `npm run build`

## Risks And Guardrails

- Do not add a backend logout endpoint in this frontend task unless explicitly requested.
- Do not split roles into separate landing pages yet.
- Do not silently bypass `/api/auth/me` on refresh, because that would conflict with the confirmed session-restore rule.
- Avoid duplicated role-label mappings across files; keep one frontend source of truth.
- Avoid repeated `/me` calls during a single validated session by tracking validation state in memory.
- Keep the current shell architecture compatible with future role-aware child routes.
