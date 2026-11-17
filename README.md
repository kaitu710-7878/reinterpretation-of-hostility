# Reinterpretation of Hostility

Minecraft 1.20.1 (Forge) 向けの敵強化MODです。**L2Hostility** をベースに、「理不尽さ」や他MOD依存（L2Complements）・救済アイテムを排し、**L2Library のみに依存**する再解釈版として設計されています。

A Minecraft 1.20.1 (Forge) enemy-scaling mod reinterpreting **L2Hostility**. It removes L2Complements dependencies, salvation items, and overly punishing mechanics, depending **only on L2Library**.

---

## 特徴 / Features

### 依存関係 / Dependencies
- **必須 / Required**: **Minecraft 1.20.1**, **Forge (47.4.0+)**, **L2Library (2.4.0+)**
- **任意互換 / Optional Compatibility**:
  - **Curios API**: 導入時、「黙示録」「封印」の対象に Curios スロットが含まれます / Included in Apocalypse and Seal targets when installed.
  - **Jade**: モブのレベルと特性を Jade のオーバーレイに表示します / Displays mob level and traits in Jade HUD.
  - **Neat** (https://www.curseforge.com/minecraft/mc-mods/neat): 頭上のレベル・特性表示が Neat の体力バーと重ならないよう位置を自動調整します / Automatically offsets overhead level/trait rendering to avoid overlapping Neat health bars.

### 段階的アンロック（不可逆） / Irreversible Unlock
- 初期状態では難易度・レベルシステムは停止しています。
- インベントリ右上の「敵対度 / Hostility」タブから **「敵対の循環に踏み入る / Step into the Cycle of Hostility」** をクリックするか、**ネザーへ到達**することで稼働します。
- The difficulty system starts dormant and is permanently activated by clicking **Step into the Cycle of Hostility** in the inventory's **Hostility** tab or by entering the **Nether**.

### 危険度とレベルスケーリング / Danger & Level Scaling
- **危険度の上昇 / Danger Progression**: 危険度は**敵モブの討伐のみ**で上昇し、上昇ペースは本家の半分（倍緩やか：`killsPerLevel = 60`）です。計算負荷防止のため蓄積上限（`maxDangerCap = 2000.0`）があります。
- **レベル算出 / Mob Level Calculation**: スポーン時のモブレベルは最寄りプレイヤーの危険度にランダム係数（`0.85〜1.15` ± 振れ幅）を掛けた相関値で決まります（ディメンションによるフラット加算はありません）。
- **レベル上限と拡張 / Level Cap & Expansion**: 基本レベル上限は **600**。**エンド到達で +10**、**他MOD製ディメンション1種到達ごとに +5** 上限が拡張されます。
- **特性の獲得条件 / Trait Acquisition**: 特性出現の最低レベル制限（Lv.90制限）はなく、モブのレベル（＝コスト予算）が特性コスト以上であれば即座に抽選・獲得されます（特性レベルの概念はなく、1体あたり最大8個まで）。

### 36種類の特性（コスト順） / 36 Mob Traits (Ordered by Cost)
- **[50] 修復力 (Regeneration)**: 8秒以上被ダメージなしで毎tick最大HPの0.1%回復 / Regenerates 0.1% max HP per tick after 8s out of combat.
- **[50] 段差無視 (Step Up)**: どんな高さの段差でも減速せず即座に移動可能 / Steps up any block height without slowing down.
- **[50] 毒撃 (Poison Strike)**: 攻撃時50%の確率で毒 Lv.1（6秒）付与 / 50% chance on hit to inflict Poison I for 6s.
- **[50] 迅速 (Swift)**: 移動速度Attributeが+15%増加 / Increases Movement Speed by +15%.
- **[50] 着火 (Ignite)**: 攻撃時45%の確率で4秒間引火させる / 45% chance on hit to set the target on fire for 4s.
- **[50] 免疫 (Immunity)**: デバフ受領時50%でレベル-1（下限Lv.1）、30秒以上の効果時間を30秒に上書き / 50% chance to lower incoming debuff level by 1 (min Lv.1); caps duration at 30s.
- **[50] エンダー (Ender)**: エンダーマン同様に飛び道具を無効化（回避・テレポートなし） / Nullifies projectiles like an Enderman without teleporting.
- **[50] 幻影 (Illusion)**: イリュージョナー同様の見た目だけの幻影を2体表示（本体は透明化しない） / Displays 2 Illusioner-style visual decoys without turning invisible.
- **[60] 鈍重 (Heavy)**: 移動速度-15%、ノックバック耐性+0.3、攻撃力+10% / Movement speed -15%, Knockback Resistance +0.3, Attack Damage +10%.
- **[70] 衰弱 (Wither Strike)**: 攻撃時50%の確率でウィザー Lv.1（6秒）付与 / 50% chance on hit to inflict Wither I for 6s.
- **[90] 怨嗟 (Resentment)**: 攻撃時40%の確率で「呪詛」Lv.1（8秒、回復量50%減少）付与 / 40% chance on hit to inflict Curse I for 8s (-50% healing).
- **[100] 頑丈 (Sturdy)**: 防御力と防具強度が固定値+8かつ割合+10%増加 / Grants +8 flat and +10% Armor and Armor Toughness.
- **[100] 反撃 (Counterattack)**: 被ダメージ時に3物理ダメージで反撃（CT6秒）。無敵時間中でも攻撃時特性のトリガー判定が発生 / Deals 3 physical damage on hit (6s CD) and triggers on-hit traits even during i-frames.
- **[120] 生命吸収 (Lifesteal)**: 攻撃ヒット時に自身の最大HPの0.5%を回復 / Heals 0.5% of max HP on hit.
- **[120] 万能耐性 (Universal Resistance)**: 全ダメージ（無敵貫通を除く）を20%軽減 / Reduces all non-invul-bypass damage by 20%.
- **[120] 活力 (Vitality)**: 最大HPが固定値+10かつ割合+10%増加（スポーン時に増加分まで全快） / Grants +10 flat and +10% Max HP (heals to full on spawn).
- **[120] 吸引 (Pull)**: 9秒クールダウンで敵対対象を自身方向へノックバックII相当の水平ベクトル加算（ノックバック耐性無効） / Every 9s, pulls the target toward the owner with Knockback II horizontal velocity (ignores KB resistance).
- **[120] 反発 (Repulse)**: 9秒クールダウンで敵対対象を反対方向へノックバックII相当の水平ベクトル加算（ノックバック耐性無効） / Every 9s, pushes the target away with Knockback II horizontal velocity (ignores KB resistance).
- **[150] 復活 (Revive)**: 死亡時に1度だけレベル・特性を維持して全快復活 / Revives once upon death with full HP, keeping level and traits.
- **[150] 学習 (Adaptation)**: 1種類のダメージに適応（被弾毎10%乗算軽減、下限50%） / Adapts to 1 damage type (0.9x per hit, floored at 0.5x).
- **[150] 破壊 (Destruction)**: 攻撃時40%でバフ1つ解除、失敗時はバフ数×10%追加ダメージ / 40% chance on hit to strip 1 buff, or deal +10% damage per buff.
- **[150] 強固 (Fortitude)**: 近接物理ダメージを70%軽減 / Reduces melee physical damage by 70%.
- **[150] 加護 (Protection)**: 魔法ダメージ（バニラ魔法タグ / `forge:is_magic`）を70%軽減 / Reduces magic damage (vanilla tags / `forge:is_magic`) by 70%.
- **[150] 狂戦士 (Berserker)**: HP50%未満で攻撃力+10%、攻撃速度+0.1、射程+1、毎秒0.1%再生（被弾後3秒停止） / Below 50% HP: +10% ATK, +0.1 ATK SPD, +1 reach, 0.1%/s regen (paused 3s on hit).
- **[150] 装甲貫通 (Armor Pierce)**: 攻撃の5%が物理防御貫通ダメージに変換 / Converts 5% of attack damage to armor-piercing damage.
- **[150] 加護貫通 (Protection Pierce)**: 攻撃の5%がエンチャント・エフェクト貫通ダメージ（物理防御は適用）に変換 / Converts 5% of attack damage to enchantment/effect-piercing damage (armor still applies).
- **[170] 精神撹乱 (Mental Disruption)**: 攻撃時40%で8秒間、L2魔法攻撃力 (`l2damagetracker:magic_damage`) に-1.5%割合減算（独立重複） / 40% chance on hit to subtract 1.5% L2 Magic Damage for 8s (independent timers).
- **[170] 麻痺 (Paralysis)**: 攻撃時35%で8秒間、近接攻撃力 (`generic.attack_damage`) に-1%割合減算（独立重複） / 35% chance on hit to subtract 1% Attack Damage for 8s (independent timers).
- **[180] 鼓舞 (Inspire)**: 20秒ごとにヴェックスを3体召喚。生存している召喚ヴェックス1体ごとに攻撃力・移動速度・防御力+5% / Summons 3 Vexes every 20s; +5% ATK/SPD/Armor per active summoned Vex.
- **[200] 非現実特攻 (Unreality)**: 攻撃時40%で10秒間防具のランダムなエンチャントLv-1（重複可・下限0、ツールチップに赤字表示） / 40% chance on hit to reduce a random armor enchantment by 1 for 10s (stackable, floor 0).
- **[200] 不死殺し (Anti-Revive)**: 攻撃時50%で中立効果「復活阻止」（8秒、不死のトーテム発動不可）付与 / 50% chance on hit to inflict Revive Prevention for 8s (blocks Totem of Undying).
- **[250] 侵食 (Corrosion)**: 攻撃時30%で8秒間、防御力-1%・防具強度-0.5%を割合減算（独立重複） / 30% chance on hit to subtract 1% Armor & 0.5% Toughness for 8s (independent timers).
- **[300] 黙示録 (Apocalypse)**: 攻撃時30%で防具/Curios 1つのAttribute恩恵を5秒間-10%（ツールチップに動的表示） / 30% chance on hit to weaken 1 armor/Curios item's attributes by 10% for 5s.
- **[300] 守護の印 (Guardian Seal)**: 敵対時40秒毎に発光ゾンビ3体召喚（レベル適用・特性なし・3倍デスポーン・スタック時即消滅）。生存中は被ダメージ75%軽減 / Every 40s in combat, summons 3 glowing level-scaled Zombies (no traits, fast despawn/discard on stuck); -75% damage taken while any live.
- **[400] 奪取 (Plunder)**: 攻撃時25%で20秒間、最終最大HPを1%ずつ割合減算（独立タイマーで重複・死亡時リセット） / 25% chance on hit to subtract 1% of final max HP for 20s (independent timers, resets on death).
- **[550] 封印 (Seal)**: 攻撃ヒット時10%で防具/Curiosからランダムに1つを5tick封印（装備恩恵をすべて無効化し自動で解除） / 10% chance on hit to seal 1 random armor/Curios item for 5 ticks.

### コマンド & デバッグツール / Commands & Debug Tools
- `/rohostility danger set <targets> <value>` / `add <targets> <delta>`: プレイヤーの危険度を設定・増減します。
- `/rohostility trait add <targets> <trait>`: 指定モブに特性を付与します。
- `/rohostility unlock <targets>`: 敵対システムを強制解放します。
- **デバッグ用モブ編集ツール (`rohostility:debug_mob_editor`)**: GUIからレベルと特性を指定し、敵モブを右クリックするとその内容で上書きリスポーンさせます。

---

## 導入手順 / Installation

1. Minecraft 1.20.1 と対応する Forge をインストールします。
2. **L2Library**（および任意で Curios API / Jade / Neat）を `mods` フォルダに入れます。
3. 本MODの JAR ファイル（`rohostility-<version>.jar`）を `mods` フォルダに配置します。
4. 詳細なコンフィグ項目については [CONFIG.md](CONFIG.md) をご覧ください。

## ライセンス・帰属表記 / License & Attribution

本プロジェクトは **GNU Lesser General Public License v2.1 (LGPL-2.1)** の下で公開されています。詳細は `LICENSE` ファイルをご確認ください。  
This project is licensed under the **GNU Lesser General Public License v2.1 (LGPL-2.1)**. See `LICENSE` for details.

- **テクスチャ流用表記 / Texture Attribution**:
  - 状態異常「呪詛 (`curse.png`)」のアイコンテクスチャは、**L2Complements**（Minecraft-LightLand 著 / LGPL-2.1）の呪い (`curse.png`) テクスチャを流用しています。
  - The status effect icon for **Curse (`curse.png`)** is reused from **L2Complements** (by Minecraft-LightLand, licensed under LGPL-2.1).
