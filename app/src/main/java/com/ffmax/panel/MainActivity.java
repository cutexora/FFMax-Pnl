package com.ffmax.panel;

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

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 41;
    private Button toggle;
    private boolean running;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(32, 48, 32, 32);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.rgb(12, 16, 18));

        TextView title = new TextView(this);
        title.setText("FF MAX PNL");
        title.setTextColor(Color.rgb(90, 255, 120));
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);

        toggle = new Button(this);
        toggle.setText("Enable Panel");
        toggle.setOnClickListener(v -> onToggle());

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.bottomMargin = 24;
        box.addView(title, titleParams);
        box.addView(toggle, new LinearLayout.LayoutParams(-1, -2));
        setContentView(box);
    }

    private void onToggle() {
        if (running) {
            stopService(new Intent(this, OverlayService.class));
            setRunning(false);
        } else if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, REQ_OVERLAY);
        } else {
            startOverlay();
        }
    }

    private void startOverlay() {
        startService(new Intent(this, OverlayService.class));
        setRunning(true);
    }

    private void setRunning(boolean value) {
        running = value;
        toggle.setText(value ? "Stop Panel" : "Enable Panel");
    }

    @Override protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        if (r == REQ_OVERLAY && (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this))) startOverlay();
    }
}
