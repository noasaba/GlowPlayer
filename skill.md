# GlowPlayer Skill

## 目的

GlowPlayer を Java 25 / Paper 26.2 / Gradle で保守し、`/glow` によるオンオフ切り替えと `/glow blue`、`/glow red` などの複数色グローを、厳格な権限管理つきで実装・検証する。

## 前提

- Java は 25 を使用する。
- Gradle Wrapper は Java 25 を実行できる Gradle 9.1.0 以上を使用する。
- Paper API は検証済みの `io.papermc.paper:paper-api:26.2.build.87-stable` に固定する。
- コマンド登録は既存の `plugin.yml` を基本にし、`/glow` のサブ操作は Java 側でも必ず権限確認する。

## 実装ルール

- `/glow` は現在の色を維持してオンオフを切り替える。
- `/glow on` と `/glow off` は明示的にオンオフを切り替える。
- `/glow list` は使用可能な色だけを表示する。
- `/glow <color>` は指定色に変更し、グローをオンにする。
- `/glow default` は現在のサーバーデフォルト色を表示する。
- `/glow default <color>` はサーバーデフォルト色を `config.yml` の `default-color` に保存する。
- 色は scoreboard team の色で表現し、プレイヤーの glowing 状態とチーム所属を同期する。
- チーム名は `gp_` prefix を使い、他プラグインのチームと衝突しにくくする。
- 他チームに所属していたプレイヤーをグロー用チームに移す場合は、可能な範囲で元チーム名を保持し、解除時に戻す。

## 権限モデル

- `glowplayer.command`: `/glow` の基本実行権限。
- `glowplayer.toggle`: `/glow`, `/glow on`, `/glow off` の権限。
- `glowplayer.list`: `/glow list` の権限。
- `glowplayer.default`: `/glow default` と `/glow default <color>` の権限。
- `glowplayer.color.<color>`: 個別色の権限。
- `glowplayer.color.*`: すべての色の権限。
- `glowplayer.admin`: 全権限。
- デフォルトは `op` とし、一般プレイヤーへは権限プラグインで必要最小限だけ付与する。

## デバッグ規約

- まず `GRADLE_USER_HOME=.gradle-cache ./gradlew build` を実行する。
- `./gradlew` に実行権限がない場合は `chmod +x gradlew` を実行する。
- 同じエラーが 3 回出たら、公式ドキュメントまたは一次情報を web で確認する。
- 修正後は同じ場所に戻って再実行し、エラーが消えたことを確認する。
- 100% 自信が持てる状態とは、ビルド成功、該当コマンドの権限分岐確認、サーバーログにロードエラーがないこと、色変更が目視またはログで確認できることを指す。

## 受け入れ条件

- Java 25 で Gradle ビルドが成功する。
- Paper 26.2 API でコンパイルできる。
- `/glow`、`/glow on`、`/glow off`、`/glow list`、`/glow blue`、`/glow red` が意図どおり動く。
- `/glow default blue` で `config.yml` の `default-color` が更新され、再起動後も保持される。
- 権限がない操作は必ず拒否される。
- tab completion には権限のある操作・色だけが出る。
- プレイヤー参加時とリスポーン時に状態が再適用される。
