class Signature {
  constructor(canvasId, canvasBgId, options = {}) {
    this.canvas = typeof canvasId === 'string' ? document.getElementById(canvasId) : canvasId;
    this.backgroundCanvas = typeof canvasBgId === 'string' ? document.getElementById(canvasBgId) : canvasBgId;
    if (!this.canvas || !this.backgroundCanvas) {
      throw new Error('Canvas elements not found');
    }
    this.ctx = this.canvas.getContext('2d', { willReadFrequently: false });
    this.bgCtx = this.backgroundCanvas.getContext('2d', { willReadFrequently: false });

    if (!this.ctx || !this.bgCtx) {
      throw new Error('Failed to get canvas 2D context. Canvas may not be supported.');
    }

    this.options = Object.assign({
      strokeColor: '#000000',
      strokeThickness: 2,
      throttleIntervalMs: 8,
      dpi: 96,
      maxCanvasWidth: 1200,
      maxCanvasHeight: 800,
      minCanvasWidth: 200,
      minCanvasHeight: 100
    }, options);
    this.strokes = [];
    this.isCapturing = false;
    this.activeStroke = null;
    this.lastEventTs = 0;
    this.listeners = new Map();
    this.pointerDownHandler = (event) => this.handlePointerDown(event);
    this.pointerMoveHandler = (event) => this.handlePointerMove(event);
    this.pointerUpHandler = (event) => this.handlePointerUp(event);
    this.resize(this.canvas.width, this.canvas.height);
    this.renderBackground();
  }

  startCapture() {
    if (!window.PointerEvent) {
      return this.startCaptureFallback();
    }
    if (this.isCapturing) {
      return;
    }
    this.canvas.addEventListener('pointerdown', this.pointerDownHandler);
    this.canvas.addEventListener('pointermove', this.pointerMoveHandler);
    window.addEventListener('pointerup', this.pointerUpHandler);
    this.canvas.style.touchAction = 'none';
    this.isCapturing = true;
  }

  startCaptureFallback() {
    if (this.isCapturing) {
      return;
    }
    const mouseDownHandler = (e) => this.handlePointerDown({ clientX: e.clientX, clientY: e.clientY, pointerId: 1, preventDefault: () => e.preventDefault() });
    const mouseMoveHandler = (e) => this.handlePointerMove({ clientX: e.clientX, clientY: e.clientY, pointerId: 1 });
    const mouseUpHandler = (e) => this.handlePointerUp({ clientX: e.clientX, clientY: e.clientY, pointerId: 1 });
    const touchStartHandler = (e) => {
      if (e.touches.length > 0) {
        const touch = e.touches[0];
        this.handlePointerDown({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier, preventDefault: () => e.preventDefault() });
      }
    };
    const touchMoveHandler = (e) => {
      if (e.touches.length > 0) {
        const touch = e.touches[0];
        this.handlePointerMove({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier });
      }
    };
    const touchEndHandler = (e) => {
      if (e.changedTouches.length > 0) {
        const touch = e.changedTouches[0];
        this.handlePointerUp({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier });
      }
    };

    this.fallbackHandlers = { mouseDownHandler, mouseMoveHandler, mouseUpHandler, touchStartHandler, touchMoveHandler, touchEndHandler };
    this.canvas.addEventListener('mousedown', mouseDownHandler);
    this.canvas.addEventListener('mousemove', mouseMoveHandler);
    window.addEventListener('mouseup', mouseUpHandler);
    this.canvas.addEventListener('touchstart', touchStartHandler, { passive: false });
    this.canvas.addEventListener('touchmove', touchMoveHandler, { passive: false });
    this.canvas.addEventListener('touchend', touchEndHandler, { passive: false });
    this.isCapturing = true;
    this.usingFallback = true;
  }

  stopCapture() {
    if (!this.isCapturing) {
      return;
    }
    if (this.usingFallback && this.fallbackHandlers) {
      this.canvas.removeEventListener('mousedown', this.fallbackHandlers.mouseDownHandler);
      this.canvas.removeEventListener('mousemove', this.fallbackHandlers.mouseMoveHandler);
      window.removeEventListener('mouseup', this.fallbackHandlers.mouseUpHandler);
      this.canvas.removeEventListener('touchstart', this.fallbackHandlers.touchStartHandler);
      this.canvas.removeEventListener('touchmove', this.fallbackHandlers.touchMoveHandler);
      this.canvas.removeEventListener('touchend', this.fallbackHandlers.touchEndHandler);
      this.fallbackHandlers = null;
      this.usingFallback = false;
    } else {
      this.canvas.removeEventListener('pointerdown', this.pointerDownHandler);
      this.canvas.removeEventListener('pointermove', this.pointerMoveHandler);
      window.removeEventListener('pointerup', this.pointerUpHandler);
    }
    this.canvas.style.touchAction = '';
    this.isCapturing = false;
  }

  resize(width, height) {
    const validatedWidth = Math.max(this.options.minCanvasWidth, Math.min(this.options.maxCanvasWidth, width));
    const validatedHeight = Math.max(this.options.minCanvasHeight, Math.min(this.options.maxCanvasHeight, height));

    if (validatedWidth !== width || validatedHeight !== height) {
      console.warn(`Canvas size clamped to ${validatedWidth}x${validatedHeight} (requested: ${width}x${height})`);
    }

    const strokes = this.getStrokeData();
    this.canvas.width = validatedWidth;
    this.canvas.height = validatedHeight;
    this.backgroundCanvas.width = validatedWidth;
    this.backgroundCanvas.height = validatedHeight;
    this.renderBackground();
    if (strokes.strokes.length) {
      const normalized = this.normalizeStrokes(strokes, validatedWidth, validatedHeight);
      this.strokes = normalized.strokes;
      this.redrawStrokes();
      this.emit('onClear', 'resize');
    }
  }

  normalizeStrokes(data, width, height) {
    const scaleX = width / data.width;
    const scaleY = height / data.height;
    const normalized = data.strokes.map((stroke) => ({
      ...stroke,
      points: stroke.points.map((pt) => ({
        x: pt.x * scaleX,
        y: pt.y * scaleY,
        time: pt.time
      }))
    }));
    return { width, height, dpi: data.dpi, strokes: normalized };
  }

  getStrokeCount() {
    return this.strokes.length;
  }

  getStrokeData() {
    return {
      width: this.canvas.width,
      height: this.canvas.height,
      dpi: this.options.dpi,
      strokes: this.strokes
    };
  }

  clearCanvas(reason = 'user') {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.strokes = [];
    this.emit('onClear', reason);
  }

  undo() {
    if (!this.strokes.length) {
      return;
    }
    this.strokes.pop();
    this.redrawStrokes();
  }

  destroy() {
    this.stopCapture();
    this.clearCanvas('destroy');
    this.listeners.clear();
  }

  setStrokeColor(color) {
    this.options.strokeColor = color;
  }

  setStrokeThickness(thickness) {
    this.options.strokeThickness = thickness;
  }

  handlePointerDown(event) {
    if (event.preventDefault) {
      event.preventDefault();
    }
    if (this.canvas.setPointerCapture && event.pointerId !== undefined) {
      try {
        this.canvas.setPointerCapture(event.pointerId);
      } catch (e) {
        console.warn('Failed to set pointer capture:', e);
      }
    }
    this.activeStroke = {
      id: `stroke-${Date.now()}`,
      color: this.options.strokeColor,
      thickness: this.options.strokeThickness,
      points: []
    };
    this.emit('onStrokeStart', this.activeStroke.id);
    this.addPoint(event);
  }

  handlePointerMove(event) {
    if (!this.activeStroke) {
      return;
    }
    const now = performance.now();
    if (now - this.lastEventTs < this.options.throttleIntervalMs) {
      return;
    }
    this.lastEventTs = now;
    this.addPoint(event);
  }

  handlePointerUp(event) {
    if (!this.activeStroke) {
      return;
    }
    if (this.canvas.releasePointerCapture && event.pointerId !== undefined) {
      try {
        this.canvas.releasePointerCapture(event.pointerId);
      } catch (e) {
        console.warn('Failed to release pointer capture:', e);
      }
    }
    this.addPoint(event);
    this.strokes.push(this.activeStroke);
    this.emit('onStrokeEnd', this.activeStroke);
    this.activeStroke = null;
  }

  addPoint(event) {
    const rect = this.canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const point = { x, y, time: Date.now() };
    this.activeStroke.points.push(point);
    const points = this.activeStroke.points;
    this.ctx.strokeStyle = this.activeStroke.color;
    this.ctx.lineWidth = this.activeStroke.thickness;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
    if (points.length === 1) {
      this.ctx.beginPath();
      this.ctx.moveTo(x, y);
      this.ctx.lineTo(x, y);
      this.ctx.stroke();
    } else {
      const prev = points[points.length - 2];
      this.ctx.beginPath();
      this.ctx.moveTo(prev.x, prev.y);
      this.ctx.lineTo(x, y);
      this.ctx.stroke();
    }
  }

  redrawStrokes() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    for (const stroke of this.strokes) {
      this.ctx.strokeStyle = stroke.color;
      this.ctx.lineWidth = stroke.thickness;
      this.ctx.lineJoin = 'round';
      this.ctx.lineCap = 'round';
      const pts = stroke.points;
      if (pts.length === 0) continue;
      this.ctx.beginPath();
      this.ctx.moveTo(pts[0].x, pts[0].y);
      for (let i = 1; i < pts.length; i++) {
        this.ctx.lineTo(pts[i].x, pts[i].y);
      }
      this.ctx.stroke();
    }
  }

  renderBackground() {
    const ctx = this.bgCtx;
    ctx.clearRect(0, 0, this.backgroundCanvas.width, this.backgroundCanvas.height);
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 1;
    const step = 25;
    for (let x = 0; x < ctx.canvas.width; x += step) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, ctx.canvas.height);
      ctx.stroke();
    }
    for (let y = 0; y < ctx.canvas.height; y += step) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(ctx.canvas.width, y);
      ctx.stroke();
    }
  }

  on(eventName, handler) {
    if (!this.listeners.has(eventName)) {
      this.listeners.set(eventName, new Set());
    }
    this.listeners.get(eventName).add(handler);
    return () => this.listeners.get(eventName).delete(handler);
  }

  emit(eventName, payload) {
    const handlers = this.listeners.get(eventName);
    if (!handlers) return;
    handlers.forEach((handler) => handler(payload));
  }
}

window.signature = Signature;
