# Signature API

電子署名画像の変換処理を行うRESTful APIサービス

## 概要

Signature APIは、Base64エンコードされた署名画像データを受け取り、指定されたフォーマットに変換して返すSpring Bootアプリケーションです。透明ピクセルのトリミング、背景色の適用、フォーマット変換（PNG/JPEG）などの機能を提供します。

### 主な機能

- **画像フォーマット変換**: PNG ⇔ JPEG
- **透明ピクセルのトリミング**: 署名画像の余白を自動削除
- **背景色の適用**: JPEG変換時の背景色指定、透明度処理
- **Base64データ処理**: Data URLまたはBase64文字列の受け入れ
- **バリデーション**: ペイロードサイズ制限、フォーマット検証
- **エラーハンドリング**: 詳細なエラーコードとメッセージ

## クイックスタート

### 前提条件

- Java 17以上
- Maven 3.6以上

### ビルドと起動

```bash
# ビルド
mvn clean package

# 起動
mvn spring-boot:run
```

アプリケーションは `http://localhost:8080` で起動します。

### API呼び出し例

```bash
curl -X POST http://localhost:8080/api/signatures \
  -H "Content-Type: application/json" \
  -d '{
    "mime": "image/png",
    "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    "metadata": {
      "width": 400,
      "height": 200
    },
    "options": {
      "outputFormat": "jpeg",
      "backgroundColor": "#FFFFFF",
      "trimTransparent": true
    }
  }'
```

### レスポンス例

```json
{
  "fileId": "sig_20251207134512_a1b2c3d4",
  "contentType": "image/jpeg",
  "sizeBytes": 2048,
  "width": 350,
  "height": 180
}
```

## プロジェクト構成

```
signature/
├── src/
│   ├── main/
│   │   └── java/com/example/signature/
│   │       ├── SignatureApplication.java         # エントリポイント
│   │       ├── controller/
│   │       │   ├── SignatureController.java      # REST APIエンドポイント
│   │       │   └── SignatureExceptionHandler.java # グローバル例外ハンドラ
│   │       ├── service/
│   │       │   └── SignatureConversionService.java # 変換ロジック
│   │       ├── model/
│   │       │   ├── SignatureRequest.java         # リクエストDTO
│   │       │   ├── SignatureResponse.java        # レスポンスDTO
│   │       │   ├── SignatureOptions.java         # 変換オプション
│   │       │   ├── SignatureMetadata.java        # メタデータ
│   │       │   ├── ConversionResult.java         # 変換結果
│   │       │   └── ApiError.java                 # エラーレスポンス
│   │       ├── config/
│   │       │   └── SignatureProperties.java      # アプリケーション設定
│   │       └── exception/
│   │           └── SignatureProcessingException.java # カスタム例外
│   └── test/
│       └── java/com/example/signature/
│           └── service/
│               └── SignatureConversionServiceTest.java
└── pom.xml
```

## 技術スタック

- **Spring Boot 3.2.5**
- **Java 17**
- **Jakarta Validation**: リクエストバリデーション
- **Java AWT/ImageIO**: 画像処理

## 設定

アプリケーションの設定は `application.properties` または `application.yml` で行います。

### 主要な設定項目

| プロパティ | デフォルト値 | 説明 |
|-----------|-------------|------|
| `signature.maxPayloadBytes` | 2,000,000 (2MB) | 受け入れる最大ペイロードサイズ（バイト） |

設定例（`application.properties`）:
```properties
signature.maxPayloadBytes=5000000
server.port=8080
```

## ドキュメント

- [API仕様](API.md) - エンドポイント、リクエスト/レスポンス詳細
- [アーキテクチャ](ARCHITECTURE.md) - システム設計と内部構造
- [開発ガイド](DEVELOPMENT.md) - 開発環境セットアップとメンテナンス

## ライセンス

このプロジェクトは参照実装として提供されています。

## バージョン

- **現在のバージョン**: 0.0.1-SNAPSHOT
