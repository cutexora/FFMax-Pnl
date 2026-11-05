package com.ffmax.panel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Panel extends FrameLayout {
    private static final int COL_BG = Color.argb(166, 15, 15, 15);
    private static final int COL_BORDER = Color.argb(26, 255, 255, 255);
    private static final int COL_VERSION = Color.argb(102, 255, 255, 255);
    private static final int COL_TEXT = Color.rgb(204, 204, 204);
    private static final int COL_VALUE = Color.rgb(170, 170, 170);
    private static final int COL_SIDEBAR = Color.argb(204, 10, 10, 10);
    private static final int COL_ITEM = Color.argb(102, 0, 0, 0);
    private static final int COL_ITEM_BORDER = Color.argb(13, 255, 255, 255);
    private static final int COL_ACCENT = Color.rgb(34, 197, 94);

    private final View dragHandle;
    private final View resizeHandle;
    private View fovRadiusBlock;
    private int fovValue = 0;
    private OnFovChangeListener fovListener;
    private final View[] navIcons = new View[4];
    private LinearLayout crosshairContent;
    private LinearLayout espContent;
    private View navView;
    private View scrollView;
    private View minimizeBubble;
    private GradientDrawable panelBg;
    private boolean minimized;
    private OnMinimizeListener minimizeListener;

    public interface OnFovChangeListener {
        void onFovChanged(int value);
    }

    public interface OnMinimizeListener {
        void onMinimizeChanged(boolean minimized);
    }

    public Panel(Context context) {
        super(context);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COL_BG);
        bg.setStroke(dp(1), COL_BORDER);
        bg.setCornerRadius(dp(12));
        setBackground(bg);
        panelBg = bg;
        setClipToOutline(true);
        setElevation(dp(15));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        addView(root, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(10), 0, dp(10));
        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColor(Color.argb(77, 0, 0, 0));
        headerBg.setCornerRadii(new float[]{dp(12), dp(12), dp(12), dp(12), 0, 0, 0, 0});
        header.setBackground(headerBg);

        TextView title = new TextView(context);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setLetterSpacing(0.18f);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(dp(8), 0, 0, Color.argb(128, 168, 85, 247));

        String brand = "CUTEXORA";
        int[] rainbow = {0xFFFF0000, 0xFFFF7F00, 0xFFFFFF00, 0xFF00FF00,
                         0xFF00FFFF, 0xFF0000FF, 0xFF4B0082, 0xFF8B00FF};
        String versionName = "1.0";
        try {
            versionName = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {}
        SpannableString headerText = new SpannableString(brand + " V" + versionName);
        for (int i = 0; i < brand.length(); i++) {
            headerText.setSpan(new ForegroundColorSpan(rainbow[i]), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        headerText.setSpan(new ForegroundColorSpan(COL_VERSION), brand.length(), headerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setText(headerText);

        header.addView(title);

        root.addView(header, new LinearLayout.LayoutParams(-1, -2));
        dragHandle = header;

        LinearLayout nav = new LinearLayout(context);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(6), dp(8), dp(6));
        nav.setBackgroundColor(COL_SIDEBAR);
        navView = nav;

        String[] glyphs = {"\u2295", "\u25C9", "\u25A3", "\u2699"};
        for (int i = 0; i < glyphs.length; i++) {
            final int index = i;
            View icon = navIcon(context, glyphs[i], i == 0);
            icon.setOnClickListener(v -> showTab(index));
            navIcons[i] = icon;
            nav.addView(icon);
        }

        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scrollView = scroll;

        LinearLayout host = new LinearLayout(context);
        host.setOrientation(LinearLayout.VERTICAL);

        crosshairContent = contentRow(context);
        LinearLayout aimCol = column(context);
        LinearLayout.LayoutParams aimParams = (LinearLayout.LayoutParams) aimCol.getLayoutParams();
        aimParams.rightMargin = dp(6);
        aimCol.addView(banner(context, "Aim", "Aimbot Settings"));
        addToggleRow(aimCol, context, "Aimbot", true);
        addToggleRow(aimCol, context, "AimBot External", false);
        addToggleRow(aimCol, context, "Silent Aim", true);
        addToggleRow(aimCol, context, "Enemy Pull", false);
        addToggleRow(aimCol, context, "Auto Fire", true);
        addToggleRow(aimCol, context, "Show Aim Fov", false, new MiniSwitch.OnCheckedChangeListener() {
            public void onChanged(boolean checked) {
                if (fovRadiusBlock != null) fovRadiusBlock.setVisibility(checked ? VISIBLE : GONE);
                if (fovListener != null) fovListener.onFovChanged(checked ? fovValue : 0);
            }
        });
        addToggleRow(aimCol, context, "No Recoil", true);

        LinearLayout triggerCol = column(context);
        triggerCol.addView(banner(context, "Trigger Module", "Fov & Trigger"));
        fovRadiusBlock = addSlider(triggerCol, context, "Fov Radius", 100, 1, 0, false, true);
        fovRadiusBlock.setVisibility(GONE);
        addSlider(triggerCol, context, "Aim Distance", 20, 50, 0, true, false);
        addToggleRow(triggerCol, context, "Glitch Fire", false);
        addToggleRow(triggerCol, context, "Sniper Scope", true);
        addToggleRow(triggerCol, context, "Sniper Switch", false);

        crosshairContent.addView(aimCol);
        crosshairContent.addView(triggerCol);

        espContent = contentRow(context);
        LinearLayout playerEspCol = column(context);
        LinearLayout.LayoutParams espParams = (LinearLayout.LayoutParams) playerEspCol.getLayoutParams();
        espParams.rightMargin = dp(6);
        playerEspCol.addView(banner(context, "ESP", "Player ESP"));
        addButton(playerEspCol, context, "Enable ESP");
        addButton(playerEspCol, context, "ESP Line");
        addButton(playerEspCol, context, "ESP Box");
        addButton(playerEspCol, context, "Skeleton");

        LinearLayout extraEspCol = column(context);
        extraEspCol.addView(banner(context, "ESP EXTRA", "Extra Esp"));
        addButton(extraEspCol, context, "ESP Rank");
        addButton(extraEspCol, context, "Rainbow ESP");
        addButton(extraEspCol, context, "ESP Dist.");

        espContent.addView(playerEspCol);
        espContent.addView(extraEspCol);
        espContent.setVisibility(GONE);

        host.addView(crosshairContent, new LinearLayout.LayoutParams(-1, -2));
        host.addView(espContent, new LinearLayout.LayoutParams(-1, -2));
        scroll.addView(host, new FrameLayout.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        ResizeGrip grip = new ResizeGrip(context);
        FrameLayout.LayoutParams gripParams = new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.BOTTOM | Gravity.RIGHT);
        gripParams.rightMargin = dp(4);
        gripParams.bottomMargin = dp(4);
        addView(grip, gripParams);
        resizeHandle = grip;

        TextView bubble = new TextView(context);
        bubble.setText("\u2295");
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(20);
        bubble.setGravity(Gravity.CENTER);
        GradientDrawable bubbleBg = new GradientDrawable();
        bubbleBg.setShape(GradientDrawable.OVAL);
        bubbleBg.setColor(Color.argb(235, 15, 15, 15));
        bubbleBg.setStroke(dp(2), Color.argb(160, 168, 85, 247));
        bubble.setBackground(bubbleBg);
        bubble.setVisibility(GONE);
        minimizeBubble = bubble;
        addView(bubble, new FrameLayout.LayoutParams(-1, -1));
    }

    public void setOnFovChangeListener(OnFovChangeListener listener) {
        fovListener = listener;
    }

    public View getDragHandle() {
        return dragHandle;
    }

    public View getResizeHandle() {
        return resizeHandle;
    }

    public View getMinimizeBubble() {
        return minimizeBubble;
    }

    public void restore() {
        setMinimized(false);
    }

    public void setOnMinimizeListener(OnMinimizeListener listener) {
        minimizeListener = listener;
    }

    public void toggleMinimize() {
        setMinimized(!minimized);
    }

    private void setMinimized(boolean min) {
        minimized = min;
        int vis = min ? GONE : VISIBLE;
        if (dragHandle != null) dragHandle.setVisibility(vis);
        if (navView != null) navView.setVisibility(vis);
        if (scrollView != null) scrollView.setVisibility(vis);
        if (resizeHandle != null) resizeHandle.setVisibility(vis);
        if (minimizeBubble != null) minimizeBubble.setVisibility(min ? VISIBLE : GONE);
        setBackground(min ? null : panelBg);
        if (minimizeListener != null) minimizeListener.onMinimizeChanged(min);
    }

    private LinearLayout column(Context context) {
        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        return col;
    }

    private LinearLayout contentRow(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(12));
        content.setBaselineAligned(false);
        return content;
    }

    private void showTab(int index) {
        crosshairContent.setVisibility(index == 0 ? VISIBLE : GONE);
        espContent.setVisibility(index == 1 ? VISIBLE : GONE);
        updateNavActive(index);
    }

    private void updateNavActive(int active) {
        for (int i = 0; i < navIcons.length; i++) {
            View icon = navIcons[i];
            if (!(icon instanceof TextView)) continue;
            TextView tv = (TextView) icon;
            boolean isActive = i == active;
            tv.setTextColor(isActive ? Color.WHITE : Color.rgb(85, 85, 85));
            if (isActive) {
                GradientDrawable g = new GradientDrawable();
                g.setColor(Color.argb(26, 255, 255, 255));
                g.setCornerRadius(dp(8));
                tv.setBackground(g);
            } else {
                tv.setBackground(null);
            }
        }
    }

    private View banner(Context context, String heading, String sub) {
        LinearLayout b = new LinearLayout(context);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(5, 46, 22), Color.rgb(22, 101, 52)});
        g.setCornerRadius(dp(8));
        g.setStroke(dp(1), Color.argb(77, 34, 197, 94));
        b.setBackground(g);

        TextView h = new TextView(context);
        h.setText(heading);
        h.setTextColor(Color.WHITE);
        h.setTextSize(14);
        h.setTypeface(null, Typeface.BOLD);
        b.addView(h);

        TextView p = new TextView(context);
        p.setText(sub);
        p.setTextColor(Color.argb(178, 255, 255, 255));
        p.setTextSize(10);
        b.addView(p);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        b.setLayoutParams(lp);
        return b;
    }

    private LinearLayout item(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable g = new GradientDrawable();
        g.setColor(COL_ITEM);
        g.setCornerRadius(dp(6));
        g.setStroke(dp(1), COL_ITEM_BORDER);
        row.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);
        return row;
    }

    private TextView label(Context context, String text, int color, float size, boolean bold) {
        TextView v = new TextView(context);
        v.setText(text);
        v.setTextColor(color);
        v.setTextSize(size);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        return v;
    }

    private void addToggleRow(LinearLayout col, Context context, String text, boolean checked) {
        addToggleRow(col, context, text, checked, null);
    }

    private void addToggleRow(LinearLayout col, Context context, String text, boolean checked,
                              MiniSwitch.OnCheckedChangeListener listener) {
        LinearLayout row = item(context);
        row.addView(label(context, text, COL_TEXT, 12, true), new LinearLayout.LayoutParams(0, -2, 1f));
        MiniSwitch toggle = new MiniSwitch(context);
        toggle.setChecked(checked);
        if (listener != null) toggle.setOnCheckedChangeListener(listener);
        row.addView(toggle);
        col.addView(row);
    }

    private void addButton(LinearLayout col, Context context, String text) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(10), dp(10), dp(10), dp(10));
        applyButtonState(btn, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(6);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> {
            boolean active = !Boolean.TRUE.equals(v.getTag());
            v.setTag(active);
            applyButtonState((TextView) v, active);
        });
        col.addView(btn);
    }

    private void applyButtonState(TextView btn, boolean active) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(6));
        if (active) {
            g.setColor(COL_ACCENT);
            g.setStroke(dp(1), Color.rgb(74, 222, 128));
            btn.setTextColor(Color.rgb(5, 20, 10));
        } else {
            g.setColor(COL_ITEM);
            g.setStroke(dp(1), COL_ITEM_BORDER);
            btn.setTextColor(COL_TEXT);
        }
        btn.setBackground(g);
    }

    private View addSlider(LinearLayout col, Context context, String text, int maxProgress,
                           int step, int initialProgress, boolean meters, boolean isFov) {
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable g = new GradientDrawable();
        g.setColor(COL_ITEM);
        g.setCornerRadius(dp(6));
        g.setStroke(dp(1), COL_ITEM_BORDER);
        block.setBackground(g);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.bottomMargin = dp(8);
        block.setLayoutParams(blp);

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(label(context, text, COL_TEXT, 12, true), new LinearLayout.LayoutParams(0, -2, 1f));

        TextView valueView = label(context, formatSliderValue(initialProgress * step, meters), COL_VALUE, 11, false);
        top.addView(valueView);
        block.addView(top);

        SeekBar bar = new SeekBar(context);
        bar.setMax(maxProgress);
        bar.setProgress(initialProgress);
        bar.setProgressTintList(ColorStateList.valueOf(COL_ACCENT));
        bar.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
        block.addView(bar, new LinearLayout.LayoutParams(-1, -2));

        final int stepSize = step;
        final boolean showMeters = meters;
        final boolean fovSlider = isFov;
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress * stepSize;
                valueView.setText(formatSliderValue(value, showMeters));
                if (fovSlider) {
                    fovValue = value;
                    if (fovListener != null) fovListener.onFovChanged(value);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        col.addView(block);
        return block;
    }

    private String formatSliderValue(int value, boolean meters) {
        if (!meters) return String.valueOf(value);
        if (value >= 1000) return "1 km";
        return value + " m";
    }

    private View navIcon(Context context, String glyph, boolean active) {
        TextView v = new TextView(context);
        v.setText(glyph);
        v.setTextSize(18);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(active ? Color.WHITE : Color.rgb(85, 85, 85));
        if (active) {
            GradientDrawable g = new GradientDrawable();
            g.setColor(Color.argb(26, 255, 255, 255));
            g.setCornerRadius(dp(8));
            v.setBackground(g);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(36));
        lp.rightMargin = dp(8);
        v.setLayoutParams(lp);
        return v;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    static class MiniSwitch extends View {
        interface OnCheckedChangeListener {
            void onChanged(boolean checked);
        }

        private boolean checked;
        private OnCheckedChangeListener listener;
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint knob = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density;

        MiniSwitch(Context context) {
            super(context);
            density = context.getResources().getDisplayMetrics().density;
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(dp(1));
            knob.setStyle(Paint.Style.FILL);
            knob.setColor(Color.WHITE);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
        }

        void setChecked(boolean value) {
            checked = value;
            invalidate();
        }

        boolean isChecked() {
            return checked;
        }

        void setOnCheckedChangeListener(OnCheckedChangeListener l) {
            listener = l;
        }

        @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(Math.round(dp(34)), Math.round(dp(18)));
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float r = h / 2f;

            track.setColor(checked ? Color.rgb(0x33, 0x33, 0x33) : Color.rgb(0x22, 0x22, 0x22));
            canvas.drawRoundRect(0, 0, w, h, r, r, track);

            border.setColor(checked ? Color.rgb(0x55, 0x55, 0x55) : Color.rgb(0x33, 0x33, 0x33));
            canvas.drawRoundRect(dp(0.5f), dp(0.5f), w - dp(0.5f), h - dp(0.5f), r, r, border);

            float knobRadius = h / 2f - dp(2);
            float cx = checked ? w - r : r;
            if (checked) {
                knob.setShadowLayer(dp(6), 0, 0, Color.argb(204, 255, 255, 255));
            } else {
                knob.clearShadowLayer();
            }
            canvas.drawCircle(cx, h / 2f, knobRadius, knob);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                checked = !checked;
                invalidate();
                if (listener != null) listener.onChanged(checked);
                performClick();
            }
            return true;
        }

        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        private float dp(float value) {
            return value * density;
        }
    }

    static class ResizeGrip extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density;

        ResizeGrip(Context context) {
            super(context);
            density = context.getResources().getDisplayMetrics().density;
            paint.setColor(Color.argb(102, 255, 255, 255));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            canvas.drawLine(w - dp(3), h * 0.45f, w * 0.45f, h - dp(3), paint);
            canvas.drawLine(w - dp(3), h * 0.72f, w * 0.72f, h - dp(3), paint);
        }

        private float dp(float value) {
            return value * density;
        }
    }
}
