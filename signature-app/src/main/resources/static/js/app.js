// DOM要素の取得
const statusEl = document.getElementById('status');
const captureStatusEl = document.getElementById('captureStatus');
const strokeCountEl = document.getElementById('strokeCount');
const pointCountEl = document.getElementById('pointCount');
const dataSizeEl = document.getElementById('dataSize');
const jsonDisplayEl = document.getElementById('jsonDisplay');
const previewAreaEl = document.getElementById('previewArea');

const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const clearBtn = document.getElementById('clearBtn');
const undoBtn = document.getElementById('undoBtn');
const colorPicker = document.getElementById('colorPicker');
const thicknessInput = document.getElementById('thickness');
const thicknessValueEl = document.getElementById('thicknessValue');
const canvasWidthInput = document.getElementById('canvasWidth');
const canvasHeightInput = document.getElementById('canvasHeight');
const resizeBtn = document.getElementById('resizeBtn');
const sendBtn = document.getElementById('sendBtn');
const copyJsonBtn = document.getElementById('copyJsonBtn');

// Signatureインスタンスの初期化
const sign = new window.signature('canvas', 'canvasBg');
sign.startCapture();
updateCaptureStatus(true);

// 情報表示の更新
function updateInfo() {
  const data = sign.getStrokeData();
  const strokeCount = data.strokes.length;
  const pointCount = data.strokes.reduce((sum, stroke) => sum + stroke.points.length, 0);
  const jsonStr = JSON.stringify(data, null, 2);
  const dataSize = new Blob([jsonStr]).size;

  strokeCountEl.textContent = strokeCount;
  pointCountEl.textContent = pointCount;
  dataSizeEl.textContent = `${(dataSize / 1024).toFixed(2)} KB`;
  jsonDisplayEl.textContent = jsonStr;

  // プレビューの更新
  updatePreview();
}

// プレビュー画像の更新
function updatePreview() {
  const canvas = document.getElementById('canvas');
  const bgCanvas = document.getElementById('canvasBg');

  // 合成用の一時キャンバスを作成
  const tempCanvas = document.createElement('canvas');
  tempCanvas.width = canvas.width;
  tempCanvas.height = canvas.height;
  const tempCtx = tempCanvas.getContext('2d');

  // 背景を描画
  tempCtx.drawImage(bgCanvas, 0, 0);
  // 署名を描画
  tempCtx.drawImage(canvas, 0, 0);

  const dataUrl = tempCanvas.toDataURL('image/png');

  if (sign.getStrokeCount() > 0) {
    previewAreaEl.innerHTML = `<img src="${dataUrl}" alt="署名プレビュー" style="max-width: 100%; border: 1px solid #d1d5db;">`;
  } else {
    previewAreaEl.innerHTML = '<p class="placeholder">署名を入力すると、ここにプレビューが表示されます</p>';
  }
}

// キャプチャ状態の更新
function updateCaptureStatus(isCapturing) {
  captureStatusEl.textContent = isCapturing ? '開始中' : '停止中';
  captureStatusEl.style.color = isCapturing ? '#10b981' : '#6b7280';
  startBtn.disabled = isCapturing;
  stopBtn.disabled = !isCapturing;
}

// イベントリスナーの設定
startBtn.addEventListener('click', () => {
  const err = sign.startCapture();
  if (err && err.code === 'UNSUPPORTED_ENV') {
    statusEl.textContent = `エラー: ${err.message}`;
    statusEl.style.color = '#ef4444';
  } else {
    updateCaptureStatus(true);
  }
});

stopBtn.addEventListener('click', () => {
  sign.stopCapture();
  updateCaptureStatus(false);
});

clearBtn.addEventListener('click', () => {
  sign.clearCanvas('user');
  updateInfo();
});

undoBtn.addEventListener('click', () => {
  sign.undo();
  updateInfo();
});

colorPicker.addEventListener('change', (e) => {
  sign.setStrokeColor(e.target.value);
});

thicknessInput.addEventListener('input', (e) => {
  const value = parseInt(e.target.value, 10) || 2;
  thicknessValueEl.textContent = value;
  sign.setStrokeThickness(value);
});

resizeBtn.addEventListener('click', () => {
  const width = parseInt(canvasWidthInput.value, 10) || 600;
  const height = parseInt(canvasHeightInput.value, 10) || 300;
  sign.resize(width, height);
  updateInfo();
  statusEl.textContent = `キャンバスサイズを ${width}x${height} に変更しました`;
  statusEl.style.color = '#10b981';
});

copyJsonBtn.addEventListener('click', () => {
  const jsonText = jsonDisplayEl.textContent;
  navigator.clipboard.writeText(jsonText).then(() => {
    statusEl.textContent = 'JSONをクリップボードにコピーしました';
    statusEl.style.color = '#10b981';
  }).catch((err) => {
    statusEl.textContent = `コピーエラー: ${err.message}`;
    statusEl.style.color = '#ef4444';
  });
});

sendBtn.addEventListener('click', async () => {
  if (sign.getStrokeCount() === 0) {
    statusEl.textContent = 'エラー: 署名が入力されていません';
    statusEl.style.color = '#ef4444';
    return;
  }

  const metadata = sign.getStrokeData();
  const canvas = document.getElementById('canvas');
  const bgCanvas = document.getElementById('canvasBg');

  // 合成用の一時キャンバスを作成
  const tempCanvas = document.createElement('canvas');
  tempCanvas.width = canvas.width;
  tempCanvas.height = canvas.height;
  const tempCtx = tempCanvas.getContext('2d');

  if (!tempCtx) {
    statusEl.textContent = 'エラー: Canvasコンテキストの取得に失敗しました';
    statusEl.style.color = '#ef4444';
    return;
  }

  // 背景を描画
  tempCtx.drawImage(bgCanvas, 0, 0);
  // 署名を描画
  tempCtx.drawImage(canvas, 0, 0);

  const dataUrl = tempCanvas.toDataURL('image/png');

  const body = {
    mime: 'image/png',
    data: dataUrl,
    metadata,
    options: {
      outputFormat: 'png',
      backgroundColor: '#FFFFFF',
      trimTransparent: true
    }
  };

  const maxRetries = 3;
  let attempt = 0;

  while (attempt < maxRetries) {
    try {
      statusEl.textContent = attempt === 0 ? '送信中...' : `再送信中 (${attempt + 1}/${maxRetries})...`;
      statusEl.style.color = '#3b82f6';
      sendBtn.disabled = true;

      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 30000);

      const response = await fetch('/api/signatures', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      statusEl.textContent = `送信成功 - fileId: ${data.fileId}, サイズ: ${data.sizeBytes} bytes (${data.width}x${data.height})`;
      statusEl.style.color = '#10b981';
      sendBtn.disabled = false;
      return;
    } catch (err) {
      attempt++;
      if (attempt >= maxRetries || err.name === 'AbortError') {
        const errorMsg = err.name === 'AbortError' ? 'タイムアウト: サーバーからの応答がありません' : err.message;
        statusEl.textContent = `送信エラー: ${errorMsg}`;
        statusEl.style.color = '#ef4444';
        sendBtn.disabled = false;
        return;
      }
      await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
    }
  }
});

// Signatureイベントのリスナー
sign.on('onStrokeStart', (strokeId) => {
  console.log('Stroke started:', strokeId);
});

sign.on('onStrokeEnd', (stroke) => {
  console.log('Stroke ended:', stroke);
  updateInfo();
});

sign.on('onClear', (reason) => {
  console.log('Canvas cleared:', reason);
  updateInfo();
});

// 初期表示
updateInfo();
