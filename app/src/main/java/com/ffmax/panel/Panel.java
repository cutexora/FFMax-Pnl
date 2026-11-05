package com.ffmax.panel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Panel extends LinearLayout {
    private static final int BG = Color.rgb(8, 13, 13);
    private static final int GREEN = Color.rgb(105, 255, 141);
    private static final int SOFT = Color.rgb(156, 182, 162);
    private static final int BORDER = Color.rgb(66, 255, 114);
    private static final int TEXT = Color.rgb(234, 255, 239);

    private final View dragHandle;

    public Panel(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackground(roundedBackground());

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(VERTICAL);
        header.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable headerBg = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{Color.rgb(16, 39, 25), Color.rgb(8, 16, 12)});
        headerBg.setCornerRadii(new float[]{dp(12), dp(12), dp(12), dp(12), 0, 0, 0, 0});
        header.setBackground(headerBg);

        TextView title = new TextView(context);
        title.setText("BLACK GREEN // COMPANION");
        title.setTextColor(GREEN);
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        TextView sub = new TextView(context);
        sub.setText("FLOATING PRACTICE PANEL");
        sub.setTextColor(SOFT);
        sub.setTextSize(10);
        header.addView(sub);

        addView(header, new LinearLayout.LayoutParams(-1, -2));
        dragHandle = header;

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(12));

        content.addView(toggleRow(context, "Practice mode", true));
        content.addView(toggleRow(context, "Crosshair guide", false));
        content.addView(toggleRow(context, "Tap-timing trainer", false));

        LinearLayout stats = row(context);
        TextView statsLabel = bold(context, "Session stats");
        stats.addView(statsLabel, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView tag = new TextView(context);
        tag.setText("READY");
        tag.setTextColor(Color.rgb(112, 255, 145));
        tag.setTextSize(10);
        GradientDrawable tagBg = new GradientDrawable();
        tagBg.setColor(Color.TRANSPARENT);
        tagBg.setStroke(dp(1), Color.rgb(46, 143, 75));
        tagBg.setCornerRadius(dp(5));
        tag.setBackground(tagBg);
        tag.setPadding(dp(6), dp(3), dp(6), dp(3));
        stats.addView(tag);
        content.addView(stats);

        content.addView(label(context, "Touch sensitivity (personal setting)"));
        content.addView(slider(context, 50));

        content.addView(label(context, "Practice FOV guide (visual only)"));
        content.addView(slider(context, 60));

        LinearLayout preset = row(context);
        TextView presetLabel = new TextView(context);
        presetLabel.setText("Preset");
        presetLabel.setTextColor(TEXT);
        presetLabel.setTextSize(14);
        preset.addView(presetLabel, new LinearLayout.LayoutParams(0, -2, 1f));
        Button save = new Button(context);
        save.setText("SAVE");
        save.setAllCaps(false);
        save.setTextColor(Color.rgb(156, 255, 178));
        save.setTextSize(12);
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(Color.rgb(23, 61, 37));
        saveBg.setStroke(dp(1), Color.rgb(66, 217, 108));
        saveBg.setCornerRadius(dp(6));
        save.setBackground(saveBg);
        save.setOnClickListener(v -> Toast.makeText(context, "Preset saved on this screen.", Toast.LENGTH_SHORT).show());
        preset.addView(save);
        content.addView(preset);

        TextView note = new TextView(context);
        note.setText("Companion only. Controls here do not change Free Fire MAX, aim, recoil, firing, or game memory. Drag the header to move the panel.");
        note.setTextColor(Color.rgb(140, 163, 148));
        note.setTextSize(10);
        note.setPadding(0, dp(10), 0, 0);
        content.addView(note);

        addView(content, new LinearLayout.LayoutParams(-1, -2));
    }

    public View getDragHandle() {
        return dragHandle;
    }

    private LinearLayout row(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));
        return row;
    }

    private LinearLayout toggleRow(Context context, String labelText, boolean checked) {
        LinearLayout row = row(context);
        TextView label = bold(context, labelText);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        Switch toggle = new Switch(context);
        toggle.setChecked(checked);
        row.addView(toggle);
        return row;
    }

    private TextView bold(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(TEXT);
        view.setTextSize(14);
        view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private TextView label(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(TEXT);
        view.setTextSize(12);
        view.setPadding(0, dp(8), 0, dp(2));
        return view;
    }

    private SeekBar slider(Context context, int progress) {
        SeekBar bar = new SeekBar(context);
        bar.setMax(100);
        bar.setProgress(progress);
        return bar;
    }

    private GradientDrawable roundedBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(BG);
        bg.setStroke(dp(1), BORDER);
        bg.setCornerRadius(dp(12));
        return bg;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
