package com.vexiquest.mapprototype;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(24, 34, 28));
        getWindow().setNavigationBarColor(Color.rgb(24, 34, 28));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(24, 34, 28));

        TextView chapter = new TextView(this);
        chapter.setText("CHAPTER 1  •  NORTHERN GATEWAYS");
        chapter.setTextColor(Color.rgb(241, 226, 184));
        chapter.setTextSize(20f);
        chapter.setGravity(Gravity.CENTER_HORIZONTAL);
        chapter.setPadding(dp(16), dp(16), dp(16), dp(4));
        chapter.setTypeface(chapter.getTypeface(), android.graphics.Typeface.BOLD);
        root.addView(chapter, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView instructions = new TextView(this);
        instructions.setText("Role o mapa e toque nos pontos. O ponto laranja libera o próximo nível.");
        instructions.setTextColor(Color.rgb(203, 208, 198));
        instructions.setTextSize(13f);
        instructions.setGravity(Gravity.CENTER_HORIZONTAL);
        instructions.setPadding(dp(18), dp(2), dp(18), dp(12));
        root.addView(instructions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scrollView.setBackgroundColor(Color.BLACK);

        ExpeditionMapView mapView = new ExpeditionMapView();
        scrollView.addView(mapView, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        setContentView(root);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class ExpeditionMapView extends View {
        private static final int LOCKED = 0;
        private static final int CLAIMABLE = 1;
        private static final int CLAIMED = 2;

        private final String[] names = {
                "Orinoco Delta",
                "Mount Roraima",
                "Guiana Shield",
                "The Guianas Coast",
                "Mount Roraima Tripoint"
        };

        private final float[][] anchors = {
                {0.480f, 0.134f},
                {0.615f, 0.291f},
                {0.384f, 0.466f},
                {0.515f, 0.645f},
                {0.482f, 0.814f}
        };

        private final int[] states = {
                CLAIMED,
                CLAIMABLE,
                LOCKED,
                LOCKED,
                LOCKED
        };

        private final Bitmap mapBitmap;
        private final Paint bitmapPaint = new Paint();
        private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect sourceRect;
        private final RectF targetRect = new RectF();

        ExpeditionMapView() {
            super(MainActivity.this);
            setClickable(true);
            setFocusable(true);
            setBackgroundColor(Color.BLACK);

            mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.northern_gateways_map);
            if (mapBitmap == null) {
                throw new IllegalStateException("The Northern Gateways map resource could not be decoded.");
            }
            sourceRect = new Rect(0, 0, mapBitmap.getWidth(), mapBitmap.getHeight());
            bitmapPaint.setFilterBitmap(false);
            bitmapPaint.setDither(false);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            if (width <= 0) {
                width = getResources().getDisplayMetrics().widthPixels;
            }
            float ratio = (float) mapBitmap.getHeight() / (float) mapBitmap.getWidth();
            int height = Math.round(width * ratio);
            setMeasuredDimension(resolveSize(width, widthMeasureSpec), height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            targetRect.set(0f, 0f, getWidth(), getHeight());
            canvas.drawBitmap(mapBitmap, sourceRect, targetRect, bitmapPaint);

            float baseRadius = getWidth() * 0.052f;
            long now = System.currentTimeMillis();
            boolean animate = false;

            for (int index = 0; index < anchors.length; index++) {
                float cx = getWidth() * anchors[index][0];
                float cy = getHeight() * anchors[index][1];
                int state = states[index];
                int accent = accentColor(state);

                if (state == CLAIMABLE) {
                    animate = true;
                    float pulse = 0.5f + 0.5f * (float) Math.sin(now / 260.0);
                    shadowPaint.setColor(withAlpha(accent, 55 + Math.round(pulse * 70f)));
                    canvas.drawCircle(cx, cy, baseRadius * (1.30f + pulse * 0.12f), shadowPaint);
                }

                shadowPaint.setColor(Color.argb(145, 0, 0, 0));
                canvas.drawCircle(cx + dp(2), cy + dp(3), baseRadius * 1.08f, shadowPaint);

                circlePaint.setStyle(Paint.Style.FILL);
                circlePaint.setColor(Color.argb(225, 31, 35, 31));
                canvas.drawCircle(cx, cy, baseRadius, circlePaint);

                circlePaint.setStyle(Paint.Style.STROKE);
                circlePaint.setStrokeWidth(Math.max(dp(3), getWidth() * 0.008f));
                circlePaint.setColor(accent);
                canvas.drawCircle(cx, cy, baseRadius, circlePaint);

                textPaint.setColor(accent);
                textPaint.setTextSize(baseRadius * 0.92f);
                Paint.FontMetrics metrics = textPaint.getFontMetrics();
                float textY = cy - (metrics.ascent + metrics.descent) / 2f;
                canvas.drawText(Integer.toString(index + 1), cx, textY, textPaint);

                circlePaint.setStyle(Paint.Style.FILL);
                circlePaint.setColor(accent);
                canvas.drawCircle(cx + baseRadius * 0.70f, cy - baseRadius * 0.70f, baseRadius * 0.17f, circlePaint);
            }

            if (animate) {
                postInvalidateDelayed(50L);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return true;
            }
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(event);
            }

            performClick();
            float normalizedX = event.getX() / Math.max(1f, getWidth());
            float normalizedY = event.getY() / Math.max(1f, getHeight());
            int hit = findNode(normalizedX, normalizedY);
            if (hit >= 0) {
                handleNodeTap(hit);
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private int findNode(float x, float y) {
            float xScale = 1f;
            float yScale = (float) getHeight() / Math.max(1f, getWidth());
            float hitRadius = 0.085f;
            int nearest = -1;
            float nearestDistance = Float.MAX_VALUE;

            for (int index = 0; index < anchors.length; index++) {
                float dx = (x - anchors[index][0]) * xScale;
                float dy = (y - anchors[index][1]) * yScale;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= hitRadius && distance < nearestDistance) {
                    nearest = index;
                    nearestDistance = distance;
                }
            }
            return nearest;
        }

        private void handleNodeTap(int index) {
            int state = states[index];
            if (state == LOCKED) {
                showMessage(names[index] + " — bloqueado");
                return;
            }

            if (state == CLAIMED) {
                showMessage(names[index] + " — concluído; abriria a apresentação do local");
                return;
            }

            states[index] = CLAIMED;
            if (index + 1 < states.length && states[index + 1] == LOCKED) {
                states[index + 1] = CLAIMABLE;
            }
            invalidate();
            showMessage(names[index] + " — recompensa coletada");
        }

        private int accentColor(int state) {
            switch (state) {
                case CLAIMABLE:
                    return Color.rgb(242, 162, 59);
                case CLAIMED:
                    return Color.rgb(53, 166, 110);
                default:
                    return Color.rgb(145, 151, 146);
            }
        }

        private int withAlpha(int color, int alpha) {
            return Color.argb(
                    Math.max(0, Math.min(255, alpha)),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
            );
        }

        private void showMessage(String message) {
            Toast.makeText(
                    MainActivity.this,
                    String.format(Locale.ROOT, "%s", message),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
