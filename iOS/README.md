# AUIKit-iOS

*English | [中文](README_zh.md)*


<!-- TOC START -->

- [AUIKit-iOS](#auikit-ios)
- [AUIKit Basic Components](#auikit-basic-components)
- [API reference](#api-reference)
  - [Service](#service)
    - [AUIMicSeatServiceDelegate](#auimicseatservicedelegate)
      - [MicSeat management protocol](#micseat-management-protocol)
    - [AUIMicSeatRespDelegate](#auimicseatrespdelegate)
      - [MicSeat management response protocol](#micseat-management-response-protocol)
    - [AUIUserServiceDelegate](#auiuserservicedelegate)
      - [User management protocol](#user-management-protocol)
    - [AUIUserRespDelegate](#auiuserrespdelegate)
      - [User management response protocol](#user-management-response-protocol)
    - [AUIChorusServiceDelegate](#auichorusservicedelegate)
      - [Chorus management protocol](#chorus-management-protocol)
    - [AUIChorusRespDelegate](#auichorusrespdelegate)
      - [Chorus management response protocol](#chorus-management-response-protocol)
    - [AUIMusicServiceDelegate](#auimusicservicedelegate)
      - [Music management protocol](#music-management-protocol)
    - [AUIMusicRespDelegate](#auimusicrespdelegate)
      - [Music management response protocol](#music-management-response-protocol)
    - [AUIPlayerServiceDelegate](#auiplayerservicedelegate)
      - [Karaoke music player protocol](#karaoke-music-player-protocol)
    - [AUIPlayerRespDelegate](#auiplayerrespdelegate)
      - [Karaoke music player response protocol](#karaoke-music-player-response-protocol)
    - [AUIRoomManagerDelegate](#auiroommanagerdelegate)
      - [Room management protocol](#room-management-protocol)
    - [AUIRoomManagerRespDelegate](#auiroommanagerrespdelegate)
      - [Room management response protocol](#room-management-response-protocol)
  - [Widget](#widget)
    - [AUIActionSheet](#auiactionsheet)
      - [Main container configuration](#main-container-configuration)
      - [List Item Configuration](#list-item-configuration)
    - [AUIAlertView](#auialertview)
    - [AUIButton](#auibutton)
    - [AUISlider](#auislider)
    - [AUITableViewCell](#auitableviewcell)
    - [AUITabs](#auitabs)
    - [AUITextField](#auitextfield)
    - [AUIToast](#auitoast)

# AUIKit Basic Components
```
AUIKit  
├─ Service                              // Basic service components
│  ├─ AUIMicSeatServiceDelegate         // MicSeat management protocol
│  ├─ AUIMicSeatRespDelegate            // MicSeat management response protocol
│  ├─ AUIMicSeatServiceImpl             // MicSeat management implementation
│  ├─ AUIUserServiceDelegate            // User management protocol      
│  ├─ AUIUserRespDelegate               // User management response protocol
│  ├─ AUIUserServiceImpl                // User management implementation
│  ├─ AUIChorusServiceDelegate          // Chorus management protocol 
│  ├─ AUIChorusRespDelegate             // Chorus management response protocol 
│  ├─ AUIChorusServiceImpl              // Chorus management implementation
│  ├─ AUIMusicServiceDelegate           // Music management protocol      
│  ├─ AUIMusicRespDelegate              // User management response protocol      
│  ├─ AUIMusicServiceImpl               // User management implementation
│  ├─ AUIPlayerServiceDelegate          // Karaoke music player protocol      
│  ├─ AUIPlayerRespDelegate             // Karaoke music player response protocol      
│  ├─ AUIPlayerServiceImpl              // Karaoke music player implementation
│  ├─ AUIRoomManagerDelegate            // Room management protocol 
│  ├─ AUIRoomManagerRespDelegate        // Room management response protocol 
│  └─ AUIRoomManagerImpl                // Room management implementation
│
├─ Widget                               // Basic UI components without business
│  ├─ AUIActionSheet                    // Action sheet
│  ├─ AUIAlert                          // Alert
│  ├─ AUIButton                         // Button
│  ├─ AUISegmented                      // Segmented
│  ├─ AUISlider                         // Slider
│  ├─ AUITableView                      // TableView
│  ├─ AUITabs                           // Tabs
│  ├─ AUITextField                      // TextField
│  └─ AUIToast                          // Toast
│
└─ Component                            // UI module containing specific business
   ├─ IM                                // IM component
   ├─ Gifts                             // Gift component 
   ├─ Invitation                        // Invitation component 
   ├─ JukeBox                           // Jukebox component 
   ├─ MicSeat                           // MicSeat component
   └─ Player                            // Karaoke player component
```
# API reference

## Service

### AUIMicSeatServiceDelegate 
#### MicSeat management protocol
| API                | Describe                              |
| ------------------ | --------------------------------- |
| bindRespDelegate   | Bind response delegate           |
| unbindRespDelegate | Unbind response delegate        |
| enterSeat          | Enter seat（Invoked by audience and owner）  |
| leaveSeat          | Leave seat（Invoked by owner）     |
| pickSeat           | Select a member to enter seat（Invoked by owner）|
| kickSeat           | Kick a member to leave seat（Invoked by owner）|
| muteAudioSeat      | Turn off/on the microphone for a seat（Invoked by owner） |
| muteVideoSeat      | Turn off/on the camera for a seat（Invoked by owner）   |
| closeSeat          | Disable/enable a seat（Invoked by owner）     |

### AUIMicSeatRespDelegate
#### MicSeat management response protocol
| API               | Describe                                |
| ----------------- | ----------------------------------- |
| onAnchorEnterSeat | Member did enter seat |
| onAnchorLeaveSeat | Member did leave seat |
| onSeatAudioMute   | Microphone did off/on for a seat        |
| onSeatVideoMute   | Camera did turn off/on for a seat     |
| onSeatClose       | The micSeat has been disabled/enable        |

### AUIUserServiceDelegate
#### User management protocol 
| API                | Describe                       |
| ------------------ | -------------------------- |
| bindRespDelegate   | Bind response delegate      |
| unbindRespDelegate | Unbind response delegate    |
| getUserInfoList    | Obtain user list  |
| muteUserAudio      | Disable/enable the microphone for oneself  |
| muteUserVideo      | Disable/enable the camera for oneself  |

### AUIUserRespDelegate
#### User management response protocol
| API                | Describe                               |
| ------------------ | ---------------------------------- |
| onRoomUserSnapshot | All user information obtained after the user enters the room |
| onRoomUserEnter    | Callback when the user enters the room |
| onRoomUserLeave    | Callback when the user leaves the room |
| onRoomUserUpdate   | Callback when user information is modified |
| onUserAudioMute    | Callback when the user turns off/on the microphone |
| onUserVideoMute    | Callback when the user turns off/on the camera |

### AUIChorusServiceDelegate
#### Chorus management protocol
| API                | Describe             |
| ------------------ | ---------------- |
| bindRespDelegate   | Bind response delegate |
| unbindRespDelegate | Unbind response delegate |
| getChoristersList  | Obtain chorus singer list |
| joinChorus         | join chorus         |
| leaveChorus        | leave chorus         |

### AUIChorusRespDelegate
#### Chorus management response protocol 
| API                 | Describe       |
| ------------------- | ---------- |
| onChoristerDidEnter | chorus singer did enter |
| onChoristerDidLeave | chorus singer did leave |

### AUIMusicServiceDelegate
#### Music management protocol
| API                  | Describe               |
| -------------------- | ------------------ |
| bindRespDelegate     | Bind Response Delegate  |
| unbindRespDelegate   | Unbind Response Delegate |
| getMusicList         | Obtain music list by charts       |
| searchMusic          | Obtain music list by key           |
| getAllChooseSongList | Obtain the current song request list |
| chooseSong           | choose a song|
| removeSong           | Remove a song  |
| pinSong              | Put the song at the top            |
| updatePlayStatus     | Update song playback status    |

### AUIMusicRespDelegate
#### Music management response protocol
| API                    | Describe                                    |
| ---------------------- | --------------------------------------- |
| onAddChooseSong        | Callback when adding a song     |
| onRemoveChooseSong     | Callback when deleting a song    |
| onUpdateChooseSong     | Callback when updating a song（For example, modifying the play status） |
| onUpdateAllChooseSongs | Callback when updating all songs（For example, pin song）             |

### AUIPlayerServiceDelegate
#### Karaoke music player protocol 

| API                            | Describe                               |
| ------------------------------ | ---------------------------------- |
| bindRespDelegate               | Bind Response Delegate            |
| unbindRespDelegate             | Unbind Response Delegate          |
| loadMusic                      | Load music                      |
| switchSingerRole               | Switch singer role              |
| startSing                      | Play music playback              |
| stopSing                       | Stop music playback             |
| resumeSing                     | Resume music playback             |
| pauseSing                      | Pause music playback           |
| seekSing                       | Adjust playback progress      |
| adjustMusicPlayerPlayoutVolume | Adjusting the sound for local music playback  |
| adjustRecordingSignalVolume    | Adjusts the recording volume |
| adjustMusicPlayerPublishVolume | Adjust publish signal volume |
| adjustPlaybackVolume           | Adjusts the playback volume  |
| selectMusicPlayerTrackMode     | Switch the audio track to original and accompaniment |
| getPlayerDuration              | Obtain playback duration         | 
| getMusicPlayer                 | Get player instance             |
| setAudioPitch                  | Set audio pitch                  |
| setAudioEffectPreset           | Set audio effect                 |
| setVoiceConversionPreset       | Set voice conversion             |
| enableEarMonitoring            | Enables in-ear monitoring        |

### AUIPlayerRespDelegate
#### Karaoke music player response protocol
| API                       | Describe             |
| ------------------------- | ---------------- |
| onPreludeDidAppear        | Prelude start loading |
| onPreludeDidDisappear     | Prelude end load |
| onPostludeDidAppear       | Postlude start loading  |
| onPostludeDidDisappear    | Postlude end load |
| onPlayerPositionDidChange | Callback when playback progress changes |
| onPlayerStateChanged      | Callback when playback status changes   |

### AUIRoomManagerDelegate
#### Room management protocol
| API                | Describe                 |
| ------------------ | -------------------- |
| bindRespDelegate   | Bind Response Delegate    |
| unbindRespDelegate | Unbind Response Delegate  |
| createRoom         | Create a room（Invoked by owner） |
| destroyRoom        | Destory the room（Invoked by owner） |
| enterRoom          | Join room（Invoked by audience） |
| exitRoom           | Exit the room（Invoked by audience） |
| getRoomInfoList    | Get room list         |

### AUIRoomManagerRespDelegate
#### Room management response protocol 
| API              | Describe             |
| ---------------- | ---------------- |
| onRoomDestroy    | Callback when the room is destroyed  |
| onRoomInfoChange | Callback when room information changes  |

## Widget
### AUIActionSheet
#### Main container configuration
| Attribute                 | Describe       |
| ------------------- | ---------- |
| collectionViewTopEdge | The distance between the displayed list content and the space above |
| itemType | List style, sliding up and down or left and right |
| itemHeight | List item height |
| titleLabelFont | Main title font |
| titleLabelTextColor | Main title font color |
| nameLabelFont | User main title font |
| nameLabelTextColor | User main title font |
| seatLabelFont | User subtitle font |
| seatLabelTextColor | User subtitle font color |
| avatarWidth | Avatar image width |
| avatarHeight | Avatar image height |

#### List Item Configuration
| Attribute                 | Describe       |
| ------------------- | ---------- |
| icon | Item icon |
| backgroundIcon | Item background icon |
| titleColor | Title color |
| imageWidth | Image width |
| imageHeight | Image height |
| backgroundImageWidth | Background image width |
| backgroundImageHeight | Background image height |
| padding | padding between text and image |
| selectedBorderColor | Selected border color |
| selectedBorderWidth | Selected border color |
| selectedBorderRadius | Selected border radius |


### AUIAlertView
| Attribute                 | Describe       |
| ------------------- | ---------- |
| background(color: UIColor?) | Background color |
| isShowCloseButton(isShow: Bool) | Show close button or not |
| title(title: String?) | Main title content |
| titleColor(color: UIColor?) | Main title text color |
| titleFont(font: UIFont?) | Main title font |
| content(content: String?) | Content |
| contentTextAligment(textAlignment: NSTextAlignment) | Message alignment |
| contentColor(color: UIColor?) | Message text color |
| contentFont(font: UIFont?) | Message text font |
| textField(text: String?) | Set the content |
| textField(color: UIColor?) | Set the text color |
| textField(font: UIFont?) | Set the text font |
| textField(cornerRadius: CGFloat) | Set the corner radius |
| textField(showBottomDivider: Bool) | Set whether the bottom division is displayed |
| textField(bottomDividerColor: UIColor?)  | Set the bottom division color |
| textFieldBackground(color: UIColor?)  | Set text field background color |
| textFieldPlaceholder(placeholder: String?)  | Set the placeholder content |
| textFieldPlaceholder(color: UIColor?)   | Set the placeholder text color |
| textFieldPlaceholder(font: UIFont?)  | Set the placeholder font size |
| leftButton(title: String?)  | Left button text content |
| leftButton(color: UIColor?)  | Left button text color |
| leftButton(font: UIFont?)  | Left button text font size |
| leftButton(cornerRadius: CGFloat)  | Left button fillet size |
| leftButtonBackground(color: UIColor?)  | Left button background color |
| leftButtonBorder(color: UIColor?)   | Left button border color |
| leftButtonBorder(width: CGFloat)   | Left button border width |
| leftButtonTapClosure(onTap: @escaping () -> Void)   | Callback when the left button is clicked |
| rightButton(title: String?)   | Right button text content |
| rightButton(color: UIColor?)   | Right button text color |
| rightButton(font: UIFont?)   | Right button text font size |
| rightButton(cornerRadius: CGFloat)   | Right button fillet size  |
| rightButtonBackground(color: UIColor?)   | Right button background color  |
| rightButtonBorder(color: UIColor?)   | Right button border color  |
| rightButtonBorder(width: CGFloat)   | Right button border width  |
| rightButtonTapClosure(onTap: @escaping (String?) -> Void)   | Callback when the right button is clicked (with text field content) |
| rightButtonTapClosure(onTap: @escaping () -> Void)   | Callback when the right button is clicked (without text field content) |

### AUIButton
| API              | Describe             |
| ---------------- | ---------------- |
| backgroundColor | Background color |
| icon    | Button icon |
| selectedIcon | Select icon |
| iconWidth | Button icon width |
| iconHeight | Button icon height |
| buttonWidth | Button width |
| buttonHeight | Button height |
| titleFont | Button text font  |
| titleColor | Button Text Color |
| selectedTitleColor | Select Text Color |
| cornerRadius | The  corner radius of the button|
| textAlpha | Transparency of button text |
| padding | Icon and text spacing |
| highlightedBackgroundColor | Background color when pressed |
| selectedBackgroundColor | Background color when selected |
| disabledBackgroundColor | Background color when disabled |
| borderColor | Border color |
| highlightedBorderColor | Border color when pressed |
| selectedBorderColor | Border color when selected |
| disabledBorderColor | Border color when disabled |
| highlightedTitleColor | Text color when pressed |
| disabledTitleColor | Text color when disabled |
| highlightedIcon | Icon when pressed |
| disabledIcon | Icon when disabled |


### AUISlider
| API              | Describe             |
| ---------------- | ---------------- |
| backgroundColor    | Background color |
| minimumTrackColor | Color of the left part of the thumb |
| maximumTrackColor | Color of the right part of the thumb |
| thumbColor | Thumb color |
| thumbBorderColor | Thumb border color |
| trackBigLabelFont | Font for numerical description (when text description is centered to the left and right) |
| trackSmallLabelFont | Font for numerical description (when text description is at the bottom) |
| trackLabelColor | Text color for numerical descriptions |
| titleLabelFont | Title Font |
| titleLabelColor | Title Color |

### AUITableViewCell

| API              | Describe             |
| ---------------- | ---------------- |
| titleFont    | Font size of the main title |
| titleColor    | Font color of the main title |
| subTitleFont    | Font size of subtitles |
| subTitleColor    | Font color of subtitles |
| detailFont    | Font size for details |
| detailColor    | Font color for details |
| highlightColor    | The color of the asterisk |
| badgeFont    | Font size of badge |
| badgeColor    | Font color of badge |
| badgeBackgroundColor    | Background color of badge|
| switchTintColor    | Background color of the switch |
| switchThumbColor    | Color of thumb |
| arrow    | Arrow icon |

### AUITabs

| API              | Describe             |
| ---------------- | ---------------- |
| titleFont    | Font size of label text |
| indicatorColor    | Select the color of the label indicator |
| titleMargin    | Spacing of labels |
| titlePendingHorizontal    | Horizontal spacing |
| titlePendingVertical    | Vertical spacing |
| minimumWidth    | Minimum width of label |
| normalTitleColor    | Text color when label is not selected |
| normalBorderColor    | Border color when label is not selected |
| selectedTitleColor    | Text color when label is selected |
| selectedBorderColor    | Border color when label is selected |
| indicatorWidth    | Indicates the width of the bar when selecting a label |

### AUITextField
| API              | Describe             |
| ---------------- | ---------------- |
| backgroundColor    | Background color |
| leftIconImage    | Image on the left side of the text field |
| rightIconImage    | Image when the right part of the text field is not selected |
| rightSelectedIconImage    | Image when the right part of the input box is selected |
| placeHolder    | The content of the placeholder |
| placeHolderColor    | Color of placeholders|
| placeHolderFont    | Font size of placeholders |
| text    | Input Content |
| textColor    | Input text color |
| textFont    | Input text font size |
| keyBoardType    | KeyBoard type |
| isSecureTextEntry    | Is it a password input box |
| clearButtonMode    | Clear button |
| textAlignment    | Style for text alignment |
| returnKeyType    | Type of keyboard return |
| cornerRadius    | Size of corner radius |
| topText    | The content of the top text |
| topTextFont    | Font size of top text |
| topTextColor    | Font color of top text |
| bottomText    | Text content at the bottom |
| bottomTextFont    | Text font size at the bottom |
| bottomTextColor    | Text color at the bottom |
| dividerColor    | The color of the bottom divider line |


### AUIToast

| API              | Describe             |
| ---------------- | ---------------- |
| text    | Text content |
| textColor    | Text color |
| font    | Font size |
| tagImage    | icon |
| postion | Display Location |
