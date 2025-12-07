# API仕様

## ベースURL

```
http://localhost:8080
```

## エンドポイント一覧

| メソッド | パス | 説明 |
|---------|------|------|
| POST | `/api/signatures` | 署名画像の変換 |

---

## POST /api/signatures

署名画像をBase64形式で受け取り、指定されたフォーマットに変換して返します。

### リクエスト

#### ヘッダー

```
Content-Type: application/json
```

#### ボディ

| フィールド | 型 | 必須 | 説明 |
|-----------|-------|------|------|
| `mime` | string | ✓ | 元画像のMIMEタイプ（例: "image/png"） |
| `data` | string | ✓ | Base64エンコードされた画像データ（Data URL形式または純粋なBase64文字列） |
| `metadata` | object | - | 署名のメタデータ（参照用、処理には影響しない） |
| `metadata.width` | integer | - | 元画像の幅 |
| `metadata.height` | integer | - | 元画像の高さ |
| `metadata.dpi` | integer | - | 解像度（DPI） |
| `metadata.strokeCount` | integer | - | ストローク数 |
| `metadata.durationMs` | integer | - | 署名にかかった時間（ミリ秒） |
| `options` | object | - | 変換オプション |
| `options.outputFormat` | string | - | 出力フォーマット。`"png"` または `"jpeg"` （デフォルト: `"png"`） |
| `options.backgroundColor` | string | - | 背景色（16進数カラーコード、例: `"#FFFFFF"`）（デフォルト: `"#FFFFFF"`） |
| `options.trimTransparent` | boolean | - | 透明ピクセルをトリミングするか（デフォルト: `false`） |

#### リクエスト例1: PNG → JPEG変換

```json
{
  "mime": "image/png",
  "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
  "metadata": {
    "width": 400,
    "height": 200,
    "dpi": 96,
    "strokeCount": 15,
    "durationMs": 2340
  },
  "options": {
    "outputFormat": "jpeg",
    "backgroundColor": "#FFFFFF",
    "trimTransparent": true
  }
}
```

#### リクエスト例2: PNG → PNG（トリミングのみ）

```json
{
  "mime": "image/png",
  "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
  "options": {
    "trimTransparent": true
  }
}
```

#### リクエスト例3: 最小構成

```json
{
  "mime": "image/png",
  "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
}
```

### レスポンス

#### 成功（200 OK）

```json
{
  "fileId": "sig_20251207134512_a1b2c3d4",
  "contentType": "image/jpeg",
  "sizeBytes": 2048,
  "width": 350,
  "height": 180
}
```

| フィールド | 型 | 説明 |
|-----------|-------|------|
| `fileId` | string | 生成されたユニークなファイルID（形式: `sig_{timestamp}_{uuid}`） |
| `contentType` | string | 変換後の画像のMIMEタイプ（`"image/png"` または `"image/jpeg"`） |
| `sizeBytes` | integer | 変換後の画像データのバイトサイズ |
| `width` | integer | 変換後の画像の幅（ピクセル） |
| `height` | integer | 変換後の画像の高さ（ピクセル） |

#### エラー

エラーレスポンスは以下の形式で返されます:

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_PAYLOAD",
  "message": "Base64 decode failed"
}
```

| フィールド | 型 | 説明 |
|-----------|-------|------|
| `timestamp` | string | エラー発生時刻（ISO 8601形式） |
| `status` | integer | HTTPステータスコード |
| `code` | string | アプリケーション固有のエラーコード |
| `message` | string | エラーメッセージ |

---

## エラーコード一覧

| HTTPステータス | エラーコード | 説明 | 原因例 |
|---------------|------------|------|--------|
| 400 | `INVALID_PAYLOAD` | 不正なペイロード | Base64デコード失敗、画像フォーマット不正、ペイロードサイズ超過 |
| 400 | `INVALID_OPTIONS` | 不正なオプション | 背景色の形式エラー（例: `"#GGGGGG"`） |
| 415 | `UNSUPPORTED_FORMAT` | サポートされていないフォーマット | `outputFormat` に `"png"`, `"jpeg"` 以外を指定 |
| 500 | `INTERNAL_ERROR` | 内部サーバーエラー | 画像書き込み失敗など |

---

## エラーレスポンス例

### 400 Bad Request - ペイロードサイズ超過

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_PAYLOAD",
  "message": "Payload exceeds max size of 2000000"
}
```

### 400 Bad Request - Base64デコード失敗

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_PAYLOAD",
  "message": "Base64 decode failed"
}
```

### 400 Bad Request - 不正な画像データ

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_PAYLOAD",
  "message": "Unsupported image format"
}
```

### 400 Bad Request - 不正な背景色

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_OPTIONS",
  "message": "Invalid color value: #GGGGGG"
}
```

### 400 Bad Request - 必須フィールド不足

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 400,
  "code": "INVALID_PAYLOAD",
  "message": "must not be blank"
}
```

### 415 Unsupported Media Type - 未サポートフォーマット

```json
{
  "timestamp": "2025-12-07T04:45:12.123Z",
  "status": 415,
  "code": "UNSUPPORTED_FORMAT",
  "message": "Format webp is not supported"
}
```

---

## 詳細仕様

### Base64データ形式

APIは以下の2つの形式をサポートしています:

1. **Data URL形式**（推奨）
   ```
   data:image/png;base64,iVBORw0KGgo...
   ```

2. **純粋なBase64文字列**
   ```
   iVBORw0KGgo...
   ```

どちらの形式でも受け付けますが、Data URL形式を推奨します（ブラウザのCanvas APIとの互換性が高いため）。

### 画像フォーマット

#### 入力フォーマット

Java ImageIOがサポートする全ての画像フォーマット（主にPNG, JPEG, GIF, BMP）を受け付けます。

#### 出力フォーマット

| フォーマット | 値 | MIMEタイプ | 特徴 |
|-------------|-----|-----------|------|
| PNG | `"png"` | `image/png` | 透明度サポート、可逆圧縮 |
| JPEG | `"jpeg"` または `"jpg"` | `image/jpeg` | 透明度非サポート、非可逆圧縮 |

**注意**: JPEGは透明度をサポートしないため、JPEG変換時は必ず背景色が適用されます。

### 背景色の適用

背景色は以下の場合に適用されます:

1. **JPEG変換時**: 必ず適用（JPEGは透明度非サポート）
2. **PNG変換時**: 元画像が透明度を持つ場合のみ適用

背景色の形式:
- 16進数カラーコード（例: `#FFFFFF`, `#FF5733`）
- `#` で始まる6桁の16進数

### 透明ピクセルのトリミング

`trimTransparent: true` を指定すると、画像の透明ピクセル（alpha = 0）を検出し、余白を削除します。

**処理内容**:
1. 全ピクセルをスキャン
2. 透明でないピクセルの最小矩形領域を計算
3. その領域のみを切り出し

**注意事項**:
- 全ピクセルが透明な場合、元画像をそのまま返す
- トリミングは背景色適用の前に実行される

### ファイルID生成ルール

生成されるファイルIDの形式:
```
sig_{yyyyMMddHHmmss}_{uuid8桁}
```

例:
```
sig_20251207134512_a1b2c3d4
```

- タイムスタンプ: 変換処理時刻（14桁）
- UUID: ランダムなUUID（先頭8文字）

### サイズ制限

| 項目 | デフォルト値 | 設定方法 |
|------|-------------|---------|
| 最大ペイロードサイズ | 2,000,000バイト（約2MB） | `signature.maxPayloadBytes` プロパティ |

サイズ超過時は `INVALID_PAYLOAD` エラーが返されます。

---

## curlでの使用例

### 例1: PNG画像をJPEGに変換

```bash
curl -X POST http://localhost:8080/api/signatures \
  -H "Content-Type: application/json" \
  -d '{
    "mime": "image/png",
    "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    "options": {
      "outputFormat": "jpeg",
      "backgroundColor": "#FFFFFF",
      "trimTransparent": true
    }
  }'
```

### 例2: ファイルから画像を読み込んで変換

```bash
# Base64エンコード
base64_data=$(base64 -i signature.png)

# API呼び出し
curl -X POST http://localhost:8080/api/signatures \
  -H "Content-Type: application/json" \
  -d "{
    \"mime\": \"image/png\",
    \"data\": \"$base64_data\",
    \"options\": {
      \"trimTransparent\": true
    }
  }"
```

### 例3: jqでレスポンスを整形

```bash
curl -X POST http://localhost:8080/api/signatures \
  -H "Content-Type: application/json" \
  -d '{
    "mime": "image/png",
    "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    "options": {
      "outputFormat": "png"
    }
  }' | jq
```

---

## JavaScriptでの使用例

### Fetch APIを使用

```javascript
// Canvas要素から署名を取得
const canvas = document.getElementById('signatureCanvas');
const dataUrl = canvas.toDataURL('image/png');

// API呼び出し
const response = await fetch('http://localhost:8080/api/signatures', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    mime: 'image/png',
    data: dataUrl,
    metadata: {
      width: canvas.width,
      height: canvas.height,
      dpi: 96,
    },
    options: {
      outputFormat: 'jpeg',
      backgroundColor: '#FFFFFF',
      trimTransparent: true,
    },
  }),
});

const result = await response.json();
console.log('File ID:', result.fileId);
console.log('Size:', result.sizeBytes, 'bytes');
console.log('Dimensions:', result.width, 'x', result.height);
```

### エラーハンドリング

```javascript
try {
  const response = await fetch('http://localhost:8080/api/signatures', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      mime: 'image/png',
      data: dataUrl,
      options: {
        outputFormat: 'jpeg',
        trimTransparent: true,
      },
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    console.error('Error:', error.code, error.message);
    throw new Error(`API Error: ${error.code}`);
  }

  const result = await response.json();
  console.log('Success:', result);
} catch (error) {
  console.error('Request failed:', error);
}
```

---

## Pythonでの使用例

```python
import requests
import base64

# 画像ファイルを読み込み
with open('signature.png', 'rb') as f:
    image_data = base64.b64encode(f.read()).decode('utf-8')

# リクエストペイロード
payload = {
    'mime': 'image/png',
    'data': f'data:image/png;base64,{image_data}',
    'options': {
        'outputFormat': 'jpeg',
        'backgroundColor': '#FFFFFF',
        'trimTransparent': True
    }
}

# API呼び出し
response = requests.post(
    'http://localhost:8080/api/signatures',
    json=payload
)

if response.status_code == 200:
    result = response.json()
    print(f"File ID: {result['fileId']}")
    print(f"Size: {result['sizeBytes']} bytes")
    print(f"Dimensions: {result['width']} x {result['height']}")
else:
    error = response.json()
    print(f"Error: {error['code']} - {error['message']}")
```

---

## 制限事項

1. **画像データの保存**: 現在のバージョンでは変換結果を保存しません。レスポンスのメタデータのみ返却します。
2. **非同期処理**: 全ての処理は同期的に実行されます。大きな画像の場合、レスポンスに時間がかかる可能性があります。
3. **サポートフォーマット**: PNGとJPEGのみサポート。WebP、SVGなどは未サポート。
4. **サイズ制限**: デフォルトで2MBまで。設定で変更可能。

---

## 関連ドキュメント

- [アーキテクチャドキュメント](ARCHITECTURE.md) - システム設計の詳細
- [開発ガイド](DEVELOPMENT.md) - 開発環境のセットアップ
- [README](README.md) - プロジェクト概要
