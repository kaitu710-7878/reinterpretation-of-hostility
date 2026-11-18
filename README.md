# Reinterpretation of Hostility

**[English](#english) | [日本語](#日本語)**

---

<a id="english"></a>
## English

A Minecraft 1.20.1 (Forge) enemy-scaling mod reinterpreting **L2Hostility**. It removes L2Complements dependencies, salvation items, and overly punishing mechanics, depending **only on L2Library (and its bundled L2DamageTracker / L2Tabs)**.

### 1. Dependencies & Optional Compatibility
- **Required**: **Minecraft 1.20.1**, **Forge (47.4.0+)**, **L2Library (2.4.0+)**
- **Optional Compatibility**:
  - **Curios API**: Included in **Apocalypse** and **Seal** target slots when installed.
  - **Jade**: Displays mob level and traits in the Jade HUD.
  - **Neat** (https://www.curseforge.com/minecraft/mc-mods/neat): Automatically offsets overhead level/trait rendering to avoid overlapping Neat health bars.

### 2. Irreversible Unlock
- The hostility system starts dormant (mobs spawn at Lv.0 without traits).
- It is permanently activated for a player when they click **Step into the Cycle of Hostility** in the **Hostility** inventory tab (`L2Tabs`) or enter the **Nether**.

### 3. Danger, Level Scaling & Stats
- **Danger Progression**:
  - Danger increases **only by killing hostile mobs** (no passive time or dimension-entry danger additions).
  - Uses an experience-table progression formula (`killsPerLevel = 60` same-level kills per danger point).
  - Accumulated danger has a hard cap (`maxDangerCap = 2000.0`) to keep calculation and rendering lightweight.
- **Spawn Level Calculation**:
  - Calculated from the nearest unlocked player's danger multiplied by a random factor (`0.85 - 1.15`) plus random variance (`±5`), with no flat dimension spawn bonuses.
  - **Level Cap**: Base cap is **`600`**. Reaching **The End** grants **`+10`**, and each unique **modded dimension** visited grants **`+5`** (viewable in the Hostility tab).
- **Attribute & Damage Scaling**:
  - **Max HP**: `+3% / Lv` (`healthBonusPerLevel = 0.03`)
  - **Attack Damage**: `+2% / Lv` (`damageBonusPerLevel = 0.02`)
  - Applies not only to melee attacks, but also to **ranged, indirect, and magic attacks** (such as Skeleton arrows, Evoker fangs, Blaze/Ghast fireballs, and Ender Dragon breath).

### 4. Trait Rolling, Status Effects & UI
- **Trait Acquisition**: Mobs roll traits as soon as their level (cost budget) meets a trait's cost (no trait levels, up to 8 traits per mob).
- **Custom Status Effects**:
  - **Curse (`rohostility:curse`)**: Harmful effect that reduces all healing received by **50%**.
  - **Revive Prevention (`rohostility:revive_prevention`)**: Neutral effect that **blocks Totem of Undying activation** while active.
- **Visuals & Tooltips**:
  - **Overhead Display**: Renders level and trait names above hostile mobs within 32 blocks in line of sight. The `L2Tabs` screen supports word-wrapping, scrolling, and hover tooltips for every trait.
  - **Unreality / Apocalypse Tooltips**: Weakened armor/Curios dynamically display reduced enchantments in red (`OrigLv-ReducedLv (Xs)`) and effective `-10%` attribute values.

### 5. All 36 Mob Traits (Ordered by Cost)

#### Cost 50
- **Regeneration (`regeneration`)**: Regenerates `0.1%` max HP per tick after 8s (160 ticks) without taking damage.
- **Step Up (`step_up`)**: Steps up any block height instantly without slowing down.
- **Poison Strike (`poison_strike`)**: `50%` chance on hit to inflict Poison I for 6s.
- **Swift (`swift`)**: Increases Movement Speed by `+15%` (`MULTIPLY_TOTAL`).
- **Ignite (`ignite`)**: `45%` chance on hit to set the target on fire for 4s.
- **Immunity (`immunity`)**: `50%` chance to reduce incoming debuff level by 1 (min Lv.1); caps incoming debuff duration at 30s.
- **Ender (`ender`)**: Nullifies projectiles (`IS_PROJECTILE`) like an Enderman without dodging or teleporting.
- **Illusion (`illusion`)**: Displays 2 Illusioner-style visual decoys at independent shifting offsets without turning invisible.

#### Cost 60 - 90
- **[60] Heavy (`heavy`)**: Movement speed `-15%`, Knockback Resistance `+0.3`, Attack Damage `+10%`.
- **[70] Wither Strike (`weakness_strike`)**: `50%` chance on hit to inflict Wither I for 6s.
- **[90] Resentment (`resentment`)**: `40%` chance on hit to inflict Curse I for 8s (`-50%` healing).

#### Cost 100 - 120
- **[100] Sturdy (`sturdy`)**: Grants `+8` flat and `+10%` Armor and Armor Toughness.
- **[100] Counterattack (`counterattack`)**: Deals `3` physical damage to attackers when hit (6s CD); triggers on-hit traits even during i-frames.
- **[120] Lifesteal (`lifesteal`)**: Heals `0.5%` of own max HP on hit.
- **[120] Universal Resistance (`universal_resistance`)**: Reduces all non-invul-bypass incoming damage by `20%`.
- **[120] Vitality (`vitality`)**: Grants `+10` flat and `+10%` Max HP and heals to full on spawn.
- **[120] Pull (`pull`)**: Every 9s, pulls the target horizontally toward the owner with Knockback II velocity (`1.4`), ignoring knockback resistance.
- **[120] Repulse (`repulse`)**: Every 9s, pushes the target horizontally away with Knockback II velocity (`1.4`), ignoring knockback resistance.

#### Cost 150
- **Revive (`revive`)**: Revives once upon death with full HP, retaining level and traits.
- **Adaptation (`adaptation`)**: Adapts to 1 damage type, multiplying damage by `0.9x` per hit (floored at `50%`).
- **Destruction (`destruction`)**: `40%` chance on hit to strip 1 beneficial effect; otherwise deals `+10%` damage per beneficial effect.
- **Fortitude (`fortitude`)**: Reduces melee physical damage by `70%`.
- **Protection (`protection`)**: Reduces magic damage (vanilla tags and `forge:is_magic`) by `70%`.
- **Berserker (`berserker`)**: Below `50%` HP: `+10%` ATK, `+0.1` ATK SPD, `+1` reach, and `0.1%/s` regen (paused 3s after taking damage).
- **Armor Pierce (`armor_pierce`)**: Converts `5%` of attack damage into armor-piercing damage.
- **Protection Pierce (`protection_pierce`)**: Converts `5%` of attack damage into enchantment/effect-piercing damage (armor still applies).

#### Cost 170 - 250
- **[170] Mental Disruption (`mental_disruption`)**: `40%` chance on hit to subtract `1.5%` from `l2damagetracker:magic_damage` for 8s (independent timers, resets on death).
- **[170] Paralysis (`paralysis`)**: `35%` chance on hit to subtract `1%` from `generic.attack_damage` for 8s (independent timers, resets on death).
- **[180] Inspire (`inspire`)**: Summons 3 Vexes every 20s in combat; gains `+5%` ATK, Speed, and Armor per active summoned Vex.
- **[200] Unreality (`unreality`)**: `40%` chance on hit to reduce a random armor enchantment level by 1 for 10s (stackable, floor 0).
- **[200] Anti-Revive (`anti_revive`)**: `50%` chance on hit to inflict Revive Prevention for 8s (blocks Totem of Undying).
- **[250] Corrosion (`corrosion`)**: `30%` chance on hit to subtract `1%` Armor and `0.5%` Toughness for 8s (independent timers, resets on death).

#### Cost 300 - 550
- **[300] Apocalypse (`apocalypse`)**: `30%` chance on hit to weaken 1 armor/Curios item's attributes by `10%` for 5s.
- **[300] Guardian Seal (`guardian_seal`)**: Every 40s in combat, summons 3 glowing level-scaled traitless Zombies; reduces damage taken by `75%` while any live. Zombies discard immediately on suffocation, >32m distance, lost target, or getting stuck, and despawn 3x faster.
- **[400] Plunder (`plunder`)**: `25%` chance on hit to subtract `1%` of final max HP for 20s (independent timers, no HUD icon, resets on death).
- **[550] Seal (`seal`)**: `10%` chance on hit to seal 1 random armor/Curios item for 5 ticks, disabling all passive benefits until auto-restored.

### 6. Commands & Debug Tools
- `/rohostility danger set <targets> <value>` / `add <targets> <delta>`: Set or modify a player's accumulated danger points.
- `/rohostility trait add <targets> <trait>`: Add a trait to target mobs.
- `/rohostility unlock <targets>`: Unlock the hostility system for target players.
- **Debug Mob Editor (`rohostility:debug_mob_editor`)**: Right-click to open a scrollable GUI to configure target level and traits; right-click any hostile mob to respawn it with those overridden settings.

### 7. License & Attribution
This project is licensed under the **GNU Lesser General Public License v2.1 (LGPL-2.1)**. See `LICENSE` for details.
- **Texture Attribution**: The status effect icon for **Curse (`curse.png`)** is reused from **L2Complements** (by Minecraft-LightLand, licensed under LGPL-2.1).

### 8. Installation & Configuration
1. Install Minecraft 1.20.1 with Forge (`47.4.0+`).
2. Place **L2Library** (and optionally Curios API / Jade / Neat) into your `mods` folder.
3. Place `rohostility-<version>.jar` into your `mods` folder.
4. See [CONFIG.md](CONFIG.md) for all configuration options.

---

<a id="日本語"></a>
## 日本語

Minecraft 1.20.1 (Forge) 向けの敵強化MODです。**L2Hostility** をベースに、「理不尽さ」や他MOD依存（L2Complements）・救済アイテムを排し、**L2Library（および内蔵の L2DamageTracker / L2Tabs）のみに依存**する再解釈版として設計されています。

### 1. 依存関係・任意互換
- **必須**: **Minecraft 1.20.1**, **Forge (47.4.0+)**, **L2Library (2.4.0+)**
- **任意互換**:
  - **Curios API**: 導入時、「黙示録」「封印」の対象スロットに Curios が含まれます。
  - **Jade**: モブのレベルと特性を Jade のオーバーレイに表示します。
  - **Neat** (https://www.curseforge.com/minecraft/mc-mods/neat): 頭上のレベル・特性表示が Neat の体力バーと重ならないよう表示位置を自動オフセットします。

### 2. 段階的アンロック（不可逆）
- 初期状態では難易度・レベルシステムは停止しており、敵モブはレベル0・特性なしでスポーンします。
- インベントリ画面の **L2Tabs「敵対度」タブ** を開き **「敵対の循環に踏み入る」** を押すか、**ネザーへ到達**すると、そのプレイヤーの難易度システムが永久に稼働します。

### 3. 危険度上昇・レベル算出・ステータス補正
- **危険度の上昇**:
  - 危険度は**敵モブの討伐のみ**で上昇します（時間経過やディメンション移動による加算はありません）。
  - 経験値テーブル式を採用しており、同レベルの敵モブ討伐で危険度が上昇します（`killsPerLevel = 60`）。
  - 計算・描画負荷を抑えるため、蓄積危険度には上限（`maxDangerCap = 2000.0`）を設けています。
- **スポーンレベルの決定**:
  - 最寄りのアンロック済みプレイヤーの危険度に対し、ランダム倍率（`0.85 〜 1.15`）およびランダム振れ幅（`±5`）を掛けて算出されます（ディメンションによるフラット加算なし）。
  - **レベル上限**: 基本上限 **`600`**。**エンド到達で `+10`**、**他MOD製ディメンション1種到達ごとに `+5`** 上限が拡張されます（現在の推定出現レベル範囲・上限はタブ画面で確認可能）。
- **ステータス補正**:
  - **最大HP**: `+3% / Lv` (`healthBonusPerLevel = 0.03`)
  - **攻撃力**: `+2% / Lv` (`damageBonusPerLevel = 0.02`)
  - 近接攻撃だけでなく、**スケルトンの弓矢・エヴォーカーの牙・ブレイズやガストの火球・エンダードラゴンのブレスなどの遠距離・間接・魔法攻撃**にもレベル倍率が正しく適用されます。

### 4. 特性の抽選と状態異常・UI
- **特性の出現条件**: モブのレベル（＝コスト予算）が特性コスト以上であれば特性を獲得できます。特性レベルの概念はなく、1体あたり最大8個までコスト・重みに基づき抽選されます。
- **状態異常**:
  - **呪詛 (`rohostility:curse`)**: 悪性効果。効果中はあらゆる回復量が **50% 減少**します。
  - **復活阻止 (`rohostility:revive_prevention`)**: 中立効果。効果中は **不死のトーテムが発動できなくなります**。
- **視認性・ツールチップ**:
  - **頭上表示**: 視界内の敵モブ（32ブロック以内）の頭上にレベルと特性名を表示します（L2Tabs 画面では各特性にカーソルを合わせて詳細説明を確認でき、自動改行＆スクロールに対応しています）。
  - **非現実特攻 / 黙示録ツールチップ**: 弱体化中の防具・Curios には赤字で低下後のエンチャントレベル (`元のLv-低下Lv (残り秒数)`) や `-10%` 後の動的 Attribute 値が表示されます。

### 5. 36種類の特性一覧（コスト順）

#### コスト 50
- **修復力 (`regeneration`)**: 8秒（160 tick）以上ダメージを受けないと、毎 tick 最大HPの `0.1%` を回復します。
- **段差無視 (`step_up`)**: どれだけ高いブロックの段差でも減速せず、ハーフブロックのように即座に登ることができます。
- **毒撃 (`poison_strike`)**: 攻撃ヒット時 `50%` の確率で「毒 Lv.1」を6秒間付与します。
- **迅速 (`swift`)**: 移動速度 Attribute に `+15%`（`MULTIPLY_TOTAL`）の補正を得ます。
- **着火 (`ignite`)**: 攻撃ヒット時 `45%` の確率で対象を4秒間引火させます。
- **免疫 (`immunity`)**: デバフを受ける際、`50%` の確率でエフェクトレベルを1下げます（Lv.1 未満にはなりません）。また、30秒以上のデバフを受けると効果時間を30秒に上書きします。
- **エンダー (`ender`)**: エンダーマンと同様に矢などの飛び道具（`IS_PROJECTILE`）を無効化します（回避やテレポートは行いません）。
- **幻影 (`illusion`)**: バニラのイリュージョナーと同様の見た目だけの幻影を2体表示します（本体は透明化せず、各幻影は独立した位置へ周期的に移動・揺らぎます）。

#### コスト 60〜90
- **[60] 鈍重 (`heavy`)**: 移動速度 `-15%`、ノックバック耐性 `+0.3`、攻撃力 `+10%` を得ます。
- **[70] 衰弱 (`weakness_strike`)**: 攻撃ヒット時 `50%` の確率で「ウィザー（衰弱） Lv.1」を6秒間付与します。
- **[90] 怨嗟 (`resentment`)**: 攻撃ヒット時 `40%` の確率で「呪詛 Lv.1」（8秒間、回復量50%減少）を付与します。

#### コスト 100〜120
- **[100] 頑丈 (`sturdy`)**: 防御力と防具強度をそれぞれ固定値 `+8` かつ割合 `+10%` 増加させます。
- **[100] 反撃 (`counterattack`)**: ダメージを受けたとき攻撃者へ `3` 物理ダメージを与えます（クールダウン6秒）。この反撃は無敵時間中でも攻撃時確率特性のトリガー判定を発生させます。
- **[120] 生命吸収 (`lifesteal`)**: 攻撃ヒット時、自身の最大HPの `0.5%` を回復します。
- **[120] 万能耐性 (`universal_resistance`)**: 無敵貫通を除くあらゆる被ダメージを `20%` 軽減します。
- **[120] 活力 (`vitality`)**: 最大HPを固定値 `+10` かつ割合 `+10%` 増加させ、スポーン時に引き上がった最大HPまで回復します。
- **[120] 吸引 (`pull`)**: 9秒クールダウンで敵対対象を特性所有者の方向へ、ノックバックII相当（強度 `1.4`）の水平ベクトル加算で引き寄せます（ノックバック耐性で軽減不可）。
- **[120] 反発 (`repulse`)**: 9秒クールダウンで敵対対象を特性所有者の反対方向へ、ノックバックII相当（強度 `1.4`）の水平ベクトル加算で弾き飛ばします（ノックバック耐性で軽減不可）。

#### コスト 150
- **復活 (`revive`)**: 死亡時に1度だけ、レベルと特性を維持したまま最大HPで復活します。
- **学習 (`adaptation`)**: 最初に受けた1種類のダメージタイプのみを記憶し、同種ダメージを受けるごとに `0.9` 倍（10%ずつ乗算軽減、下限 `50%`）に軽減します。
- **破壊 (`destruction`)**: 攻撃時 `40%` の確率で対象の有益バフを1つ解除し、解除しなかった場合は対象の有益バフ数 × `10%` の追加ダメージを与えます。
- **強固 (`fortitude`)**: 近接物理ダメージを `70%` 軽減します。
- **加護 (`protection`)**: 魔法ダメージ（バニラ魔法タグおよび `forge:is_magic`）を `70%` 軽減します。
- **狂戦士 (`berserker`)**: HPが50%未満になると攻撃力 `+10%`、攻撃速度 `+0.1`、射程 `+1`、毎秒 `0.1%` 再生（被ダメージ後3秒間停止）を得ます。
- **装甲貫通 (`armor_pierce`)**: 攻撃の `5%` が物理防御貫通ダメージとして別枠で与えられます。
- **加護貫通 (`protection_pierce`)**: 攻撃の `5%` がエンチャント・ポーション等の耐性貫通ダメージとして与えられます（物理防御による軽減は適用）。

#### コスト 170〜250
- **[170] 精神撹乱 (`mental_disruption`)**: 攻撃時 `40%` の確率で8秒間、対象の `l2damagetracker:magic_damage` Attribute に `-1.5%` の割合減算を加えます（奪取と同じ独立タイマー重複・死亡時リセット）。
- **[170] 麻痺 (`paralysis`)**: 攻撃時 `35%` の確率で8秒間、対象の `generic.attack_damage` Attribute に `-1%` の割合減算を加えます（奪取と同じ独立タイマー重複・死亡時リセット）。
- **[180] 鼓舞 (`inspire`)**: 戦闘中20秒ごとにヴェックスを3体召喚し、生存している召喚ヴェックス1体ごとに自身の攻撃力・移動速度・防御力を `+5%` 上昇させます。
- **[200] 非現実特攻 (`unreality`)**: 攻撃時 `40%` の確率で10秒間、対象防具のランダムなエンチャントレベルを1低下させます（重複可・下限0）。
- **[200] 不死殺し (`anti_revive`)**: 攻撃時 `50%` の確率で中立エフェクト「復活阻止」（8秒間、不死のトーテム発動不可）を付与します。
- **[250] 侵食 (`corrosion`)**: 攻撃時 `30%` の確率で8秒間、対象の防御力に `-1%`・防具強度に `-0.5%` の割合減算を加えます（奪取と同じ独立タイマー重複・死亡時リセット）。

#### コスト 300〜550
- **[300] 黙示録 (`apocalypse`)**: 攻撃時 `30%` の確率で防具/Curios の中から1つの Attribute 恩恵を5秒間 `-10%` 弱体化します。
- **[300] 守護の印 (`guardian_seal`)**: 敵対時40秒のクールダウンで、常時発光・特性所有者のレベル適用・特性なしのゾンビを3体召喚します。召喚ゾンビが1体でも生存している間、特性所有者の受けるダメージを `75%` 軽減します。ゾンビは窒息・32ブロック離脱・敵対対象喪失・停止（スタック）時に即座に消滅し、通常より3倍デスポーンしやすい仕様です。
- **[400] 奪取 (`plunder`)**: 攻撃時 `25%` の確率で20秒間、対象の最終最大HPを `1%` ずつ割合減算します（独立タイマーで重複・バフアイコンなし・死亡時リセット）。
- **[550] 封印 (`seal`)**: 攻撃ヒット時 `10%` の確率で防具/Curios からランダムに1つを 5 tick 間「封印されたアイテム」に置き換え、Attribute 以外の特殊恩恵も含めて一時停止させます（自動解除）。

### 6. コマンド & デバッグツール
- `/rohostility danger set <targets> <value>` / `add <targets> <delta>`: プレイヤーの蓄積危険度を設定・増減します。
- `/rohostility trait add <targets> <trait>`: 指定モブへ特性を付与します。
- `/rohostility unlock <targets>`: 対象プレイヤーの敵対システムをアンロックします。
- **デバッグ用モブ編集ツール (`rohostility:debug_mob_editor`)**:
  - 右クリックでスクロール対応の専用GUIを開き、目標レベルと特性セットを保存できます。
  - 敵モブに対して右クリックすると、指定したレベル・特性に上書きした状態でリスポーン（他MODのスポーン補正含む再計算）させます。

### 7. ライセンス・帰属表記
本プロジェクトは **GNU Lesser General Public License v2.1 (LGPL-2.1)** の下で公開されています。詳細は `LICENSE` ファイルをご確認ください。
- **テクスチャ流用表記**: 状態異常「呪詛 (`curse.png`)」のアイコンテクスチャは、**L2Complements**（Minecraft-LightLand 著 / LGPL-2.1）の呪い (`curse.png`) テクスチャを流用しています。

### 8. 導入手順・コンフィグ
1. Minecraft 1.20.1 と対応する Forge（`47.4.0+`）をインストールします。
2. **L2Library**（および任意で Curios API / Jade / Neat）を `mods` フォルダに入れます。
3. 本MODの JAR ファイル（`rohostility-<version>.jar`）を `mods` フォルダに配置します。
4. 詳細なコンフィグ項目については [CONFIG.md](CONFIG.md) をご覧ください。
