<!--
Sync Impact Report
Version change: uninitialized -> 1.0.0
Modified principles: initial adoption
Added sections: Technical Guardrails; Delivery Workflow
Removed sections: none
Templates requiring updates:
- .specify/templates/plan-template.md: updated
- .specify/templates/spec-template.md: updated
- .specify/templates/tasks-template.md: updated
Follow-up TODOs: none
-->

# BankApp Constitution

## Core Principles

### 1. Static-First Delivery
BankApp MUST ship as a static web app composed of build-time assets suitable for static hosting. New work MUST avoid introducing a required server-side runtime unless this constitution is amended first.

Rationale: Static delivery keeps hosting, operations, and failure modes simple and predictable.

### 2. Simplicity Over Framework Sprawl
Implementation choices MUST prefer the smallest solution that satisfies the requirement. New frameworks, major libraries, or build tooling SHOULD only be added when the existing approach cannot reasonably meet the need.

Rationale: Limiting moving parts reduces maintenance cost, bundle growth, and onboarding friction.

### 3. Accessible, Responsive UI
User-facing changes MUST remain usable on common mobile and desktop viewports and MUST support semantic structure, keyboard access, and readable contrast. Accessibility and responsive behavior MUST be defined in specs when UI is affected.

Rationale: A banking interface is only acceptable if it is broadly usable and reliable across devices.

### 4. Testable Quality Gates
Changes MUST include verifiable acceptance criteria and MUST add or update tests or documented manual checks for affected behavior. Critical user flows MUST be regression-checked before release.

Rationale: Small static apps still need disciplined checks to prevent visible regressions.

### 5. Safe Configuration And Dependencies
Client-side code MUST NOT expose secrets, private keys, or privileged tokens. Dependencies SHOULD be kept minimal, versioned intentionally, and reviewed for security and necessity before adoption or upgrade.

Rationale: Static apps expose shipped assets directly, so configuration and dependency discipline is a primary security control.

## Technical Guardrails

BankApp MUST remain deployable on static hosting without application servers, server-rendered routes, or runtime-only backend configuration.

Client-side configuration MUST be safe to publish. Sensitive operations MUST NOT depend on secrets embedded in browser code, HTML, or static environment files.

Third-party assets and libraries SHOULD be minimized. Added JavaScript, CSS, fonts, images, and external scripts MUST have a clear user-facing purpose and acceptable performance impact.

## Delivery Workflow

Implementation plans MUST complete a constitution check covering static-hosting fit, accessibility and responsiveness, performance impact, testing, and client-side secret safety.

Feature specs MUST describe accessibility and responsive expectations whenever UI changes are introduced, and MUST call out any static-hosting or client-side constraints that affect scope or design.

Task lists MUST include verification work for relevant accessibility, performance, security/configuration, and deployment concerns before completion.

## Governance

This constitution applies to all BankApp planning, specification, tasking, implementation, and review work. Pull requests that conflict with it MUST be revised or receive an explicit documented exception approved by the maintainers.

Amendments MUST be made in the same change set as any required template updates and MUST include an updated Sync Impact Report. Ratification requires maintainer approval.

Semantic versioning policy for this constitution:
- MAJOR: incompatible governance or principle changes
- MINOR: new principles, new sections, or materially expanded requirements
- PATCH: clarifications, wording improvements, or non-behavioral corrections

Compliance review expectations:
- Every implementation plan MUST pass the constitution check before work begins.
- Every review SHOULD confirm that accessibility, testing, performance, and client-side configuration expectations were addressed where applicable.
- Non-compliance MUST be tracked and resolved before release unless an exception is explicitly approved.

**Version**: 1.0.0 | **Ratified**: 2026-04-08 | **Last Amended**: 2026-04-08