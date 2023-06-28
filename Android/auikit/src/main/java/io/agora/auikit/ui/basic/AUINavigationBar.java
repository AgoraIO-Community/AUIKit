package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.util.Locale;

import io.agora.auikit.R;

public class AUINavigationBar extends LinearLayout {
    private LinearLayout llItems;
    private NavBgDrawable mBackground;
    private int menuDefStyle;
    private int contentPaddingTopWithoutOffset;
    private Runnable onLayoutRunnable;
    private View itemViewActivated;

    public AUINavigationBar(@NonNull Context context) {
        this(context, null);
    }

    public AUINavigationBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUINavigationBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUINavigationBar, defStyleAttr, R.style.AUINavigationBar);

        // Inflate menu
        int menuResourceId = typedArray.getResourceId(R.styleable.AUINavigationBar_aui_navigationBar_menu, R.menu.aui_navigation_menu);
        menuDefStyle = typedArray.getResourceId(R.styleable.AUINavigationBar_aui_navigationBar_menu_appearance, R.style.AUINavigationBarMenu);
        parseAndCreateMenu(menuResourceId);

        // Background
        int bgShadowColor = typedArray.getColor(R.styleable.AUINavigationBar_aui_navigationBar_shadowColor, 0);
        int bgShadowWidth = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_shadowWidth, 0);
        int bgColor = typedArray.getColor(R.styleable.AUINavigationBar_aui_navigationBar_backgroundColor, 0);
        mBackground = new NavBgDrawable(bgColor, bgShadowColor, bgShadowWidth);
        llItems.setBackground(mBackground);

        // 间距
        int contentPadding = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_contentPadding, 0);
        int contentPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_contentPaddingStart, 0);
        int contentPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_contentPaddingTop, 0);
        int contentPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_contentPaddingEnd, 0);
        int contentPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBar_aui_navigationBar_contentPaddingBottom, 0);
        if(contentPadding > 0){
            contentPaddingTopWithoutOffset = contentPadding + bgShadowWidth;
            llItems.setPadding(contentPadding, contentPaddingTopWithoutOffset, contentPadding, contentPadding);
        }else{
            contentPaddingTopWithoutOffset = contentPaddingTop + bgShadowWidth;
            llItems.setPadding(contentPaddingStart, contentPaddingTopWithoutOffset, contentPaddingEnd, contentPaddingBottom);
        }

        typedArray.recycle();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_navigation_bar_layout, this);
        llItems = findViewById(R.id.llItems);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (onLayoutRunnable != null) {
            onLayoutRunnable.run();
        }
    }

    private void parseAndCreateMenu(int menuResId) {

        XmlResourceParser parser = getContext().getResources().getLayout(menuResId);
        AttributeSet attrs = Xml.asAttributeSet(parser);

        try {
            int eventType = parser.getEventType();
            String tagName;
            boolean lookingForEndOfUnknownTag = false;
            String unknownTagName = null;
            // This loop will skip to the menu start tag
            do {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equals("menu")) {
                        // Go to next tag
                        eventType = parser.next();
                        break;
                    }

                    throw new RuntimeException("Expecting menu, got " + tagName);
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);

            boolean reachedEndOfMenu = false;
            int index = 0;
            while (!reachedEndOfMenu) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (lookingForEndOfUnknownTag) {
                            break;
                        }

                        tagName = parser.getName();
                        if (tagName.equals("item")) {
                            createMenuItemView(index, attrs);
                            index ++;
                        } else {
                            lookingForEndOfUnknownTag = true;
                            unknownTagName = tagName;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                            lookingForEndOfUnknownTag = false;
                            unknownTagName = null;
                        }
                        if (tagName.equals("menu")) {
                            reachedEndOfMenu = true;
                        }
                        break;

                    case XmlPullParser.END_DOCUMENT:
                        throw new RuntimeException("Unexpected end of document");
                }

                eventType = parser.next();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void createMenuItemView(int index, AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AUINavigationBarMenu, 0, menuDefStyle);
        // 布局id
        int id = typedArray.getResourceId(R.styleable.AUINavigationBarMenu_android_id, 0);
        int layout = typedArray.getResourceId(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_layout, 0);
        View itemView = View.inflate(getContext(), layout, null);
        itemView.setId(id);

        // 浮动
        boolean isFloating = typedArray.getBoolean(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_isFloating, false);
        int floatingHeight = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_floatingHeight, 0);
        int floatingOffsetStart = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_floatingOffsetStart, 0);
        int floatingOffsetTop = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_floatingOffsetTop, 0);
        int floatingOffsetEnd = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_floatingOffsetEnd, 0);
        int floatingOffsetBottom = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_floatingOffsetBottom, 0);
        if (isFloating) {
            onLayoutRunnable = () -> {
                mBackground.floatingHeight = floatingHeight;
                mBackground.floatingStart = (int) itemView.getX() + floatingOffsetStart;
                mBackground.floatingEnd = (int) itemView.getX() + itemView.getWidth() - floatingOffsetEnd;
                llItems.setBackground(mBackground);
                llItems.setPadding(llItems.getPaddingLeft(), contentPaddingTopWithoutOffset + floatingOffsetTop, llItems.getPaddingEnd(), llItems.getPaddingBottom());
            };
        }

        // 图标
        ImageView ivIcon = itemView.findViewById(R.id.ivIcon);
        Drawable icon = typedArray.getDrawable(R.styleable.AUINavigationBarMenu_android_icon);
        int iconTint = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconTint, 0);
        int iconTintActivated = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconTintActivated, 0);
        int iconWidth = typedArray.getLayoutDimension(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconWidth, 0);
        int iconHeight = typedArray.getLayoutDimension(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconHeight, 0);
        int iconPadding = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconPadding, 0);
        int iconPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconPaddingStart, 0);
        int iconPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconPaddingTop, 0);
        int iconPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconPaddingEnd, 0);
        int iconPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_iconPaddingBottom, 0);
        ivIcon.setImageDrawable(icon);
        if (!isFloating) {
            ivIcon.setImageTintList(new ColorStateList(new int[][]{{android.R.attr.state_activated}, {}}, new int[]{iconTintActivated, iconTint}));
        }
        ViewGroup.LayoutParams iconParams = ivIcon.getLayoutParams();
        iconParams.width = iconWidth;
        iconParams.height = iconHeight;
        ivIcon.setLayoutParams(iconParams);
        if (iconPadding > 0) {
            ivIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        } else {
            ivIcon.setPadding(iconPaddingStart, iconPaddingTop, iconPaddingEnd, iconPaddingBottom);
        }

        // 标题
        TextView tvTitle = itemView.findViewById(R.id.tvTitle);
        String title = typedArray.getString(R.styleable.AUINavigationBarMenu_android_title);
        int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_titleTextSize, 0);
        int titleTextColor = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_titleTextColor, 0);
        int titleTextColorActivated = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_titleTextColorActivated, 0);
        tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        tvTitle.setTextColor(new ColorStateList(new int[][]{{android.R.attr.state_activated}, {}}, new int[]{titleTextColorActivated, titleTextColor}));

        // 小红点
        TextView tvDot = itemView.findViewById(R.id.tvDot);
        int dotWidth = typedArray.getLayoutDimension(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotWidth, 0);
        int dotHeight = typedArray.getLayoutDimension(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotHeight, 0);
        int dotText = typedArray.getInt(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotText, 0);
        int dotTextSize = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotTextSize, 0);
        int dotPadding = typedArray.getDimensionPixelSize(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotPadding, 0);
        int dotTextColor = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotTextColor, 0);
        int dotBackgroundColor = typedArray.getColor(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_dotBackgroundColor, 0);
        ViewGroup.LayoutParams dotParams = tvDot.getLayoutParams();
        dotParams.width = dotWidth;
        dotParams.height = dotHeight;
        tvDot.setLayoutParams(dotParams);
        tvDot.setTextSize(TypedValue.COMPLEX_UNIT_PX, dotTextSize);
        tvDot.setTextColor(dotTextColor);
        tvDot.setVisibility(dotText <= 0 ? View.GONE : View.VISIBLE);
        tvDot.setText(String.format(Locale.US, "%d", Math.min(dotText, 99)));
        GradientDrawable dotBackgroundDrawable = new GradientDrawable();
        dotBackgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        dotBackgroundDrawable.setCornerRadius(Float.MAX_VALUE);
        dotBackgroundDrawable.setColor(dotBackgroundColor);
        tvDot.setBackground(dotBackgroundDrawable);
        tvDot.setPadding(dotPadding, dotPadding, dotPadding, dotPadding);


        // 缩放
        float scale = typedArray.getFloat(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_scale, 1.0f);
        itemView.setScaleX(scale);
        itemView.setScaleY(scale);
        // 占比
        int weight = typedArray.getInt(R.styleable.AUINavigationBarMenu_aui_navigationBarMenu_weight, 1);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.weight = weight;
        if (isFloating) {
            itemParams.bottomMargin = floatingOffsetBottom;
        }
        llItems.addView(itemView, itemParams);
        typedArray.recycle();

        // 点击事件
        itemView.setOnClickListener(v -> {
            changeItemActivated(itemView);
        });
        if(index == 0){
            changeItemActivated(itemView);
        }
    }

    private void changeItemActivated(View itemView) {
        boolean activated = !itemView.isActivated();
        if (activated) {
            if (itemViewActivated != null && itemViewActivated != itemView) {
                itemViewActivated.setActivated(false);
            }
        } else {
            if (itemViewActivated == itemView) {
                return;
            }
        }
        itemViewActivated = itemView;
        itemView.setActivated(activated);
    }

    private static class NavBgDrawable extends Drawable {

        private int floatingHeight = 0;
        private int floatingStart = 0;
        private int floatingEnd = 0;

        private final int color;
        private final int shadowColor;
        private final int shadowWidth;

        private final Paint drawPaint;

        public NavBgDrawable(int color, int shadowColor, int shadowWidth) {
            this.color = color;
            this.shadowColor = shadowColor;
            this.shadowWidth = shadowWidth;
            drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            drawPaint.setColor(color);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();

            // 画背景
            drawPaint.setShadowLayer(25f, -5f, 2f, shadowColor);
            canvas.drawRect(new Rect(bounds.left, floatingHeight + shadowWidth, floatingStart, bounds.bottom), drawPaint);
            canvas.drawRect(new Rect(floatingEnd, floatingHeight + shadowWidth, bounds.right, bounds.bottom), drawPaint);

            Path path = new Path();
            path.moveTo(floatingStart, floatingHeight + shadowWidth);
            path.quadTo(floatingStart + (floatingEnd - floatingStart) * 1.0f / 2, 0,
                    floatingEnd, floatingHeight + shadowWidth);
            canvas.drawPath(path, drawPaint);

            drawPaint.clearShadowLayer();
            canvas.drawRect(new Rect(bounds.left, floatingHeight + shadowWidth, bounds.right, bounds.bottom), drawPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            final int oldAlpha = drawPaint.getAlpha();
            if (alpha != oldAlpha) {
                drawPaint.setAlpha(alpha);
                invalidateSelf();
            }
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            drawPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }


}
