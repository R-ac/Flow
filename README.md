# 🌊 Flow: Flooded IUT Campus 2D Rescue Mission

A 2D top-down retro pixel-art co-op disaster rescue game built with **JavaFX** for Visual Programming Lab projects.

Set on a flooded IUT campus, players navigate flooded avenues, air-lift stranded survivors, cook meals in the campus cafeteria, triage injuries in the medical center, and manage evacuation operations across multi-vehicle systems.

---

## 🎮 Game Concept & Role Division

- **Player 1 — Pilot**:
  - Steers the rescue vehicle (`WASD`).
  - Controls vehicle velocity, acceleration, steering rotation, and avoids obstacles.
  - Manages vehicle cargo capacity (**10 slots max**).
  - Switches between 3 rescue vehicles (`[V]`):
    - 🚤 **Flood Rescue Boat**: Fast maneuverability on open water.
    - 🚜 **6x6 Land Rover Buggy**: All-terrain ground buggy.
    - 🚁 **Emergency Rescue Helicopter**: Flies over buildings, features 3D flight shadow, spinning rotors, aerial winch rescue, and supply airdrops (`[H]`).

- **Player 2 — Crew**:
  - Operates the rescue equipment (`Arrow Keys`).
  - Aim & throw rope lasso to rescue stranded survivors (`[R]` / `[SPACE]`, range 750px).
  - Proximity magnet automatically rescues survivors aboard within 120px.

---

## 🩺 Complex Survivor Triage & Cravings

Survivors are categorized into 3 types with deep triage needs:
1. 🟢 **Normal / Healthy**: Ready for direct evacuation to the Safe Platform.
2. 🍲 **Hungry / Malnourished**:
   - Displays specific meal cravings above their head:
     - 🍛 **Warm Rice & Curry** (`RICE_CURRY`)
     - 🍲 **Grilled Fish Stew** (`FISH_STEW`)
     - 🍵 **Hot Tea & Snacks** (`TEA_SNACKS`)
   - **Perfect Meal Match (+350 PTS)**: Feeding their exact craving (`[F]`) grants full cure & bonus score. Non-matching meals yield partial recovery.
3. 🚑 **Injured / Medical Care**:
   - Displays specific injury severity icons above their head:
     - 🩹 **Laceration / Cut**: Needs Bandages & Antiseptic.
     - 🧪 **Dehydration & Shock**: Needs IV Drip & Saline.
     - 🚑 **Critical Trauma**: Needs Emergency First Aid Kit.
   - **Perfect Triage Match (+400 PTS)**: Administering matching medical kits (`[T]`) grants full cure.
4. 🌡️ **Secondary Fever Complications**:
   - Untreated injured survivors left waiting develop **Fever / Sepsis (🌡️)**, requiring **both** medical treatment and a warm meal to clear!

---

## 🍽️ Campus Kitchen & Medical Center

- **Cafeteria Kitchen (`[C]`)**:
  - Transition into the 2D Kitchen interior view to prepare survivor meals.
  - Tracks individual stocks of Rice & Curry 🍛, Fish Stew 🍲, and Tea & Snacks 🍵.
- **Medical Center (`[M]`)**:
  - Transition into the 2D Medical Center interior view to prepare treatment kits.
  - Tracks individual stocks of Bandages 🩹, Saline Drips 🧪, and First Aid Kits 🚑.
- **Campus Restock Chests**:
  - Collecting supply chests on campus adds **+2 to all kitchen & medical stocks**.

---

## 🎯 Difficulty & Cozy Modes

Selectable from the main menu:
- 💙 **COZY MODE (Relaxed)**: Frozen hunger decay, no failure penalty, peaceful exploration.
- 🟢 **EASY**: 8 evacuation goal, 25 allowed perished threshold, 80% slower hunger decay.
- 🟡 **MEDIUM**: 12 evacuation goal, 15 allowed perished threshold, 40% slower hunger decay.
- 🔴 **HARD**: 18 evacuation goal, 10 allowed perished threshold, faster monster & survivor spawns.

---

## 🧭 HUD & Radar Navigation

- **Off-Screen Survivor Radar Arrow**: Glowing cyan compass arrow (`🚨 SURVIVOR (meters)`) points toward the nearest stranded survivor from anywhere on campus.
- **Triage & Kitchen Inventory Bar**: Displays real-time stock counters (`KITCHEN: 🍛2 🍲2 🍵2 | MEDICAL: 🩹2 🧪2 🚑2`).

---

## 🛠️ Project Structure

```
Flow/
├── lib/                     # JavaFX 21 runtime JARs
├── bin/                     # Compiled bytecode output (.class)
├── run_game.bat             # Windows launcher script
├── run_game.ps1             # PowerShell launcher script
└── src/
    └── com/flow/
        ├── Main.java               # App entry launcher
        ├── controller/
        │   ├── GameEngine.java     # Engine loop, vehicle switching, triage matching & score
        │   ├── InputHandler.java   # Input filtering (WASD + Arrows + Hotkeys)
        │   └── MainController.java # FXML menu & screen overlay manager
        ├── model/
        │   ├── Boat.java           # Vehicle physics & multi-vehicle system
        │   ├── Building.java       # IUT campus building hitboxes & roofing
        │   ├── Survivor.java       # Survivor AI, cravings, injury severity & fever
        │   ├── SurvivorCraving.java# Enum for Rice, Stew, Tea cravings
        │   ├── InjuryType.java     # Enum for Cut, Shock, Trauma injuries
        │   ├── CafeteriaState.java # Kitchen interior & dish stockpiles
        │   ├── MedicalCenterState.java # Healthcare interior & kit stockpiles
        │   ├── VehicleType.java    # Stats for Boat, Rover, Helicopter
        │   └── SafeZone.java       # Safe Platform delivery goal
        └── view/
            ├── AssetManager.java   # 16-bit retro pixel-art sprite loader
            ├── GameRenderer.java   # Canvas renderer, 3D flight shadow, radar arrow & HUD
            ├── SoundManager.java   # Audio feedback & sound effects
            └── GameView.fxml       # FXML layout overlays
```

---

## 🚀 How to Run

### Option 1: Batch File (Windows)
Double-click `run_game.bat` inside the project root directory.

### Option 2: Command Line / Terminal
```bash
# 1. Compile
javac --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -d bin src/com/flow/*.java src/com/flow/controller/*.java src/com/flow/model/*.java src/com/flow/view/*.java

# 2. Run
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp bin com.flow.Main
```
