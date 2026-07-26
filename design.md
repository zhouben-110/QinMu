# Design System: Soft Neumorphism Light Blue Music Player UI
(Extracted & Refactored from Figma Design: Music Player Mobile App UI)

## 1. Visual Theme & Atmosphere

Inspired by Rohan Kumar's **Music Player Mobile App UI** Figma design, the application has been refactored into a high-fidelity **Soft Neumorphism Light Blue Aesthetic**. The interface replaces flat dark backgrounds with tactile 3D soft extruded surfaces, gentle ambient drop shadows, soft sky blue gradients (`#B1D6EA` -> `#D5EAF5` -> `#F0F8FA`), and vibrant royal blue (`#2368A4`) / coral red (`#FF3B30`) accents.

The key design philosophy is **"Tactile Audio Device & Soft 3D Material"**:
- Surfaces feel soft, embossed, and physically responsive to touch.
- Heavy dual-shadow neumorphism:
  - Top-left specular highlight (`rgba(255, 255, 255, 0.9)`)
  - Bottom-right soft blue drop shadow (`rgba(153, 188, 207, 0.45)`)
- Music Player Elements:
  - Concentric Rotating Vinyl Disc with progress arc gauge (`#FF3B30`).
  - Vertical Capsule Floating Control Bar for action controls (Pause, Skip, Reset, Favorite).
  - Popular Playlist preset cards with soft gradient covers.
  - News & Health Tips capsule rows with circular avatars.
  - Floating Neumorphic Pill Navigation Bar.

---

## 2. Color Palette & Tokens

### Sky Blue Ambient Backgrounds
- **Sky Gradient Start**: `#B1D6EA`
- **Sky Gradient End**: `#D5EAF5`
- **Sky Background Light**: `#F0F8FA`

### Neumorphic Surfaces
- **Neumorphic Base Surface**: `#E1F0F7`
- **Neumorphic Card Surface**: `#E8F4FA`
- **Neumorphic Elevated Surface**: `#EEF7FC`
- **Highlight (Specular)**: `#FFFFFF`
- **Shadow Dark**: `#99BCCF` (alpha 0.45)

### Brand & Text Tokens
- **Text Primary (Dark Navy)**: `#1A365D` — Deep high-contrast navy
- **Text Secondary (Muted Blue)**: `#3B6285`
- **Text Caption / Muted**: `#6FABD3`
- **Accent Royal Blue**: `#2368A4`
- **Accent Coral Red**: `#FF3B30`
- **Accent Mint Green**: `#34C759`
- **Accent Warm Orange**: `#FF9500`

---

## 3. UI Component Architecture

1. **Neumorphic Modifier & Cards (`Neumorphism.kt`)**:
   - Custom `Modifier.neumorphicShadow(...)`: Draws dual specular top-left highlight and bottom-right dark blur shadow.
   - `NeumorphicCard`: Rounded container with 3D soft elevation and subtle gradient border outline.
   - `NeumorphicPillButton`: Full capsule pill button (`500dp` corner radius).
   - `NeumorphicIconButton`: Round circular button (`50%` corner radius).

2. **Vinyl Music Disc Progress Gauge**:
   - Rotatable concentric vinyl disc with eye-care status and digital timer ("19:42").
   - Coral red progress arc ring (`#FF3B30`) showing real-time countdown progress.

3. **Floating Capsule Control Bar**:
   - Vertical side bar containing quick controls (Skip, Reset, Play/Pause, Favorite).

4. **Floating Pill Navigation Bar**:
   - Floating capsule bottom bar with active pill indicator and smooth tab switching.
