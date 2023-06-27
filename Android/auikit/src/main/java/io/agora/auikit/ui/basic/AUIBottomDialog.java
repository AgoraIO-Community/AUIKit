package io.agora.auikit.ui.basic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.auikit.R;

public class AUIBottomDialog extends BottomSheetDialog {

    private View contentView;
    private TextView tvTitle;
    private FrameLayout customContainer;
    private ViewPager2 viewPager;
    private TabLayout viewPagerIndicator;
    private int itemLayoutId = R.layout.aui_bottom_dialog_item;
    private int orientation;
    private final Drawable dividerDrawable;
    private int listSpan = 1;
    private boolean listSingleCheck = true;
    private InnerGroupAdapter groupAdapter;
    private int[] lastCheckItemIds;
    private final int pageIndicatorRes;
    private OnItemClickListener onItemClickListener;
    private OnItemCheckChangeListener onItemCheckChangeListener;
    private int menuDefStyle;

    private static int getDefaultTheme(Context context, int themeId) {
        if(themeId == 0){
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(R.attr.aui_bottomDialog_appearance, outValue, true)) {
                themeId = outValue.resourceId;
            }else{
                themeId = R.style.AUIBottomDialog;
            }
        }
        return themeId;
    }

    public AUIBottomDialog(@NonNull Context context) {
        this(context, 0);
    }

    public AUIBottomDialog(@NonNull Context context, int theme) {
        super(context, getDefaultTheme(context, theme));
        initView();

        TypedArray typedArray = getContext().obtainStyledAttributes(null, R.styleable.AUIBottomDialog, 0, theme);
        // 背景
        int backgroundColor = typedArray.getColor(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundColor, Color.WHITE);
        int backgroundCornerRadiusLeftTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundCornerRadiusLeftTop, 0);
        int backgroundCornerRadiusLeftBottom = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundCornerRadiusLeftBottom, 0);
        int backgroundCornerRadiusRightTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundCornerRadiusRightTop, 0);
        int backgroundCornerRadiusRightBottom = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundCornerRadiusRightBottom, 0);
        int backgroundShadowColor = typedArray.getColor(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundShadowColor, Color.GRAY);
        int backgroundShadowRadius = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundShadowRadius, 0);
        int backgroundShadowOffsetX = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundShadowOffsetX, 0);
        int backgroundShadowOffsetY = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundShadowOffsetY, 0);
        int backgroundOffsetTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundOffsetTop, 0);


        Drawable background = new AUIShadowRectDrawable()
                .setColor(backgroundColor)
                .setCornerRadii(new float[]{
                        backgroundCornerRadiusLeftTop, backgroundCornerRadiusLeftTop,
                        backgroundCornerRadiusRightTop, backgroundCornerRadiusRightTop,
                        backgroundCornerRadiusLeftBottom, backgroundCornerRadiusLeftBottom,
                        backgroundCornerRadiusRightBottom, backgroundCornerRadiusRightBottom
                })
                .setShadowColor(backgroundShadowColor)
                .setShadowRadius(backgroundShadowRadius)
                .setShadowOffsetX(backgroundShadowOffsetX)
                .setShadowOffsetY(backgroundShadowOffsetY)
                .setOffsetTop(backgroundOffsetTop);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                LayerDrawable layerBackground = new LayerDrawable(new Drawable[]{
                        background,
                        null
                });
                int backgroundIndicatorTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_backgroundIndicatorTop, 0);
                Drawable backgroundIndicator = AppCompatResources.getDrawable(getContext(), R.drawable.aui_bottom_dialog_bg_indicator);
                if(backgroundIndicator != null){
                    layerBackground.setDrawable(1, backgroundIndicator);
                    layerBackground.setLayerGravity(1, Gravity.CENTER_HORIZONTAL);
                    layerBackground.setLayerInsetTop(1, backgroundIndicatorTop + backgroundOffsetTop);
                    layerBackground.setLayerSize(1, backgroundIndicator.getIntrinsicWidth(), backgroundIndicator.getIntrinsicHeight());
                }
                background = layerBackground;
            } catch (Exception e) {
                // do nothing
            }
        }

        contentView.setBackground(background);

        // 间距/大小
        int contentPadding = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentPadding, 0);
        int contentPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentPaddingStart, 0);
        int contentPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentPaddingTop, 0);
        int contentPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentPaddingEnd, 0);
        int contentPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentPaddingBottom, 0);
        int contentMinHeight = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_contentMinHeight, 0);
        contentView.setMinimumHeight(contentMinHeight);
        if (contentPadding > 0) {
            contentView.setPadding(contentPadding, contentPadding, contentPadding, contentPadding);
        } else {
            contentView.setPadding(contentPaddingStart, contentPaddingTop, contentPaddingEnd, contentPaddingBottom);
        }


        // 标题
        int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titleTextSize, 0);
        int titleTextColor = typedArray.getColor(R.styleable.AUIBottomDialog_aui_bottomDialog_titleTextColor, Color.BLACK);
        int titlePadding = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titlePadding, 0);
        int titlePaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titlePaddingStart, 0);
        int titlePaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titlePaddingTop, 0);
        int titlePaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titlePaddingEnd, 0);
        int titlePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUIBottomDialog_aui_bottomDialog_titlePaddingBottom, 0);
        tvTitle.setVisibility(View.GONE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        tvTitle.setTextColor(titleTextColor);
        if (titlePadding > 0) {
            tvTitle.setPadding(titlePadding, titlePadding, titlePadding, titlePadding);
        } else {
            tvTitle.setPadding(titlePaddingStart, titlePaddingTop, titlePaddingEnd, titlePaddingBottom);
        }

        // 分页
        pageIndicatorRes = typedArray.getResourceId(R.styleable.AUIBottomDialog_aui_bottomDialog_pageIndicator, View.NO_ID);
        if (pageIndicatorRes != View.NO_ID) {
            Drawable indicatorDrawable = AppCompatResources.getDrawable(getContext(), pageIndicatorRes);
            if (indicatorDrawable != null) {
                viewPagerIndicator.setVisibility(View.VISIBLE);
                int pageHeight = indicatorDrawable.getIntrinsicHeight();
                ViewGroup.LayoutParams pageParams = viewPagerIndicator.getLayoutParams();
                pageParams.height = pageHeight;
                viewPagerIndicator.setLayoutParams(pageParams);
                viewPagerIndicator.setSelectedTabIndicator(null);
                viewPagerIndicator.setTabIconTint(null);
            } else {
                viewPagerIndicator.setVisibility(View.GONE);
            }
        } else {
            viewPagerIndicator.setVisibility(View.GONE);
        }

        // 列表
        orientation = typedArray.getInt(R.styleable.AUIBottomDialog_aui_bottomDialog_listOrientation, LinearLayout.VERTICAL);
        itemLayoutId = typedArray.getResourceId(R.styleable.AUIBottomDialog_aui_bottomDialog_listItem, itemLayoutId);
        dividerDrawable = typedArray.getDrawable(R.styleable.AUIBottomDialog_aui_bottomDialog_listDivider);
        listSpan = typedArray.getInt(R.styleable.AUIBottomDialog_aui_bottomDialog_listSpan, listSpan);
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        viewPager.setLayoutParams(layoutParams);
        viewPagerIndicator.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

        // menu样式
        menuDefStyle = typedArray.getResourceId(R.styleable.AUIBottomDialog_aui_bottomDialog_menu_appearance, R.style.AUIBottomDialogMenu);

        typedArray.recycle();
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupAdapter.notifyDataSetChanged();
        viewPagerIndicator.setVisibility(groupAdapter.groupInfoList.size() < 2 ? View.GONE : View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        new TabLayoutMediator(viewPagerIndicator, viewPager, (tab, position) -> tab.setIcon(AppCompatResources.getDrawable(getContext(), pageIndicatorRes))).attach();
    }

    private void initView() {
        setContentView(R.layout.aui_bottom_dialog_layout);
        contentView = findViewById(R.id.contentRoot);
        tvTitle = findViewById(R.id.tvTitle);
        customContainer = findViewById(R.id.customContainer);
        viewPager = findViewById(R.id.viewPager);
        viewPagerIndicator = findViewById(R.id.viewPagerIndicator);
        groupAdapter = new InnerGroupAdapter();
        viewPager.setAdapter(groupAdapter);
        viewPager.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        try {
            Field recyclerViewField = viewPager.getClass().getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);
            if(recyclerView != null){
                recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            }

        } catch (Exception e) {
            // do nothing
        }
    }

    public void setCustomView(@NonNull View view) {
        customContainer.removeAllViews();
        customContainer.addView(view);
    }

    public void setCustomView(@LayoutRes int viewResId) {
        customContainer.removeAllViews();
        View.inflate(getContext(), viewResId, customContainer);
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
        tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTitle(@StringRes int titleId) {
        tvTitle.setText(titleId);
        tvTitle.setVisibility(titleId == View.NO_ID ? View.GONE : View.VISIBLE);
    }

    public void setBackground(@Nullable Drawable background) {
        contentView.setBackground(background);
    }

    public void setMenu(@MenuRes int menuResId) {
        groupAdapter.groupInfoList.addAll(parseMenu(menuResId));
    }

    public void removeMenu(@IdRes int menuId){
        for (GroupInfo groupInfo : groupAdapter.groupInfoList) {
            Iterator<ItemInfo> iterator = groupInfo.itemInfoList.iterator();
            while (iterator.hasNext()){
                ItemInfo next = iterator.next();
                if(next.id == menuId){
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public void setItemOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setCheckItemIds(int[] lastCheckItemIds) {
        this.lastCheckItemIds = lastCheckItemIds;
    }

    public void setListSingleCheck(boolean listSingleCheck) {
        this.listSingleCheck = listSingleCheck;
    }

    public void setItemLayoutId(int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemCheckChangeListener(OnItemCheckChangeListener listener) {
        this.onItemCheckChangeListener = listener;
    }

    public interface OnItemCheckChangeListener {
        void onItemCheckChanged(int groupId, int itemId, boolean isChecked);
    }

    public interface OnItemClickListener {
        void onItemClicked(int itemId);
    }

    private List<GroupInfo> parseMenu(int menuResId) {
        XmlResourceParser parser = getContext().getResources().getLayout(menuResId);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        List<GroupInfo> groupInfoList = new ArrayList<>();
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

            GroupInfo groupInfo = new GroupInfo();
            while (!reachedEndOfMenu) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (lookingForEndOfUnknownTag) {
                            break;
                        }

                        tagName = parser.getName();
                        if (tagName.equals("group")) {
                            groupInfo = new GroupInfo();

                            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AUIBottomDialogMenu, 0, 0);
                            groupInfo.id = typedArray.getResourceId(R.styleable.AUIBottomDialogMenu_android_id, 0);
                            typedArray.recycle();
                        } else if (tagName.equals("item")) {
                            groupInfo.itemInfoList.add(createItemInfo(attrs));
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
                        if (tagName.equals("group")) {
                            groupInfoList.add(groupInfo);
                        } else if (tagName.equals("menu")) {
                            reachedEndOfMenu = true;
                            if (!groupInfoList.contains(groupInfo)) {
                                groupInfoList.add(groupInfo);
                            }
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
        return groupInfoList;
    }

    private ItemInfo createItemInfo(AttributeSet attrs) {
        ItemInfo itemInfo = new ItemInfo();

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AUIBottomDialogMenu, 0, menuDefStyle);
        itemInfo.id = typedArray.getResourceId(R.styleable.AUIBottomDialogMenu_android_id, 0);
        itemInfo.icon = typedArray.getDrawable(R.styleable.AUIBottomDialogMenu_android_icon);
        itemInfo.iconTint = typedArray.getColorStateList(R.styleable.AUIBottomDialogMenu_android_iconTint);
        itemInfo.title = typedArray.getString(R.styleable.AUIBottomDialogMenu_android_title);
        itemInfo.titleTextColor = typedArray.getColorStateList(R.styleable.AUIBottomDialogMenu_android_titleTextColor);

        typedArray.recycle();

        return itemInfo;
    }


    private static class GroupInfo {
        public int id;
        public final List<ItemInfo> itemInfoList = new ArrayList<>();
    }

    private static class ItemInfo {
        public int id;
        public Drawable icon;
        public ColorStateList iconTint;
        public String title;
        public ColorStateList titleTextColor;
    }

    private class InnerGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<GroupInfo> groupInfoList = new ArrayList<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView itemView = new RecyclerView(parent.getContext());
            itemView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new RecyclerView.ViewHolder(itemView) {
            };
        }

        @SuppressLint("WrongConstant")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RecyclerView recyclerView = (RecyclerView) holder.itemView;
            recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), listSpan, orientation, false));
            GroupInfo groupInfo = groupInfoList.get(position);
            AUIDividerItemDecoration decor = new AUIDividerItemDecoration(recyclerView.getContext(), orientation);
            decor.setDrawable(dividerDrawable);
            recyclerView.addItemDecoration(decor);
            InnerItemAdapter adapter = new InnerItemAdapter(position);
            adapter.itemInfoList.addAll(groupInfo.itemInfoList);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public int getItemCount() {
            return groupInfoList.size();
        }
    }

    private class InnerItemAdapter extends RecyclerView.Adapter<InnerItemViewHolder> {

        private final List<ItemInfo> itemInfoList = new ArrayList<>();
        private View lastCheckedView;
        private int lastCheckedId;

        private final int pageIndex;

        InnerItemAdapter(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        @NonNull
        @Override
        public InnerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new InnerItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull InnerItemViewHolder holder, int position) {
            ItemInfo itemInfo = itemInfoList.get(position);
            if (holder.tvTitle != null) {
                holder.tvTitle.setText(itemInfo.title);
                holder.tvTitle.setTextColor(itemInfo.titleTextColor);
            }
            if (holder.ivIcon != null) {
                if (itemInfo.icon != null) {
                    holder.ivIcon.setVisibility(View.VISIBLE);
                    holder.ivIcon.setImageDrawable(itemInfo.icon);
                    holder.ivIcon.setImageTintList(itemInfo.iconTint);
                } else {
                    holder.ivIcon.setVisibility(View.GONE);
                }
            }

            boolean activated = false;
            if (lastCheckItemIds != null) {
                for (int id : lastCheckItemIds) {
                    if (id == itemInfo.id) {
                        activated = true;
                        break;
                    }
                }
            }
            holder.itemView.setActivated(activated);
            if (activated) {
                lastCheckedId = itemInfo.id;
                lastCheckedView = holder.itemView;
            }
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClicked(itemInfo.id);
                }
                if (listSingleCheck) {
                    boolean activate = !v.isActivated();
                    if (lastCheckedView != v && lastCheckedView != null) {
                        lastCheckedView.setActivated(false);
                        if (onItemCheckChangeListener != null) {
                            onItemCheckChangeListener.onItemCheckChanged(groupAdapter.groupInfoList.get(pageIndex).id, lastCheckedId, false);
                        }
                    }
                    if (activate) {
                        v.setActivated(true);
                        lastCheckedView = v;
                        lastCheckedId = itemInfo.id;
                        if (onItemCheckChangeListener != null) {
                            onItemCheckChangeListener.onItemCheckChanged(groupAdapter.groupInfoList.get(pageIndex).id, itemInfo.id, true);
                        }
                    }
                } else {
                    v.setActivated(!v.isActivated());
                    if (onItemCheckChangeListener != null) {
                        onItemCheckChangeListener.onItemCheckChanged(groupAdapter.groupInfoList.get(pageIndex).id, itemInfo.id, v.isActivated());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return itemInfoList.size();
        }
    }

    private static class InnerItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final ImageView ivIcon;

        public InnerItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }


}
