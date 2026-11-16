# Reinterpretation of Hostility

Minecraft 1.20.1 (Forge) 向けの敵強化MODです。**L2Hostility** をベースに、「理不尽さ」や他MOD依存（L2Complements）・救済アイテムを排し、**L2Library のみに依存**する再解釈版として設計されています。

A Minecraft 1.20.1 (Forge) enemy-scaling mod reinterpreting **L2Hostility**. It removes L2Complements dependencies, salvation items, and overly punishing mechanics, depending **only on L2Library**.

---

## 特徴 / Features

- **依存関係 / Dependencies**:
  - 必須 / Required: **Minecraft 1.20.1**, **Forge (47.4.0+)**, **L2Library (2.4.0+)**
  - 任意互換 / Optional Compatibility:
    - **Curios API**: 導入時、「黙示録」「封印」の対象に Curios スロットが含まれます / Included in Apocalypse and Seal targets when installed.
    - **Jade**: モブのレベルと特性を Jade のオーバーレイに表示します / Displays mob level and traits in Jade HUD.
    - **Neat** (https://www.curseforge.com/minecraft/mc-mods/neat): 頭上のレベル・特性表示が Neat の体力バーと重ならないよう位置を自動調整します / Automatically offsets overhead level/trait rendering to avoid overlapping Neat health bars.
- **段階的アンロック（不可逆） / Irreversible Unlock**:
  - 初期状態では難易度・レベルシステムは停止しています。
  - インベントリ右上の「敵対度 / Hostility」タブから **「敵対の循環に踏み入る / Step into the Cycle of Hostility」** をクリックするか、**ネザーへ到達**することで稼働します。
- **緩やかな危険度上昇と天井 / Gentle Progression & Level Cap**:
  - 危険度は**敵モブの討伐のみ**で上昇し、上昇ペースは本家の半分（倍緩やか）です。
  - レベル上限は **600**（危険度がどれだけ高くても天井）。
  - 特性は **Lv.90** 以上で出現し、特性レベルの概念はありません。
- **9種類の特性 / 9 Mob Traits**:
  - **復活 (Revive)**: 死亡時に1度だけ全快復活（レベル・特性維持）
  - **修復力 (Regeneration)**: 8秒以上被ダメージなしで毎tick最大HPの0.1%回復
  - **学習 (Adaptation)**: 1種類のダメージに適応（1回毎10%乗算軽減、下限50%）
  - **黙示録 (Apocalypse)**: 攻撃時30%で装備/Curios 1つのAttribute恩恵を5秒間-10%
  - **破壊 (Destruction)**: 攻撃時40%でバフ1つ解除、失敗時はバフ数×10%追加ダメージ
  - **非現実特攻 (Unreality)**: 攻撃時40%で10秒間ランダムなエンチャントLv-1（重複可・下限0）
  - **強固 (Fortitude)**: 近接物理ダメージを70%軽減
  - **加護 (Protection)**: 魔法ダメージを70%軽減
  - **奪取 (Plunder)**: 攻撃時25%で20秒間最大HP Attributeに×0.99（独立重複・上限なし・死亡時リセット）

## 導入手順 / Installation

1. Minecraft 1.20.1 と対応する Forge をインストールします。
2. **L2Library**（および任意で Curios API）を `mods` フォルダに入れます。
3. 本MODの JAR ファイル（`rohostility-<version>.jar`）を `mods` フォルダに配置します。

## ライセンス・帰属表記 / License & Attribution

本プロジェクトは **GNU Lesser General Public License v2.1 (LGPL-2.1)** の下で公開されています。詳細は `LICENSE` ファイルをご確認ください。  
This project is licensed under the **GNU Lesser General Public License v2.1 (LGPL-2.1)**. See `LICENSE` for details.

- **テクスチャ流用表記 / Texture Attribution**:
  - 状態異常「呪詛 (`curse.png`)」のアイコンテクスチャは、**L2Complements**（Minecraft-LightLand 著 / LGPL-2.1）の呪い (`curse.png`) テクスチャを流用しています。
  - The status effect icon for **Curse (`curse.png`)** is reused from **L2Complements** (by Minecraft-LightLand, licensed under LGPL-2.1).
