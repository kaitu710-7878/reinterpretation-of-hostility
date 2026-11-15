# Changelog / 変更履歴

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
