# RepoRadar Design Directions

## Approach 1

**Theme Name:** Signal Ledger

**Very Brief Intro:** A precise, editorial developer workspace shaped by data annotation, source-control diff marks, and observatory instrumentation. It makes repository analysis feel calm, legible, and evidence-led rather than decorative.

**Probability:** 0.07

## Approach 2

**Theme Name:** Infrared Terminal

**Very Brief Intro:** A high-contrast operational console that uses dark graphite, ember signal accents, and compact telemetry panels. It is assertive and fast, but deliberately avoids neon excess.

**Probability:** 0.04

## Approach 3

**Theme Name:** Library Index

**Very Brief Intro:** A warm, document-inspired intelligence tool with archival labels and generous reading space. It presents repository facts as a carefully maintained research record.

**Probability:** 0.08

# Chosen Direction: Signal Ledger

## Design Movement

Signal Ledger draws on **Swiss editorial design, scientific field notes, and modern developer observability tools**. The interface is an asymmetric analyst’s desk: strong hierarchy, restrained annotations, and data views that feel authored rather than templated.

## Core Principles

1. **Evidence before decoration:** Every visual treatment must help a user identify status, source, time range, or engineering signal.
2. **Deliberate asymmetry:** Pages use a left information rail and a broader working surface rather than centered marketing stacks.
3. **Quiet precision:** Fine borders, datum lines, small caps labels, and honest empty states replace ornamental cards and generic gradients.
4. **Readable density:** Dense repository data receives generous spacing and unambiguous grouping, allowing the dashboard to feel capable without becoming noisy.

## Color Philosophy

The foundation is a near-black graphite surface that supports long periods of focused reading. Warm off-white text and parchment panels establish editorial clarity. **Signal orange** is the ownable color for active analyses, important actions, and meaningful status changes; muted mineral blues and greens distinguish secondary measurements without competing with the primary signal.

## Layout Paradigm

The main product layout follows a **ledger rail + analysis canvas** model. A slim, persistent left rail carries identity, navigation, and the current repository context. The wide canvas uses vertical reading sequences, staggered data slabs, and a small inspection column rather than a repetitive centered-card grid. The landing page has a directional composition: a narrative left column, a live analysis receipt on the right, and evidence bands below.

## Signature Elements

1. **Signal line:** a thin orange vertical rule or small square appears beside primary headings, selected navigation, and key outcomes.
2. **Source chips:** compact mono labels such as `GITHUB / LIVE`, `SNAPSHOT / 2m`, and `DEFAULT BRANCH` clarify provenance.
3. **Ledger dividers:** hairline rules punctuated by compact index labels organize content and reinforce the analysis-record motif.

## Interaction Philosophy

Interactions should reward inspection. Hovering a chart point, data row, or repository tile reveals specific provenance or a next action. Primary actions are direct and unmistakable. Keyboard-driven navigation and focus states are prominent, while incidental controls remain quiet until needed.

## Animation

Only opacity and transform transitions are used. Panels enter with a 180–240ms upward fade and 40ms stagger; hoverable rows lift by 1–2px and brighten their datum line. Analysis loading uses an animated scanning rule and sequential metric pulses, never an oversized spinner. Motion respects `prefers-reduced-motion` and is disabled for keyboard-driven state changes.

## Typography System

**Space Grotesk** provides high-contrast, technically confident display typography for repository names and section titles. **IBM Plex Sans** carries body copy and controls, while **IBM Plex Mono** is reserved for URLs, SHAs, source labels, and metrics. Headings use tight tracking and strong contrast; annotations use uppercase mono text at restrained sizes; body copy remains relaxed and highly legible.

## Brand Essence

**RepoRadar is an evidence-led GitHub intelligence workspace for developers who need to understand a public codebase before they trust it.**

Personality: **observant, precise, grounded**.

## Brand Voice

Headlines state a technical outcome; calls to action name the next exact operation; microcopy identifies the source and limits of the data. Avoid hype, filler, and vague claims.

Examples:

> “Read the repository behind the README.”

> “Analyze a public GitHub URL”

## Wordmark & Logo

The mark is a radar aperture constructed from three interrupted concentric arcs around a square signal point. It is intentionally text-free, legible at favicon size, and paired with a compact `REPO / RADAR` wordmark treatment in Space Grotesk.

## Signature Brand Color

**Signal Orange — `#FF5C35`**

## Style Decisions

- Avoid rounded rectangles as the primary visual language; use 6–12px radii only where controls need tactile affordance.
- Do not use purple gradients, translucent glass panels, or stock “AI” imagery.
- Use real GitHub analysis states where available; the frontend must never present invented repository insights as live facts.
- Prominent landing content should use the generated radar motif and product-shaped analysis visuals, while data displays remain code-native and text-led.
- Hero visuals always include a product-shaped evidence artifact with source, scope, measurement, and snapshot labels; atmosphere is never the primary signal alone.
- Authentication surfaces inherit the ledger language through mono provenance labels, signal-square headings, and recorded entry-state markers.
- The repeatable wordmark lockup is `REPO / RADAR`, paired with the radar aperture and a restrained signal-orange emphasis.
