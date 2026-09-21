# 🌊 Flow: Flooded IUT Campus 2D Rescue Mission

> A 2D top-down retro pixel-art \*\*co-operative disaster rescue game\*\* built with \*\*JavaFX 21\*\*.

Set on a fully flooded version of the IUT campus, two players team up to navigate flooded avenues, airlift rooftop survivors, cook meals in the campus cafeteria, triage injuries in the medical centre, outrun floodwater creatures, and manage evacuation operations across a 6000×4200-unit open world — all in real time.

\---

## 📋 Table of Contents

* [Game Concept](#-game-concept)
* [Two-Player Roles \& Controls](#-two-player-roles--controls)
* [Multi-Vehicle System](#-multi-vehicle-system)
* [Survivor Types \& Triage System](#-survivor-types--triage-system)
* [Campus Kitchen \& Medical Centre](#️-campus-kitchen--medical-centre)
* [Collectibles \& Obstacles](#-collectibles--obstacles)
* [Flood Creatures](#-flood-creatures)
* [Scoring System](#-scoring-system)
* [Difficulty Modes](#-difficulty-modes)
* [HUD \& Navigation](#-hud--navigation)
* [IUT Campus Map](#️-iut-campus-map)
* [Win \& Lose Conditions](#-win--lose-conditions)
* [Project Structure \& Architecture](#️-project-structure--architecture)
* [Technologies Used](#-technologies-used)
* [How to Run](#-how-to-run)
* [Contribution Breakdown](#-contribution-breakdown)

\---

## 🎮 Game Concept

A catastrophic flood has hit IUT campus. Survivors are stranded — some on rooftops, others drowning in floodwater avenues. As a two-player rescue team, your mission is to:

1. **Navigate** the flooded campus using your rescue vehicle.
2. **Rescue** survivors with a rope lasso or proximity magnet.
3. **Triage** injured survivors with the correct medical kit.
4. **Feed** hungry survivors their exact meal craving.
5. **Evacuate** enough survivors to the Safe Platform before too many perish.

The game runs at a **smooth 60 FPS** using JavaFX's `AnimationTimer` with proper **delta-time physics** — all vehicle movement, health decay, and animations are frame-rate independent.

\---

## 🕹️ Two-Player Roles \& Controls

Flow is designed for **two players on the same keyboard**, each controlling a distinct role.

### Player 1 — Pilot (`WASD`)

|Key|Action|
|-|-|
|`W`|Accelerate forward|
|`S`|Reverse / brake|
|`A`|Turn left|
|`D`|Turn right|
|`V`|Switch vehicle *(must be inside Workshop Depot)*|
|`C`|Enter Cafeteria Kitchen *(when nearby)*|
|`M`|Enter Medical Centre *(when nearby)*|
|`H`|Helicopter aerial food airdrop *(helicopter only)*|
|`Q` / `Scroll Up`|Zoom in camera|
|`E` / `Scroll Down`|Zoom out camera|
|`P` / `ESC`|Pause / unpause|

### Player 2 — Crew (`Arrow Keys`)

|Key|Action|
|-|-|
|`↑ ↓ ← →`|Move crew member on deck (within 80px radius of boat)|
|`R` / `SPACE`|Throw rope lasso toward aimed survivor (750px range)|
|`F`|Feed staged hungry survivor at Safe Platform|
|`T`|Treat staged injured survivor at Safe Platform|

> \*\*Proximity Magnet:\*\* Any survivor within \*\*120px\*\* of the boat is automatically rescued aboard — no lasso needed.

\---

## 🚗 Multi-Vehicle System

The pilot can cycle through **3 distinct rescue vehicles** by pressing `\[V]` — but **only while inside the Emergency Vehicle Workshop \& Depot** on the lower-middle section of the map. Attempting to switch outside the depot shows a warning.

|Vehicle|Display Name|Max Speed|Acceleration|Turn Speed|Friction|Special Ability|
|-|-|-|-|-|-|-|
|🚤|Flood Rescue Boat|9.5|0.60|5.5|0.996|Fast water maneuverability|
|🚜|6×6 Land Rover Buggy|6.2|0.38|4.2|0.980|Can drive through fields \& forests|
|🚁|Emergency Rescue Helicopter|13.0|0.85|6.5|0.992|Flies over ALL buildings \& terrain; 3D shadow, spinning rotors, aerial winch, food airdrop `\[H]`|

**Vehicle physics** are fully simulated each frame:

* **Angle-based acceleration** using trigonometry (`cos`/`sin` of heading angle)
* **Friction deceleration** applied every tick
* **Speed clamping** via `Math.hypot`
* **Map boundary bounce-back** with velocity dampening (`× −0.3`)
* **Temporary Speed Boost** from collecting Turbo Fuel — multiplies max speed and acceleration by **1.4×** for a duration

**Collision rules:**

* 🚤 Boat — blocked by all solid buildings; blocked by fields and forests
* 🚜 Rover — passes freely through sports fields (`FIELD`) and tree parks (`PARK\_TREES`)
* 🚁 Helicopter — **flies over everything**, no building collision at all

**Vehicle switching** cycles: `BOAT → ROVER → HELICOPTER → BOAT`

\---

## 🩺 Survivor Types \& Triage System

Survivors are not passive targets — they have **health, movement AI, cravings, injuries, and time-based complications**.

### Survivor AI Behaviour

* **Rooftop-trapped survivors** are confined strictly within their building's rooftop perimeter. When the boat approaches within 550px, they run toward the nearest rooftop edge to signal for rescue.
* **Drowning water survivors** swim actively toward the boat when it comes within 550px. They bounce off solid building walls and can never enter buildings.

### 🟢 Normal / Healthy

* 100 HP, no special need.
* Can be directly delivered to the Safe Platform for **+250 pts** (or **+375 pts** via helicopter).

### 🍲 Hungry / Malnourished

* **200 HP**, health depletes over time at a rate scaled by `hungerDepletionMultiplier`.
* Displays their specific **meal craving** floating above their head. Craving is randomly assigned from:

|Enum Value|Meal|Icon|
|-|-|-|
|`RICE\_CURRY`|Warm Rice \& Curry|🍛|
|`FISH\_STEW`|Grilled Fish Stew|🍲|
|`TEA\_SNACKS`|Hot Tea \& Snacks|🍵|

* **Perfect Craving Match `\[F]`** → Feeds exact craving meal → **+350 pts**, fully cured.
* **Craving Mismatch** → Partial recovery (+60 HP only), survivor remains hungry.
* If a hungry survivor's HP reaches 0 they **perish**, costing −200 pts and incrementing the fail counter.

### 🚑 Injured / Medical Care

* **150 HP**, health is stable unless fever develops.
* Displays their specific **injury severity icon** above their head:

|Enum Value|Injury|Icon|Required Treatment|
|-|-|-|-|
|`MINOR\_CUT`|Laceration / Cut|🩹|Bandages \& Antiseptic|
|`DEHYDRATION\_SHOCK`|Dehydration \& Shock|🧪|IV Drip \& Saline Solution|
|`CRITICAL\_TRAUMA`|Critical Trauma|🚑|Emergency First Aid Kit|

* **Perfect Triage Match `\[T]`** → Applies matching kit → **+400 pts**, fully cured.
* **Treatment Mismatch** → Partial healing (+50 HP only), survivor remains injured.

### 🌡️ Secondary Fever Complication

If an injured survivor is **left untreated for 45 seconds**, they develop **Fever / Sepsis (`🌡️`)**.

* Fever causes **continuous HP drain** over time (`0.5 HP/s × hungerDepletionMultiplier`).
* A fever survivor requires **both** a matching medical treatment **AND** a warm meal to fully cure.
* This creates urgent triage pressure that escalates as the game progresses.

\---

## 🍽️ Campus Kitchen \& Medical Centre

### Cafeteria Kitchen `\[C]`

* Accessible when the boat is **within 420px** of the **Cafeteria / Library** building.
* Triggers a **smooth fade-screen transition** (alpha interpolation at 3.0× speed, \~0.33s) into the 2D Kitchen interior view.
* Inside, the crew can cook any of the 3 meal types. Each completed cooking session yields **+2 portions** of that dish.
* Tracks 3 separate stockpiles:

  * 🍛 Rice \& Curry stock
  * 🍲 Fish Stew stock
  * 🍵 Tea \& Snacks stock
* Exit the kitchen to return to the campus world.

### Medical Centre `\[M]`

* Accessible when the boat is **within 420px** of the **Medical Centre \& Laundry** building (far west).
* Same smooth fade-transition into the 2D Medical interior view.
* Crew prepares medical kits with a progress timer. Each session yields **+2 kits**:

  * 🩹 Bandages \& Antiseptic
  * 🧪 IV Drip \& Saline Solution
  * 🚑 Emergency First Aid Kit

### Campus Restock Chests

* **Supply Chest** collectibles scattered on campus add **+2 to ALL 6 stock types** (kitchen + medical) when collected, and grant **+300 pts**.

\---

## 📦 Collectibles \& Obstacles

### Collectibles (UsefulObjects)

|Type|Points|Effect|
|-|-|-|
|`SUPPLY\_CHEST`|+300|Adds +2 to all kitchen \& medical stocks|
|`TURBO\_FUEL`|+150|Grants a temporary speed boost (1.4× speed/accel)|
|`RECYCLABLE\_TRASH`|+100|Score bonus only|

### Obstacles

Floating debris blocks vehicle movement (3 hits to clear):

* `SUBMERGED\_CAR` — sunken vehicles in flood lanes
* `ROAD\_BARRIER` — broken road barriers
* `FALLEN\_TREE` — toppled campus trees
* `FLOODED\_DUMPSTER` — floating dumpsters

\---

## 🐊 Flood Creatures

Dangerous creatures lurk in the floodwaters. They stalk the boat when it gets close and can be scared off temporarily.

* When the boat comes within **220px**, a creature slowly lurks toward it and its `dangerLevel` rises.
* If `dangerLevel > 0.2` and the crew interacts, `scareOff()` triggers — the creature retreats for **10 seconds**.
* **Workshop Depot Sanctuary**: Creatures de-aggro and cannot enter the Workshop Depot — it's a safe zone for vehicle changes.
* Creatures are blocked by all solid buildings (collision resolution mirrors the survivor AI).

|Creature|Location|
|-|-|
|Central Pond Monster|Central campus pond|
|Auditorium Alley Croc|Auditorium alley|
|Workshop Swamp Beast|Workshop area *(HARD difficulty only)*|

The number of active creatures scales with difficulty (0 on COZY, 1 on EASY, 2 on MEDIUM, 3 on HARD).

\---

## 🏆 Scoring System

|Action|Points|
|-|-|
|Rope lasso rescue|+120 pts (+ combo bonus)|
|Proximity magnet rescue|+120 pts|
|Deliver normal survivor|+250 pts|
|Deliver normal survivor via helicopter|+375 pts|
|Perfect craving meal match `\[F]`|+350 pts|
|Perfect triage treatment match `\[T]`|+400 pts|
|Helicopter aerial food airdrop `\[H]`|+400 pts|
|Collect supply chest|+300 pts|
|Collect turbo fuel|+150 pts|
|Collect recyclable trash|+100 pts|
|Survivor perishes|**−200 pts**|

### 🔥 Rescue Combo Streak

* Successfully rescue survivors in quick succession (within **8 seconds** of each other) to build a combo streak.
* **Combo bonus** = `(comboCount − 1) × 50` extra pts per rescue.
* A floating `🔥 x3 COMBO!` text appears on-screen.

\---

## 🎯 Difficulty Modes

Select from the main menu before starting.

|Mode|Evacuation Goal|Perish Limit|Spawn Interval|Hunger Decay|Creatures|
|-|-|-|-|-|-|
|💙 **COZY**|15|Unlimited|25s|**0% (Frozen)**|0|
|🟢 **EASY**|8|25|20s|20%|1|
|🟡 **MEDIUM**|12|15|15s|40%|2|
|🔴 **HARD**|18|10|8s|80%|3|

* **Survivor type distribution** also scales with difficulty — on HARD, 50% of spawns are Hungry, 35% Injured.
* A continuous **survivor respawn system** automatically spawns new survivors (rooftop or drowning) when the unrescued count drops below a threshold, or when the spawn timer elapses.

\---

## 🧭 HUD \& Navigation

The game's HUD provides all critical real-time information without pausing gameplay:

* **Stock Inventory Bar** — always visible at the top:

```
  KITCHEN: 🍛2 🍲2 🍵2 | MEDICAL: 🩹2 🧪2 🚑2
  ```

* **Score \& Delivered Counter** — `Score: 1250 | Delivered: 4/8`
* **Perished Counter** — `Perished: 2/25`
* **Combo Streak Indicator** — `🔥 x3 COMBO!` with timer bar
* **Status Message Bar** — context-sensitive messages (e.g., "🏥 MEDICAL CENTER NEARBY!")
* **Off-Screen Survivor Radar Arrow** — a glowing cyan compass arrow (`🚨 SURVIVOR (meters)`) that always points toward the nearest unrescued survivor from anywhere on campus, with distance in metres
* **Floating Score Text** — animated point popups (`+350 PTS!`) that drift upward and fade out
* **Health Bars** — displayed above each survivor's sprite
* **Craving / Injury Icons** — shown above survivor heads at all times
* **3D Helicopter Shadow** — rendered as a separate canvas pass when flying

**Camera:**

* Follows the pilot's vehicle at all times.
* Zoom range: `0.4×` (zoomed out) to `1.8×` (zoomed in), adjustable with `Q`/`E` or mouse scroll wheel.

\---

## 🗺️ IUT Campus Map

The game world is a **6000×4200 pixel** faithful recreation of IUT campus, with 26+ named buildings and landmarks:

|Zone|Buildings|
|-|-|
|**West Campus**|Medical Centre \& Laundry, Residential Buildings 1 \& 2, Female Hall of Residence, Female Common Facilities|
|**Central Academic**|Academic Building-3, North/Middle/South Academic Wings, Academic Building-2, Academic Building-1, OIC Tree Park|
|**North Halls**|North Hall of Residence (×3), South Hall of Residence (×3)|
|**Admin Zone**|Administrative Building, Cafeteria / Library, Auditorium, IUT Mosque|
|**East Campus**|Students Centre \& Gymnasium, IUT Sports Field, North Cafeteria \& Common|
|**Lower Middle**|Emergency Vehicle Workshop \& Depot *(vehicle switch station + creature sanctuary)*|

**16 initial survivors** are pre-positioned across the map at game start:

* **11 rooftop-trapped** survivors on named buildings
* **5 drowning water** survivors in campus flood avenues

New survivors continue to spawn throughout the mission.

\---

## 🏁 Win \& Lose Conditions

### 🎉 Victory

Deliver the required number of survivors to the Safe Platform (next to Cafeteria / Library):

* COZY: 15 · EASY: 8 · MEDIUM: 12 · HARD: 18

### 💀 Game Over

Exceed the perished survivor limit before reaching the evacuation goal:

* COZY: Never · EASY: 25 · MEDIUM: 15 · HARD: 10

> Perished survivors also deduct \*\*−200 pts\*\* from the score.

\---

## 🏗️ Project Structure \& Architecture

The project follows a strict **MVC (Model-View-Controller)** pattern:

```
Flow/
├── lib/                            # JavaFX 21 runtime JARs (required)
├── bin/                            # Compiled bytecode output (.class files)
├── run\_game.bat                    # Windows one-click launcher (compile + run)
├── run\_game.ps1                    # PowerShell launcher (compile + run)
└── src/
    └── com/flow/
        ├── Main.java               # JavaFX Application subclass
        ├── MainLauncher.java       # Entry-point launcher (avoids module issues)
        │
        ├── controller/
        │   ├── GameEngine.java     # ★ Core game logic (1100+ lines)
        │   │                       #   Physics, triage, scoring, AI, spawning
        │   ├── InputHandler.java   # Key event capture (WASD, arrows, hotkeys)
        │   └── MainController.java # FXML menu \& 60 FPS AnimationTimer game loop
        │
        ├── model/
        │   ├── Boat.java           # Vehicle physics, cargo, multi-vehicle switching
        │   ├── Building.java       # Campus building hitboxes \& collision detection
        │   ├── Survivor.java       # Survivor AI, health, cravings, injury, fever
        │   ├── SurvivorType.java   # Enum: NORMAL, HUNGRY, INJURED
        │   ├── SurvivorCraving.java# Enum: RICE\_CURRY, FISH\_STEW, TEA\_SNACKS
        │   ├── InjuryType.java     # Enum: MINOR\_CUT, DEHYDRATION\_SHOCK, CRITICAL\_TRAUMA
        │   ├── CafeteriaState.java # Cafeteria interior, cooking progress, dish stockpiles
        │   ├── MedicalCenterState.java # Medical interior, treatment progress, kit stockpiles
        │   ├── VehicleType.java    # Enum: physics stats per vehicle (speed, accel, friction)
        │   ├── Creature.java       # Flood monster AI (lurk, threaten, retreat, sanctuary)
        │   ├── Crew.java           # Crew member offset movement relative to boat
        │   ├── Obstacle.java       # Floating debris with 3-hit clearance
        │   ├── UsefulObject.java   # Collectibles (supply chest, turbo fuel, trash)
        │   ├── RopeAnimation.java  # Lasso throw animation \& target tracking
        │   └── SafeZone.java       # Safe Platform delivery zone detection
        │
        └── view/
            ├── GameRenderer.java   # ★ Canvas rendering engine (1670+ lines)
            │                       #   World, HUD, radar, shadows, particles, rain
            ├── AssetManager.java   # 16-bit retro pixel-art sprite loader \& cache
            ├── SoundManager.java   # Audio feedback \& sound effects
            ├── GameView.fxml       # FXML layout: canvas + menu/gameover/victory overlays
            └── menu.css            # Retro-styled menu CSS
```

### Architecture Notes

* **`GameEngine.update(inputHandler, deltaSeconds)`** — called every frame; runs all physics, AI, and game logic in delta-time.
* **`GameRenderer.render(engine)`** — pure read-only pass over game state; draws everything to JavaFX Canvas.
* **`MainController`** — owns the `AnimationTimer` game loop; bridges FXML UI events to engine state.
* **Interior view switching** — `CafeteriaState` and `MedicalCenterState` use a fade-alpha transition system; while transitioning, the engine skips the main update loop.
* **Collision resolution** — minimum-overlap axis separation for building walls; radius-based push-out for obstacles and creatures.

\---

## 💻 Technologies Used

|Layer|Technology|
|-|-|
|Language|**Java 21**|
|GUI \& Rendering|**JavaFX 21** — Canvas, GraphicsContext, AnimationTimer|
|Layout|**FXML** (`GameView.fxml`) + **CSS** (`menu.css`)|
|Audio|**JavaFX Media** — SoundManager|
|Architecture|**MVC** — Model / View / Controller separation|
|Build \& Launch|Windows Batch (`run\_game.bat`) \& PowerShell (`run\_game.ps1`)|
|Assets|Custom 16-bit retro pixel-art sprites (loaded via AssetManager)|

\---

## 🚀 How to Run

### Prerequisites

* **Java 21** (or later) installed and on your system `PATH`
* **JavaFX 21** runtime JARs — already included in the `lib/` folder of this project

Verify your Java installation:

```bash
java --version
```

You should see `java 21` or higher.

\---

### ✅ Option 1: Windows Batch File (Recommended — Easiest)

1. Open **File Explorer** and navigate to the project root:

```
   Flow-main\\Flow-main\\Flow-main\\
   ```

2. ## **Double-click** `run\_game.bat`

The script will automatically:

* Create the `bin/` output directory if it doesn't exist
* Compile all Java source files with the JavaFX module path
* Copy `GameView.fxml`, `menu.css`, and all image assets to `bin/`
* Launch the game

If compilation fails, an error message will appear in the console window — press any key to dismiss.

\---

### ✅ Option 2: PowerShell

1. Open **PowerShell** in the project root directory.
2. If script execution is restricted, allow it first:

```powershell
   Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
   ```

3. Run:

```powershell
   .\\run\_game.ps1
   ```

The script auto-detects the project directory, compiles, copies assets, and launches.

\---

### ✅ Option 3: Manual Command Line

**Step 1 — Compile** (run from the project root):

```bash
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media ^
  -d bin ^
  src/com/flow/\*.java ^
  src/com/flow/controller/\*.java ^
  src/com/flow/model/\*.java ^
  src/com/flow/view/\*.java
```

**Step 2 — Copy resources**:

```bash
# Windows
copy src\\com\\flow\\view\\GameView.fxml bin\\com\\flow\\view\\
copy src\\com\\flow\\view\\menu.css bin\\com\\flow\\view\\
xcopy /Y /S /I src\\Resources\\Images bin\\Resources\\Images
```

**Step 3 — Run**:

```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media ^
  -cp bin com.flow.MainLauncher
```

> \*\*Linux / macOS note:\*\* Replace `^` with `\\` for line continuation, replace `\\` path separators with `/`, and use `cp`/`rsync` instead of `copy`/`xcopy`.

\---

### ❗ Common Issues

|Problem|Fix|
|-|-|
|`javac: command not found`|Install Java 21+ and add `JAVA\_HOME/bin` to `PATH`|
|`Error: JavaFX runtime components are missing`|Make sure the `lib/` folder exists and contains the JavaFX JARs|
|Black screen / no assets|Run from the project root directory, not from inside `src/`|
|`Compilation failed`|Check that all `.java` files in `src/` are present; re-extract the project ZIP if needed|
|Script blocked (PowerShell)|Run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` in PowerShell|

\---

## 👥 Contribution Breakdown

|Feature / Component|Contributor|
|-|-|
|Project setup \& MVC architecture|Rakib (243)|
|Main menu, FXML layout \& CSS styling|Rakib (243)|
|Core vehicle physics (`Boat.java`)|Rakib (243)|
|Multi-vehicle switching system|Nirjhor (209)|
|Survivor AI, bobbing \& movement|Rakib (243)|
|Survivor triage \& craving system|Rakib (243)|
|Secondary fever complication logic|Rakib (243)|
|Cafeteria kitchen interior \& cooking|Rakib (243)|
|Medical centre interior \& treatment|Rakib (243)|
|IUT campus map design (26+ buildings)|Rakib (243)|
|60 FPS game loop (`MainController`)|Rakib (243)|
|GameRenderer — world \& HUD rendering|Nirjhor (209)|
|Radar arrow \& off-screen navigation|Nirjhor (209)|
|Flood creature AI (`Creature.java`)|Rakib (243)|
|Obstacle \& collectible system|Nirjhor (209)|
|Scoring, combo streak \& difficulty|Rakib (243)|
|Survivor respawn \& spawn validation|Nirjhor (209)|
|Sound manager \& audio feedback|Nirjhor (209)|
|Asset manager \& sprite loading|Rakib (243)|
|Batch / PowerShell launcher scripts|Nirjhor (209)|

\---

*Flow — Visual Programming Lab Project · Rakib (243) \& Nirjhor (209)*

