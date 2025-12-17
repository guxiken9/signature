# 開発ガイド

## 開発環境のセットアップ

### 必要な環境

| ツール | バージョン | 説明 |
|--------|-----------|------|
| Java | 8以上 | OpenJDK推奨 |
| Maven | 3.6以上 | ビルドツール |
| Git | 任意 | バージョン管理 |
| IDE | 任意 | IntelliJ IDEA / Eclipse / VS Code推奨 |

### Java環境の確認

```bash
java -version
# 出力例: openjdk version "1.8.0_292"

mvn -version
# 出力例: Apache Maven 3.8.4
```

### プロジェクトのクローンとビルド

```bash
# リポジトリのクローン（該当する場合）
git clone <repository-url>
cd signature

# 依存関係のダウンロードとビルド
mvn clean install

# ビルド成功確認
# [INFO] BUILD SUCCESS が表示されることを確認
```

---

## プロジェクト構成の詳細

```
signature/
├── pom.xml                                           # 親POM
├── signature-core/                                   # 純粋Javaライブラリ（Spring非依存）
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/signature/core/
│       │   ├── service/
│       │   │   └── SignatureConversionService.java   # 画像処理ロジック
│       │   ├── model/
│       │   │   ├── SignatureRequest.java
│       │   │   ├── SignatureOptions.java
│       │   │   ├── ConversionResult.java
│       │   │   └── SignatureMetadata.java
│       │   ├── config/
│       │   │   └── SignatureConfig.java              # 設定クラス
│       │   └── exception/
│       │       └── SignatureProcessingException.java
│       └── test/java/
│           └── com/example/signature/core/service/
│               └── SignatureConversionServiceTest.java
├── signature-spring-boot/                            # Spring Boot統合モジュール
│   ├── pom.xml
│   └── src/
│       └── main/java/com/example/signature/spring/
│           ├── config/
│           │   ├── SignatureProperties.java          # Spring設定
│           │   └── SignatureServiceConfiguration.java
│           ├── controller/
│           │   ├── SignatureController.java          # REST API
│           │   └── SignatureExceptionHandler.java
│           └── model/
│               ├── SignatureRequestDto.java
│               ├── SignatureOptionsDto.java
│               ├── SignatureResponse.java
│               └── ApiError.java
├── signature-app/                                    # 実行可能アプリケーション
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/signature/
│       │   │   └── SignatureApplication.java         # エントリポイント
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── application-dev.properties
│       │       └── application-prod.properties
│       └── test/
├── target/                                           # ビルド出力（自動生成）
├── README.md                                         # プロジェクト概要
├── ARCHITECTURE.md                                   # アーキテクチャドキュメント
├── API.md                                            # API仕様
├── DEVELOPMENT.md                                    # このファイル
└── DOCKER.md                                         # Dockerデプロイメントガイド
```

---

## ビルドとテスト

### ビルドコマンド

```bash
# クリーンビルド
mvn clean compile

# パッケージング（JARファイル作成）
mvn clean package

# テストをスキップしてビルド
mvn clean package -DskipTests

# インストール（ローカルMavenリポジトリに配置）
mvn clean install
```

### テストの実行

```bash
# 全テスト実行
mvn test

# 特定のテストクラスのみ実行
mvn test -Dtest=SignatureConversionServiceTest

# 特定のテストメソッドのみ実行
mvn test -Dtest=SignatureConversionServiceTest#convertPngPayload

# カバレッジレポート生成（JaCoCo追加が必要）
mvn clean test jacoco:report
```

### アプリケーションの起動

```bash
# Spring Boot Maven Pluginで起動
mvn spring-boot:run

# JARファイルから起動
mvn clean package
java -jar target/signature-0.0.1-SNAPSHOT.jar

# プロファイル指定で起動
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# ポート番号を変更して起動
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

起動後、`http://localhost:8080` でアクセス可能になります。

---

## IDE設定

### IntelliJ IDEA

1. **プロジェクトのインポート**
   - File > Open > `pom.xml` を選択
   - "Open as Project" を選択

2. **Java SDKの設定**
   - File > Project Structure > Project
   - SDK: Java 8を選択
   - Language Level: 8を選択

3. **実行設定**
   - Run > Edit Configurations
   - \+ > Spring Boot
   - Main Class: `com.example.signature.SignatureApplication`
   - JRE: Java 8

4. **推奨プラグイン**
   - Lombok（将来的な拡張用）
   - SonarLint（コード品質チェック）

### Eclipse

1. **プロジェクトのインポート**
   - File > Import > Existing Maven Projects
   - プロジェクトディレクトリを選択

2. **Java SDKの設定**
   - Project > Properties > Java Build Path
   - Libraries > Add Library > JRE System Library > Java 8

3. **実行設定**
   - Run > Run Configurations > Spring Boot App
   - Main Type: `com.example.signature.SignatureApplication`

### VS Code

1. **必要な拡張機能**
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (VMware)

2. **プロジェクトを開く**
   - File > Open Folder > プロジェクトディレクトリ

3. **起動**
   - F5キーまたはRun > Start Debugging
   - `SignatureApplication.java` を右クリック > Run Java

---

## 設定ファイル

### application.properties

プロジェクトルートに `src/main/resources/application.properties` を作成します。

```properties
# サーバー設定
server.port=8080

# Signature API設定
signature.maxPayloadBytes=2000000

# ログレベル設定
logging.level.com.example.signature=DEBUG
logging.level.org.springframework.web=INFO

# JSONレスポンスの整形（開発環境のみ）
spring.jackson.serialization.indent-output=true
```

### application-dev.properties（開発環境用）

```properties
# 開発環境固有の設定
server.port=8080
logging.level.com.example.signature=DEBUG
spring.jackson.serialization.indent-output=true
signature.maxPayloadBytes=5000000
```

### application-prod.properties（本番環境用）

```properties
# 本番環境固有の設定
server.port=8080
logging.level.com.example.signature=INFO
spring.jackson.serialization.indent-output=false
signature.maxPayloadBytes=2000000
```

プロファイルの切り替え:
```bash
# 開発環境で起動
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 本番環境で起動
java -jar target/signature-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## デバッグ

### ログ出力の追加

現在のコードにはロギング実装がありませんが、必要に応じて追加できます。

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SignatureConversionService {
    private static final Logger logger = LoggerFactory.getLogger(SignatureConversionService.class);

    public ConversionResult convert(SignatureRequest request) {
        logger.debug("Converting signature with options: {}", request.options());
        // 処理...
        logger.info("Conversion completed: fileId={}", fileId);
        return result;
    }
}
```

### リモートデバッグ

```bash
# リモートデバッグモードで起動
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# IDEでリモートデバッグ接続
# IntelliJ: Run > Edit Configurations > Remote JVM Debug
# Host: localhost, Port: 5005
```

### アプリケーションログの確認

```bash
# 起動ログの確認
mvn spring-boot:run

# ログレベルを変更して起動
mvn spring-boot:run -Dlogging.level.com.example.signature=TRACE
```

---

## テストの追加

### 単体テストの例

新しいテストクラスを追加する場合:

```java
package com.example.signature.service;

import com.example.signature.config.SignatureProperties;
import com.example.signature.exception.SignatureProcessingException;
import com.example.signature.model.SignatureRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignatureConversionServiceNewTest {

    private SignatureConversionService service;

    @BeforeEach
    void setup() {
        SignatureProperties props = new SignatureProperties();
        props.setMaxPayloadBytes(1000000);
        this.service = new SignatureConversionService(props);
    }

    @Test
    void testInvalidBase64() {
        SignatureRequest request = new SignatureRequest(
            "image/png",
            "invalid-base64!!!",
            null,
            null
        );

        assertThatThrownBy(() -> service.convert(request))
            .isInstanceOf(SignatureProcessingException.class)
            .hasMessageContaining("Base64 decode failed");
    }
}
```

### 統合テストの追加

```java
package com.example.signature.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SignatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testConvertEndpoint() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
              "options": {
                "outputFormat": "png"
              }
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").exists())
            .andExpect(jsonPath("$.contentType").value("image/png"));
    }
}
```

---

## コーディング規約

### パッケージ構成ルール

- `controller`: REST APIエンドポイント
- `service`: ビジネスロジック
- `model`: データモデル（イミュータブルなPOJO推奨）
- `config`: 設定クラス
- `exception`: カスタム例外

### 命名規約

| 種類 | 規約 | 例 |
|------|------|-----|
| クラス | PascalCase | `SignatureController` |
| メソッド | camelCase | `convertSignature()` |
| 定数 | UPPER_SNAKE_CASE | `MAX_PAYLOAD_SIZE` |
| 変数 | camelCase | `fileId` |

### コードスタイル

- インデント: スペース4つ
- 行の最大長: 120文字
- イミュータブルなPOJO使用推奨
- final変数の活用
- メソッドは1つの責務のみ

### Javadocの推奨

パブリックAPIには必ずJavadocを追加:

```java
/**
 * 署名画像を指定されたフォーマットに変換します。
 *
 * @param request 変換リクエスト
 * @return 変換結果
 * @throws SignatureProcessingException 変換に失敗した場合
 */
public ConversionResult convert(SignatureRequest request) {
    // 実装...
}
```

---

## トラブルシューティング

### よくある問題と解決策

#### 1. ビルドエラー: "Java version mismatch"

**原因**: Java 8が使用されていない

**解決策**:
```bash
# Javaバージョンを確認
java -version

# Maven用のJAVA_HOMEを設定
export JAVA_HOME=/path/to/java8
mvn clean install
```

#### 2. 起動エラー: "Port 8080 already in use"

**原因**: ポート8080が既に使用中

**解決策**:
```bash
# ポートを変更して起動
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# または使用中のプロセスを終了
lsof -i :8080
kill -9 <PID>
```

#### 3. テストエラー: "OutOfMemoryError"

**原因**: ヒープメモリ不足

**解決策**:
```bash
# メモリを増やしてテスト実行
mvn test -DargLine="-Xmx1024m"
```

#### 4. API呼び出しエラー: "INVALID_PAYLOAD"

**原因**: Base64エンコードが不正

**解決策**:
- Data URL形式を使用: `data:image/png;base64,...`
- Base64文字列に改行が含まれていないか確認
- ペイロードサイズが制限以下か確認

---

## 機能追加ガイド

### 新しい画像フォーマットのサポート追加

1. **SignatureConversionService.java の修正**

[signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java](signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java)の`sanitizeFormat`メソッドにフォーマットを追加:

```java
private String sanitizeFormat(String format) {
    String normalized = (format == null) ? "png" : format.toLowerCase(Locale.US);
    if ("png".equals(normalized)) {
        return "png";
    } else if ("jpg".equals(normalized) || "jpeg".equals(normalized)) {
        return "jpeg";
    } else if ("webp".equals(normalized)) {
        return "webp";  // 追加
    } else {
        throw new SignatureProcessingException("UNSUPPORTED_FORMAT", "Format " + normalized + " is not supported");
    }
}
```

2. **contentTypeFor メソッドの拡張**

[signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java](signature-core/src/main/java/com/example/signature/core/service/SignatureConversionService.java)を修正:

```java
private String contentTypeFor(String format) {
    if ("png".equals(format)) {
        return "image/png";
    } else if ("jpeg".equals(format)) {
        return "image/jpeg";
    } else if ("webp".equals(format)) {
        return "image/webp";
    } else {
        return "application/octet-stream";
    }
}
```

3. **テストの追加**

新しいフォーマット用のテストを追加します。

### ストレージ機能の追加

変換結果をファイルシステムやS3に保存する機能を追加する場合:

1. **新しいサービスクラスを作成**

```java
package com.example.signature.service;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {

    private final Path storageLocation = Paths.get("storage");

    public void store(String fileId, byte[] data) throws IOException {
        Files.createDirectories(storageLocation);
        Path filePath = storageLocation.resolve(fileId + ".bin");
        Files.write(filePath, data);
    }

    public byte[] load(String fileId) throws IOException {
        Path filePath = storageLocation.resolve(fileId + ".bin");
        return Files.readAllBytes(filePath);
    }
}
```

2. **SignatureConversionService に統合**

```java
private final StorageService storageService;

public SignatureConversionService(SignatureProperties properties, StorageService storageService) {
    this.properties = properties;
    this.storageService = storageService;
}

public ConversionResult convert(SignatureRequest request) {
    // 既存の処理...

    // ストレージに保存
    storageService.store(fileId, output);

    return new ConversionResult(fileId, contentType, output, processed.getWidth(), processed.getHeight());
}
```

3. **ダウンロードエンドポイントの追加**

```java
@GetMapping("/{fileId}")
public ResponseEntity<byte[]> download(@PathVariable String fileId) {
    byte[] data = storageService.load(fileId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(data);
}
```

---

## デプロイメント

### JAR形式でのデプロイ

```bash
# ビルド
mvn clean package -DskipTests

# 実行
java -jar target/signature-0.0.1-SNAPSHOT.jar

# バックグラウンド実行
nohup java -jar target/signature-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### Docker化

```dockerfile
# Dockerfile
FROM openjdk:8-jdk-slim
WORKDIR /app
COPY target/signature-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# ビルド
mvn clean package -DskipTests
docker build -t signature-api .

# 実行
docker run -p 8080:8080 signature-api
```

### 環境変数での設定

```bash
# 環境変数で設定を上書き
export SIGNATURE_MAXPAYLOADBYTES=5000000
export SERVER_PORT=8081
java -jar target/signature-0.0.1-SNAPSHOT.jar
```

---

## メンテナンス

### 依存関係の更新

```bash
# 依存関係の確認
mvn dependency:tree

# 古い依存関係の確認
mvn versions:display-dependency-updates

# Spring Bootバージョンの更新
# pom.xmlのspring-boot-starter-parentバージョンを変更
```

### セキュリティ脆弱性のチェック

```bash
# 脆弱性スキャン
mvn dependency:check

# OWASPチェック（プラグイン追加が必要）
mvn org.owasp:dependency-check-maven:check
```

### パフォーマンス監視

Spring Boot Actuatorの追加を推奨:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
```

---

## 関連ドキュメント

- [README](README.md) - プロジェクト概要とクイックスタート
- [ARCHITECTURE](ARCHITECTURE.md) - システムアーキテクチャと設計思想
- [API仕様](API.md) - RESTful API詳細仕様
