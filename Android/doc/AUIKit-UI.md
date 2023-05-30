# AUIKit UI

*English | [中文](AUIKit-UI.zh.md)*

AUIKit UI provides basic ui components and functional ui components. Based on these two sets of ui components, developers can quickly build a scene-based application with a unified ui style.

## Directory Structure
```
Functional ui components:
auikit/src/main/java/io/agora/auikit/ui
├── micseats                                            micseat ui component
│ ├── IMicSeatsView.java                                microphone ui control interface
│ ├── IMicSeatItemView.java                             Maibit Item ui control interface
│ ├── IMicSeatDialogView.java                           Mai bit pop-up window ui control interface
│ ├── impl
│ │ ├── AUIMicSeatDialogView.java                       wheat pop-up window ui control
│ │ ├── AUIMicSeatItemView.java                         Maibit Item ui control
│ │ └── AUIMicSeatsView.java                            microphone ui control
│ └── res
│ ├── drawable                                          wheat image resources
│ ├── drawable-xxhdpi                                   wheat image resource
│ ├── layout                                            wheat layout resources
│ ├── values
│ │ ├── attrs.xml                                       Mic custom attributes
│ │ ├── styles.xml                                      default style
│ │ └── values.xml                                      Mic English scheme
│ └── values-en
│ └── values.xml                                        Mic Chinese scheme
│
├── jukebox                                             juke ui component
│ ├── IAUIJukeboxView.java                              juke ui control interface
│ ├── IAUIJukeboxChosenItemView.java                    has sung item ui control interface
│ ├── impl
│ │ ├── AUIJukeboxChooseItemView.java                   song selection item ui control
│ │ ├── AUIJukeboxChooseView.java                       ui control for song selection
│ │ ├── AUIJukeboxChosenItemView.java                   Chosen item ui control
│ │ ├── AUIJukeboxChosenView.java                       ui control
│ │ └── AUIJukeboxView.java                             juke ui control
│ └── res
│ ├── color                                             on-demand color resources
│ ├── drawable                                          on demand picture resources
│ ├── drawable-xxhdpi                                   on-demand picture resources
│ ├── layout                                            on-demand layout resources
│ ├── values
│ │ ├── attrs.xml                                       custom attributes
│ │ ├── styles.xml                                      default mode
│ │ └── values.xml                                      VOD English program
│ └── values-zh
│ └── values.xml                                        on-demand Chinese program
│
└── musicplayer                                         play ui component
     ├── IMusicPlayerView.java                          playback ui control interface
     ├── impl
     │ ├── AUIMusicPlayerControllerDialogView.java      play control popup ui control
     │ ├── AUIMusicPlayerEffectItemView.java            Play sound effect ui control
     │ ├── AUIMusicPlayerGradeView.java                 Play scoring ui control
     │ ├── AUIMusicPlayerPresetDialogView.java          Play preset ui control
     │ └── AUIMusicPlayerView.java                      Play UI control
     ├── listener
     │ ├── IMusicPlayerActionListener.java              Play event callback listener
     │ └── IMusicPlayerEffectActionListener.java        Play sound effect event callback listener
     ├── res
     │ ├── drawable                                     play image resources
     │ ├── drawable-xxhdpi                              play image resources
     │ ├── layout                                       play layout resource
     │ ├── mipmap                                       play image resources
     │ ├── values
     │ │ ├── attrs.xml                                  play custom attributes
     │ │ ├── styles.xml                                 play default styles
     │ │ └── values.xml                                 play English copy
     │ └── values-zh
     │ └── values.xml                                   play Chinese copywriting
     └── utils                                          playback related tools


Basic ui components:
auikit/src/main/java/io/agora/auikit/ui/basic
├── AUIAlertDialog.java                                 public pop-up window
├── AUIBottomDialog.java                                public bottom pop-up window
├── AUIButton.java                                      public button
├── AUIDividers.java                                    public divider
├── AUIEditText.java                                    public input box
├── AUINavigationBar.java                               common bottom navigation bar
├── AUITabLayout.java                                   public Tab column
├── AUITitleBar.java                                    public title bar
└── res
     ├── drawable                                       public image resource
     ├── drawable-xxhdpi                                public image resources
     ├── layout                                         public layout resources
     ├── menu                                           common menu resources
     └── values
             ├── attrs_aui_alert_dialog.xml             popup window custom attributes
             ├── attrs_aui_bottom_dialog.xml            Bottom pop-up window custom attributes
             ├── attrs_aui_button.xml                   Bottom pop-up window custom attributes
             ├── attrs_aui_divider.xml                  Divider custom attribute
             ├── attrs_aui_edittext.xml                 input box custom attributes
             ├── attrs_aui_navigation_bar.xml           bottom navigation bar custom attributes
             ├── attrs_aui_tab_layout.xml               Tab column custom attribute
             ├── attrs_aui_title_bar.xml                title bar custom attributes
             ├── themes.xml                             bright theme
             └── themes_dark.xml                        dark theme
```
## theme

### <span>**`Introduction`**</span>

The theme here is a concept with the theme that comes with Android, and the theme of AUIKit is expanded based on the Material theme.
By using themes, you can achieve global and unified UI modification, and you can also achieve dynamic skinning.

* For **basic ui components**, AUIKit provides two sets of basic ui themes, as follows
  Light theme (default) -> [Theme.AUIKit.Basic](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/themes.xml)
  Dark theme -> [Theme.AUIKit.Basic.Dark](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/themes_dark.xml)

Developers can define their own themes based on these two sets of themes, and then modify the default style of the basic UI components by modifying the appearance configuration of the components in their own themes.

* For **functional ui components**, AUIKit provides a set of functional ui themes, namely [Theme.AUIKit](../auikit/src/main/res/values/themes.xml)

Developers can also define their own themes based on this set of themes, and then modify the default style of functional ui components by modifying the appearance configuration of the components in their own themes.

### <span>**`Theme usage`**</span>
The following uses the [Theme.AUIKit](../auikit/src/main/res/values/themes.xml) theme as an example to illustrate how to use the theme.

- Integrate [auikit](../auikit) source code in the project
- Defined in src/main/res/value/themes.xml of the app module
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
     <!-- Own theme, inherit Theme.AUIKit -->
     <style name="Theme. MyTheme" parent="Theme. AUIKit">
         <!-- Customizable component style -->
     </style>
</resources>
```
- Configure the theme in the AndroidManifest.xml in the aap module to point to the theme defined above
```xml
<application
     android:theme="@style/Theme.MyTheme"
     tools:replace="android:theme">
</application>
```
- After completing the above configuration, when using basic ui components or functional ui components, it will be displayed according to the style configured in the theme

### <span>**`Theme modification`**</span>
In the theme, the styles of different components correspond to different appearance configuration values. By modifying the style style of the appearance configuration, the style adjustment of the components can be realized.
The following takes the wheat bit background modification of the wheat bit component as an example to introduce how to modify it through the theme.

- Define the style of the microphone component
> The default style of different components and their corresponding attribute values are detailed in the component attribute list below
```
<!-- Inherit the default wheat style (AUIMicSeatItem.Appearance) modification -->
<style name="AUIMicSeatItem.Appearance.My">
     <!-- Your own wheat bit background image -->
     <item name="aui_micSeatItem_seatBackground">@drawable/ktv_ic_seat</item>
</style>
```
- Configure in the theme, take the theme defined by the above theme as an example
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
     <!-- Own theme, inherit Theme.AUIKit -->
     <style name="Theme. MyTheme" parent="Theme. AUIKit">
         <!-- Customizable component style -->
        
         <!-- wheat bit component -->
         <item name="aui_micSeatItem_appearance">@style/AUIMicSeatItem.Appearance.My</item>
     </style>
</resources>
```
- After the above modification, when using AUIMicSeatsView, the microphone background image will also be modified

## Component properties

Component properties are divided into two types: theme properties and style properties.
- Theme attributes are used in Theme, that is, the android:theme theme configured in AndroidManifest.xml, and the corresponding attribute values can be read through ?attr/aui_micSeatItem_appearance in the xml layout file.
- Style attributes are used in style, that is, <AUIMicSeatsView style="@style/TextStyle"> in layout xml or specify <AUIMicSeatsView app:aui_micSeatItem_seatBackground="@drawable/ktv_ic_seat"> individually.

The following describes the value of the functional ui components provided by AUIKit and the theme attributes, style attributes and default styles provided by the basic ui components.

### <span>**`Functional ui component`**</span>
#### **Wheat bit component**

Wheat ui control -> [AUIMicSeatsView](../auikit/src/main/java/io/agora/auikit/ui/micseats/impl/AUIMicSeatsView.java)
Microseat custom attributes -> [AUIMicSeatsViewAttrs](../auikit/src/main/java/io/agora/auikit/ui/micseats/res/values/attrs.xml)
Default style of microphone seats -> [AUIMicSeatsViewStyle](../auikit/src/main/java/io/agora/auikit/ui/micseats/res/values/styles.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_micSeats_appearance | Mic seat style |
| aui_micSeatItem_appearance | Mic seat style |
| aui_micSeatDialog_appearance | Mic seat dialog style |

**Wheat bit style attribute**
| Properties | Comments |
| :-- | :-- |
| aui_micSeats_spaceHorizontal | Horizontal spacing of microphone seats |
| aui_micSeats_spaceVertical | vertical spacing of microphone seats |
| aui_micSeats_background | background color |

**Wheat Seat Style**
| Properties | Comments |
| :-- | :-- |
| aui_micSeatItem_background | background color |
| aui_micSeatItem_dimensionRatio | aspect ratio |
| aui_micSeatItem_seatBackground | Seat Background |
| aui_micSeatItem_seatIconIdle | Icon when the seat is idle |
| aui_micSeatItem_seatIconLock | Icon when the seat is locked |
| aui_micSeatItem_seatIconMargin | Margin outside the seat icon |
| aui_micSeatItem_seatIconDimensionRatio | seat icon aspect ratio |
| aui_micSeatItem_audioMuteIcon | Mic bit mute icon resource |
| aui_micSeatItem_videoMuteIcon | Mic seat off video icon resource |
| aui_micSeatItem_audioMuteIconWidth | Mute mute icon width |
| aui_micSeatItem_audioMuteIconHeight | Mute icon height |
| aui_micSeatItem_audioMuteIconGravity | Mute mute icon position: center or bottom right |
| aui_micSeatItem_roomOwnerWidth | Room Owner Name Width |
| aui_micSeatItem_roomOwnerHeight | Room Owner Name Height |
| aui_micSeatItem_roomOwnerText | Room Owner Name Text |
| aui_micSeatItem_roomOwnerTextColor | Room owner name text color |
| aui_micSeatItem_roomOwnerTextSize | Room owner name text font size |
| aui_micSeatItem_roomOwnerBackground | Room owner name text background |
| aui_micSeatItem_roomOwnerPaddingHorizontal | Room owner name text horizontal padding |
| aui_micSeatItem_roomOwnerPaddingVertical | Room owner name text vertical padding |
| aui_micSeatItem_titleIdleText | Mic seat main title text |
| aui_micSeatItem_titleTextSize | Mic Seat Main Title Text Size |
| aui_micSeatItem_titleTextColor | Mic Seat Main title text color |
| aui_micSeatItem_chorusIcon | Mic seat chorus icon resource |
| aui_micSeatItem_chorusText | Mic Seat Chorus Text |
| aui_micSeatItem_chorusTextColor | Mic Seat Chorus text font color |
| aui_micSeatItem_chorusTextSize | Mic Seat Chorus text font size |
| aui_micSeatItem_leadSingerIcon | Icon resource for lead singer |
| aui_micSeatItem_leadSingerText | Lead singer text |
| aui_micSeatItem_leadSingerTextColor | Mic Seat Lead Singer text font color |
| aui_micSeatItem_leadSingerTextSize | font size of lead singer text |

**Wheat bit pop-up window style**
| Properties | Comments |
| :-- | :-- |
| aui_micSeatDialog_background | popup window background |
| aui_micSeatDialog_marginTop | Pop-up window top spacing |
| aui_micSeatDialog_padding | Spacing inside the pop-up window |
| aui_micSeatDialog_titleVisibility | Pop-up window title display |
| aui_micSeatDialog_titleText | popup window title text |
| aui_micSeatDialog_titleTextSize | popup window title text size |
| aui_micSeatDialog_titleTextColor | popup window title text color |
| aui_micSeatDialog_userGravity | Pop-up window user information display position, center or left |
| aui_micSeatDialog_userAvatarIdle | Default avatar for pop-up user information |
| aui_micSeatDialog_userAvatarWidth | Pop-up window user information avatar width |
| aui_micSeatDialog_userAvatarHeight | Pop-up window user information avatar height |
| aui_micSeatDialog_userNameTextSize | Pop-up window user information user name text size |
| aui_micSeatDialog_userNameTextColor | Pop-up window user information user name text color |
| aui_micSeatDialog_userNameMarginTop | Spacing on user name of pop-up window user information |
| aui_micSeatDialog_userDesTextSize | Pop-up window user information user description text size |
| aui_micSeatDialog_userDesTextColor | Pop-up window user information user description text color |
| aui_micSeatDialog_userDesText | Pop-up user information user description text |
| aui_micSeatDialog_userDesVisible | Whether to display the pop-up user information and user description |
| aui_micSeatDialog_buttonsOrientation | Arrangement of pop-up operation buttons, horizontal or vertical |
| aui_micSeatDialog_buttonsDivider | Divider for pop-up operation buttons |
| aui_micSeatDialog_buttonsDividerPadding | Padding between popup operation buttons |
| aui_micSeatDialog_buttonsMarginTop | Spacing on the popup window operation buttons |
| aui_micSeatDialog_buttonsMarginBottom | MarginBottom bottom spacing of pop-up operation buttons |
| aui_micSeatDialog_buttonBackground | Background of popup operation button |
| aui_micSeatDialog_buttonPaddingHorizontal | The horizontal inner spacing of the popup window operation button |
| aui_micSeatDialog_buttonPaddingVertical | Vertical inner spacing of popup window operation buttons |
| aui_micSeatDialog_buttonMarginHorizontal | Inner, horizontal and outer spacing of popup window operation buttons |
| aui_micSeatDialog_buttonMarginVertical | Inner and outer vertical spacing of popup window operation buttons |
| aui_micSeatDialog_buttonTextSize | Text size of popup window operation button |
| aui_micSeatDialog_buttonNormalTextColor | Text color of pop-up operation button |
| aui_micSeatDialog_buttonAbandonTextColor | Text color when the popup action button is disabled |

#### **Voice component**

Juke ui control -> [AUIJukeboxView](../auikit/src/main/java/io/agora/auikit/ui/jukebox/impl/AUIJukeboxView.java)
Juke Custom Attributes -> [AUIJukeboxViewAttrs](../auikit/src/main/java/io/agora/auikit/ui/jukebox/res/values/attrs.xml)
Juke default style -> [AUIJukeboxViewStyle](../auikit/src/main/java/io/agora/auikit/ui/jukebox/res/values/styles.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_jukebox_appearance | jukebox ui style |
| aui_jukeboxChoose_appearance | JukeboxChoose ui style |
| aui_jukeboxChooseItem_appearance | Chosen item ui style |
| aui_jukeboxChosen_appearance | Jukebox Chosen ui style |
| aui_jukeboxChosenItem_appearance | Jukebox Chosen Item ui style |

**Voice ui style**
| Properties | Comments |
| :-- | :-- |
| aui_jukebox_background | background |
| aui_jukebox_minHeight | minimum height |
| aui_jukebox_paddingTop | Top padding |
| aui_jukebox_tab_layout_background | title tab background |
| aui_jukebox_tab_background | title tab item background |
| aui_jukebox_titleTabChooseText | The text of the selected song in the title tab |
| aui_jukebox_titleTabChosenText | The selected text in the title tab |
| aui_jukebox_titleTabMode | Title tab display mode, fixed: stretch a part on both sides, scrollable: slide to the left, auto: slide to the left |
| aui_jukebox_titleTabGravity | Title tab display position, fill: fill, center: center, start: left |
| aui_jukebox_titleTabTextColor | title tab text color |
| aui_jukebox_titleTabSelectedTextColor | The color when the title tab text is selected |
| aui_jukebox_titleTabTextSize | title tab text size |
| aui_jukebox_titleTabIndicator | Title tab subscript style |
| aui_jukebox_titleTabDivider | Divider under the title tab |
| aui_jukebox_numTagWidth | Title quantity tag width |
| aui_jukebox_numTagHeight | title quantity tag height |
| aui_jukebox_numTagBackground | Title quantity label tag background |
| aui_jukebox_numTagTextColor | Title quantity label tag text color |
| aui_jukebox_numTagTextSize | Title quantity label tag text size |

**Click and select song ui style**
| Properties | Comments |
| :-- | :-- |
| aui_jukeboxChoose_searchBackground | Search input box background |
| aui_jukeboxChoose_searchPaddingHorizontal | Horizontal inner spacing of the search input box |
| aui_jukeboxChoose_searchMarginHorizontal | Horizontal margin of the search input box |
| aui_jukeboxChoose_searchInputMarginHorizontal | The horizontal margin of the input part of the search input box |
| aui_jukeboxChoose_searchPaddingVertical | Vertical inner spacing of the search input box |
| aui_jukeboxChoose_searchMarginVertical | Vertical outer spacing of the search input box |
| aui_jukeboxChoose_searchIcon | Search input box search icon |
| aui_jukeboxChoose_searchCloseIcon | Search input box close icon |
| aui_jukeboxChoose_searchHintText | Text when there is no input in the search input box |
| aui_jukeboxChoose_searchHintTextColor | Text color when there is no input in the search input box |
| aui_jukeboxChoose_searchTextSize | Search input box input text size |
| aui_jukeboxChoose_searchTextColor | Search input box input text color |
| aui_jukeboxChoose_categoryTabHeight | category tab height |
| aui_jukeboxChoose_categoryTabMode | category tab display mode, fixed: stretch a part on both sides, scrollable: slide to the left, auto: slide to the left |
| aui_jukeboxChoose_categoryTabGravity | Category tab display position, fill: fill, center: center, start: left |
| aui_jukeboxChoose_categoryTabIndicator | Category tab subscript style |
| aui_jukeboxChoose_categoryTabTextSize | category tab text size |
| aui_jukeboxChoose_categoryTabTextColor | category tab text color |
| aui_jukeboxChoose_categoryTabSelectedTextColor | Text color when category tab is selected |
| aui_jukeboxChoose_categoryTabDivider | Divider under the category tab |
| aui_jukeboxChoose_listDivider | list divider |
| aui_jukeboxChoose_listPaddingHorizontal | List horizontal inner spacing |

**Item ui style for song selection**
| Properties | Comments |
| :-- | :-- |
| aui_jukeboxChooseItem_paddingHorizontal | horizontal inner spacing |
| aui_jukeboxChooseItem_paddingVertical | Vertical inner spacing |
| aui_jukeboxChooseItem_coverWidth | Lyric cover width |
| aui_jukeboxChooseItem_coverHeight | Lyrics cover height |
| aui_jukeboxChooseItem_coverCircleRadius | Lyrics cover circle |
| aui_jukeboxChooseItem_coverDefaultImg | Lyric cover default image |
| aui_jukeboxChooseItem_songNameTextColor | Lyric name text color |
| aui_jukeboxChooseItem_songNameTextSize | Lyric name text size |
| aui_jukeboxChooseItem_songNameMarginStart | Margin before the lyrics name |
| aui_jukeboxChooseItem_singerNameTextColor | singer name text color |
| aui_jukeboxChooseItem_singerNameTextSize | singer name text size |
| aui_jukeboxChooseItem_singerNameMarginStart | margin before and after singer name |
| aui_jukeboxChooseItem_buttonWidth | choose button width |
| aui_jukeboxChooseItem_buttonHeight | choose button height |
| aui_jukeboxChooseItem_buttonBackground | Choose button background |
| aui_jukeboxChooseItem_buttonTextColor | Select button text color |
| aui_jukeboxChooseItem_buttonTextSize | choose button text size |
| aui_jukeboxChooseItem_buttonText | choose button text |
| aui_jukeboxChooseItem_buttonCheckedText | Text when the button is checked |

**Sing the selected ui style**
| Properties | Comments |
| :-- | :-- |
| aui_jukeboxChosen_listDivider | list divider |
| aui_jukeboxChosen_listPaddingHorizontal | list horizontal spacing |

**Call the selected Item ui style**
| Properties | Comments |
| :-- | :-- |
| aui_jukeboxChosenItem_paddingHorizontal | horizontal inner spacing |
| aui_jukeboxChosenItem_paddingVertical | vertical inner spacing |
| aui_jukeboxChosenItem_orderMinWidth | minimum width of order label |
| aui_jukeboxChosenItem_orderTextColor | Order label text color |
| aui_jukeboxChosenItem_orderTextSize | Order text size |
| aui_jukeboxChosenItem_coverWidth | Lyric cover width |
| aui_jukeboxChosenItem_coverHeight | Lyrics cover height |
| aui_jukeboxChosenItem_coverCircleRadius | Lyrics cover rounded corner |
| aui_jukeboxChosenItem_coverDefaultImg | Lyric cover default image |
| aui_jukeboxChosenItem_songNameTextColor | Lyric name text color |
| aui_jukeboxChosenItem_songNameTextSize | Lyric name text size |
| aui_jukeboxChosenItem_songNameMarginStart | Margin before and outside the lyrics name |
| aui_jukeboxChosenItem_singerTextColor | Jukebox/Chosen Item Text Color |
| aui_jukeboxChosenItem_singerTextSize | jukebox/chosen name text size |
| aui_jukeboxChosenItem_singerMarginStart | Margin before jukebox/chorus name |
| aui_jukeboxChosenItem_singerSoloText | Singer name text |
| aui_jukeboxChosenItem_singerChorusText | Chorus name text |
| aui_jukeboxChosenItem_playingTagSrc | Singing icon |
| aui_jukeboxChosenItem_playingTagPadding | Inner spacing of icons during singing |
| aui_jukeboxChosenItem_playingTagWidth | Icon width during singing |
| aui_jukeboxChosenItem_playingTagHeight | Icon height during singing |
| aui_jukeboxChosenItem_playingTagLocation | Icon location during singing, aboveOrder: before the sequence tag, toTextStart: before the jukebox/chorus name |
| aui_jukeboxChosenItem_playingBtnBackground | Background of playing button |
| aui_jukeboxChosenItem_playingBtnText | Playing button text |
| aui_jukeboxChosenItem_playingBtnTextColor | The text color of the playing button |
| aui_jukeboxChosenItem_playingBtnTextSize | Playing button text size |
| aui_jukeboxChosenItem_playingBtnWidth | Josen button width |
| aui_jukeboxChosenItem_playingBtnHeight | Button height during singing |
| aui_jukeboxChosenItem_deleteBtnBackground | delete button background |
| aui_jukeboxChosenItem_deleteBtnText | delete button text |
| aui_jukeboxChosenItem_deleteBtnTextColor | Delete button text color |
| aui_jukeboxChosenItem_deleteBtnTextSize | delete button text size |
| aui_jukeboxChosenItem_deleteBtnWidth | delete button width |
| aui_jukeboxChosenItem_deleteBtnHeight | delete button height |
| aui_jukeboxChosenItem_topBtnBackground | Top button background |
| aui_jukeboxChosenItem_topBtnText | Top button text |
| aui_jukeboxChosenItem_topBtnTextColor | Top button text color |
| aui_jukeboxChosenItem_topBtnTextSize | Top button text size |
| aui_jukeboxChosenItem_topBtnWidth | Width of the top button |
| aui_jukeboxChosenItem_topBtnHeight | Top button height |
| aui_jukeboxChosenItem_topBtnMarginEnd | right outer margin of the top button |

#### **play component**

Play ui control -> [AUIMusicPlayerView](../auikit/src/main/java/io/agora/auikit/ui/musicplayer/impl/AUIMusicPlayerView.java)
Play custom attributes -> [AUIMusicPlayerViewAttrs](../auikit/src/main/java/io/agora/auikit/ui/musicplayer/res/values/attrs.xml)
Play default style -> [AUIMusicPlayerViewStyle](../auikit/src/main/java/io/agora/auikit/ui/musicplayer/res/values/styles.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_musicPlayer_appearance | Play ui style |
| aui_musicPlayerControllerDialog_appearance | Play control popup ui style |
| aui_musicPlayerEffectPresetItem_appearance | Play sound item ui style |

**play ui style**
| Properties | Comments |
| :-- | :-- |
| aui_musicPlayer_backgroundColor | background color |
| aui_musicPlayer_minHeight | minimum height |
| aui_musicPlayer_cornerRadius | Corner size |
| aui_musicPlayer_titleIcon | title icon |
| aui_musicPlayer_titleTextColor | title text color |
| aui_musicPlayer_titleTextSize | title text size |
| aui_musicPlayer_cumulativeScoreTextColor | cumulative score text color |
| aui_musicPlayer_cumulativeScoreTextSize | cumulative score text size |
| aui_musicPlayer_lineScoreTextColor | real-time score text color |
| aui_musicPlayer_lineScoreTextSize | Live score text size |
| aui_musicPlayer_idleIcon | Default icon for unordered songs |
| aui_musicPlayer_idleTextColor | Unordered song title text color |
| aui_musicPlayer_idleTextSize | Unordered song title text size |
| aui_musicPlayer_idleOrderText | Song order button text when no song is ordered |
| aui_musicPlayer_idleOrderTextColor | Song order button text color when no song is ordered |
| aui_musicPlayer_idleOrderTextSize | Song order button text size when no song is ordered |
| aui_musicPlayer_idleOrderBackground | Song order button background when no song is ordered |
| aui_musicPlayer_prepareIcon | song loading icon |
| aui_musicPlayer_prepareTextColor | Song loading text color |
| aui_musicPlayer_prepareTextSize | Song loading text size |
| aui_musicPlayer_activeTextSize | Text size of selected songs |
| aui_musicPlayer_activeTextColor | Text color of active songs |
| aui_musicPlayer_activeStartIcon | The song has been ordered to start playing icon |
| aui_musicPlayer_activeSwitchIcon | Selected song switch icon |
| aui_musicPlayer_activeChooseIcon | Selected song icon |
| aui_musicPlayer_activeLeaveChorusIcon | Chorus icon that has ordered a song |
| aui_musicPlayer_activeVoiceSettingsIcon | Audio setting icon for selected songs |
| aui_musicPlayer_activeMusicPresetIcon | Voice change icon for selected songs |
| aui_musicPlayer_activeSwitchOriginalIcon | Original song icon |
| aui_musicPlayer_activeJoinChorusBackground | Join Chorus Background |
| aui_musicPlayer_activeJoinChorusTextColor | join chorus text color |
| aui_musicPlayer_activeJoinChorusTextSize | join chorus text size |

**Playback control popup ui style**
| Properties | Comments |
| :-- | :-- |
| aui_musicPlayerControllerDialog_background | background |
| aui_musicPlayerControllerDialog_titleTextSize | title text size |
| aui_musicPlayerControllerDialog_titleTextColor | title text color |
| aui_musicPlayerControllerDialog_subTitleTextSize | subtitle text size |
| aui_musicPlayerControllerDialog_subTitleTextColor | subtitle text color |
| aui_musicPlayerControllerDialog_dividerColor | Divider color |
| aui_musicPlayerControllerDialog_dividerHeight | divider height |
| aui_musicPlayerControllerDialog_checkbox | check box background |
| aui_musicPlayerControllerDialog_seekbarProgressDrawable | Slider background |
| aui_musicPlayerControllerDialog_seekbarThumb | Sliding bar image |


**Play sound effect Item ui style**
| Properties | Comments |
| :-- | :-- |
| aui_musicPlayerEffectPresetItem_backgroundColor | background |
| aui_musicPlayerEffectPresetItem_outStokeColor | Outer border color |
| aui_musicPlayerEffectPresetItem_outIconSize | Outer image size |
| aui_musicPlayerEffectPresetItem_innerIconSize | inner image size |
| aui_musicPlayerEffectPresetItem_textSize | text size |
| aui_musicPlayerEffectPresetItem_textColor | text color |


### <span>**`Basic ui components`**</span>
#### **Button**

Button control -> [AUIButton](../auikit/src/main/java/io/agora/auikit/ui/basic/AUIButton.java)
Button Custom Attributes -> [AUIButtonAttrs](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_button.xml)
Button default style -> [AUIButtonwStyle](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_button.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_button_appearance | normal button appearance |
| aui_button_appearance_stroke | Normal line button style |
| aui_button_appearance_min | Small button appearance |
| aui_button_appearance_min_stroke | Small stroke button style |
| aui_button_appearance_circle | Circle button style |
| aui_button_appearance_circle_stroke | Circular stroke button style |


#### **EditText**

Input box control -> [AUIEditText](../auikit/src/main/java/io/agora/auikit/ui/basic/AUIEditText.java)
Input box custom attributes -> [AUIEditTextAttrs](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_edittext.xml)
Input box default style -> [AUIEditTextStyle](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_edittext.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_editText_appearance | Normal input box style |
| aui_editText_appearance_outline | Underline input box style |

#### **AlertDialog**

Pop-up control -> [AUIAlertDialog](../auikit/src/main/java/io/agora/auikit/ui/basic/AUIAlertDialog.java)
Pop-up custom attributes -> [AUIAlertDialogAttrs](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_alert_dialog.xml)
Default popup style -> [AUIAlertDialogStyle](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_alert_dialog.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_alertDialog_appearance | Normal popup style |
| aui_alertDialog_appearance_outline | Underline input box pop-up window style |

#### **BottomDialog**

Bottom popup control -> [AUIBottomDialog](../auikit/src/main/java/io/agora/auikit/ui/basic/AUIBottomDialog.java)
Bottom pop-up custom attributes -> [AUIBottomDialogAttrs](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_bottom_dialog.xml)
Bottom popup default style -> [AUIBottomDialogStyle](../auikit/src/main/java/io/agora/auikit/ui/basic/res/values/attrs_aui_bottom_dialog.xml)

**Theme Properties**
| Properties | Comments |
| :-- | :-- |
| aui_bottomDialog_appearance | Bottom dialog style |


## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).