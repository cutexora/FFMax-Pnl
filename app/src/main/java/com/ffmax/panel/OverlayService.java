package com.ffmax.panel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class OverlayService extends Service {
    private WindowManager wm;
    private View panel;
    private FovCircleView fovCircle;
    private WindowManager.LayoutParams params;
    private int minWidth;
    private int minHeight;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        minWidth = Math.round(240 * metrics.density);
        minHeight = Math.round(280 * metrics.density);

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        fovCircle = new FovCircleView(this);
        WindowManager.LayoutParams fovParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT);
        fovParams.gravity = Gravity.TOP | Gravity.LEFT;
        fovParams.x = 0;
        fovParams.y = 0;

        Panel panelView = new Panel(this);
        panelView.setOnFovChangeListener(value -> fovCircle.setFov(value));

        params = new WindowManager.LayoutParams(
            Math.round(metrics.widthPixels * 0.92f),
            Math.round(metrics.heightPixels * 0.62f), type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 90;

        attachDrag(panelView.getDragHandle());
        attachResize(panelView.getResizeHandle());

        panel = panelView;
        try { wm.addView(panel, params); } catch (Exception e) { stopSelf(); }
        try { wm.addView(fovCircle, fovParams); } catch (Exception ignored) {}
    }

    private void attachDrag(View handle) {
        handle.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy;
            int ox, oy;
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = e.getRawX(); sy = e.getRawY();
                        ox = params.x; oy = params.y;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = ox + (int) (e.getRawX() - sx);
                        params.y = oy + (int) (e.getRawY() - sy);
                        try { wm.updateViewLayout(panel, params); } catch (Exception ignored) {}
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });
    }

    private void attachResize(View handle) {
        handle.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy;
            int sw, sh;
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = e.getRawX(); sy = e.getRawY();
                        sw = params.width; sh = params.height;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.width = Math.max(minWidth, sw + (int) (e.getRawX() - sx));
                        params.height = Math.max(minHeight, sh + (int) (e.getRawY() - sy));
                        try { wm.updateViewLayout(panel, params); } catch (Exception ignored) {}
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });
    }

    @Override public void onDestroy() {
        if (wm != null) {
            if (panel != null) try { wm.removeView(panel); } catch (Exception ignored) {}
            if (fovCircle != null) try { wm.removeView(fovCircle); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    static class FovCircleView extends View {
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density;
        private int fov = 0;

        FovCircleView(Context context) {
            super(context);
            density = context.getResources().getDisplayMetrics().density;
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(dp(2));
            stroke.setColor(Color.argb(230, 66, 255, 114));
            glow.setStyle(Paint.Style.STROKE);
            glow.setStrokeWidth(dp(6));
            glow.setColor(Color.argb(60, 66, 255, 114));
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        void setFov(int value) {
            fov = Math.max(0, Math.min(100, value));
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            if (fov <= 0) return;
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float maxRadius = Math.min(getWidth(), getHeight()) / 2f * 0.9f;
            float radius = maxRadius * fov / 100f;
            canvas.drawCircle(cx, cy, radius, glow);
            canvas.drawCircle(cx, cy, radius, stroke);
            float tick = dp(8);
            canvas.drawLine(cx - tick, cy, cx + tick, cy, stroke);
            canvas.drawLine(cx, cy - tick, cx, cy + tick, stroke);
        }

        private float dp(float value) {
            return value * density;
        }
    }
}
