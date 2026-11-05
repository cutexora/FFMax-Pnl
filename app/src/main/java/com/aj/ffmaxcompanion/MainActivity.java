package com.aj.ffmaxcompanion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 41;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(32, 48, 32, 32);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.rgb(12,16,18));
        TextView title = new TextView(this);
        title.setText("FF MAX COMPANION");
        title.setTextColor(Color.rgb(90,255,120));
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this);
        info.setText("\nFloating practice/settings panel\n\nThis companion does not modify Free Fire MAX or provide gameplay cheats.\n");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        Button start = new Button(this);
        start.setText("ENABLE FLOATING PANEL");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(i, REQ_OVERLAY);
            } else startOverlay();
        });
        Button stop = new Button(this);
        stop.setText("STOP PANEL");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        box.addView(title, new LinearLayout.LayoutParams(-1, -2));
        box.addView(info, new LinearLayout.LayoutParams(-1, -2));
        box.addView(start, new LinearLayout.LayoutParams(-1, -2));
        box.addView(stop, new LinearLayout.LayoutParams(-1, -2));
        setContentView(box);
    }
    @Override protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r,c,d);
        if (r == REQ_OVERLAY && (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this))) startOverlay();
    }
    private void startOverlay() { startService(new Intent(this, OverlayService.class)); }
}
