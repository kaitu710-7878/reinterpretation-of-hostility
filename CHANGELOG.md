# Changelog / 変更履歴

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
