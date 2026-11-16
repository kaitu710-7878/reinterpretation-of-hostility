# Configuration Guide / コンフィグ設定ガイド

設定ファイルは `config/rohostility-common.toml` に生成されます。  
The configuration file is generated at `config/rohostility-common.toml`.

---

## `[general]` - 基本・進行設定 / General & Progression

| パラメータ / Key | 型 / Type | デフォルト / Default | 説明 / Description |
| --- | --- | --- | --- |
| `maxLevel` | Integer (`1..10000`) | `600` | 基本モブレベル上限（エンド・他MODディメンション到達で増加） / Base maximum mob level cap |
| `maxDangerCap` | Double (`1.0..100000.0`) | `2000.0` | 蓄積危険度のストッパー上限 / Hard cap for accumulated danger points |
| `traitMinLevel` | Integer (`1..10000`) | `90` | 特性が出現し始める最低レベル / Minimum level for traits to appear |
| `maxTraitCount` | Integer (`1..20`) | `8` | 1体のモブに付与される最大特性数 / Maximum traits per mob |
| `killsPerLevel` | Integer (`1..100000`) | `60` | 同レベルモブ何体討伐で危険度が1上がるか（本家30の倍緩やか） / Kills of same-level mob per danger level (original=30, 60=half pace) |
| `levelRandomVariance` | Integer (`0..100`) | `5` | スポーン時のレベルランダム振れ幅（±） / Random level variance (+/-) |

## `[scaling]` - ステータス補正 / Stat Scaling

| パラメータ / Key | 型 / Type | デフォルト / Default | 説明 / Description |
| --- | --- | --- | --- |
| `healthBonusPerLevel` | Double (`0.0..10.0`) | `0.03` | レベル1あたりの最大HP増加率（0.03 = +3%、本家準拠） / Max HP bonus per level |
| `damageBonusPerLevel` | Double (`0.0..10.0`) | `0.02` | レベル1あたりの攻撃力増加率（0.02 = +2%） / Attack damage bonus per level |
| `statScalingModifier` | Double (`0.0..100.0`) | `1.0` | 上記ステータス上昇量への全体倍率補正（1.0未満で弱体化、1.0超で強化） / Overall multiplier for stat scaling (<1.0 weakens, >1.0 strengthens) |

## `[display]` - 表示設定 / Display

| パラメータ / Key | 型 / Type | デフォルト / Default | 説明 / Description |
| --- | --- | --- | --- |
| `showMobOverheadInfo` | Boolean | `true` | 視線を向けたモブの頭上にレベルと特性を表示するか / Show level and traits overhead when looking at a mob |

## `[traits.<id>]` - 特性別設定 / Per-Trait Settings

各特性（`revive`, `regeneration`, `adaptation`, `apocalypse`, `destruction`, `unreality`, `fortitude`, `protection`, `plunder`, `berserker`, `seal`, `lifesteal`, `armor_pierce`, `protection_pierce`, `step_up`, `universal_resistance`, `sturdy`, `vitality`, `anti_revive`, `counterattack`, `inspire`, `poison_strike`, `weakness_strike`, `resentment`）ごとに以下を設定できます。  
Each trait has the following entries:

| パラメータ / Key | 型 / Type | 説明 / Description |
| --- | --- | --- |
| `enabled` | Boolean | 特性の有効/無効 / Enable or disable this trait |
| `cost` | Integer (`1..1000`) | モブのレベル予算から消費するコスト / Budget cost to roll this trait |
| `weight` | Integer (`1..1000`) | 抽選時の重み（出現しやすさ） / Selection weight when rolling |
