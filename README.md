# Signature API

電子署名画像の変換処理を行うRESTful APIサービス

## 概要

Signature APIは、Base64エンコードされた署名画像データを受け取り、指定されたフォーマットに変換して返すSpring Bootアプリケーションです。透明ピクセルのトリミング、画像リサイズ、背景色の適用、フォーマット変換（PNG/JPEG）などの機能を提供します。

### 主な機能

- **画像フォーマット変換**: PNG ⇔ JPEG
- **画像リサイズ**: 幅・高さを指定してリサイズ（アスペクト比維持）
- **透明ピクセルのトリミング**: 署名画像の余白を自動削除
- **背景色の適用**: JPEG変換時の背景色指定、透明度処理
- **Base64データ処理**: Data URLまたはBase64文字列の受け入れ
- **バリデーション**: ペイロードサイズ制限、フォーマット検証
- **エラーハンドリング**: 詳細なエラーコードとメッセージ
- **モジュラー設計**: 純粋Javaライブラリとして再利用可能

## クイックスタート

### 前提条件

- Java 8以上
- Maven 3.6以上

### ビルドと起動

```bash
# 全モジュールをビルド
mvn clean install

# アプリケーション起動
cd signature-app
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
      "trimTransparent": true,
      "width": 800,
      "height": 600
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

本プロジェクトは、再利用性を高めるため3つのMavenモジュールに分割されています：

### モジュール構成

```
signature/
├── pom.xml                           # 親POM
├── signature-core/                   # 純粋Javaライブラリ（Spring非依存）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/signature/core/
│       │   ├── service/
│       │   │   └── SignatureConversionService.java  # 画像処理ロジック
│       │   ├── model/
│       │   │   ├── SignatureRequest.java
│       │   │   ├── SignatureOptions.java
│       │   │   ├── ConversionResult.java
│       │   │   └── SignatureMetadata.java
│       │   ├── config/
│       │   │   └── SignatureConfig.java             # 設定クラス
│       │   └── exception/
│       │       └── SignatureProcessingException.java
│       └── test/
│
├── signature-spring-boot/            # Spring Boot統合モジュール
│   ├── pom.xml
│   └── src/
│       └── main/java/com/example/signature/spring/
│           ├── config/
│           │   ├── SignatureProperties.java         # Spring設定
│           │   └── SignatureServiceConfiguration.java
│           ├── controller/
│           │   ├── SignatureController.java         # REST API
│           │   └── SignatureExceptionHandler.java
│           └── model/
│               ├── SignatureRequestDto.java
│               ├── SignatureOptionsDto.java
│               ├── SignatureResponse.java
│               └── ApiError.java
│
└── signature-app/                    # 実行可能アプリケーション
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/example/signature/
        │   │   └── SignatureApplication.java        # エントリポイント
        │   └── resources/
        │       ├── application.properties
        │       ├── application-dev.properties
        │       └── application-prod.properties
        └── test/
```

### モジュールの役割

- **signature-core**: 純粋なJavaライブラリとして画像処理ロジックを提供。Spring依存なし。
- **signature-spring-boot**: Spring Bootとの統合レイヤー（REST API、設定管理）。
- **signature-app**: 最終的な実行可能Spring Bootアプリケーション。

## 技術スタック

- **Spring Boot 2.7.18**
- **Java 8**
- **Bean Validation (javax.validation)**: リクエストバリデーション
- **Java AWT/ImageIO**: 画像処理

## 画像リサイズ機能

画像の幅と高さを指定してリサイズできます。アスペクト比は自動的に維持されます。

### リサイズの動作

- **幅のみ指定**: 高さはアスペクト比に基づいて自動計算
- **高さのみ指定**: 幅はアスペクト比に基づいて自動計算
- **両方指定**: 指定されたサイズに収まる最大サイズで、アスペクト比を維持

### 使用例

```json
{
  "options": {
    "width": 800,
    "height": 600
  }
}
```

元画像が1600x900の場合、800x450にリサイズされます（16:9のアスペクト比を維持）。

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

## ビルドオプション

### 特定モジュールのみビルド

```bash
# signature-coreのみテスト
mvn -pl signature-core clean test

# signature-appのみビルド
mvn -pl signature-app clean package

# signature-appとその依存モジュールをビルド
mvn -pl signature-app -am clean package
```

## ドキュメント

- [API仕様](API.md) - エンドポイント、リクエスト/レスポンス詳細
- [アーキテクチャ](ARCHITECTURE.md) - システム設計と内部構造
- [開発ガイド](DEVELOPMENT.md) - 開発環境セットアップとメンテナンス

## ライセンス

このプロジェクトは参照実装として提供されています。

## バージョン

- **現在のバージョン**: 0.0.1-SNAPSHOT
