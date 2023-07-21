# AUIKit UI

*[English](AUIKit-UI.md) | 中文*

AUIKit UI提供了基础ui组件和功能ui组件，基于这两套ui组件，开发者可以快速搭建一个统一ui风格的场景化应用。

## 目录结构
```
功能ui组件：
auikit-ui/src/main/java/io/agora/auikit/ui
├── micseats                                            麦位ui组件
│       ├── IMicSeatsView.java                          麦位ui控制接口
│       ├── IMicSeatItemView.java                       麦位Item ui控制接口
│       ├── IMicSeatDialogView.java                     麦位弹窗 ui控制接口
│       ├── impl
│       │       ├── AUIMicSeatDialogView.java           麦位弹窗ui控件
│       │       ├── AUIMicSeatItemView.java             麦位Item ui控件
│       │       └── AUIMicSeatsView.java                麦位ui控件
│       └── res                                     
│           ├── drawable                                麦位图片资源
│           ├── drawable-xxhdpi                         麦位图片资源
│           ├── layout                                  麦位布局资源
│           ├── values                              
│           │       ├── attrs.xml                       麦位自定义属性
│           │       ├── styles.xml                      麦位默认样式
│           │       └── values.xml                      麦位英文方案
│           └── values-zh
│               └── values.xml                          麦位中文方案
│
├── jukebox                                             点唱ui组件
│       ├── IAUIJukeboxView.java                        点唱ui控制接口
│       ├── IAUIJukeboxChosenItemView.java              已唱item ui控制接口
│       ├── impl
│       │       ├── AUIJukeboxChooseItemView.java       点唱选歌item ui控件
│       │       ├── AUIJukeboxChooseView.java           点唱选歌ui控件
│       │       ├── AUIJukeboxChosenItemView.java       已唱item ui控件
│       │       ├── AUIJukeboxChosenView.java           已唱ui控件
│       │       └── AUIJukeboxView.java                 点唱ui控件
│       └── res
│           ├── color                                   点唱颜色资源
│           ├── drawable                                点唱图片资源
│           ├── drawable-xxhdpi                         点唱图片资源
│           ├── layout                                  点唱布局资源
│           ├── values
│           │       ├── attrs.xml                       点唱自定义属性
│           │       ├── styles.xml                      点唱默认模式
│           │       └── values.xml                      点唱英文方案
│           └── values-zh
│               └── values.xml                          点唱中文方案
│
└── musicplayer                                         播放ui组件
    ├── IMusicPlayerView.java                           播放ui控制接口
    ├── impl
    │       ├── AUIMusicPlayerControllerDialogView.java 播放控制弹窗ui控件
    │       ├── AUIMusicPlayerEffectItemView.java       播放音效ui控件
    │       ├── AUIMusicPlayerGradeView.java            播放打分ui控件
    │       ├── AUIMusicPlayerPresetDialogView.java     播放预设ui控件
    │       └── AUIMusicPlayerView.java                 播放ui控件
    ├── listener
    │       ├── IMusicPlayerActionListener.java         播放事件回调监听者
    │       └── IMusicPlayerEffectActionListener.java   播放音效事件回调监听者
    ├── res
    │       ├── drawable                                播放图片资源
    │       ├── drawable-xxhdpi                         播放图片资源
    │       ├── layout                                  播放布局资源
    │       ├── mipmap                                  播放图片资源
    │       ├── values
    │       │       ├── attrs.xml                       播放自定义属性
    │       │       ├── styles.xml                      播放默认样式
    │       │       └── values.xml                      播放英文文案
    │       └── values-zh
    │           └── values.xml                          播放中文文案
    └── utils                                           播放相关工具类


基础ui组件：
auikit-ui/src/main/java/io/agora/auikit/ui/basic
├── AUIAlertDialog.java                                 公用弹窗
├── AUIBottomDialog.java                                公用底部弹窗
├── AUIButton.java                                      公用按钮
├── AUIDividers.java                                    公用分隔线
├── AUIEditText.java                                    公用输入框
├── AUINavigationBar.java                               公用底部导航栏
├── AUITabLayout.java                                   公用Tab分栏
├── AUITitleBar.java                                    公用标题栏
└── res
    ├── drawable                                        公用图片资源
    ├── drawable-xxhdpi                                 公用图片资源
    ├── layout                                          公用布局资源
    ├── menu                                            公用菜单资源
    └── values
            ├── attrs_aui_alert_dialog.xml              弹窗自定义属性
            ├── attrs_aui_bottom_dialog.xml             底部弹窗自定义属性
            ├── attrs_aui_button.xml                    底部弹窗自定义属性
            ├── attrs_aui_divider.xml                   分隔线自定义属性
            ├── attrs_aui_edittext.xml                  输入框自定义属性
            ├── attrs_aui_navigation_bar.xml            底部导航栏自定义属性
            ├── attrs_aui_tab_layout.xml                Tab分栏自定义属性
            ├── attrs_aui_title_bar.xml                 标题栏自定义属性
            ├── themes.xml                              亮主题
            └── themes_dark.xml                         暗主题
```
## 主题

### <span>**`介绍`**</span>

这里的主题和Android自带的主题是一个概念，AUIKit的主题是基于Material主题上拓展的。
通过使用主题，可以实现全局统一的ui修改，也可以实现动态换肤功能。

* 对于**基础ui组件**，AUIKit提供了两套基础ui主题，如下
亮主题(默认) -> [Theme.AUIKit.Basic](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/themes.xml)
暗主题 -> [Theme.AUIKit.Basic.Dark](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/themes_dark.xml)

开发者可以基于这两套主题定义自己的主题，然后在自己主题里通过修改组件的appearance配置来修改基础ui组件默认样式。

* 对于**功能ui组件**，AUIKit提供了一套功能ui主题，即[Theme.AUIKit](../auikit-ui/src/main/res/values/themes.xml)

开发者同样可以基于这套主题定义自己的主题，然后在自己主题里通过修改组件的appearance配置来修改功能ui组件默认样式。

### <span>**`主题的使用`**</span>
下面以[Theme.AUIKit](../auikit-ui/src/main/res/values/themes.xml)主题的使用为例说明主题如何使用。

- 在项目里集成[auikit](../auikit-ui)源码
- 在app模块的src/main/res/value/themes.xml里定义
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 自己的主题，继承Theme.AUIKit -->
    <style name="Theme.MyTheme" parent="Theme.AUIKit">
        <!-- 可自定义组件样式 -->
    </style>
</resources>
```
- 在aap模块里的AndroidManifest.xml里配置主题指向上面定义好的主题
```xml
<application
    android:theme="@style/Theme.MyTheme"
    tools:replace="android:theme">
</application>
```
- 做完以上配置后，使用基础ui组件或者功能ui组件时就会按主题里配置的样式来显示

### <span>**`主题的修改`**</span>
在主题里，不同组件的样式对应不同的appearance配置值，通过修改appearance配置的style样式可以实现组件的样式调整。
下面以麦位组件的麦位背景修改为例介绍如何通过主题修改。

- 定义好麦位组件的style
> 不同组件的默认style及其对应的属性值详见下面组件属性列表
```
<!-- 继承默认麦位style(AUIMicSeatItem.Appearance)修改 -->
<style name="AUIMicSeatItem.Appearance.My">
    <!-- 自己的麦位背景图 -->
    <item name="aui_micSeatItem_seatBackground">@drawable/ktv_ic_seat</item>
</style>
```
- 在主题里配置，以上面主题使用定义的主题为例
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 自己的主题，继承Theme.AUIKit -->
    <style name="Theme.MyTheme" parent="Theme.AUIKit">
        <!-- 可自定义组件样式 -->
        
        <!-- 麦位组件 -->
        <item name="aui_micSeatItem_appearance">@style/AUIMicSeatItem.Appearance.My</item>
    </style>
</resources>
```
- 做完上面修改后，在使用AUIMicSeatsView时，麦位背景图也会随着修改


## 组件属性

组件的属性分为两种：主题属性和样式属性。
- 主题属性在Theme使用，即AndroidManifest.xml里配置的android:theme主题，可以在xml布局文件里通过 ?attr/aui_micSeatItem_appearance 来读取对应的属性值。
- 样式属性在style里使用，即布局xml里<AUIMicSeatsView style="@style/TextStyle">或者单个指定<AUIMicSeatsView app:aui_micSeatItem_seatBackground="@drawable/ktv_ic_seat">。

下面分别说明AUIKit提供的功能ui组件和基础ui组件提供的主题属性、样式属性及默认样式的值。

### <span>**`功能ui组件`**</span>
#### **麦位组件**

麦位ui控件 -> [AUIMicSeatsView](../auikit-ui/src/main/java/io/agora/auikit/ui/micseats/impl/AUIMicSeatsView.java)
麦位自定义属性 -> [AUIMicSeatsViewAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/micseats/res/values/attrs.xml)
麦位默认样式 -> [AUIMicSeatsViewStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/micseats/res/values/styles.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_micSeats_appearance | 麦位样式 |
| aui_micSeatItem_appearance | 麦位座位样式 |
| aui_micSeatDialog_appearance | 麦位弹窗样式 |

**麦位样式属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_micSeats_spaceHorizontal | 麦位横向间距 |
| aui_micSeats_spaceVertical | 麦位纵向间距 |
| aui_micSeats_background | 背景颜色 |

**麦位座位样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_micSeatItem_background | 背景色 |
| aui_micSeatItem_dimensionRatio | 宽高比例 |
| aui_micSeatItem_seatBackground | 座位背景 |
| aui_micSeatItem_seatIconIdle | 座位空闲时图标 |
| aui_micSeatItem_seatIconLock | 座位被锁时图标 |
| aui_micSeatItem_seatIconMargin | 座位图标外间距 |
| aui_micSeatItem_seatIconDimensionRatio | 座位图标宽高比例 |
| aui_micSeatItem_audioMuteIcon | 麦位静音图标资源 |
| aui_micSeatItem_videoMuteIcon | 麦位关闭视频图标资源 |
| aui_micSeatItem_audioMuteIconWidth | 麦位静音图标宽 |
| aui_micSeatItem_audioMuteIconHeight | 麦位静音图标高 |
| aui_micSeatItem_audioMuteIconGravity | 麦位静音图标位置：居中或右下 |
| aui_micSeatItem_roomOwnerWidth | 房主名称宽 |
| aui_micSeatItem_roomOwnerHeight | 房主名称高 |
| aui_micSeatItem_roomOwnerText | 房主名称文本 |
| aui_micSeatItem_roomOwnerTextColor | 房主名称文本颜色 |
| aui_micSeatItem_roomOwnerTextSize | 房主名称文本字体大小 |
| aui_micSeatItem_roomOwnerBackground | 房主名称文本背景 |
| aui_micSeatItem_roomOwnerPaddingHorizontal | 房主名称文本横向内间距 |
| aui_micSeatItem_roomOwnerPaddingVertical | 房主名称文本纵向内间距 |
| aui_micSeatItem_titleIdleText | 麦位主标题文本 |
| aui_micSeatItem_titleTextSize | 麦位主标题文本大小 |
| aui_micSeatItem_titleTextColor | 麦位主标题文本颜色 |
| aui_micSeatItem_chorusIcon | 麦位合唱图标资源 |
| aui_micSeatItem_chorusText | 麦位合唱文本 |
| aui_micSeatItem_chorusTextColor | 麦位合唱文本字体颜色 |
| aui_micSeatItem_chorusTextSize | 麦位合唱文本字体大小 |
| aui_micSeatItem_leadSingerIcon | 麦位主唱图标资源 |
| aui_micSeatItem_leadSingerText | 麦位主唱文本 |
| aui_micSeatItem_leadSingerTextColor | 麦位主唱文本字体颜色 |
| aui_micSeatItem_leadSingerTextSize | 麦位主唱文本字体大小 |

**麦位弹窗样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_micSeatDialog_background | 弹窗背景 |
| aui_micSeatDialog_marginTop | 弹窗顶部间距 |
| aui_micSeatDialog_padding | 弹窗内间距 |
| aui_micSeatDialog_titleVisibility | 弹窗标题显示显示 |
| aui_micSeatDialog_titleText | 弹窗标题文本 |
| aui_micSeatDialog_titleTextSize | 弹窗标题文本大小 |
| aui_micSeatDialog_titleTextColor | 弹窗标题文本颜色 |
| aui_micSeatDialog_userGravity | 弹窗用户信息显示位置，居中或靠左 |
| aui_micSeatDialog_userAvatarIdle | 弹窗用户信息默认头像 |
| aui_micSeatDialog_userAvatarWidth | 弹窗用户信息头像宽 |
| aui_micSeatDialog_userAvatarHeight | 弹窗用户信息头像高 |
| aui_micSeatDialog_userNameTextSize | 弹窗用户信息用户名称文本大小 |
| aui_micSeatDialog_userNameTextColor | 弹窗用户信息用户名称文本颜色 |
| aui_micSeatDialog_userNameMarginTop | 弹窗用户信息用户名称上间距 |
| aui_micSeatDialog_userDesTextSize | 弹窗用户信息用户描述文本大小 |
| aui_micSeatDialog_userDesTextColor | 弹窗用户信息用户描述文本颜色 |
| aui_micSeatDialog_userDesText | 弹窗用户信息用户描述文本 |
| aui_micSeatDialog_userDesVisible | 弹窗用户信息用户描述是否显示 |
| aui_micSeatDialog_buttonsOrientation | 弹窗操作按钮排列，横向或竖向 |
| aui_micSeatDialog_buttonsDivider | 弹窗操作按钮分隔线 |
| aui_micSeatDialog_buttonsDividerPadding | 弹窗操作按钮分隔线间距 |
| aui_micSeatDialog_buttonsMarginTop | 弹窗操作按钮上间距 |
| aui_micSeatDialog_buttonsMarginBottom | 弹窗操作按钮下间距 |
| aui_micSeatDialog_buttonBackground | 弹窗操作按钮背景 |
| aui_micSeatDialog_buttonPaddingHorizontal | 弹窗操作按钮内横向内间距 |
| aui_micSeatDialog_buttonPaddingVertical | 弹窗操作按钮内竖向内间距 |
| aui_micSeatDialog_buttonMarginHorizontal | 弹窗操作按钮内横向外间距 |
| aui_micSeatDialog_buttonMarginVertical | 弹窗操作按钮内竖向外间距 |
| aui_micSeatDialog_buttonTextSize | 弹窗操作按钮文本大小 |
| aui_micSeatDialog_buttonNormalTextColor | 弹窗操作按钮文本颜色 |
| aui_micSeatDialog_buttonAbandonTextColor | 弹窗操作按钮禁用时文本颜色 |

#### **点唱组件**

点唱ui控件 -> [AUIJukeboxView](../auikit-ui/src/main/java/io/agora/auikit/ui/jukebox/impl/AUIJukeboxView.java)
点唱自定义属性 -> [AUIJukeboxViewAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/jukebox/res/values/attrs.xml)
点唱默认样式 -> [AUIJukeboxViewStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/jukebox/res/values/styles.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukebox_appearance | 点唱ui样式 |
| aui_jukeboxChoose_appearance | 点唱选歌ui样式 |
| aui_jukeboxChooseItem_appearance | 点唱选歌Item ui样式 |
| aui_jukeboxChosen_appearance | 点唱已选ui样式 |
| aui_jukeboxChosenItem_appearance | 点唱已选Item ui样式 |

**点唱ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukebox_background | 背景 |
| aui_jukebox_minHeight | 最小高度 |
| aui_jukebox_paddingTop | 上内间距 |
| aui_jukebox_tab_layout_background | 标题tab背景 |
| aui_jukebox_tab_background | 标题tab item背景 |
| aui_jukebox_titleTabChooseText | 标题tab中选歌的文本 |
| aui_jukebox_titleTabChosenText | 标题tab中已选的文本 |
| aui_jukebox_titleTabMode | 标题tab显示模式，fixed:两边拉伸一部分，scrollable:靠左可滑动，auto：靠左可滑动 |
| aui_jukebox_titleTabGravity | 标题tab显示位置，fill: 填满，center：居中，start：靠左 |
| aui_jukebox_titleTabTextColor | 标题tab文本颜色 |
| aui_jukebox_titleTabSelectedTextColor | 标题tab文本选中时颜色 |
| aui_jukebox_titleTabTextSize | 标题tab文本大小 |
| aui_jukebox_titleTabIndicator | 标题tab下标样式 |
| aui_jukebox_titleTabDivider | 标题tab下分隔线 |
| aui_jukebox_numTagWidth | 标题数量标签tag宽 |
| aui_jukebox_numTagHeight | 标题数量标签tag高 |
| aui_jukebox_numTagBackground | 标题数量标签tag背景 |
| aui_jukebox_numTagTextColor | 标题数量标签tag文本颜色 |
| aui_jukebox_numTagTextSize | 标题数量标签tag文本大小 |

**点唱选歌ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukeboxChoose_searchBackground | 搜索输入框背景 |
| aui_jukeboxChoose_searchPaddingHorizontal | 搜索输入框横向内间距 |
| aui_jukeboxChoose_searchMarginHorizontal | 搜索输入框横向外间距 |
| aui_jukeboxChoose_searchInputMarginHorizontal | 搜索输入框输入部分的横向外间距 |
| aui_jukeboxChoose_searchPaddingVertical | 搜索输入框竖向内间距 |
| aui_jukeboxChoose_searchMarginVertical | 搜索输入框竖向外间距 |
| aui_jukeboxChoose_searchIcon | 搜索输入框搜索图标 |
| aui_jukeboxChoose_searchCloseIcon | 搜索输入框关闭图标 |
| aui_jukeboxChoose_searchHintText | 搜索输入框无输入时文本 |
| aui_jukeboxChoose_searchHintTextColor | 搜索输入框无输入时文本颜色 |
| aui_jukeboxChoose_searchTextSize | 搜索输入框输入文本大小 |
| aui_jukeboxChoose_searchTextColor | 搜索输入框输入文本颜色 |
| aui_jukeboxChoose_categoryTabHeight | 分类tab高度 |
| aui_jukeboxChoose_categoryTabMode | 分类tab显示模式，fixed:两边拉伸一部分，scrollable:靠左可滑动，auto：靠左可滑动 |
| aui_jukeboxChoose_categoryTabGravity | 分类tab显示位置，fill: 填满，center：居中，start：靠左 |
| aui_jukeboxChoose_categoryTabIndicator | 分类tab下标样式 |
| aui_jukeboxChoose_categoryTabTextSize | 分类tab文本大小 |
| aui_jukeboxChoose_categoryTabTextColor | 分类tab文本颜色 |
| aui_jukeboxChoose_categoryTabSelectedTextColor | 分类tab选中时文本颜色 |
| aui_jukeboxChoose_categoryTabDivider | 分类tab下分隔线 |
| aui_jukeboxChoose_listDivider | 列表分隔线 |
| aui_jukeboxChoose_listPaddingHorizontal | 列表横向内间距 |

**点唱选歌Item ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukeboxChooseItem_paddingHorizontal | 横向内间距 |
| aui_jukeboxChooseItem_paddingVertical | 纵向内间距 |
| aui_jukeboxChooseItem_coverWidth | 歌词封面宽 |
| aui_jukeboxChooseItem_coverHeight | 歌词封面高 |
| aui_jukeboxChooseItem_coverCircleRadius | 歌词封面圆角 |
| aui_jukeboxChooseItem_coverDefaultImg | 歌词封面默认图片 |
| aui_jukeboxChooseItem_songNameTextColor | 歌词名文本颜色 |
| aui_jukeboxChooseItem_songNameTextSize | 歌词名文本大小 |
| aui_jukeboxChooseItem_songNameMarginStart | 歌词名前外间距 |
| aui_jukeboxChooseItem_singerNameTextColor | 歌手名文本颜色 |
| aui_jukeboxChooseItem_singerNameTextSize | 歌手名文本大小 |
| aui_jukeboxChooseItem_singerNameMarginStart | 歌手名前外间距 |
| aui_jukeboxChooseItem_buttonWidth | 选择按钮宽 |
| aui_jukeboxChooseItem_buttonHeight | 选择按钮高 |
| aui_jukeboxChooseItem_buttonBackground | 选择按钮背景 |
| aui_jukeboxChooseItem_buttonTextColor | 选择按钮文本颜色 |
| aui_jukeboxChooseItem_buttonTextSize | 选择按钮文本大小 |
| aui_jukeboxChooseItem_buttonText | 选择按钮文本 |
| aui_jukeboxChooseItem_buttonCheckedText | 选择按钮check时文本 |

**点唱已选ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukeboxChosen_listDivider | 列表分隔线 |
| aui_jukeboxChosen_listPaddingHorizontal | 列表横向间距 |

**点唱已选Item ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_jukeboxChosenItem_paddingHorizontal | 横向内间距 |
| aui_jukeboxChosenItem_paddingVertical | 纵向内间距 |
| aui_jukeboxChosenItem_orderMinWidth | 顺序标号最小宽度 |
| aui_jukeboxChosenItem_orderTextColor | 顺序标号文本颜色 |
| aui_jukeboxChosenItem_orderTextSize | 顺序标号文本大小 |
| aui_jukeboxChosenItem_coverWidth | 歌词封面宽 |
| aui_jukeboxChosenItem_coverHeight | 歌词封面高 |
| aui_jukeboxChosenItem_coverCircleRadius | 歌词封面圆角 |
| aui_jukeboxChosenItem_coverDefaultImg | 歌词封面默认图片 |
| aui_jukeboxChosenItem_songNameTextColor | 歌词名文本颜色 |
| aui_jukeboxChosenItem_songNameTextSize | 歌词名文本大小 |
| aui_jukeboxChosenItem_songNameMarginStart | 歌词名前外间距 |
| aui_jukeboxChosenItem_singerTextColor | 点唱/合唱者名称文本颜色 |
| aui_jukeboxChosenItem_singerTextSize | 点唱/合唱者名称文本大小 |
| aui_jukeboxChosenItem_singerMarginStart | 点唱/合唱者名称前间距 |
| aui_jukeboxChosenItem_singerSoloText | 点唱者名称文本 |
| aui_jukeboxChosenItem_singerChorusText | 合唱者名称文本 |
| aui_jukeboxChosenItem_playingTagSrc | 演唱中图标 |
| aui_jukeboxChosenItem_playingTagPadding | 演唱中图标内间距 |
| aui_jukeboxChosenItem_playingTagWidth | 演唱中图标宽 |
| aui_jukeboxChosenItem_playingTagHeight | 演唱中图标高 |
| aui_jukeboxChosenItem_playingTagLocation | 演唱中图标位置，aboveOrder：在顺序标号前，toTextStart：在点唱/合唱者名称前 |
| aui_jukeboxChosenItem_playingBtnBackground | 演唱中按钮背景 |
| aui_jukeboxChosenItem_playingBtnText | 演唱中按钮文本 |
| aui_jukeboxChosenItem_playingBtnTextColor | 演唱中按钮文本颜色 |
| aui_jukeboxChosenItem_playingBtnTextSize | 演唱中按钮文本大小 |
| aui_jukeboxChosenItem_playingBtnWidth | 演唱中按钮宽 |
| aui_jukeboxChosenItem_playingBtnHeight | 演唱中按钮高 |
| aui_jukeboxChosenItem_deleteBtnBackground |   删除按钮背景 |
| aui_jukeboxChosenItem_deleteBtnText |  删除按钮文本 |
| aui_jukeboxChosenItem_deleteBtnTextColor |  删除按钮文本颜色 |
| aui_jukeboxChosenItem_deleteBtnTextSize |  删除按钮文本大小 |
| aui_jukeboxChosenItem_deleteBtnWidth |  删除按钮宽 |
| aui_jukeboxChosenItem_deleteBtnHeight |  删除按钮高 |
| aui_jukeboxChosenItem_topBtnBackground |   置顶按钮背景 |
| aui_jukeboxChosenItem_topBtnText |  置顶按钮文本 |
| aui_jukeboxChosenItem_topBtnTextColor |  置顶按钮文本颜色 |
| aui_jukeboxChosenItem_topBtnTextSize |  置顶按钮文本大小 |
| aui_jukeboxChosenItem_topBtnWidth |  置顶按钮宽 |
| aui_jukeboxChosenItem_topBtnHeight |  置顶按钮高 |
| aui_jukeboxChosenItem_topBtnMarginEnd |  置顶按钮右外间距 |

#### **播放组件**

播放ui控件 -> [AUIMusicPlayerView](../auikit-ui/src/main/java/io/agora/auikit/ui/musicplayer/impl/AUIMusicPlayerView.java)
播放自定义属性 -> [AUIMusicPlayerViewAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/musicplayer/res/values/attrs.xml)
播放默认样式 -> [AUIMusicPlayerViewStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/musicplayer/res/values/styles.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_musicPlayer_appearance | 播放ui样式 |
| aui_musicPlayerControllerDialog_appearance | 播放控制弹窗ui样式 |
| aui_musicPlayerEffectPresetItem_appearance | 播放音效Item ui样式 |

**播放ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_musicPlayer_backgroundColor | 背景颜色 |
| aui_musicPlayer_minHeight | 最小高度 |
| aui_musicPlayer_cornerRadius | 圆角大小 |
| aui_musicPlayer_titleIcon | 标题图标 |
| aui_musicPlayer_titleTextColor | 标题文本颜色 |
| aui_musicPlayer_titleTextSize | 标题文本大小 |
| aui_musicPlayer_cumulativeScoreTextColor | 累积分数文本颜色 |
| aui_musicPlayer_cumulativeScoreTextSize | 累积分数文本大小 |
| aui_musicPlayer_lineScoreTextColor | 实时分数文本颜色 |
| aui_musicPlayer_lineScoreTextSize | 实时分数文本大小 |
| aui_musicPlayer_idleIcon | 未点歌默认图标 |
| aui_musicPlayer_idleTextColor | 未点歌标题文本颜色 |
| aui_musicPlayer_idleTextSize | 未点歌标题文本大小 |
| aui_musicPlayer_idleOrderText | 未点歌时点歌按钮文本 |
| aui_musicPlayer_idleOrderTextColor | 未点歌时点歌按钮文本颜色 |
| aui_musicPlayer_idleOrderTextSize | 未点歌时点歌按钮文本大小 |
| aui_musicPlayer_idleOrderBackground | 未点歌时点歌按钮背景 |
| aui_musicPlayer_prepareIcon | 歌曲加载中图标 |
| aui_musicPlayer_prepareTextColor | 歌曲加载中文本颜色 |
| aui_musicPlayer_prepareTextSize | 歌曲加载中文本大小 |
| aui_musicPlayer_activeTextSize | 已点歌文本大小 |
| aui_musicPlayer_activeTextColor | 已点歌文本颜色 |
| aui_musicPlayer_activeStartIcon | 已点歌开始播放图标 |
| aui_musicPlayer_activeSwitchIcon | 已点歌切歌图标 |
| aui_musicPlayer_activeChooseIcon | 已点歌选歌图标 |
| aui_musicPlayer_activeLeaveChorusIcon | 已点歌退出合唱图标 |
| aui_musicPlayer_activeVoiceSettingsIcon | 已点歌音效设置图标 |
| aui_musicPlayer_activeMusicPresetIcon | 已点歌变声图标 |
| aui_musicPlayer_activeSwitchOriginalIcon | 已点歌原唱图标 |
| aui_musicPlayer_activeJoinChorusBackground | 加入合唱背景 |
| aui_musicPlayer_activeJoinChorusTextColor | 加入合唱文本颜色 |
| aui_musicPlayer_activeJoinChorusTextSize | 加入合唱文本大小 |

**播放控制弹窗ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_musicPlayerControllerDialog_background | 背景 |
| aui_musicPlayerControllerDialog_titleTextSize | 标题文本大小 |
| aui_musicPlayerControllerDialog_titleTextColor | 标题文本颜色 |
| aui_musicPlayerControllerDialog_subTitleTextSize | 子标题文本大小 |
| aui_musicPlayerControllerDialog_subTitleTextColor | 子标题文本颜色 |
| aui_musicPlayerControllerDialog_dividerColor | 分隔线颜色 |
| aui_musicPlayerControllerDialog_dividerHeight | 分隔线高度 |
| aui_musicPlayerControllerDialog_checkbox | 选中框背景 |
| aui_musicPlayerControllerDialog_seekbarProgressDrawable | 滑动条背景 |
| aui_musicPlayerControllerDialog_seekbarThumb | 滑动条块图片 |


**播放音效Item ui样式**
| 属性 | 注释 |
| :-- | :-- |
| aui_musicPlayerEffectPresetItem_backgroundColor | 背景 |
| aui_musicPlayerEffectPresetItem_outStokeColor | 外边框颜色 |
| aui_musicPlayerEffectPresetItem_outIconSize | 外图片大小 |
| aui_musicPlayerEffectPresetItem_innerIconSize | 内图片大小 |
| aui_musicPlayerEffectPresetItem_textSize | 文本大小 |
| aui_musicPlayerEffectPresetItem_textColor | 文本颜色 |


### <span>**`基础ui组件`**</span>
#### **Button**

按钮控件 -> [AUIButton](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIButton.java)
按钮自定义属性 -> [AUIButtonAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_button.xml)
按钮默认样式 -> [AUIButtonwStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_button.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_button_appearance | 普通按钮样式 |
| aui_button_appearance_stroke | 普通线条按钮样式 |
| aui_button_appearance_min | 小按钮样式 |
| aui_button_appearance_min_stroke | 小线条按钮样式 |
| aui_button_appearance_circle | 圆形按钮样式 |
| aui_button_appearance_circle_stroke | 圆形线条按钮样式 |


#### **EditText**

输入框控件 -> [AUIEditText](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIEditText.java)
输入框自定义属性 -> [AUIEditTextAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_edittext.xml)
输入框默认样式 -> [AUIEditTextStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_edittext.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_editText_appearance | 普通输入框样式 |
| aui_editText_appearance_outline | 下划线输入框样式 |

#### **AlertDialog**

弹窗控件 -> [AUIAlertDialog](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIAlertDialog.java)
弹窗自定义属性 -> [AUIAlertDialogAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_alert_dialog.xml)
弹窗默认样式 -> [AUIAlertDialogStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_alert_dialog.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_alertDialog_appearance | 普通弹窗样式 |
| aui_alertDialog_appearance_outline | 下划线输入框弹窗样式 |

#### **BottomDialog**

底部弹窗控件 -> [AUIBottomDialog](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIBottomDialog.java)
底部弹窗自定义属性 -> [AUIBottomDialogAttrs](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_bottom_dialog.xml)
底部弹窗默认样式 -> [AUIBottomDialogStyle](../auikit-ui/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_bottom_dialog.xml)

**主题属性**
| 属性 | 注释 |
| :-- | :-- |
| aui_bottomDialog_appearance | 底部弹窗样式 |


## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../../LICENSE)