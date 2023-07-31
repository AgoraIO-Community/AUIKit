package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import org.xmlpull.v1.XmlPullParser;

import java.util.Locale;

import io.agora.auikit.ui.R;

public class AUITabLayout extends FrameLayout implements TabLayout.OnTabSelectedListener {

    private View vDivider;
    private TabLayout tabLayout;
    private int menuDefStyle;
    private OnTabSelectChangeListener onTabSelectChangeListener;

    public AUITabLayout(@NonNull Context context) {
        this(context, null);
    }

    public AUITabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUITabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

        int themeId = R.style.AUITabLayout;
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.aui_tabLayout_appearance, outValue, true)) {
            themeId = outValue.resourceId;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUITabLayout, defStyleAttr, themeId);

        // 分隔线
        Drawable divider = typedArray.getDrawable(R.styleable.AUITabLayout_aui_tabLayout_divider);
        int dividerHeight = typedArray.getLayoutDimension(R.styleable.AUITabLayout_aui_tabLayout_dividerHeight, 0);
        ViewGroup.LayoutParams dividerParams = vDivider.getLayoutParams();
        dividerParams.height = dividerHeight;
        vDivider.setLayoutParams(dividerParams);
        vDivider.setBackground(divider);

        // Tab显示模式
        int tabMode = typedArray.getInt(R.styleable.AUITabLayout_aui_tabLayout_tabMode, TabLayout.MODE_FIXED);
        int tabGravity = typedArray.getInt(R.styleable.AUITabLayout_aui_tabLayout_tabGravity, TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(tabMode);
        tabLayout.setTabGravity(tabGravity);

        // 指示器
        Drawable indicator = typedArray.getDrawable(R.styleable.AUITabLayout_aui_tabLayout_indicator);
        indicator.setBounds(0, 0, indicator.getIntrinsicWidth(), indicator.getIntrinsicHeight());
        boolean indicatorFullWidth = typedArray.getBoolean(R.styleable.AUITabLayout_aui_tabLayout_indicatorFullWidth, true);
        int indicatorGravity = typedArray.getInt(R.styleable.AUITabLayout_aui_tabLayout_indicatorGravity, TabLayout.INDICATOR_GRAVITY_BOTTOM);
        tabLayout.setSelectedTabIndicator(indicator);
        tabLayout.setTabIndicatorFullWidth(indicatorFullWidth);
        tabLayout.setSelectedTabIndicatorGravity(indicatorGravity);

        // Menu配置
        int menu = typedArray.getResourceId(R.styleable.AUITabLayout_aui_tabLayout_menu, 0);
        menuDefStyle = typedArray.getResourceId(R.styleable.AUITabLayout_aui_tabLayout_menu_appearance, R.style.AUITabLayoutMenu);
        parseAndCreateMenu(menu);

        typedArray.recycle();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.aui_tab_layout_layout, this);
        vDivider = view.findViewById(R.id.vDivider);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);
    }

    public void setOnTabSelectChangeListener(OnTabSelectChangeListener listener){
        onTabSelectChangeListener = listener;
    }

    public void selectTab(int index){
        tabLayout.selectTab(tabLayout.getTabAt(index));
    }
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(onTabSelectChangeListener != null){
            onTabSelectChangeListener.onTabSelectChanged(tab.getPosition(), tab.getId(), true);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if(onTabSelectChangeListener != null){
            onTabSelectChangeListener.onTabSelectChanged(tab.getPosition(), tab.getId(), false);
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void addOnTabSelectedListener(TabLayout.OnTabSelectedListener onTabSelectedListener) {
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    public void removeAllTabs() {
        tabLayout.removeAllTabs();
    }

    public TabLayout.Tab newTab() {
        return createMenuItemTab(null);
    }

    public void addTab(TabLayout.Tab tab) {
        tabLayout.addTab(tab);
    }

    public int getSelectedTabPosition() {
        return tabLayout.getSelectedTabPosition();
    }

    public TabLayout.Tab getTabAt(int position) {
        return tabLayout.getTabAt(position);
    }

    public void setTabDotNum(int index, int num) {
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if (tab != null && tab.getCustomView() != null) {
            TextView tvDot = tab.getCustomView().findViewById(R.id.tvDot);
            if (tvDot != null) {
                tvDot.setVisibility(num <= 0 ? View.GONE : View.VISIBLE);
                tvDot.setText(String.format(Locale.US, "%d", Math.min(num, 99)));
            }
        }
    }

    public interface OnTabSelectChangeListener{
        void onTabSelectChanged(int index, int menuId, boolean selected);
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
                            TabLayout.Tab tab = createMenuItemTab(attrs);
                            tabLayout.addTab(tab, index);
                            index++;
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

    private TabLayout.Tab createMenuItemTab(AttributeSet attrs) {

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AUITabLayoutMenu, 0, menuDefStyle);
        // 布局id
        int id = typedArray.getResourceId(R.styleable.AUITabLayoutMenu_android_id, 0);
        int layout = typedArray.getResourceId(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_layout, 0);
        TabLayout.Tab tab = tabLayout.newTab()
                .setId(id)
                .setCustomView(layout);

        // 是否选中
        boolean isSelect = typedArray.getBoolean(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_selected, false);
        if(isSelect){
            tab.select();
        }

        // 图标
        ImageView ivIcon = tab.getCustomView().findViewById(android.R.id.icon);
        Drawable icon = typedArray.getDrawable(R.styleable.AUITabLayoutMenu_android_icon);
        int iconTint = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconTint, 0);
        int iconTintSelected = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconTintSelected, 0);
        int iconWidth = typedArray.getLayoutDimension(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconWidth, 0);
        int iconHeight = typedArray.getLayoutDimension(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconHeight, 0);
        int iconPadding = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconPadding, 0);
        int iconPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconPaddingStart, 0);
        int iconPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconPaddingTop, 0);
        int iconPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconPaddingEnd, 0);
        int iconPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_iconPaddingBottom, 0);
        ivIcon.setVisibility(icon != null ? View.VISIBLE: View.GONE);
        ivIcon.setImageDrawable(icon);
        ivIcon.setImageTintList(new ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{iconTintSelected, iconTint}));
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
        TextView tvTitle = tab.getCustomView().findViewById(android.R.id.text1);
        String title = typedArray.getString(R.styleable.AUITabLayoutMenu_android_title);
        int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_titleTextSize, 0);
        int titleTextColor = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_titleTextColor, 0);
        int titleTextColorSelected = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_titleTextColorSelected, 0);
        tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        tvTitle.setTextColor(new ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{titleTextColorSelected, titleTextColor}));

        // 小红点
        TextView tvDot = tab.getCustomView().findViewById(R.id.tvDot);
        int dotWidth = typedArray.getLayoutDimension(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotWidth, 0);
        int dotHeight = typedArray.getLayoutDimension(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotHeight, 0);
        int dotText = typedArray.getInt(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotText, 0);
        int dotTextSize = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotTextSize, 0);
        int dotPadding = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotPadding, 0);
        int dotPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotPaddingStart, 0);
        int dotPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotPaddingTop, 0);
        int dotPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotPaddingEnd, 0);
        int dotPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotPaddingBottom, 0);
        int dotTextColor = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotTextColor, 0);
        int dotTextColorSelected = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotTextColorSelected, 0);
        int dotBackgroundColor = typedArray.getColor(R.styleable.AUITabLayoutMenu_aui_tabLayoutMenu_dotBackgroundColor, 0);
        ViewGroup.LayoutParams dotParams = tvDot.getLayoutParams();
        dotParams.width = dotWidth;
        dotParams.height = dotHeight;
        tvDot.setLayoutParams(dotParams);
        tvDot.setTextSize(TypedValue.COMPLEX_UNIT_PX, dotTextSize);
        tvDot.setTextColor(new ColorStateList(new int[][]{{android.R.attr.state_selected}, {}}, new int[]{dotTextColorSelected, dotTextColor}));
        tvDot.setVisibility(dotText <= 0 ? View.GONE : View.VISIBLE);
        tvDot.setText(String.format(Locale.US, "%d", Math.min(dotText, 99)));
        GradientDrawable dotBackgroundDrawable = new GradientDrawable();
        dotBackgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        dotBackgroundDrawable.setCornerRadius(Float.MAX_VALUE);
        dotBackgroundDrawable.setColor(dotBackgroundColor);
        tvDot.setBackground(dotBackgroundDrawable);
        if(dotPadding > 0){
            tvDot.setPadding(dotPadding, dotPadding, dotPadding, dotPadding);
        }else{
            tvDot.setPadding(dotPaddingStart, dotPaddingTop, dotPaddingEnd, dotPaddingBottom);
        }

        typedArray.recycle();

        return tab;
    }

}
