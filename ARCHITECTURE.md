# アーキテクチャドキュメント

## 概要

Signature APIは、レイヤードアーキテクチャを採用したSpring Bootアプリケーションです。各レイヤーは明確な責務を持ち、疎結合で保守性の高い設計となっています。

## アーキテクチャ図

```
┌─────────────────────────────────────────────┐
│         クライアント（HTTP Client）           │
└─────────────────┬───────────────────────────┘
                  │ HTTP Request/Response
                  │ (JSON)
┌─────────────────▼───────────────────────────┐
│          Controller Layer                   │
│  ┌─────────────────────────────────────┐   │
│  │   SignatureController               │   │
│  │   - POST /api/signatures            │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │   SignatureExceptionHandler         │   │
│  │   - グローバル例外ハンドリング        │   │
│  └─────────────────────────────────────┘   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Service Layer                      │
│  ┌─────────────────────────────────────┐   │
│  │   SignatureConversionService        │   │
│  │   - 画像変換ロジック                │   │
│  │   - Base64デコード                  │   │
│  │   - トリミング処理                  │   │
│  │   - 背景色適用                      │   │
│  │   - フォーマット変換                │   │
│  └─────────────────────────────────────┘   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Model Layer                        │
│  - SignatureRequest/Response                │
│  - SignatureOptions/Metadata                │
│  - ConversionResult                         │
│  - ApiError                                 │
└─────────────────────────────────────────────┘
```

## レイヤー構成

### 1. Controller Layer

**役割**: HTTPリクエストの受付とレスポンスの返却

#### SignatureController
- **場所**: [signature-spring-boot/src/main/java/com/example/signature/spring/controller/SignatureController.java](signature-spring-boot/src/main/java/com/example/signature/spring/controller/SignatureController.java)
- **責務**:
  - エンドポイント定義 (`POST /api/signatures`)
  - リクエストバリデーション (`@Valid`)
  - サービス層へのデリゲート
  - レスポンスDTOへの変換

#### SignatureExceptionHandler
- **場所**: [signature-spring-boot/src/main/java/com/example/signature/spring/controller/SignatureExceptionHandler.java](signature-spring-boot/src/main/java/com/example/signature/spring/controller/SignatureExceptionHandler.java)
- **責務**:
  - グローバル例外ハンドリング (`@RestControllerAdvice`)
  - エラーコードからHTTPステータスへのマッピング
  - 統一されたエラーレスポンス生成

### 2. Service Layer

**役割**: ビジネスロジックの実装

#### SignatureConversionService
- **場所**: [signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java](signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java)
- **責務**:
  - 画像変換処理の orchestration
  - Base64デコード（Data URL対応）
  - 画像読み込み・書き込み
  - 透明ピクセルトリミング
  - 背景色適用
  - フォーマット変換
  - 画像リサイズ（アスペクト比維持）
  - ファイルID生成

**主要メソッド**:

| メソッド | 説明 |
|---------|------|
| `convert(SignatureRequest)` | 変換処理のエントリポイント |
| `decodePayload(String)` | Base64デコードとサイズ検証 |
| `readImage(byte[])` | バイト配列から画像を読み込み |
| `trimTransparentPixels(BufferedImage)` | 透明ピクセルのトリミング |
| `resizeImage(BufferedImage, SignatureOptions)` | 画像のリサイズ（アスペクト比維持） |
| `calculateDimensions(int, int, Integer, Integer)` | リサイズ後の寸法を計算 |
| `applyBackground(BufferedImage, String, String)` | 背景色の適用 |
| `writeImage(BufferedImage, String)` | 画像をバイト配列に変換 |
| `generateFileId()` | ユニークなファイルIDを生成 |

### 3. Model Layer

**役割**: データ構造の定義

すべてのモデルクラスはJava Recordを使用してイミュータブルに実装されています。

#### SignatureRequest
- **場所**: [signature-core/src/main/java/com/example/signature/core/model/SignatureRequest.java](signature-core/src/main/java/com/example/signature/core/model/SignatureRequest.java)
- **役割**: API入力データ
- **フィールド**:
  - `mime`: MIMEタイプ（必須）
  - `data`: Base64エンコードされた画像データ（必須）
  - `metadata`: メタデータ（オプショナル）
  - `options`: 変換オプション（オプショナル）

#### SignatureResponse
- **場所**: [signature-spring-boot/src/main/java/com/example/signature/spring/model/SignatureResponse.java](signature-spring-boot/src/main/java/com/example/signature/spring/model/SignatureResponse.java)
- **役割**: API出力データ
- **フィールド**:
  - `fileId`: 生成されたファイルID
  - `contentType`: 変換後のMIMEタイプ
  - `sizeBytes`: 変換後のファイルサイズ
  - `width`: 変換後の画像幅
  - `height`: 変換後の画像高さ

#### SignatureOptions
- **場所**: [signature-core/src/main/java/com/example/signature/core/model/SignatureOptions.java](signature-core/src/main/java/com/example/signature/core/model/SignatureOptions.java)
- **役割**: 変換オプションとデフォルト値の解決
- **フィールド**:
  - `outputFormat`: 出力フォーマット（デフォルト: "png"）
  - `backgroundColor`: 背景色（デフォルト: "#FFFFFF"）
  - `trimTransparent`: トリミング有効化（デフォルト: false）
  - `width`: リサイズ後の幅（オプショナル）
  - `height`: リサイズ後の高さ（オプショナル）

#### ConversionResult
- **場所**: [signature-core/src/main/java/com/example/signature/core/model/ConversionResult.java](signature-core/src/main/java/com/example/signature/core/model/ConversionResult.java)
- **役割**: サービス層の内部結果
- **フィールド**:
  - `fileId`: ファイルID
  - `contentType`: MIMEタイプ
  - `data`: 変換後のバイナリデータ
  - `width`: 画像幅
  - `height`: 画像高さ

### 4. Configuration Layer

#### SignatureProperties
- **場所**: [signature-spring-boot/src/main/java/com/example/signature/spring/config/SignatureProperties.java](signature-spring-boot/src/main/java/com/example/signature/spring/config/SignatureProperties.java)
- **役割**: アプリケーション設定の外部化
- **設定項目**:
  - `maxPayloadBytes`: 最大ペイロードサイズ（デフォルト: 2MB）

### 5. Exception Layer

#### SignatureProcessingException
- **場所**: [signature-core/src/main/java/com/example/signature/core/exception/SignatureProcessingException.java](signature-core/src/main/java/com/example/signature/core/exception/SignatureProcessingException.java)
- **役割**: ドメイン固有の例外
- **エラーコード**:
  - `INVALID_PAYLOAD`: 不正なペイロード（Base64エラー、サイズ超過など）
  - `UNSUPPORTED_FORMAT`: サポートされていないフォーマット
  - `INVALID_OPTIONS`: 不正なオプション（色指定エラーなど）
  - `INTERNAL_ERROR`: 内部エラー

## データフロー

### 1. 正常系フロー

```
1. クライアント → SignatureController
   POST /api/signatures
   {
     "mime": "image/png",
     "data": "data:image/png;base64,...",
     "options": { "outputFormat": "jpeg", ... }
   }

2. SignatureController → バリデーション
   @Valid による入力検証

3. SignatureController → SignatureConversionService
   convert(request) 呼び出し

4. SignatureConversionService 内部処理:
   a. decodePayload() - Base64デコード
   b. readImage() - BufferedImageに変換
   c. trimTransparentPixels() - トリミング（オプション）
   d. resizeImage() - 画像リサイズ（オプション）
   e. applyBackground() - 背景色適用
   f. writeImage() - フォーマット変換
   g. generateFileId() - ID生成

5. SignatureConversionService → SignatureController
   ConversionResult 返却

6. SignatureController → クライアント
   SignatureResponse を JSON で返却
   {
     "fileId": "sig_20251207134512_a1b2c3d4",
     "contentType": "image/jpeg",
     "sizeBytes": 2048,
     "width": 350,
     "height": 180
   }
```

### 2. エラーフロー

```
1. 例外発生（例: SignatureProcessingException）

2. SignatureExceptionHandler が捕捉

3. エラーコードに応じてHTTPステータスを決定:
   - UNSUPPORTED_FORMAT → 415 Unsupported Media Type
   - INVALID_PAYLOAD / INVALID_OPTIONS → 400 Bad Request
   - その他 → 500 Internal Server Error

4. ApiError を生成して返却
   {
     "timestamp": "2025-12-07T04:45:12.123Z",
     "status": 400,
     "code": "INVALID_PAYLOAD",
     "message": "Base64 decode failed"
   }
```

## 設計原則

### 1. 単一責任の原則（SRP）
- 各クラスは1つの責務のみを持つ
- Controller: リクエスト処理のみ
- Service: ビジネスロジックのみ
- Model: データ構造のみ

### 2. 依存性注入（DI）
- Spring Frameworkのコンストラクタインジェクション使用
- テスタビリティの向上
- 疎結合な設計

### 3. イミュータビリティ
- Record使用によるイミュータブルなモデル
- スレッドセーフな実装
- 副作用のない処理

### 4. エラーハンドリング
- ドメイン固有例外の定義
- グローバル例外ハンドラによる統一されたエラー処理
- エラーコードによる分類

### 5. 設定の外部化
- `@ConfigurationProperties` による設定管理
- 環境ごとの設定切り替えが容易

## 技術的な決定事項

### Java AWT/ImageIO の使用

**理由**:
- 標準ライブラリで軽量
- 外部依存なし
- PNG/JPEG変換に十分な機能

**トレードオフ**:
- 高度な画像処理には不向き
- パフォーマンスは中程度

### Record の使用

**理由**:
- ボイラープレートコード削減
- イミュータビリティの強制
- equals/hashCode/toString の自動生成

**要件**:
- Java 17以上

### Base64 Data URL 対応

**理由**:
- ブラウザJavaScript (Canvas.toDataURL) との互換性
- クライアント実装の簡素化

**実装**:
- 正規表現による柔軟なパース
- Data URLまたは純粋なBase64の両方に対応

## パフォーマンス考慮事項

### メモリ使用
- 画像データはメモリ上で処理
- 最大ペイロードサイズ制限でメモリ枯渇を防止
- 現在の制限: 2MB（設定変更可能）

### スレッドセーフティ
- Serviceクラスはステートレスでスレッドセーフ
- イミュータブルなモデルクラス
- Spring Beanのシングルトンスコープでも安全

### スケーラビリティ
- ステートレス設計により水平スケール可能
- 外部ストレージ依存なし（将来的に追加可能）

## セキュリティ考慮事項

### 入力検証
- Jakarta Validation による型安全なバリデーション
- ペイロードサイズ制限（DoS対策）
- Base64デコード失敗時の適切なエラーハンドリング

### エラー情報の露出
- 内部エラーの詳細を隠蔽
- ユーザーフレンドリーなエラーメッセージ
- ログには詳細情報を記録（実装推奨）

## 拡張性

### 将来的な拡張ポイント

1. **ストレージ統合**
   - 変換結果をS3/Azure Blobなどに保存
   - ファイルIDでダウンロード可能に

2. **追加フォーマット対応**
   - WebP, SVG, BMPなど
   - `sanitizeFormat()` の拡張

3. **画像処理機能追加**
   - リサイズ、回転
   - フィルター、エフェクト
   - Serviceメソッドの追加

4. **非同期処理**
   - 大容量画像の非同期変換
   - Spring WebFlux/Reactive対応

5. **認証・認可**
   - Spring Securityの統合
   - APIキー、OAuth2対応

## テスト戦略

現在のテストカバレッジ:
- `SignatureConversionServiceTest`: サービス層の単体テスト

推奨される追加テスト:
- Controller統合テスト（MockMvc）
- 異常系テスト
- エッジケーステスト（巨大画像、空画像など）
