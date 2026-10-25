# Parcheggi Roma (Termini–Esquilino) — MVP Implementation Plan

Parcheggi Roma is a community-driven Android application designed for navigating the congested Termini–Esquilino district in Rome. It provides real-time parking availability across anonymous 30–75m micro-zones, a 5-minute departure signaling mechanism ("Sto liberando un posto"), and intelligent Roman driving assistance (ZTL active camera alerts, street cleaning, strisce blu payment rules, and parking trap prevention).

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions synthesize the product requirements and your questionnaire responses:
>
> - **Native Android Runtime**: Built exclusively in modern Kotlin and Jetpack Compose (Material 3), fully executable in the AI Studio streaming emulator.
> - **Tri-Modal Situational Visualization**: The interface adapts between three contextual views:
>   1. *Interactive Micro-Area Map Canvas*: Real-time heat bubbles (30–75m anonymous radii) with live 5-minute countdowns and probability rings.
>   2. *Street-by-Street List & Route View*: Ordered by availability percentage, walking distance from Termini, and regulation status.
>   3. *Neighborhood Radar Overview*: Rapid density summary showing high/medium/low pressure sectors across the Esquilino/Castro Pretorio perimeter.
> - **Roman Parking Hazards & ZTL AI First**: Gemini intelligence is prioritized to warn drivers of active ZTL gates (Varco ZTL Centro Storico, Trastevere, San Lorenzo), street cleaning schedules (*lavaggio strade* tow-away risk), and street-rule gotchas (strisce blu vs bianche vs gialle), while also handling signal reliability scoring and departure assistance.
> - **Strict Privacy Guarantee**: No exact GPS coordinates or vehicle identifiers are broadcast. Departure signals represent approximate micro-zones and automatically expire after 5 minutes.

---

### 1. Overview & Core Concept

- **What It Does**: Enables drivers approaching the Rome Termini–Esquilino area (Via Giolitti, Via Cavour, Piazza Vittorio Emanuele II, Via Merulana, Via Principe Amedeo) to check live parking probability, receive expiring departure notices from outgoing drivers, and avoid heavy Roman fines (ZTL cameras and street cleaning tows).
- **Target Audience / Persona**: Roman commuters, regional train travelers arriving at Termini, and residents navigating tight parking regulations in the Esquilino neighborhood.
- **Key Value**: Drastically cuts cruising time and prevents unexpected tickets through crowdsourced signals (valid for 5 minutes) and contextual Roman mobility intelligence.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Exploration & Navigation**:
   - The user opens the app and sees the **Termini–Esquilino District Hub**.
   - Context switcher toggles smoothly between **Mappa Micro-Aree**, **Elenco Vie**, and **Radar Quartiere**.
   - Zone cards show instant color-coded probability: **Verde** (Alta disponibilità, >60%), **Ambra** (Media disponibilità, 30–60%), **Rosso** (Satura, <30%).

2. **Leaving a Spot ("Sto liberando un posto")**:
   - Single-tap primary action button.
   - User selects or confirms the micro-area (e.g. *Piazza Vittorio Emanuele II (lato nord)* or *Via Giolitti fronte binari*).
   - Confirms departure time (immediate / 2 min).
   - Generates an anonymous, circular micro-zone active for exactly 5 minutes with a live countdown bar.
   - Generates reliability karma points for the user.

3. **Community Validation ("Feedback Cercatore")**:
   - Drivers in the area can tap a live signal to view details: *Segnalato 2 min fa • Micro-area ~50m • Affidabilità 94%*.
   - Quick one-tap feedback buttons: **"Posto Trovato"**, **"Non trovato / Occupato"**, **"Dato errato / Falso"**.
   - Dynamic reliability score adjustment prevents malicious spamming.

4. **Roman Driving & ZTL Hazard Copilot (Gemini AI)**:
   - Dedicated card and search banner: *"Controllo ZTL & Rischi Sosta"*.
   - Drivers query any destination or street in Rome (e.g. *Via Merulana ore 19:30* or *Via Giovanni Giolitti*).
   - Gemini evaluates:
     - **ZTL Status**: Active gates, daytime/nighttime schedules, camera locations.
     - **Strisce Blu & Tariffe**: Hourly rates (es. 1.00€/h o 1.50€/h), free time slots, resident exemptions.
     - **Lavaggio Strade**: Days and hours where parking leads to vehicle removal.
     - **Consiglio Tattico**: Alternative safe streets within 3 minutes on foot.

5. **Profile, Privacy & Favorites**:
   - Saved areas (e.g. *Stazione Termini - Lato Giolitti*, *Piazza Dante*).
   - Full Account Transparency: One-tap *"Cancella account e dati"* per GDPR and Google Play compliance.
   - Manual Mode: Complete usability even if GPS permissions are denied.

#### Visual Identity & Theme

- **Aesthetic Direction**: *Roman Utilitarian & Civic Precision*. High contrast, refined typography, and purposeful mobility color coding.
- **Color Palette**:
  - `Primary`: Deep Roman Ochre / Terracotta (`#A33B20` / `#D45D3A`) paired with Warm Obsidian (`#16191D`) and Clean Off-White (`#F9F9FB`).
  - `Availability High`: Emerald Green (`#1E824C`)
  - `Availability Medium`: Amber Gold (`#D9822B`)
  - `Availability Low`: Crimson Red (`#C0392B`)
  - `ZTL Alert`: Caution Electric Amber (`#E67E22`) with warning badges.
- **Typography & Layout**:
  - Bold, legible headers for quick in-car glanceability.
  - Generous 48dp+ touch targets for all primary buttons.
  - Floating Action Button for immediate departure reporting.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Micro-Zone Map Engine (Canvas + Procedural Overlay)**
  - *Chosen Approach*: Custom Compose Canvas engine rendering the actual geometric street grid of Termini–Esquilino with pinch/pan, pulse animations, and 30–75m micro-area heat bubbles, integrated with fallback to Google Maps grounding via Gemini.
  - *Why*: Guarantees instant 60fps rendering, offline resilience, and zero API quota failure during prototype testing, while ensuring no exact vehicle GPS points are leaked.
- **Decision 2: 3-Way Context Switcher (Map, List, Radar)**
  - *Chosen Approach*: Segmented control switching between Interactive Micro-Area Map, Street Inventory List, and Sector Radar, satisfying the user's preference for situational adaptability.
  - *Why*: When driving, a fast list or radar is easier to read than zooming into small map pins; when parked or walking, the visual map provides spatial clarity.
- **Decision 3: Gemini 3-Tier Specialized Integration**
  - *Chosen Approach*:
    - `gemini-3.5-flash` with Google Maps context for comprehensive Roman ZTL and parking regulation analysis.
    - `gemini-3.1-flash-lite` for ultra-fast signal credibility scoring and quick parking tips.
    - `gemini-3.1-pro-preview` with High Thinking Level for complex route, schedule, and multi-variable parking hazard arbitration.
  - *Why*: Meets the specific model instructions provided in the brief while optimizing for both speed and reasoning depth.
- **Decision 4: Ephemeral Signal Lifecycle with Decaying Weight**
  - *Chosen Approach*: Real-time state engine where signals strictly decrement from 300 seconds (5 minutes) down to 0, automatically transitioning to expired state.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PARCHEGGI ROMA UI LAYER                         │
├────────────────────────────────────────────────────────────────────────┤
│  TopAppBar (Zone Selector & Status)                                    │
│  ├── Tri-Modal View Container                                          │
│  │   ├── MicroAreaMapCanvas (Pinch/Pan, 30-75m Circles, Heatmaps)      │
│  │   ├── StreetAvailabilityList (Sorted by distance & probability)     │
│  │   └── NeighborhoodRadarView (Sector density & traffic pressure)     │
│  ├── Quick Action Bar ("Sto liberando un posto" - 5 Min Countdown)     │
│  ├── ZTL & Parking Hazard Copilot Drawer/Sheet                         │
│  └── Community Validation Dialog ("Trovato" / "Non Trovato")           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                        ViewModel (StateFlow)
                                    │
        ┌───────────────────────────┴───────────────────────────┐
        ▼                                                       ▼
┌──────────────────────────────┐        ┌───────────────────────────────┐
│     ParkingSignalRepository  │        │       GeminiAdvisorService    │
├──────────────────────────────┤        ├───────────────────────────────┤
│ • Micro-Area Definitions     │        │ • Model: gemini-3.5-flash     │
│   (Termini, Giolitti, Cavour,│        │   (ZTL gates & tariff rules)  │
│    Pza Vittorio, Merulana)   │        │ • Model: gemini-3.1-flash-lite│
│ • Active 5-min signals       │        │   (Real-time risk rating)     │
│ • Community karma & feedback │        │ • Model: gemini-3.1-pro       │
│ • Signal decay timer (Ticker)│        │   (High thinking route advice)│
│ • Account & privacy controls │        │                               │
└──────────────────────────────┘        └───────────────────────────────┘
```

#### Core Data Entities

- `MicroArea`: ID, name, center coordinate (latitude, longitude), radius (30–75m), baseline availability (0–100%), parking bay type (strisce blu, bianche, carico merci), ZTL status.
- `DepartureSignal`: Signal ID, microAreaId, timestampCreated, expiryTimestamp (epoch ms, exactly +5 mins), userKarmaScore, status (`ACTIVE`, `VERIFIED_FOUND`, `EXPIRED`, `REPORTED_FALSE`), feedbackFoundCount, feedbackNotFoundCount.
- `ZtlAlert`: Zone name, active hours (e.g. 06:30–18:00 lun-ven), fine penalty, closest access gate (*varco*), camera active boolean.
- `ParkingHazardAssessment`: Risk score, ztlWarning, streetCleaningWarning, strisceTariff, parkingAdvice.
