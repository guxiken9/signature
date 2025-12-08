# Docker デプロイメントガイド

## 概要

Signature APIをDockerコンテナで実行するための完全なガイドです。

## 📋 前提条件

- Docker 20.10以上
- Docker Compose 2.0以上（オプション）
- 最小システム要件: 512MB RAM, 1 CPU

## 🚀 クイックスタート

### 1. Dockerイメージのビルド

```bash
docker build -t signature-api:latest .
```

### 2. コンテナの起動

```bash
docker run -d \
  --name signature-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  signature-api:latest
```

### 3. アクセス確認

```bash
# アプリケーション
curl http://localhost:8080

# ヘルスチェック（Spring Boot Actuator追加時）
curl http://localhost:8080/actuator/health
```

---

## 🐳 Docker Compose を使用した起動

### 基本起動

```bash
# ビルドして起動
docker-compose up -d --build

# ログ確認
docker-compose logs -f signature-api

# 停止
docker-compose down
```

### Nginxリバースプロキシ付き起動

```bash
# Nginxを含めて起動
docker-compose --profile with-nginx up -d --build

# 確認
curl http://localhost
```

---

## ⚙️ 環境変数

コンテナ起動時に以下の環境変数で設定をカスタマイズできます。

| 環境変数 | デフォルト値 | 説明 |
|---------|-------------|------|
| `SPRING_PROFILES_ACTIVE` | `default` | Spring プロファイル (`dev`, `prod`) |
| `SIGNATURE_MAXPAYLOADBYTES` | `2000000` | 最大ペイロードサイズ（バイト） |
| `SERVER_PORT` | `8080` | サーバーポート |
| `LOGGING_LEVEL_ROOT` | `INFO` | ルートログレベル |
| `LOGGING_LEVEL_COM_EXAMPLE_SIGNATURE` | `INFO` | アプリケーションログレベル |
| `SERVER_COMPRESSION_ENABLED` | `true` | レスポンス圧縮の有効化 |

### 環境変数の設定例

```bash
docker run -d \
  --name signature-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SIGNATURE_MAXPAYLOADBYTES=5000000 \
  -e LOGGING_LEVEL_COM_EXAMPLE_SIGNATURE=DEBUG \
  signature-api:latest
```

---

## 📊 リソース制限

### Docker run

```bash
docker run -d \
  --name signature-api \
  -p 8080:8080 \
  --memory="512m" \
  --cpus="1.0" \
  signature-api:latest
```

### Docker Compose

`docker-compose.yml` に記載済み:

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 512M
    reservations:
      cpus: '0.5'
      memory: 256M
```

---

## 🏥 ヘルスチェック

### Dockerfile内のヘルスチェック

自動的に30秒ごとにヘルスチェックを実行します。

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### 手動ヘルスチェック

```bash
# コンテナ内から
docker exec signature-api curl -f http://localhost:8080/actuator/health

# ホストから
curl http://localhost:8080/actuator/health
```

**注意**: ヘルスチェックエンドポイントを有効にするには、`pom.xml` に Spring Boot Actuator を追加する必要があります。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## 🔒 セキュリティのベストプラクティス

### 1. 非rootユーザーで実行

Dockerfileでは `appuser` という非rootユーザーで実行されます。

### 2. JVMメモリ設定

コンテナ環境に最適化されたJVM設定:

```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
```

### 3. マルチステージビルド

最終イメージサイズを最小化:
- ビルドステージ: Maven + JDK
- ランタイムステージ: JRE のみ

---

## 🔧 トラブルシューティング

### コンテナが起動しない

```bash
# ログ確認
docker logs signature-api

# 詳細なログ
docker logs signature-api --tail 100 -f
```

### ポートが使用中

```bash
# ポート8080を使用しているプロセスを確認
lsof -i :8080

# 別のポートで起動
docker run -d -p 8081:8080 signature-api:latest
```

### メモリ不足エラー

```bash
# メモリ制限を増やす
docker run -d --memory="1g" signature-api:latest

# またはJVMメモリ設定を調整
docker run -d \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=60.0" \
  signature-api:latest
```

### イメージのリビルド

```bash
# キャッシュなしでビルド
docker build --no-cache -t signature-api:latest .

# Docker Composeでリビルド
docker-compose build --no-cache
docker-compose up -d
```

---

## 📦 本番環境へのデプロイ

### 1. イメージのタグ付けとプッシュ

```bash
# タグ付け
docker tag signature-api:latest your-registry.com/signature-api:1.0.0

# レジストリにプッシュ
docker push your-registry.com/signature-api:1.0.0
```

### 2. 本番用Docker Compose

```yaml
version: '3.8'
services:
  signature-api:
    image: your-registry.com/signature-api:1.0.0
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SIGNATURE_MAXPAYLOADBYTES=2000000
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
```

### 3. Nginx with SSL

```bash
# SSL証明書を配置
mkdir -p ssl
cp your-cert.pem ssl/cert.pem
cp your-key.pem ssl/key.pem

# nginx.confのSSL設定をアンコメント
# docker-compose.ymlのvolumesをアンコメント

# 起動
docker-compose --profile with-nginx up -d
```

---

## 🔄 アップデート手順

### 1. 新バージョンのビルド

```bash
# 新しいイメージをビルド
docker build -t signature-api:2.0.0 .
docker tag signature-api:2.0.0 signature-api:latest
```

### 2. ローリングアップデート

```bash
# 新コンテナを起動（別ポート）
docker run -d --name signature-api-new -p 8081:8080 signature-api:2.0.0

# 正常動作を確認
curl http://localhost:8081/actuator/health

# 旧コンテナを停止して新コンテナにポート変更
docker stop signature-api
docker rm signature-api
docker run -d --name signature-api -p 8080:8080 signature-api:2.0.0
```

### 3. Docker Composeでのアップデート

```bash
# 新イメージでビルド
docker-compose build

# ダウンタイムなしで再起動
docker-compose up -d --no-deps --build signature-api
```

---

## 📈 モニタリング

### ログの確認

```bash
# リアルタイムログ
docker-compose logs -f signature-api

# 最新100行
docker logs signature-api --tail 100

# タイムスタンプ付き
docker logs signature-api --timestamps
```

### リソース使用状況

```bash
# CPU/メモリ使用率
docker stats signature-api

# すべてのコンテナ
docker stats
```

### コンテナ詳細情報

```bash
# コンテナの詳細
docker inspect signature-api

# ポートマッピング確認
docker port signature-api
```

---

## 🧹 クリーンアップ

### コンテナの削除

```bash
# 停止して削除
docker-compose down

# ボリュームも削除
docker-compose down -v
```

### イメージの削除

```bash
# 特定のイメージを削除
docker rmi signature-api:latest

# 未使用イメージをすべて削除
docker image prune -a
```

### システムクリーンアップ

```bash
# 未使用のすべてのリソースを削除
docker system prune -a --volumes
```

---

## 🎯 パフォーマンスチューニング

### JVMチューニング

```bash
docker run -d \
  -e JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication" \
  signature-api:latest
```

### Spring Boot設定の最適化

```bash
docker run -d \
  -e SERVER_TOMCAT_THREADS_MAX=200 \
  -e SERVER_TOMCAT_ACCEPT_COUNT=100 \
  signature-api:latest
```

---

## 📚 関連ドキュメント

- [README.md](README.md) - プロジェクト概要
- [DEVELOPMENT.md](DEVELOPMENT.md) - 開発ガイド
- [API.md](API.md) - API仕様

## 🆘 サポート

問題が発生した場合:
1. [GitHub Issues](https://github.com/your-repo/signature/issues) で報告
2. ログファイルを添付
3. `docker version` と `docker-compose version` の出力を含める
