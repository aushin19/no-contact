---
name: "mobile-mockup-imagegen"
description: "Generate high-fidelity mobile app UI screen mockups using the built-in image_gen tool. Use this skill whenever the user wants to generate, visualize, or render a mobile app screen, UI screen, app mockup, phone screen design, or any mobile interface visual. Trigger on phrases like 'generate a screen', 'create a mockup', 'show me the UI', 'design this screen', 'render this app screen', 'mobile UI', 'app interface mockup', or any request to visualize a mobile app layout. Always use this skill — even for partial requests like 'show me how the home screen looks' or 'create the onboarding screen'. Only generates mobile screen UI mockups — not desktop UIs, illustrations, icons, or generic images."
---

# Mobile App UI Mockup Skill

Generates pixel-accurate, high-fidelity mobile app screen mockups using the built-in `image_gen` tool **only**. No CLI. No fallback. No desktop UIs. No generic image generation.

---

## Step 0 — DESIGN.md Gate (Mandatory)

**Before generating any mockup**, check if the user has attached or uploaded a `DESIGN.md` file in the conversation.

- **If DESIGN.md is present**: Extract and load the full design system — colors, typography, component styles, spacing, shadows, and radius values. Use it as ground truth for all visual decisions.
- **If DESIGN.md is missing**: Ask the user to attach it before proceeding.

> "Please attach your `DESIGN.md` file so I can generate mockups that accurately follow your design system — colors, typography, components, and spacing."

Do not skip this step. Do not guess or invent a design system. The mockup must reflect the actual project design system.

**What to extract from DESIGN.md:**
- Primary / background / surface colors
- Typography: font family, sizes, weights, letter-spacing per role (hero, heading, body, caption, button)
- Component styles: button variants, card shadows, badge styles, nav bar style
- Border radius scale
- Depth / shadow system
- Accent colors and their roles
- Do's and Don'ts section (treat as hard constraints)

---

## Scope — What This Skill Does

**Only generates:**
- Mobile app screen mockups (portrait orientation, phone viewport)
- Single screens or multi-screen flows (e.g., onboarding, home, settings, profile, dashboard)
- Realistic UI mockups with accurate component placement — not wireframes, not icons, not illustrations

**Does not generate:**
- Desktop or tablet UIs
- Generic images, photos, or illustrations
- Logos or icons in isolation
- Wireframes or low-fidelity sketches (unless user explicitly asks for lo-fi)

If the user asks for something outside this scope, redirect: *"This skill only generates mobile screen mockups. For [X], please use the general imagegen skill."*

---

## Execution Mode

**One mode only: built-in `image_gen` tool.**

- Never use CLI fallback
- Never use `scripts/image_gen.py`
- Never ask for `OPENAI_API_KEY`
- If `image_gen` is unavailable, tell the user the tool is not available in this session — do not attempt workarounds

---

## Workflow

### 1. Gate Check
Confirm DESIGN.md is present and parsed. If not, request it (see Step 0).

### 2. Clarify Screen Intent
If the user hasn't specified, ask:
- Which screen? (Home, Onboarding, Login, Settings, Profile, Dashboard, etc.)
- What is the primary action or content on this screen?
- Any specific content or copy to include? (headlines, labels, placeholder data)
- Dark mode or light mode? (default: match DESIGN.md background)

Only ask questions that are genuinely ambiguous. If the intent is clear, proceed.

### 3. Build the Prompt

Construct a structured prompt using the schema below. Pull all visual values directly from the loaded DESIGN.md — never invent colors, fonts, or spacing.

```
Use case: mobile-screen-mockup
Screen: <screen name, e.g. "Home Feed", "Onboarding Step 1 of 3">
Platform: iOS / Android (infer from context; default iOS)
Orientation: Portrait
Viewport: 390×844px (iPhone 14 Pro equivalent) or 360×800px (Android standard)
Frame: NONE — flat screenshot crop only. No phone bezel, no device chrome, no notch, no Dynamic Island, no hardware buttons, no rounded device corners. Output is the screen content only, edge-to-edge.
Mode: Light / Dark

--- Design System (from DESIGN.md) ---
Background: <exact hex>
Surface / Card: <exact hex + shadow string>
Primary text: <exact hex, font, size, weight, letter-spacing>
Secondary text: <exact hex, font, size, weight>
Button (primary): <bg, text color, radius, padding, shadow>
Button (secondary): <bg, text color, radius, shadow>
Accent colors: <name: hex — role>
Border / shadow style: <exact shadow string>
Radius scale: <values in use>

--- Screen Content ---
Navigation bar: <describe top nav — title, icons, back arrow, etc.>
Hero / Header: <content, typography spec>
Body content: <cards, lists, sections — describe each with design system values>
Bottom bar / Tab bar: <items, active state>
CTA: <label, button style>
Status bar: <carrier, time, battery — light or dark icons>

--- Constraints ---
<paste Do's from DESIGN.md>
Avoid: <paste Don'ts from DESIGN.md>
Text (verbatim): "<any exact copy>"
```

### 4. Generate
Call `image_gen` with the constructed prompt. Request 1024×1024 or portrait-optimized dimensions.

### 5. Validate Output
After generation, check:
- [ ] No phone frame, bezel, device chrome, or hardware elements visible
- [ ] Colors match DESIGN.md palette exactly
- [ ] Typography reflects correct sizes, weights, and letter-spacing from DESIGN.md
- [ ] Components (buttons, cards, nav) match component specs from DESIGN.md
- [ ] No invented colors, fonts, or UI patterns not in DESIGN.md
- [ ] Screen content is legible and properly laid out

If validation fails on a critical point, iterate with a single targeted correction.

### 6. Iterate
For follow-up edits, re-use the same structured prompt, mark what changed, and note invariants explicitly: *"Change only [X]; keep [Y] unchanged."*

---

## Prompt Schema — Quick Reference

```
Use case: mobile-screen-mockup
Screen: [name]
Platform: iOS | Android
Orientation: Portrait
Viewport: 390×844px
Frame: NONE — screenshot crop, no device chrome
Mode: Light | Dark
Background: [hex]
Primary text: [hex, font, size, weight, tracking]
Secondary text: [hex, font, size, weight]
Card surface: [hex + shadow]
Button primary: [bg, text, radius, shadow]
Accent: [hex — role]
Nav bar: [description]
Content: [section-by-section breakdown]
CTA: [label + button style]
Constraints: [from DESIGN.md Do's]
Avoid: [from DESIGN.md Don'ts]
Text (verbatim): "[exact UI copy]"
```

---

## Prompt Augmentation Rules

- **Specific prompt**: Normalize and structure it. Do not add creative embellishments.
- **Generic prompt**: Add only layout/composition hints that improve fidelity. Never add colors, fonts, or components not in DESIGN.md.
- **Always include**: status bar (as UI element within the screen), edge-to-edge layout filling the full viewport.
- **Never add**: phone frame, device bezel, hardware buttons, notch/Dynamic Island as device chrome, rounded device corners, drop shadows implying a floating device. extra characters, fictional brand elements, colors outside DESIGN.md, non-standard UI patterns.

---

## Mobile Screen Taxonomy

Classify each request into one:

| Slug | Description |
|------|-------------|
| `onboarding-screen` | Welcome, value prop, sign-up, permissions |
| `auth-screen` | Login, signup, forgot password |
| `home-feed` | Main feed, dashboard, activity stream |
| `detail-screen` | Article, product, profile, item detail |
| `settings-screen` | Preferences, toggles, account settings |
| `profile-screen` | User profile, stats, avatar, bio |
| `search-screen` | Search bar, filters, results |
| `form-screen` | Input forms, checkout, multi-step flow |
| `empty-state` | No data, error, 404 screen |
| `modal-overlay` | Bottom sheet, dialog, action sheet |
| `tab-navigation` | Tab bar focused screen |
| `notification-screen` | Notification list or alert overlay |

---

## Example Prompt (Vercel Design System)

```
Use case: mobile-screen-mockup
Screen: Home Dashboard
Platform: iOS
Orientation: Portrait
Viewport: 390×844px
Frame: NONE — flat screenshot, no device chrome, no bezel, no notch, no hardware. Screen content fills edge-to-edge.
Mode: Light

Background: #ffffff
Primary text: #171717, Geist Sans, 16px, weight 500, letter-spacing normal
Secondary text: #4d4d4d, Geist Sans, 14px, weight 400
Card surface: white background, shadow: rgba(0,0,0,0.08) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 2px, #fafafa 0px 0px 0px 1px, radius 8px
Button primary: background #171717, text #ffffff, radius 6px, padding 8px 16px
Accent colors: Develop Blue #0a72ef (active states), Ship Red #ff5b4f (alerts)
Border style: shadow-as-border rgba(0,0,0,0.08) 0px 0px 0px 1px — no traditional CSS borders

Status bar: Light mode, carrier left, time center, battery/wifi right
Nav bar: White sticky header, "Dashboard" title 16px Geist weight 600 #171717, settings icon right, shadow-border bottom
Content:
  - Section label "Projects" — 12px Geist Mono uppercase, #4d4d4d
  - 2 project cards (full card shadow), each: title 24px weight 600 letter-spacing -0.96px, subtitle 14px #4d4d4d, status badge pill (#ebf5ff bg, #0068d6 text, 9999px radius)
  - Divider: 1px solid #171717
  - Section label "Activity" — same mono style
  - 3 list items with shadow-border, 16px text
Bottom tab bar: 4 tabs — Home (active, #0a72ef), Projects, Deploy, Settings. White bar, shadow-border top.

Constraints: Use shadow-as-border only — no CSS borders. Three font weights max: 400, 500, 600. Negative letter-spacing at display sizes. Workflow accent colors (Red/Pink/Blue) only in context.
Avoid: Warm colors, yellow/orange/green in chrome, border-radius 9999px on buttons, heavy shadows (>0.1 opacity), positive letter-spacing on Geist.
Text (verbatim): "Dashboard", "Projects", "Activity", "Deploy"
```

---

## Output Reporting

After each generation, report:
- Screen generated: `[screen name]`
- Design system applied: `[DESIGN.md source]`
- Tool used: `image_gen (built-in)`
- Any deviations from DESIGN.md (if unavoidable, explain why)
- Suggested next screens or iteration notes