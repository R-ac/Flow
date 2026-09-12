# Trash Sailors: Seaward Co-Op ⛵

A 2-Player local co-op 2D sea adventure game built with **JavaFX** for Visual Programming Lab projects.

## 🎮 Game Concept & Role Division

- **Player 1 — Pilot**:
  - Steers the boat across the map (`WASD`).
  - Controls boat velocity, acceleration, steering rotation, and avoids water hazards/obstacles.
  - Manages boat cargo capacity (5 slots max).

- **Player 2 — Crew**:
  - Walks on the boat deck (`Arrow Keys`).
  - Proximity action trigger using `Space` key.
  - **4 Sub-Actions**:
    1. **Pick Up Survivors**: Rescues floating survivors into the boat's cargo slots.
    2. **Move / Clear Obstacles**: Clears floating debris & rocks blocking boat passage.
    3. **Scare Off Creatures**: Fends off sea monsters before they drain the game timer/danger meter.
    4. **Take Useful Objects**: Collects chests/fuel for score points and boat turbo speed boosts!

- **No Health Bar**: Challenge relies on time management, survivor rescue counts, and danger handling.

- **⭐ Extra Feature**:
  - When the timer reaches the warning phase (under 25 seconds), unrescued survivors **swim towards the boat** to help you make clutch rescues under pressure!

- **Victory Condition**:
  - Deliver all rescued survivors to the **Lighthouse Safe Harbor** before time runs out.

---

## 🛠️ Project Structure

```
TrashSailorsJavaFX/
├── lib/                     # JavaFX 21 runtime JARs
├── bin/                     # Compiled bytecode output (.class)
├── run_game.bat             # Double-click script to compile & run on Windows
├── run_game.ps1             # PowerShell script to run
└── src/
    └── com/trashsailors/
        ├── Main.java               # App launcher & UI scene manager
        ├── game/
        │   ├── GameEngine.java     # Physics, collision, entity loop, timer & logic
        │   └── InputHandler.java   # Dual-player keyboard inputs (WASD + Arrows + Space)
        ├── model/
        │   ├── Boat.java           # Boat physics, rotation & cargo slots
        │   ├── Crew.java           # Deck-bound crew character
        │   ├── Survivor.java       # Survivor bobbing & swimming AI
        │   ├── Obstacle.java       # Water hazards / debris
        │   ├── Creature.java       # Sea monster danger system
        │   ├── UsefulObject.java   # Collectible loot & speed boosts
        │   └── SafeZone.java       # Harbor delivery goal
        └── view/
            ├── GameRenderer.java   # Canvas rendering engine, ocean waves, HUD & minimap
            └── SoundManager.java   # Audio feedback sounds
```

---

## 🚀 How to Run

### Option 1: Double-Click Batch File (Windows)
Double-click `run_game.bat` inside the project folder.

### Option 2: Command Line / Terminal
```bash
# 1. Compile
javac --module-path lib --add-modules javafx.controls,javafx.media -d bin src/com/trashsailors/*.java src/com/trashsailors/game/*.java src/com/trashsailors/model/*.java src/com/trashsailors/view/*.java

# 2. Run
java --module-path lib --add-modules javafx.controls,javafx.media -cp bin com.trashsailors.Main
```
