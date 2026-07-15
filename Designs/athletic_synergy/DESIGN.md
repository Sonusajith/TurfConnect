---
name: Athletic Synergy
colors:
  surface: '#f4faff'
  surface-dim: '#cfdce4'
  surface-bright: '#f4faff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#e9f6fd'
  surface-container: '#e3f0f8'
  surface-container-high: '#ddeaf2'
  surface-container-highest: '#d7e4ec'
  on-surface: '#111d23'
  on-surface-variant: '#41493e'
  inverse-surface: '#263238'
  inverse-on-surface: '#e6f3fb'
  outline: '#717a6d'
  outline-variant: '#c0c9bb'
  surface-tint: '#2a6b2c'
  primary: '#00450d'
  on-primary: '#ffffff'
  primary-container: '#1b5e20'
  on-primary-container: '#90d689'
  inverse-primary: '#91d78a'
  secondary: '#006e1c'
  on-secondary: '#ffffff'
  secondary-container: '#91f78e'
  on-secondary-container: '#00731e'
  tertiary: '#583100'
  on-tertiary: '#ffffff'
  tertiary-container: '#794500'
  on-tertiary-container: '#ffb66b'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#acf4a4'
  primary-fixed-dim: '#91d78a'
  on-primary-fixed: '#002203'
  on-primary-fixed-variant: '#0c5216'
  secondary-fixed: '#94f990'
  secondary-fixed-dim: '#78dc77'
  on-secondary-fixed: '#002204'
  on-secondary-fixed-variant: '#005313'
  tertiary-fixed: '#ffdcbe'
  tertiary-fixed-dim: '#ffb870'
  on-tertiary-fixed: '#2c1600'
  on-tertiary-fixed-variant: '#693c00'
  background: '#f4faff'
  on-background: '#111d23'
  surface-variant: '#d7e4ec'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 48px
  xl: 80px
  gutter: 20px
  margin-mobile: 16px
  margin-desktop: 40px
---

## Brand & Style

The design system is engineered to evoke the high-octane energy of a match day while maintaining the technical precision of a premium booking platform. The brand personality is **Athletic, Reliable, and Kinetic**. It targets active individuals and team organizers who value efficiency and professional-grade quality.

The aesthetic follows a **Corporate-Modern** direction with **Tactile** influences. This is achieved through a structural card-based layout, high-performance typography, and a "field-ready" color palette. The UI should feel fast and responsive, utilizing generous whitespace to allow the vibrant primary greens to signify growth, vitality, and the physical turf itself. Every interaction should feel like a successful play: intentional, smooth, and rewarding.

## Colors

The color strategy is rooted in the natural tones of professional sports environments. 

- **Primary & Secondary Greens:** The "Deep Athletic Green" (#1B5E20) provides the grounded, professional foundation used for navigation and core branding. The "Vibrant Grass Green" (#4CAF50) is used for "Success" states, active indicators, and secondary buttons, mirroring the freshness of a well-maintained pitch.
- **Action Orange:** The "Energetic Orange" (#FF9800) is reserved exclusively for high-priority Call-to-Actions (CTAs) and urgent alerts, creating a powerful visual contrast against the green spectrum.
- **Neutrals:** We utilize "Slate Gray" (#263238) for deep contrast in typography and "Crisp White" for surface containers to maintain a clean, tech-forward atmosphere.

## Typography

The design system exclusively utilizes **Inter** to project a modern, systematic, and utilitarian feel. 

Headlines use heavy weights (700-800) and tight letter-spacing to mimic sports broadcasting graphics. Body text remains clean and highly legible with standard tracking. Label styles are frequently uppercased and tracked out to provide a clear distinction between data points (like "TIME SLOTS" or "TURF TYPE") and general content. On mobile devices, headline sizes are scaled down slightly to ensure optimal readability and impact without overwhelming the smaller viewport.

## Layout & Spacing

This design system employs a **Fluid Grid** with a strict 8px spacing logic. 

- **Desktop:** 12-column grid with 20px gutters and 40px outer margins. Content is organized in card modules that typically span 3, 4, or 6 columns.
- **Mobile:** Single-column layout with 16px side margins. Cards span the full width of the safe area.
- **Rhythm:** Vertical spacing between cards should be consistent (24px), while internal padding within cards should scale based on the content density—usually 16px for mobile and 24px for desktop.

## Elevation & Depth

Visual hierarchy is achieved through **Tonal Layers** combined with **Ambient Shadows**. 

The base background uses a subtle neutral grey (#F5F5F5), while primary content surfaces are "raised" on crisp white cards. Shadows are designed to be "Athletic and Soft":
- **Low Elevation:** Used for resting cards. A 4px blur with 5% opacity of the Slate Gray.
- **Medium Elevation:** Used for hover states and active selections. An 8px blur with 10% opacity, slightly tinted with the Deep Athletic Green to create a cohesive "glow" effect.
- **High Elevation:** Reserved for modals and dropdowns. A 16px blur with 15% opacity for maximum separation.

## Shapes

The shape language is defined by **Rounded** geometry to maintain an approachable and user-friendly feel.

Standard UI components like inputs, buttons, and small cards use a 0.5rem (8px) radius. Larger layout containers and featured turf cards utilize the `rounded-lg` (16px) or `rounded-xl` (24px) settings to create a distinct, modern silhouette. This balance of soft corners ensures the platform feels safe and professional rather than sharp or aggressive.

## Components

- **Buttons:** Primary buttons are Solid Energetic Orange with white text. Secondary buttons use a Ghost style with a Deep Athletic Green border. Interactive states should include a subtle scale-down (0.98x) on click to feel "squishy" and tactile.
- **Cards:** Turf booking cards feature a top-aligned image with a 16px radius, a content area with clear Typography Labels for price/location, and a prominent Action Orange button.
- **Chips:** Used for "Available," "Booked," or "Maintenance" tags. They use a Pill-shape (3) with high-contrast backgrounds (e.g., Light Green background with Deep Green text).
- **Input Fields:** Use 8px rounded corners, a 1px border in Light Slate, and a 2px Deep Athletic Green border on focus.
- **Booking Progress Bar:** A specialized horizontal component using the Vibrant Grass Green to show completion steps, reinforcing the "path to the pitch" metaphor.
- **Lists:** Transactional lists (e.g., booking history) should use subtle dividers (#ECEFF1) and include leading icons related to the sport type (football, tennis, etc.).