# TurfConnect — Frontend UI Guidelines & Design System

This document outlines the **Athletic Synergy** design system implemented in the TurfConnect React/Vite frontend. It serves as the single source of truth for frontend styling, component usage, and aesthetic standards.

## 1. Design System Overview

**Athletic Synergy** is designed to feel highly premium, dynamic, and state-of-the-art. It avoids generic "minimum viable product" looks by heavily utilizing tailored HSL colors, glassmorphism, micro-animations, and generous whitespace.

### Color Palette

| Token | Hex / HSL | Usage |
| :--- | :--- | :--- |
| **Primary (Deep Athletic Green)** | `#1B5E20` | Brand identity, primary buttons, hero gradients |
| **Secondary (Vibrant Grass Green)**| `#4CAF50` | Success states, active indicators, secondary elements |
| **Accent (Energetic Orange)** | `#FF6D00` | CTA buttons ("Book Now", "Check Availability"), attention grabbers |
| **Surface** | `#FFFFFF` | Card backgrounds, modals, dropdowns |
| **Background** | `#F4FAFF` | Main application background |

### Typography

- **Font Family:** `Inter` (sans-serif)
- **Hierarchy:**
  - `h1`: 3xl-6xl, Extra Bold, tight tracking
  - `h2`: 2xl-3xl, Bold
  - `body`: text-base, medium weight
  - `small`: text-sm/xs, often uppercase with wide tracking for metadata

### Animations & Interactions

- **Hover Lifts:** Interactive cards use `-translate-y-1` and expanded shadows on hover.
- **Glassmorphism:** Overlays and hero badges use `backdrop-blur-md` with semi-transparent backgrounds.
- **Micro-animations:** Subtle pulse effects (`animate-ping`) on live availability indicators and smooth slide-ins for lists.

---

## 2. Component Hierarchy

All core UI components reside in `src/components/ui/` and are built using Tailwind CSS. They are designed to be fully reusable.

### `Button`
The standard button component supports multiple variants (`primary`, `secondary`, `accent`, `outline`, `ghost`, `danger`) and handles loading states internally.

### `Card`
A compound component used for structured data (like `TurfCard`).
- Includes `Card`, `CardContent`, `CardHeader`, `CardTitle`, and `CardFooter`.
- The `interactive` prop enables hover animations.

### `Badge`
Used for statuses, ratings, and tags. Supports `primary`, `success`, `warning`, `danger`, and `default`.

### `Skeleton`
Animated pulse loaders for different shapes (`rectangular`, `circular`, `text`).

---

## 3. Best Practices & Accessibility

1. **Accessibility (a11y):** All interactive elements have proper focus rings (`focus:ring-primary`). Inputs use associated labels and `aria-label` tags.
2. **Responsiveness:** Grids naturally collapse from 3-4 columns on desktop down to 1 column on mobile (`grid-cols-1 md:grid-cols-2 lg:grid-cols-3`).
3. **Performance:** Skeleton loaders are used instead of blank screens while fetching data, preventing layout shifts.
