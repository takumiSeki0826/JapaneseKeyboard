# Groq 音声入力フォーク 設計ドキュメント

## 概要

Sumire（KazumaProject/JapaneseKeyboard, MIT）をフォークし、既存の `VOICE_INPUT` ショートカット（Android `SpeechRecognizer`）を Groq Whisper による文字起こしに差し替える。かな漢字変換・QWERTY ローマ字入力・フローティング表示は Sumire の既存機能をそのまま使う。Groq 関連コードは `groq-voice-keyboard` リポジトリから移植する。

## 要件

- 文字起こし結果は**即確定**（`commitText`）。未確定文字として挿入しない
- 録音操作は**タップで開始 / 再タップで停止**。無音自動停止はしない
- **フローティング表示をデフォルト ON**。固定表示のコードは削除しない
- **lite エディション**ベース（zenz / gemma なし）
- 上流との差分を最小限に保ち、`upstream` からのマージを容易にする

## ビルド構成

- `channel` 次元に新フレーバー `groq` を追加。対象バリアントは `liteGroqDebug` / `liteGroqRelease`（`fullGroq` は `variantFilter` で除外）
- `applicationIdSuffix ".groq"` → `com.kazumaproject.markdownhelperkeyboard.lite.groq`。Play ストア版 Sumire と共存可能。アプリ名も「Sumire Groq」に変更
- `INTERNET` 権限は `app/src/groq/AndroidManifest.xml` にのみ追加（上流のオフライン版は影響を受けない）
- `BuildConfig.GROQ_API_KEY` は `local.properties` から注入。他フレーバーでは空文字
- `BuildConfig.USE_GROQ_VOICE`：`groq` フレーバーのみ `true`
- OkHttp は全フレーバーの `implementation` に追加する（フレーバー別のソースセットやスタブを避けるため）。Groq のコードは `USE_GROQ_VOICE` が false なら呼ばれない

## コンポーネント

新規コードは専用パッケージ `com.kazumaproject.markdownhelperkeyboard.groq_voice` に置く。Android 固有の処理（開始音・トースト・権限と API キーの確認）は `GroqVoiceInput` にまとめ、`IMEService` からはこれだけを呼ぶ。

1. **`GroqAudioRecorder`** — `MediaRecorder` で `cacheDir` に m4a(AAC) 録音。start / stop / cancel、振幅取得。`groq-voice-keyboard` の `AudioRecorder` を移植
2. **`GroqTranscriptionClient`** — OkHttp で `https://api.groq.com/openai/v1/audio/transcriptions` に multipart POST（`whisper-large-v3-turbo`, `language=ja`）。移植
3. **`VoiceRecordingController`** — 状態遷移（Idle → Recording → Transcribing → Idle / Error）を管理。録音・送信・結果コールバックをまとめ、`IMEService` からは `toggle()` と `cancel()` だけを呼ぶ

## IMEService.kt への変更（最小限）

- `ShortcutType.VOICE_INPUT` 分岐: `if (BuildConfig.USE_GROQ_VOICE) groqVoice.toggle() else startVoiceInput(mainView)`
- 成功時: 未確定文字があれば先に確定し、結果を `currentInputConnection.commitText(text, 1)` で入力
- `onFinishInputView` で録音中なら `cancel()`（録音ファイル破棄）
- 認識中は既存の `setSuggestionProgressVisible(SuggestionProgressReason.VoiceInput, …)` を流用

## UX

- マイクは既存ショートカットツールバーの `VOICE_INPUT`。`groq` フレーバーでは初期ショートカットに含め、**右端**に配置
- ツールバーは横スクロールの `RecyclerView` のため、幅の狭いフローティング表示でも右端のマイクが隠れないよう、`groq` フレーバーでは初期ショートカットを7個（設定・絵文字・コピー・貼り付け・キーボード切替・クリップボード・音声入力）に絞る
- アイコン: 待機 🎤 / 録音中 ⏹（赤）/ 認識中はプログレス表示
- 録音開始時に `ToneGenerator` のビープ音（移植）
- `AppPreference` の `keyboard_floating_preference` の初期値を `groq` フレーバーでは `true` にする。設定画面の XML の `defaultValue` も、フレーバー別の `@bool/keyboard_floating_default` を参照させる（設定画面を開いたときに `false` が保存されないようにするため）

### 対象外（YAGNI）

- 音量連動のパルスアニメーション
- 無音自動停止
- 未確定文字として挿入するモード

## エラーハンドリング

- マイク権限なし → 設定 Activity に権限リクエストを追加。IME ではトーストで案内
- API キー未設定 → 録音を開始せずトースト表示
- ネットワーク / API エラー（401・429・タイムアウト等）→ Error 状態にしてアイコンを赤くし、トーストで簡潔に通知。数秒後に Idle へ戻す。詳細は Logcat
- 録音の開始・停止失敗 → Error 状態にし、録音ファイルを削除

## テスト方針

- JVM 単体テスト: `VoiceRecordingController` の状態遷移、`GroqTranscriptionClient`（MockWebServer）、`GroqAudioRecorder` の振幅処理（既存テストを移植）
- 上流の既存テスト: `./gradlew :app:testLiteGroqDebugUnitTest` が通ること
- 実機での手動確認: フローティング表示での QWERTY ローマ字変換、マイクでの文字起こし確定、録音中にキーボードを閉じても落ちないこと

## 実機調整で追加した変更（2026-09-29）

Galaxy Z Fold（Android 17）での確認をもとに、`groq` フレーバーの初期値と挙動を次のように調整した。いずれも `BuildConfig.USE_GROQ_VOICE` で分岐し、上流フレーバーの挙動は変えない。

- **ツールバー**: フローティングには独立ツールバーがないため、ショートカットを候補バーに統合表示する。初期ショートカットは「閉じる（新規 `HIDE_KEYBOARD`）・クリップボード・音声入力・定型文・キーボード調整」。フローティングのヘッダー行は、サイズ編集中（完了ボタンが必要なとき）だけ表示する
- **候補バーの高さ**: 上流の 3/5（単一行 36dp）。フローティングのレイアウトで 58dp に固定されていた高さを、設定値で上書きする
- **キー配列・入力**: タブレット判定でもテンキーを既定にする。フリックのみ入力（同じキーの連打で文字が切り替わらない）を既定にする。§ キーのアイコンを 😂 にする（core の `KeyboardFontGlyphDrawable.resourceOverrides` で差し替え）
- **英字キーボード**: フローティング中の QWERTY は、テンキーと同じ幅・高さを使う（`FloatingQwertySize`）。サイズ編集の対象もテンキー側にそろえる
- **クリップボード**: クリップボード履歴を既定で有効にする。貼り付けたプレビューは候補バーから消す。フローティング時にクリップボードボタンで絵文字タブが開く上流のバグを修正（`resolveSymbolKeyboardInitialMode`）
- **定型文**: フローティング時にも一覧が表示されるよう、表示位置をフローティングのショートカット行にする。未登録なら案内を表示する
- **開始音**: 通知音量 0 でも聞こえるよう `STREAM_MUSIC` で鳴らす
- **配色**: オーナーのランチャー（nothing-fold-launcher）の配色に固定したテーマオーバーレイ `ThemeOverlay.SumireGroq.LauncherPalette` を当てる（背景 `#1A1A1A`、キー `#333333`、特殊キー `#D1432B`、文字 `#F5F5F5`）。シード色からの自動配色は全体が茶色がかるため採用しない

上流の既存テストのうち、上流既定値（50音配列・候補バーの高さ）を前提にしたものは、フレーバーに応じて期待値を切り替えるか、上流フレーバーでのみ実行する。
