# Ricoshot Wiki & Guide

Welcome to the **Ricoshot** wiki! This guide will teach you everything you need to know about coin tossing, perfect timing shots, and shield parrying.

---

## 1. How to Toss and Shoot Coins

To do a Ricoshot, you need to toss a coin into the air and shoot it with your bow.

### Setup:
1. Place a **Bow** in your main hand.
2. Place **Golden Nuggets** in your off-hand (your shield slot).
3. Hold a **Carrot on a Stick** (in any slot) or click right-click to toss.

### Controls:
* **Toss Coin:** Right-click with your bow and nuggets equipped. This will consume 1 gold nugget and toss it forward. It will play a chime sound when thrown.
* **Shoot Coin:** Draw your bow and shoot the coin while it is flying in the air!

---

## 2. Ricoshot vs. Perfect Timing (Ultraricoshot)

### The Normal Ricoshot
If you shoot the coin at any normal time while it flies, the arrow bounces off it and hits the nearest mob automatically.
* **Damage:** Deals **85% to 95%** of the mob's health based on how close they are.
* **Text Feedback:** Displays `+ RICOSHOT` on your action bar.

### The Perfect Timing (Ultraricoshot)
When the coin reaches the highest point of its throw, it will play a special chime sound and flash green stars in the air. 
* **The Window:** If you shoot the coin exactly during this sparkle window, you trigger an **Ultraricoshot**.
* **Damage:** Deals at least **100%** of the mob's max health (instant kill!).
* **Targeting Rules:** The bounce is not fully automatic. For the beam to hit a target:
  1. The target must be within **40 blocks** of the coin.
  2. The target must have a **clear line-of-sight** to the coin (no solid blocks in the way).
  3. The target must be within the shooter's **120-degree field of view** cone.
* **Text Feedback:** Displays `+ ULTRARICOSHOT (PERFECT SPLIT!)` with a bright white flash!

---

## 3. Shield Parrying

You can deflect the Ricoshot beam using a shield!

* **How to Parry:** If another player is shooting a Ricoshot at you, hold your shield up and look towards the coin.
* **The Result:** If you block the beam, it will play a shield block sound and a loud anvil chime. 
* **Durability:** Parrying successfully will block all damage completely, but it will take **10 points** of durability away from your shield.
* **Text Feedback:** Displays `+ SHIELD PARRY!` on your screen.

---

## 4. How to Change Settings

You can customize the texts and settings of the mod easily.

### The Config File:
Inside your Minecraft folder, go to the `config` folder and open the `ricoshot.json` file with any text editor (like Notepad).

You can change these lines to edit the messages that appear on your screen:
* `"enableActionBarText"`: Set to `true` or `false` to turn the screen messages on or off.
* `"ricoshotText"`: The text shown for a normal ricoshot.
* `"ultraRicoshotPerfectText"`: The text shown for a perfect split.
* `"shieldParryText"`: The text shown when parrying with a shield.

### Pause Menu Button:
If you are playing in the game, press **ESC** to open the pause menu. There is a gold coin button on the screen. Click it to quickly toggle the screen texts on or off!
