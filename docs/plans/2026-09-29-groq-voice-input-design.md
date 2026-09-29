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

## コンポーネント

新規コードは専用パッケージ `com.kazumaproject.markdownhelperkeyboard.groq_voice` に置く。

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
- アイコン: 待機 🎤 / 録音中 ⏹（赤）/ 認識中はプログレス表示
- 録音開始時に `ToneGenerator` のビープ音（移植）
- `AppPreference` の `keyboard_floating_preference` の初期値を `groq` フレーバーでは `true` にする

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
