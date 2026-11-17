# Changelog / 変更履歴

## [0.2.11-beta] - 2026-11-17

### 日本語
- **幻影 (`illusion`) の角度固定化**: 本体を中心に回転し続ける仕様を改め、エンティティごとに内部決定された固定角度へ幻影2体を配置（イリュージョナー風の微小な揺らぎのみ維持）。
- **吸引 (`pull`) / 反発 (`repulse`) の強化**: クールダウンを `18秒` から `9秒` に短縮し、水平ベクトル加算強度をノックバックII相当（`1.4`）へ引き上げ。
- **侵食 (`corrosion`) の確率引き上げ**: 発動確率を `25%` から `30%` に引き上げ（`README.md`・言語ファイルも同期更新）。

### English
- **Fixed-Angle Illusion Decoys**: Replaced orbital rotation around the mob with deterministic entity-specific fixed angles and subtle Illusioner-like sway.
- **Buffed Pull & Repulse**: Reduced cooldown from `18s` to `9s` and increased horizontal velocity impulse to Knockback II equivalent (`1.4`).
- **Buffed Corrosion Chance**: Increased trigger chance on hit from `25%` to `30%` (updated `README.md` and localization files).

## [0.2.10-beta] - 2026-11-17

### 日本語
- **デバッグ用モブ編集UIのスクロール・レスポンシブ対応**: 特性数の増加およびGUIスケール設定によって画面からはみ出さないよう、ウィンドウサイズに応じたパネルサイズ自動調整と、クリッピング（Scissor）＋スクロールバー付きのスクロール一覧へ改修。

### English
- **Scrollable & Responsive Debug Mob Editor UI**: Added dynamic panel sizing, scissored trait grid viewport, and mouse-wheel scrolling with scrollbar so the Debug Mob Editor never overflows regardless of GUI scale or trait count.

## [0.2.9-beta] - 2026-11-17

### 日本語
- **精神撹乱のフォールバック削除**: `L2DamageTracker`（`l2damagetracker:magic_damage`）を直接参照する前提とし、未導入時のフォールバック処理を削除。
- **吸引・反発の垂直ベクトル削除**: 水平方向ベクトルのみを加算するよう修正（Y軸方向への加算なし）。

### English
- **Removed Mental Disruption Fallback**: Removed fallback damage scaling so Mental Disruption relies directly on `l2damagetracker:magic_damage`.
- **Horizontal-Only Pull & Repulse**: Removed vertical (`Y`) velocity addition from Pull and Repulse vector impulses.

## [0.2.8-beta] - 2026-11-17

### 日本語
- **間接・遠距離・魔法攻撃へのレベル倍率適用**: `ATTACK_DAMAGE` Attribute を持たないモブだけでなく、スケルトンの弓矢・エヴォーカーの牙・エンダードラゴンのブレス/火球などの遠距離・間接・魔法攻撃に対してもレベル補正が正しく乗算されるよう改善（近接攻撃の二重適用を防止）。
- **12種類の新特性を追加**:
  - **迅速 (`swift`, 50)**: 移動速度+15%
  - **着火 (`ignite`, 50)**: 攻撃時45%で4秒間引火
  - **吸引 (`pull`, 120)** / **反発 (`repulse`, 120)**: 18秒CDでノックバック耐性無視のベクトル加算（ノックバックI相当）
  - **鈍重 (`heavy`, 60)**: 移動速度-15%、ノックバック耐性+0.3、攻撃力+10%
  - **免疫 (`immunity`, 50)**: デバフ受領時50%でLv-1（下限Lv.1）、30秒以上の効果時間を30秒に上書き
  - **侵食 (`corrosion`, 250)**: 攻撃時25%で8秒間、防御力-1%・防具強度-0.5%（奪取と同ロジック・独立重複）
  - **精神撹乱 (`mental_disruption`, 170)**: 攻撃時40%で8秒間、`l2damagetracker:magic_damage` -1.5%（奪取と同ロジック・未導入時は魔法与ダメ軽減フォールバック）
  - **麻痺 (`paralysis`, 170)**: 攻撃時35%で8秒間、`generic.attack_damage` -1%（奪取と同ロジック・独立重複）
  - **エンダー (`ender`, 50)**: エンダーマン同様に飛び道具を無効化（回避・テレポートなし）
  - **幻影 (`illusion`, 50)**: イリュージョナー同様の見た目だけの幻影を2体表示（透明化なし）
  - **守護の印 (`guardian_seal`, 300)**: 敵対時40秒CDで発光・レベル適用・特性なしゾンビを3体召喚し、生存中は被ダメージ75%軽減（窒息/32m離脱/見失い/停止で即消滅、3倍デスポーン）

### English
- **Level Scaling for Ranged, Indirect, and Magic Attacks**: Extended level-based damage scaling beyond `ATTACK_DAMAGE`-less mobs to projectiles, Evoker fangs, Ender Dragon breath/fireballs, and indirect/magic attacks (avoiding double-scaling on vanilla melee).
- **Added 12 New Traits**:
  - **Swift (`swift`, 50)**: +15% Movement Speed.
  - **Ignite (`ignite`, 50)**: 45% chance on hit to ignite target for 4s.
  - **Pull (`pull`, 120)** / **Repulse (`repulse`, 120)**: 18s CD Knockback-I-equivalent velocity impulse ignoring KB resistance.
  - **Heavy (`heavy`, 60)**: -15% Movement Speed, +0.3 Knockback Resistance, +10% Attack Damage.
  - **Immunity (`immunity`, 50)**: 50% chance to lower incoming debuff level by 1 (min Lv.1); caps duration at 30s.
  - **Corrosion (`corrosion`, 250)**: 25% chance on hit to subtract 1% Armor & 0.5% Toughness for 8s (stackable).
  - **Mental Disruption (`mental_disruption`, 170)**: 40% chance on hit to subtract 1.5% `l2damagetracker:magic_damage` for 8s (with fallback if L2DamageTracker is absent).
  - **Paralysis (`paralysis`, 170)**: 35% chance on hit to subtract 1% `generic.attack_damage` for 8s (stackable).
  - **Ender (`ender`, 50)**: Nullifies projectiles like an Enderman without teleporting.
  - **Illusion (`illusion`, 50)**: Renders 2 Illusioner-style visual decoys without invisibility.
  - **Guardian Seal (`guardian_seal`, 300)**: Every 40s in combat, summons 3 glowing level-scaled traitless Zombies; -75% damage taken while any live (auto-discards on suffocation/32m range/lost target/stuck, 3x faster despawn).

## [0.2.7-beta] - 2026-11-16

### 日本語
- **特性出現レベル制限の撤廃**: `traitMinLevel` のデフォルト値を `90` から `1` へ変更し、モブのレベル（コスト予算）が特性コスト以上であれば即座に特性を獲得できるよう調整（UIの見出し表記および `CONFIG.md` も同期更新）。
- **README.md 最新化**: 全24種類の特性一覧、危険度・レベル上限システム、コマンド＆デバッグツール、任意互換MOD情報を最新の仕様に合わせて全面更新。

### English
- **Removed Trait Minimum Level Gate**: Changed default `traitMinLevel` from `90` to `1` so mobs can immediately acquire traits as soon as their level meets the trait cost (updated UI header and `CONFIG.md`).
- **Updated README.md**: Updated documentation to reflect all 24 traits, current danger/level cap mechanics, commands, debug item, and optional mod integrations.

## [0.2.6-beta] - 2026-11-16

### 日本語
- **加護 (`protection`) の魔法判定整理**: 特定MOD名指しの判定や文字列マッチを撤廃し、バニラ標準タグおよび `L2DamageTracker`（`L2DamageTypes.MAGIC` = `forge:is_magic`）の標準タグによる魔法ダメージ判定へ統一。

### English
- **Protection Trait Magic Tag Cleanup**: Removed mod-specific references and string matching, standardizing magic damage detection on vanilla tags and `forge:is_magic` (matching `L2DamageTracker`'s `L2DamageTypes.MAGIC`).

## [0.2.5-beta] - 2026-11-16

### 日本語
- **UI 改行＆全体スクロール対応**: `HostilityInfoScreen` で自動改行と全体スクロール（スクロールバー付き）に対応し、GUI サイズ設定に左右されず全項目を閲覧できるよう改善。
- **Jade 連携 & オーバーヘッド描画改善 & Neat 互換**:
  - `Jade` プラグイン（`RoHostilityJadePlugin`）を追加し、モブのレベルと特性を表示。
  - 頭上レベル・特性描画の視線・距離判定を本家準拠に改善し、`Neat` 導入時は表示位置を自動オフセットして重なりを回避（`README.md` にリンク記載）。
- **ツールチップ視認性向上**:
  - **非現実特攻**: 低下中のエンチャントを赤文字で `エンチャント名 元のLv-低下Lv (残り秒数)` 形式で表示。
  - **黙示録**: 弱体化中の旨（赤文字）と `-10%` 適用後の実効 Attribute 値を動的表示。
- **対象スロット限定**: 黙示録・封印・非現実特攻の対象を「防具スロット（＋Curios）」のみに限定し、メインハンド／オフハンドを対象外に修正。
- **バランス・効果修正**:
  - 衰弱特性の付与効果を「ウィザー系の衰弱 (`MobEffects.WITHER`)」へ修正。
  - 生命吸収の回復割合を `0.5%` に引き上げ。
  - 加護特性の魔法判定を `l2damagetracker:magic` / `forge:is_magic` / `irons_spellbooks:magic` や `magic`/`spell` 系ダメージタイプに対応させ、ISS 呪文等も正しく 70% 軽減するよう修正。
- **コマンドメッセージ修正**: 危険度設定・変更コマンドの引数順バグを修正。

### English
- **UI Word Wrap & Full Scrolling**: Added line wrapping and full-panel scrolling with scrollbar to `HostilityInfoScreen` independent of GUI scale.
- **Jade Integration, Overhead Rendering & Neat Compatibility**:
  - Added `RoHostilityJadePlugin` to display mob level and traits in Jade.
  - Improved overhead rendering visibility (line-of-sight + 32-block range) and automatically offset rendering when `Neat` is installed (added CurseForge link to `README.md`).
- **Tooltip Enhancements**:
  - **Unreality**: Displays weakened enchantments in red as `Enchantment OrigLv-ReducedLv (Xs)`.
  - **Apocalypse**: Displays active weakening notice and dynamic `-10%` effective attribute values.
- **Slot Scope Fix**: Restricted Apocalypse, Seal, and Unreality to armor slots (+ Curios), excluding mainhand/offhand.
- **Effect & Scaling Fixes**:
  - Fixed Weakness/Wither Strike to apply `MobEffects.WITHER`.
  - Increased Lifesteal recovery to `0.5%` max HP on hit.
  - Expanded Protection trait magic detection (`l2damagetracker:magic`, `forge:is_magic`, `irons_spellbooks:magic`, and `magic`/`spell` damage types) so ISS spells are properly reduced by 70%.
- **Command Message Fix**: Fixed argument ordering in danger `set` and `add` translation strings.

## [0.2.4-beta] - 2026-11-16

### 日本語
- **クラッシュ修正**: `EntityDebuffCapability` / `MobTraitCapability` の取得時に `getOrNull` による安全な存在確認を行うよう修正し、`Creeper` 等の tick 時に発生していた `NoSuchElementException` を解消。
- **クリーパー対応**: `ATTACK_DAMAGE` 属性を持たないモブの爆発等にもレベル攻撃力倍率を適用し、`PersistentData` フォールバックにより爆発直前に `discard()` されたクリーパーでも攻撃時確率特性が正常に判定されるよう修正。
- **危険度上昇ペースを本家の倍遅い設定へ修正**: 本家 L2Hostility と同じ経験値テーブル式を導入し、`killsPerLevel` を本家デフォルト `30` の倍である `60` に設定（基礎HP倍率も本家準拠の `0.03` に調整）。
- **UI はみ出し修正**: `HostilityInfoScreen` のテキストをパネル幅（`156px`）内に収まるよう調整。
- **加護貫通の説明文修正**: 「エンチャント・エフェクト貫通ダメージ（物理防御は適用）」に修正。

### English
- **Crash Fix**: Added safe `getOrNull` capability accessors for `EntityDebuffCapability` and `MobTraitCapability` to prevent `NoSuchElementException` during entity ticks (e.g., Creepers).
- **Creeper Support**: Applied level-based damage scaling to mobs lacking `Attributes.ATTACK_DAMAGE` (such as Creeper explosions) and added `PersistentData` fallback so discarded Creepers still trigger on-hit traits properly.
- **Danger Progression Pace Fix**: Adopted L2Hostility's kill-credit experience formula with `killsPerLevel = 60` (twice as slow as original `30`) and aligned `healthBonusPerLevel` to `0.03`.
- **UI Text Overflow Fix**: Constrained `HostilityInfoScreen` text strings within the `156px` panel width.
- **Protection Pierce Description Fix**: Updated localization to accurately state it bypasses enchantments and effects while respecting armor.

## [0.2.3-beta] - 2026-11-16

### 日本語
- モブスポーン時のディメンション固定レベル補正（ネザー/エンド加算）を削除し、ディメンション到達による効果は最大レベル上限拡張のみとなるよう統一。

### English
- Removed flat dimension level bonuses (`netherDimensionBonus` / `endDimensionBonus`) on mob spawn; dimension progression only expands the maximum level cap.

## [0.2.2-beta] - 2026-11-16

### 日本語
- **L2Tabs ベースのタブUI統合**: 本家 L2Hostility と同じ `L2Tabs` 機構（`TabRegistry` / `BaseTextScreen`）による「敵対度」タブへ移行し、システム開始ボタン・危険度・レベル帯・最大レベル上限・コスト順の特性一覧を表示。
- **危険度とレベルの相関乱数化＆ストッパー**: 危険度は上限 `2000.0` まで上昇し、モブレベルは危険度との相関＋乱数（`0.85〜1.15倍 ± 揺らぎ`）で算出して上限レベルでキャップするよう変更。
- **ディメンション到達による最大レベル拡張**: エンド到達で最大レベル `+10`、MOD製ディメンション到達で 1種につき `+5` を加算。
- **コマンドとデバッグ用アイテムを追加**:
  - `/rohostility danger set|add`, `/rohostility trait add`, `/rohostility unlock`
  - `rohostility:debug_mob_editor`（ミッシングテクスチャ、GUIでレベルと特性を設定し、敵モブへ右クリックで他MODバフ含めリスポーン再計算）。

### English
- **L2Tabs Integration**: Migrated the Hostility tab to `L2Tabs` (`TabRegistry` / `BaseTextScreen`), featuring the system unlock button, danger display, mob level range, max level cap, and cost-sorted trait list.
- **Correlated Randomized Level Scaling & Danger Stopper**: Capped danger points at `2000.0` while scaling mob levels via danger correlation + randomness (`0.85-1.15x +/- variance`) clamped by the player's effective max level.
- **Dimension Max Level Expansion**: Visiting the End grants `+10` max level, and each unique modded dimension visited grants `+5` max level.
- **Commands & Debug Item**:
  - Added `/rohostility danger set|add`, `/rohostility trait add`, and `/rohostility unlock`.
  - Added `rohostility:debug_mob_editor` (missing texture) with a GUI to configure level/traits and respawn target hostile mobs on right-click.

## [0.2.1-beta] - 2026-11-16

### 日本語
- **起動時クラッシュ修正**: L2Library 側で自動登録される Capability を MOD メインクラス側でも二重登録していたため `IllegalArgumentException: Cannot register a capability implementation multiple times` が発生していた問題を修正。

### English
- **Startup Crash Fix**: Fixed `IllegalArgumentException: Cannot register a capability implementation multiple times` caused by manually registering capabilities that L2Library already registers automatically.

## [0.2.0-beta] - 2026-11-16

### 日本語
- **ベータ版リリース (`0.2.0-beta`)**
- 状態異常「呪詛 (`curse.png`)」および「復活阻止 (`revive_prevention.png`)」の 16×16 アイコンテクスチャを正式登録。
- 全体コードレビューおよびクリーンビルド検証を実施。

### English
- **Beta Release (`0.2.0-beta`)**
- Registered 16x16 status effect icon textures for Curse (`curse.png`) and Revive Prevention (`revive_prevention.png`).
- Completed full code review and clean build verification.

## [0.1.3-alpha] - 2026-11-16

### 日本語
- **加護貫通 (`protection_pierce`)**: `bypasses_armor` タグから除外し、エンチャントや魔法耐性・ポーション耐性は貫通しつつ物理防御（Armor）の影響を受けるよう修正。
- **狂戦士 (`berserker`)**: 毎秒 0.1% 回復がダメージを受けてから 3秒間（60 tick）停止するよう修正。
- **封印 (`seal`)**: 本家ラグナロク方式（専用封印アイテム `rohostility:sealed_item` への一時差し替え＆5 tick 後の自動復元）に強化し、Attribute 以外のエンチャント・防具・Curios 固有効果も完全に封印するよう変更。
- ドキュメント（`README.md` / `LICENSE`）に L2Complements の `curse.png` 流用表記を明記。

### English
- **Protection Pierce**: Removed from `bypasses_armor` tag so it pierces enchantments/effects/resistance while still respecting physical Armor.
- **Berserker**: Stopped passive 0.1%/s regeneration for 3 seconds (60 ticks) after taking damage.
- **Seal**: Upgraded to Ragnarok-style item sealing (`rohostility:sealed_item` temporary replacement with automatic unwrap after 5 ticks) to block all non-attribute gear/Curio effects as well.
- Added attribution for reusing `curse.png` from L2Complements (LGPL-2.1) in `README.md` and `LICENSE`.

## [0.1.2-alpha] - 2026-11-16

### 日本語
- **奪取 (`plunder`)**: 最終値への `0.99` 乗算方式から、最終値を1%ずつ減らす割合減算方式へ変更。
- **新規特性15種を追加**: 狂戦士 (150)、封印 (550)、生命吸収 (120)、装甲貫通 (150)、加護貫通 (150)、段差無視 (50)、万能耐性 (120)、頑丈 (100)、活力 (120)、不死殺し (200)、反撃 (100)、鼓舞 (180)、毒撃 (50)、衰弱 (70)、怨嗟 (90)。
- **新規状態異常・ダメージタイプ**: 復活阻止（中立エフェクト）、呪詛（悪性エフェクト、回復量50%減）、装甲貫通ダメージ、加護貫通ダメージを追加。
- 難易度情報画面 (`HostilityInfoScreen`) の特性一覧をマウスホイールスクロールに対応。

### English
- **Plunder**: Changed from `0.99` multiplicative stacking to subtracting `1%` of the final max HP value per stack.
- **Added 15 new traits**: Berserker (150), Seal (550), Lifesteal (120), Armor Pierce (150), Protection Pierce (150), Step Up (50), Universal Resistance (120), Sturdy (100), Vitality (120), Anti-Revive (200), Counterattack (100), Inspire (180), Poison Strike (50), Weakness Strike (70), and Resentment (90).
- **Added new status effects & damage types**: Revive Prevention (neutral effect), Curse (harmful effect, -50% healing), Armor Pierce damage, and Protection Pierce damage.
- Added mouse wheel scrolling to the trait list in `HostilityInfoScreen`.

## [0.1.1-alpha] - 2026-11-15

### 日本語
- 各特性のデフォルトコストをレベル帯イメージに合わせて調整：
  - 修復力: `50`
  - 復活・学習・破壊・強固・加護: `150`
  - 非現実特攻: `200`
  - 黙示録: `300`
  - 奪取: `400`

### English
- Adjusted default trait costs to match target level tiers:
  - Regeneration: `50`
  - Revive, Adaptation, Destruction, Fortitude, Protection: `150`
  - Unreality: `200`
  - Apocalypse: `300`
  - Plunder: `400`

## [0.1.0-alpha] - 2026-11-15

### 日本語
- 初期実装（Minecraft 1.20.1 / Forge / L2Library 依存のみ）。
- 不可逆アンロックシステム（「敵対の循環に踏み入る」タブボタン、またはネザー到達）を実装。
- 敵討伐による緩やかな危険度上昇、レベル上限 600、Lv.90 以上での特性付与システムを実装。
- 9種類の特性（復活・修復力・学習・黙示録・破壊・非現実特攻・強固・加護・奪取）を実装。
- Curios API 任意連携（黙示録によるアクセサリー弱体化）を実装。
- 各種コンフィグ設定（ステータス倍率補正、特性コスト/重み等）および日英ローカライズを追加。

### English
- Initial implementation for Minecraft 1.20.1 (Forge), depending solely on L2Library.
- Added irreversible unlock system via the inventory Hostility tab button or entering the Nether.
- Added gentler kill-only danger progression, level cap at 600, and trait rolling starting at Lv.90.
- Implemented 9 traits: Revive, Regeneration, Adaptation, Apocalypse, Destruction, Unreality, Fortitude, Protection, and Plunder.
- Added optional Curios API compatibility for Apocalypse slot weakening.
- Added common configuration options (stat scaling modifier, per-trait cost/weight, etc.) and EN/JA localization.
