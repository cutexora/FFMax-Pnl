package com.ffmax.panel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
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
    private static final int COL_PURPLE = Color.rgb(168, 85, 247);
    private static final int COL_VERSION = Color.argb(102, 255, 255, 255);
    private static final int COL_TEXT = Color.rgb(204, 204, 204);
    private static final int COL_VALUE = Color.rgb(170, 170, 170);
    private static final int COL_SIDEBAR = Color.argb(204, 10, 10, 10);
    private static final int COL_ITEM = Color.argb(102, 0, 0, 0);
    private static final int COL_ITEM_BORDER = Color.argb(13, 255, 255, 255);
    private static final int COL_ACCENT = Color.rgb(34, 197, 94);

    private final View dragHandle;
    private final View resizeHandle;
    private OnFovChangeListener fovListener;

    public interface OnFovChangeListener {
        void onFovChanged(int value);
    }

    public Panel(Context context) {
        super(context);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COL_BG);
        bg.setStroke(dp(1), COL_BORDER);
        bg.setCornerRadius(dp(12));
        setBackground(bg);
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
        title.setText("CUTEXORA");
        title.setTextColor(COL_PURPLE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setLetterSpacing(0.18f);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(dp(8), 0, 0, Color.argb(128, 168, 85, 247));
        header.addView(title);

        TextView version = new TextView(context);
        version.setText("1");
        version.setTextColor(COL_VERSION);
        version.setTextSize(11);
        version.setGravity(Gravity.CENTER);
        header.addView(version);

        root.addView(header, new LinearLayout.LayoutParams(-1, -2));
        dragHandle = header;

        LinearLayout nav = new LinearLayout(context);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(6), dp(8), dp(6));
        nav.setBackgroundColor(COL_SIDEBAR);

        nav.addView(navIcon(context, "\u2295", true));
        nav.addView(navIcon(context, "\u25C9", false));
        nav.addView(navIcon(context, "\u25A3", false));
        nav.addView(navIcon(context, "\u2699", false));

        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(12));
        content.setBaselineAligned(false);

        LinearLayout col1 = column(context);
        LinearLayout.LayoutParams c1p = (LinearLayout.LayoutParams) col1.getLayoutParams();
        c1p.rightMargin = dp(6);
        col1.addView(banner(context, "Aim", "Aimbot Settings"));
        addToggleRow(col1, context, "Aimbot", true);
        addToggleRow(col1, context, "AimBot External", false);
        addToggleRow(col1, context, "Silent Aim", true);
        addToggleRow(col1, context, "Enemy Pull", false);
        addToggleRow(col1, context, "Auto Fire", true);
        addToggleRow(col1, context, "Show Aim Fov", false);
        addToggleRow(col1, context, "No Recoil", true);

        LinearLayout col2 = column(context);
        col2.addView(banner(context, "Trigger Module", "Fov & Trigger"));
        addSlider(col2, context, "Fov Radius", 100, 1, 0, false, true);
        addSlider(col2, context, "Aim Distance", 20, 50, 0, true, false);
        addToggleRow(col2, context, "Glitch Fire", false);
        addToggleRow(col2, context, "Sniper Scope", true);
        addToggleRow(col2, context, "Sniper Switch", false);

        content.addView(col1);
        content.addView(col2);
        scroll.addView(content, new FrameLayout.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        ResizeGrip grip = new ResizeGrip(context);
        FrameLayout.LayoutParams gripParams = new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.BOTTOM | Gravity.RIGHT);
        gripParams.rightMargin = dp(4);
        gripParams.bottomMargin = dp(4);
        addView(grip, gripParams);
        resizeHandle = grip;
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

    private LinearLayout column(Context context) {
        LinearLayout col = new LinearLayout(context);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        return col;
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
        LinearLayout row = item(context);
        row.addView(label(context, text, COL_TEXT, 12, true), new LinearLayout.LayoutParams(0, -2, 1f));
        MiniSwitch toggle = new MiniSwitch(context);
        toggle.setChecked(checked);
        row.addView(toggle);
        col.addView(row);
    }

    private void addSlider(LinearLayout col, Context context, String text, int maxProgress,
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
                if (fovSlider && fovListener != null) fovListener.onFovChanged(value);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        col.addView(block);
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
