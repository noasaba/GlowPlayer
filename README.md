[![GitHub release](https://img.shields.io/github/v/release/noasaba/GlowPlayer?include_prereleases)](https://github.com/noasaba/GlowPlayer/releases)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub issues](https://img.shields.io/github/issues/noasaba/GlowPlayer)](https://github.com/noasaba/GlowPlayer/issues)
[![Last Commit](https://img.shields.io/github/last-commit/noasaba/GlowPlayer)](https://github.com/noasaba/GlowPlayer/commits)


# GlowPlayer

## English Description

**GlowPlayer** is a Paper plugin for Java 25 / Paper 26.2 that makes players glow by default and lets permitted players toggle glow or choose a strict permission-controlled glow color.

### Features

- **Default Glowing:**  
  Every player joining the server is automatically set to be glowing.
  
- **Toggle and Color Commands:**  
  Players can use `/glow` to toggle, `/glow on`, `/glow off`, and `/glow blue`, `/glow red`, etc. to choose a color.

- **Persistent Default Color:**  
  Operators can use `/glow default <color>` to save the server default glow color to `config.yml`.

- **Strict Permission Control:**  
  Command access, toggle access, color listing, and each color are controlled by separate permission nodes. Defaults are operator-only.

- **TAB Compatibility:**
  When TAB is detected, GlowPlayer stops managing scoreboard teams and exposes `%glowplayer_glowcolor%` through PlaceholderAPI so TAB remains the sole team manager.

- **Lightweight and Simple:**  
  Designed to be minimal and efficient, ensuring low server overhead.

### Installation

1. Build or download the `GlowPlugin-1.3.jar` file.
2. Place the JAR file into your server's `plugins` folder.
3. Restart your server or reload the plugins.

### TAB Compatibility

TAB and glow colors both use scoreboard teams. GlowPlayer automatically enables compatibility mode when the `TAB` plugin is present and no longer moves players between teams.

1. Install [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi).
2. Open TAB's `plugins/TAB/groups.yml`.
3. Add `%glowplayer_glowcolor%` at the very end of every applicable `tagprefix`.
4. Run `/tab reload` or restart the server.

Example:

```yaml
_DEFAULT_:
  tagprefix: '%vault-prefix%%glowplayer_glowcolor%'
```

The placeholder must be the last color code in `tagprefix`, as required by TAB's glow compatibility mechanism. `%glowplayer_color%` is an alias, and `%glowplayer_enabled%` reports the current on/off state.

### Usage

- All players will be glowing by default when they join the server.
- Use `/glow` to toggle the glowing effect.
- Use `/glow on` and `/glow off` to explicitly set the state.
- Use `/glow list` to show colors the player is allowed to use.
- Use `/glow blue`, `/glow red`, `/glow green`, etc. to change color.
- Use `/glow default` to show the saved server default color.
- Use `/glow default blue` to save a new server default color to `config.yml`.
- The default color commands can also be run from the server console.

### Permissions

- `glowplayer.command`: Allows the base `/glow` command.
- `glowplayer.toggle`: Allows `/glow`, `/glow on`, and `/glow off`.
- `glowplayer.list`: Allows `/glow list`.
- `glowplayer.default`: Allows `/glow default` and `/glow default <color>`.
- `glowplayer.color.<color>`: Allows one specific color, such as `glowplayer.color.blue`.
- `glowplayer.color.*`: Allows all colors.
- `glowplayer.admin`: Allows every GlowPlayer action.

### Plugin Information

- **Plugin Name:** GlowPlayer
- **Version:** 1.3
- **API Version:** 26.2
- **Java Version:** 25
- **Author:** nanosize

### License

This project is licensed under the GNU General Public License (GPL) version 3.  
See [LICENSE](LICENSE) for details.

---

## 日本語説明

**GlowPlayer** は、Java 25 / Paper 26.2 向けに、プレイヤーをデフォルトで光らせ、権限に応じてオンオフや複数色のグローを切り替えられるプラグインです。

### 特徴

- **デフォルトで光る:**  
  サーバーに参加したすべてのプレイヤーが自動的に光る状態になります。
  
- **切り替え・色変更コマンド:**  
  `/glow` でオンオフ、`/glow on`、`/glow off`、`/glow blue`、`/glow red` などで状態や色を切り替えられます。

- **デフォルト色の永続化:**  
  `/glow default <色>` で、サーバーのデフォルトグロー色を `config.yml` に保存できます。

- **厳格なパーミッション対応:**  
  コマンド基本権限、オンオフ権限、一覧表示権限、色ごとの権限を分けています。デフォルトは op のみです。

- **TAB 互換モード:**
  TAB を検出すると GlowPlayer の scoreboard team 操作を停止し、PlaceholderAPI の `%glowplayer_glowcolor%` から色をTABへ渡します。チーム管理はTABだけが行うため、所属競合を防げます。

- **シンプルで軽量:**  
  シンプルで効率的な設計により、サーバーへの負荷を最小限に抑えます。

### インストール方法

1. `GlowPlugin-1.3.jar` ファイルをビルドまたはダウンロードしてください。
2. ダウンロードした JAR ファイルをサーバーの `plugins` フォルダに配置します。
3. サーバーを再起動するか、プラグインをリロードしてください。

### TABとの併用

TABとグロー色は、どちらもscoreboard teamを使用します。`TAB` プラグインが存在する場合、GlowPlayerは互換モードへ自動的に切り替わり、プレイヤーをチーム間で移動しません。

1. [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) を導入します。
2. TABの `plugins/TAB/groups.yml` を開きます。
3. 対象となる各 `tagprefix` の末尾へ `%glowplayer_glowcolor%` を追加します。
4. `/tab reload` を実行するか、サーバーを再起動します。

例:

```yaml
_DEFAULT_:
  tagprefix: '%vault-prefix%%glowplayer_glowcolor%'
```

TABの仕様上、このプレースホルダーは `tagprefix` 内で最後の色コードにしてください。別名として `%glowplayer_color%`、オン・オフ確認用として `%glowplayer_enabled%` も利用できます。

### 使用方法

- サーバー参加時に、すべてのプレイヤーがデフォルトで光る状態になります。
- `/glow` で、光る状態のオン・オフを切り替えられます。
- `/glow on` と `/glow off` で明示的に状態を指定できます。
- `/glow list` で、自分が使える色を確認できます。
- `/glow blue`、`/glow red`、`/glow green` などで色を変更できます。
- `/glow default` で、保存済みのサーバーデフォルト色を確認できます。
- `/glow default blue` で、サーバーデフォルト色を `config.yml` に保存できます。
- デフォルト色コマンドはサーバーコンソールからも実行できます。

### 権限

- `glowplayer.command`: `/glow` の基本実行権限。
- `glowplayer.toggle`: `/glow`、`/glow on`、`/glow off` の権限。
- `glowplayer.list`: `/glow list` の権限。
- `glowplayer.default`: `/glow default` と `/glow default <色>` の権限。
- `glowplayer.color.<color>`: `glowplayer.color.blue` など、個別色の権限。
- `glowplayer.color.*`: すべての色の権限。
- `glowplayer.admin`: GlowPlayer の全操作権限。

### プラグイン情報

- **プラグイン名:** GlowPlayer
- **バージョン:** 1.3
- **API バージョン:** 26.2
- **Java バージョン:** 25
- **作者:** nanosize

### ライセンス

本プロジェクトは GNU General Public License (GPL) バージョン 3 の下でライセンスされています。  
詳細は [LICENSE](LICENSE) ファイルをご覧ください。
