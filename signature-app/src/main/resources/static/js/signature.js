class Signature {
  constructor(canvasId, canvasBgId, options = {}) {
    // Private properties (internal use only)
    this._canvas = typeof canvasId === 'string' ? document.getElementById(canvasId) : canvasId;
    this._backgroundCanvas = typeof canvasBgId === 'string' ? document.getElementById(canvasBgId) : canvasBgId;
    if (!this._canvas || !this._backgroundCanvas) {
      throw new Error('Canvas elements not found');
    }
    this._ctx = this._canvas.getContext('2d', { willReadFrequently: false });
    this._bgCtx = this._backgroundCanvas.getContext('2d', { willReadFrequently: false });

    if (!this._ctx || !this._bgCtx) {
      throw new Error('Failed to get canvas 2D context. Canvas may not be supported.');
    }

    this._options = Object.assign({
      strokeColor: '#000000',
      strokeThickness: 2,
      throttleIntervalMs: 8,
      dpi: 96,
      maxCanvasWidth: 1200,
      maxCanvasHeight: 800,
      minCanvasWidth: 200,
      minCanvasHeight: 100
    }, options);
    this._strokes = [];
    this._isCapturing = false;
    this._activeStroke = null;
    this._lastEventTs = 0;
    this._listeners = new Map();
    this._pointerDownHandler = (event) => this._handlePointerDown(event);
    this._pointerMoveHandler = (event) => this._handlePointerMove(event);
    this._pointerUpHandler = (event) => this._handlePointerUp(event);
    this._fallbackHandlers = null;
    this._usingFallback = false;
    this.resize(this._canvas.width, this._canvas.height);
    this._renderBackground();
  }

  // Public API: Start capturing signature input
  startCapture() {
    if (!window.PointerEvent) {
      return this._startCaptureFallback();
    }
    if (this._isCapturing) {
      return;
    }
    this._canvas.addEventListener('pointerdown', this._pointerDownHandler);
    this._canvas.addEventListener('pointermove', this._pointerMoveHandler);
    window.addEventListener('pointerup', this._pointerUpHandler);
    this._canvas.style.touchAction = 'none';
    this._isCapturing = true;
  }

  // Public API: Stop capturing signature input
  stopCapture() {
    if (!this._isCapturing) {
      return;
    }
    if (this._usingFallback && this._fallbackHandlers) {
      this._canvas.removeEventListener('mousedown', this._fallbackHandlers.mouseDownHandler);
      this._canvas.removeEventListener('mousemove', this._fallbackHandlers.mouseMoveHandler);
      window.removeEventListener('mouseup', this._fallbackHandlers.mouseUpHandler);
      this._canvas.removeEventListener('touchstart', this._fallbackHandlers.touchStartHandler);
      this._canvas.removeEventListener('touchmove', this._fallbackHandlers.touchMoveHandler);
      this._canvas.removeEventListener('touchend', this._fallbackHandlers.touchEndHandler);
      this._fallbackHandlers = null;
      this._usingFallback = false;
    } else {
      this._canvas.removeEventListener('pointerdown', this._pointerDownHandler);
      this._canvas.removeEventListener('pointermove', this._pointerMoveHandler);
      window.removeEventListener('pointerup', this._pointerUpHandler);
    }
    this._canvas.style.touchAction = '';
    this._isCapturing = false;
  }

  // Public API: Resize the canvas
  resize(width, height) {
    const validatedWidth = Math.max(this._options.minCanvasWidth, Math.min(this._options.maxCanvasWidth, width));
    const validatedHeight = Math.max(this._options.minCanvasHeight, Math.min(this._options.maxCanvasHeight, height));

    if (validatedWidth !== width || validatedHeight !== height) {
      console.warn(`Canvas size clamped to ${validatedWidth}x${validatedHeight} (requested: ${width}x${height})`);
    }

    const strokes = this.getStrokeData();
    this._canvas.width = validatedWidth;
    this._canvas.height = validatedHeight;
    this._backgroundCanvas.width = validatedWidth;
    this._backgroundCanvas.height = validatedHeight;
    this._renderBackground();
    if (strokes.strokes.length) {
      const normalized = this._normalizeStrokes(strokes, validatedWidth, validatedHeight);
      this._strokes = normalized.strokes;
      this._redrawStrokes();
      this._emit('onClear', 'resize');
    }
  }

  // Public API: Get the number of strokes
  getStrokeCount() {
    return this._strokes.length;
  }

  // Public API: Get stroke data
  getStrokeData() {
    return {
      width: this._canvas.width,
      height: this._canvas.height,
      dpi: this._options.dpi,
      strokes: this._strokes
    };
  }

  // Public API: Clear the canvas
  clearCanvas(reason = 'user') {
    this._ctx.clearRect(0, 0, this._canvas.width, this._canvas.height);
    this._strokes = [];
    this._emit('onClear', reason);
  }

  // Public API: Undo the last stroke
  undo() {
    if (!this._strokes.length) {
      return;
    }
    this._strokes.pop();
    this._redrawStrokes();
  }

  // Public API: Destroy the signature instance
  destroy() {
    this.stopCapture();
    this.clearCanvas('destroy');
    this._listeners.clear();
  }

  // Public API: Set stroke color
  setStrokeColor(color) {
    this._options.strokeColor = color;
  }

  // Public API: Set stroke thickness
  setStrokeThickness(thickness) {
    this._options.strokeThickness = thickness;
  }

  // Public API: Register event listener
  on(eventName, handler) {
    if (!this._listeners.has(eventName)) {
      this._listeners.set(eventName, new Set());
    }
    this._listeners.get(eventName).add(handler);
    return () => this._listeners.get(eventName).delete(handler);
  }

  // Private: Fallback for browsers without PointerEvent support
  _startCaptureFallback() {
    if (this._isCapturing) {
      return;
    }
    const mouseDownHandler = (e) => this._handlePointerDown({ clientX: e.clientX, clientY: e.clientY, pointerId: 1, preventDefault: () => e.preventDefault() });
    const mouseMoveHandler = (e) => this._handlePointerMove({ clientX: e.clientX, clientY: e.clientY, pointerId: 1 });
    const mouseUpHandler = (e) => this._handlePointerUp({ clientX: e.clientX, clientY: e.clientY, pointerId: 1 });
    const touchStartHandler = (e) => {
      if (e.touches.length > 0) {
        const touch = e.touches[0];
        this._handlePointerDown({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier, preventDefault: () => e.preventDefault() });
      }
    };
    const touchMoveHandler = (e) => {
      if (e.touches.length > 0) {
        const touch = e.touches[0];
        this._handlePointerMove({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier });
      }
    };
    const touchEndHandler = (e) => {
      if (e.changedTouches.length > 0) {
        const touch = e.changedTouches[0];
        this._handlePointerUp({ clientX: touch.clientX, clientY: touch.clientY, pointerId: touch.identifier });
      }
    };

    this._fallbackHandlers = { mouseDownHandler, mouseMoveHandler, mouseUpHandler, touchStartHandler, touchMoveHandler, touchEndHandler };
    this._canvas.addEventListener('mousedown', mouseDownHandler);
    this._canvas.addEventListener('mousemove', mouseMoveHandler);
    window.addEventListener('mouseup', mouseUpHandler);
    this._canvas.addEventListener('touchstart', touchStartHandler, { passive: false });
    this._canvas.addEventListener('touchmove', touchMoveHandler, { passive: false });
    this._canvas.addEventListener('touchend', touchEndHandler, { passive: false });
    this._isCapturing = true;
    this._usingFallback = true;
  }

  // Private: Normalize strokes for resizing
  _normalizeStrokes(data, width, height) {
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

  // Private: Handle pointer down event
  _handlePointerDown(event) {
    if (event.preventDefault) {
      event.preventDefault();
    }
    if (this._canvas.setPointerCapture && event.pointerId !== undefined) {
      try {
        this._canvas.setPointerCapture(event.pointerId);
      } catch (e) {
        console.warn('Failed to set pointer capture:', e);
      }
    }
    this._activeStroke = {
      id: `stroke-${Date.now()}`,
      color: this._options.strokeColor,
      thickness: this._options.strokeThickness,
      points: []
    };
    this._emit('onStrokeStart', this._activeStroke.id);
    this._addPoint(event);
  }

  // Private: Handle pointer move event
  _handlePointerMove(event) {
    if (!this._activeStroke) {
      return;
    }
    const now = performance.now();
    if (now - this._lastEventTs < this._options.throttleIntervalMs) {
      return;
    }
    this._lastEventTs = now;
    this._addPoint(event);
  }

  // Private: Handle pointer up event
  _handlePointerUp(event) {
    if (!this._activeStroke) {
      return;
    }
    if (this._canvas.releasePointerCapture && event.pointerId !== undefined) {
      try {
        this._canvas.releasePointerCapture(event.pointerId);
      } catch (e) {
        console.warn('Failed to release pointer capture:', e);
      }
    }
    this._addPoint(event);
    this._strokes.push(this._activeStroke);
    this._emit('onStrokeEnd', this._activeStroke);
    this._activeStroke = null;
  }

  // Private: Add a point to the active stroke
  _addPoint(event) {
    const rect = this._canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const point = { x, y, time: Date.now() };
    this._activeStroke.points.push(point);
    const points = this._activeStroke.points;
    this._ctx.strokeStyle = this._activeStroke.color;
    this._ctx.lineWidth = this._activeStroke.thickness;
    this._ctx.lineCap = 'round';
    this._ctx.lineJoin = 'round';
    if (points.length === 1) {
      this._ctx.beginPath();
      this._ctx.moveTo(x, y);
      this._ctx.lineTo(x, y);
      this._ctx.stroke();
    } else {
      const prev = points[points.length - 2];
      this._ctx.beginPath();
      this._ctx.moveTo(prev.x, prev.y);
      this._ctx.lineTo(x, y);
      this._ctx.stroke();
    }
  }

  // Private: Redraw all strokes
  _redrawStrokes() {
    this._ctx.clearRect(0, 0, this._canvas.width, this._canvas.height);
    for (const stroke of this._strokes) {
      this._ctx.strokeStyle = stroke.color;
      this._ctx.lineWidth = stroke.thickness;
      this._ctx.lineJoin = 'round';
      this._ctx.lineCap = 'round';
      const pts = stroke.points;
      if (pts.length === 0) continue;
      this._ctx.beginPath();
      this._ctx.moveTo(pts[0].x, pts[0].y);
      for (let i = 1; i < pts.length; i++) {
        this._ctx.lineTo(pts[i].x, pts[i].y);
      }
      this._ctx.stroke();
    }
  }

  // Private: Render the background grid
  _renderBackground() {
    const ctx = this._bgCtx;
    ctx.clearRect(0, 0, this._backgroundCanvas.width, this._backgroundCanvas.height);
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

  // Private: Emit an event
  _emit(eventName, payload) {
    const handlers = this._listeners.get(eventName);
    if (!handlers) return;
    handlers.forEach((handler) => handler(payload));
  }
}

window.signature = Signature;
