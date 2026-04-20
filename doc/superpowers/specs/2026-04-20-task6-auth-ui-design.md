# Task 6 Auth UI Design

## Scope

This design covers the UI shell work for Task 6 only:

- `t-observer-web/src/views/login/LoginView.vue`
- `t-observer-web/src/layouts/MainLayout.vue`

This pass does not wire the real login request flow, route guards, or downstream member/leader business pages. It establishes the visual system and interaction skeleton those pieces will plug into.

## Goals

- Build a polished login page that fits the T-Observer MVP and the user-provided UI constraints.
- Build a reusable application shell with sidebar, header, breadcrumbs, and user menu.
- Keep the structure compatible with the remaining Task 6 auth flow work and the member/leader pages planned in Task 7 and Task 8.
- Use Element Plus component patterns and spacing discipline without inheriting heavy default borders.

## Visual Direction

### Color

- Brand primary: `#409EFF`
- Content background: `#f5f7fa`
- Card background: white or slightly translucent white
- Accent gradients: light blue tones derived from the brand color
- Border treatment: very light neutral lines only when needed; prefer shadow and contrast over strong outlines

### Shape

- Buttons, cards, inputs, dropdown panels, sidebar sections: `8px` border radius
- Avoid mixed radius values except where a larger page-level container is visually necessary

### Typography

- Font stack: system default sans-serif
- Primary page title: `18px`
- Body and helper text: `14px`
- Secondary meta text can be slightly smaller, but the visible hierarchy must stay close to the requested sizes

### Spacing

- Increase padding and margin compared with default scaffold output
- Maintain enough white space between form fields, header groups, sidebar items, and content cards
- Prioritize readable, breathable layout over compact dashboard density

## Login View

### Layout

- Full-height viewport layout
- Center a login card vertically and horizontally
- Keep the card width around `380px-440px` on desktop
- On small screens, keep the card fluid with safe side margins

### Background

- Use a light blue gradient background
- Add a subtle geometric or soft radial highlight layer for atmosphere
- Avoid busy illustrations or dark overlays

### Card Style

- Glassmorphism-inspired surface:
  - semi-transparent white background
  - soft backdrop blur
  - light shadow
- No heavy borders
- Rounded corners aligned to the 8px system or a close page-level variant that still reads consistently

### Form Composition

- Use Element Plus form building blocks:
  - `ElCard` or a custom glass container
  - `ElForm`
  - `ElFormItem`
  - `ElInput`
  - `ElButton`
- Show:
  - page title
  - short helper copy
  - username field
  - password field
  - primary login button

### Field Behavior

- Inputs receive a clear focus state with brand-blue highlight
- Focus should be readable but not harsh: thin border shift plus subtle shadow
- Validation styling remains lighter than stock Element Plus error emphasis

### Button Behavior

- Login button uses `#409EFF`
- Hover: slightly brighter or stronger shadow
- Active/click: subtle press feedback through transform or shadow reduction

## Main Layout

### High-Level Structure

- Left sidebar navigation
- Top header across the content area
- Main content zone on `#f5f7fa`
- White inner content cards for the member, leader, and analytics pages that will be mounted inside the shell in later tasks

### Sidebar

- Default state: expanded on desktop
- Desktop: supports manual collapse
- Mobile: becomes a drawer
- Visual treatment:
  - cleaner Element Plus admin style
  - low-border, low-noise surface
  - branded active state using the primary blue

### Header

- Left section:
  - sidebar toggle
  - breadcrumb trail
- Right section:
  - avatar trigger
  - current user name
  - role text
  - dropdown menu with at least:
    - a non-destructive profile menu item for shell completeness
    - logout

### Content Area

- Background stays `#f5f7fa`
- Inner content uses white cards with soft shadows and 8px radius
- Width and padding scale down for tablet and mobile

## Responsive Rules

### Desktop

- Sidebar fixed at the left
- Header remains compact and horizontal
- Login page shows centered card with full atmospheric background

### Tablet

- Reduce outer padding
- Keep sidebar and header readable
- Prevent breadcrumb and user menu crowding

### Mobile

- Sidebar switches to drawer mode
- Header content wraps or compresses gracefully
- Login card fills available width with safe padding
- Touch targets remain comfortable

## Component Boundaries

### `LoginView.vue`

Responsible for:

- visual page background
- login card shell
- form layout and visual interaction states
- local visual button interaction state, including hover and active feedback, without invoking the real auth API yet

Not responsible for:

- final auth API wiring in this pass
- route redirection logic

### `MainLayout.vue`

Responsible for:

- sidebar shell
- collapse state
- mobile drawer state
- header, breadcrumb skeleton, user dropdown shell
- slot/container for child content

Not responsible for:

- final route-aware menu map for all business pages
- fully dynamic breadcrumb registry

## Element Plus Usage Strategy

Recommended approach: use Element Plus as the structural base, then tune its visual weight.

- Keep native behavior and accessibility where practical
- Override:
  - border heaviness
  - shadows
  - background transparency
  - active/hover brand tone
- Avoid rebuilding standard form and dropdown primitives from scratch

## Styling Strategy

- Define a small set of CSS variables in the relevant page/layout scope or shared app scope:
  - primary color
  - content background
  - card background
  - border color
  - shadow values
  - radius
- Keep style tokens aligned between login page and main shell so later pages inherit a stable visual baseline

## Testing Strategy

### Login View

- Rendering test:
  - login title appears
  - username/password fields appear
  - primary login button appears

### Main Layout

- Rendering test:
  - sidebar container appears
  - breadcrumb/header region appears
  - user dropdown trigger appears

### Build Safety

- `npm run type-check`
- `npm run build`

## Risks And Guardrails

- Do not let the design drift into a generic dark dashboard; the requested tone is light, airy, and blue-led.
- Do not rely on strong border boxes for separation; prefer spacing, shadow, and background contrast.
- Keep the shell compatible with the remaining auth wiring work and with future role-aware navigation changes.
- Avoid introducing broad design-system refactors outside Task 6.
