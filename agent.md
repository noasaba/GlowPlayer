# GlowPlayer Agent Plan

## 実行方針

このエージェントは GlowPlayer を Java 25 / Paper 26.2 対応の、複数色グローと厳格な権限管理を持つ Minecraft プラグインとして完成させる。作業は小さく進め、各修正後に Gradle で検証する。

## フェーズ

1. 現状確認
   - `build.gradle`、`gradle-wrapper.properties`、`plugin.yml`、メインクラスを読む。
   - Java と Gradle のバージョン不一致、package とファイルパスの不一致、権限の default 値を確認する。

2. ビルド基盤更新
   - Gradle Wrapper を 9.1.0 以上へ更新する。
   - `targetJavaVersion` を 25 にする。
   - Paper Maven repository を追加し、検証済みの Paper API `26.2.build.87-stable` を `compileOnly` で参照する。
   - `GRADLE_USER_HOME=.gradle-cache ./gradlew build` で検証する。

3. コマンド実装
   - `/glow` はオンオフ toggle。
   - `/glow on`、`/glow off`、`/glow list`、`/glow <color>`、`/glow default <color>` を実装する。
   - blue、red、green、yellow、gold、aqua、purple 系など複数色を enum で管理する。
   - デフォルト色は `config.yml` の `default-color` で保存・読み込みする。
   - tab completion は権限に応じて候補を絞る。

4. 権限実装
   - `glowplayer.command` を基本権限にする。
   - toggle、list、default、個別色、全色、admin の権限を分離する。
   - `plugin.yml` にすべての権限ノードを明示する。
   - Java 側でもサブ操作ごとに権限を再確認する。

5. 状態管理
   - プレイヤーごとにオンオフと現在色を保持する。
   - join と respawn で状態を再適用する。
   - quit でグロー用チームから外し、元のチーム所属を復元する。
   - disable 時はグロー用チームから外し、glowing を解除する。

6. 検証
   - Gradle build を通す。
   - コンパイルエラー、plugin.yml エラー、権限分岐エラーを分けて記録する。
   - 同じエラーが 3 回続いたら web で公式情報を確認する。
   - 修正後は必ず同じ検証地点に戻る。

## コマンド

```bash
chmod +x gradlew
GRADLE_USER_HOME=.gradle-cache ./gradlew build
```

Paper サーバー上で確認する場合:

```bash
java -Xms2G -Xmx2G -jar paper-26.2.jar --nogui
```

リモートデバッグする場合:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar paper-26.2.jar --nogui
```

## 手動テスト

- 権限なしのプレイヤーで `/glow` が拒否される。
- `glowplayer.command` のみでは toggle や color が拒否される。
- `glowplayer.toggle` 付与で `/glow`、`/glow on`、`/glow off` が動く。
- `glowplayer.default` 付与で `/glow default`、`/glow default blue` が動き、`config.yml` に保存される。
- `glowplayer.color.blue` 付与で `/glow blue` のみ動く。
- `glowplayer.color.red` 付与で `/glow red` のみ動く。
- `glowplayer.color.*` 付与ですべての色が動く。
- `glowplayer.admin` 付与で全操作が動く。
- リスポーン後も直前の状態と色が維持される。

## 完了条件

- Gradle build 成功。
- Paper 26.2 サーバーでロード成功。
- `/glow` 系コマンドの正常系と権限拒否系が確認済み。
- 既知の不安点があれば README または issue に残す。
