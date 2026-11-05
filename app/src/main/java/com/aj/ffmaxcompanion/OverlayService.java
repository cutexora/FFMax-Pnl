package com.aj.ffmaxcompanion;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OverlayService extends Service {
    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams params;
    @Override public IBinder onBind(Intent i) { return null; }
    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        WebView web = new WebView(this);
        web.setBackgroundColor(0xEE0B1010);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("file:///android_asset/panel.html");
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
            (int)(getResources().getDisplayMetrics().widthPixels * 0.78f),
            WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.x = 18; params.y = 130;
        web.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy; int ox, oy;
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) { sx=e.getRawX(); sy=e.getRawY(); ox=params.x; oy=params.y; return false; }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = ox + (int)(sx-e.getRawX());
                    params.y = oy + (int)(e.getRawY()-sy);
                    try { wm.updateViewLayout(web, params); } catch(Exception ignored) {}
                    return false;
                }
                return false;
            }
        });
        panel = web;
        try { wm.addView(panel, params); } catch(Exception e) { stopSelf(); }
    }
    @Override public void onDestroy() {
        if (wm != null && panel != null) try { wm.removeView(panel); } catch(Exception ignored) {}
        super.onDestroy();
    }
}
