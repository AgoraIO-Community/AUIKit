# AUIKit Service

*English | [中文](AUIKit-Service.zh.md)*

AUIKit Service provides a set of common service interfaces that can be used for data interaction. This set of interfaces can be combined with rtm2.0 or other serverless cloud services to implement a complete set of services


## Directory Structure
```
service interface:
auikit-service/src/main/java/io/agora/auikit/service
├── IAUICommonService.java                          basic service abstract class
├── IAUIRoomManager.java                            room management
├── IAUIUserService.java                            user management
├── IAUIMicSeatService.java                         microphone seat management
├── IAUIJukeboxService.java                         jukebox management
├── IAUIChorusService.java                          chorus management
├── IAUIMusicPlayerService.java                     Play management
├── impl                                            Agora implementation
└── callback                                        callback interface

data structure:
auikit-service/src/main/java/io/agora/auikit/model
├── AUICommonConfig.java                            public configuration class
├── AUIRoomConfig.java                              room configuration
├── AUIRoomContext.java                             room context
├── AUICreateRoomInfo.java                          Create room information
├── AUIRoomInfo.java                                room information
├── AUIUserThumbnailInfo.java                       basic user information
├── AUIUserInfo.java                                complete user information
├── AUIMicSeatInfo.java                             Wheat information
├── AUIMicSeatStatus.java                           Mic position status
├── AUIMusicModel.java                              song information
├── AUIChooseMusicModel.java                        song information
├── AUIChoristerModel.java                          Chorus information
├── AUIEffectVoiceInfo.java                         Play sound effect information
├── AUILoadMusicConfiguration.java                  Play and load music configuration
├── AUIMusicSettingInfo.java                        Play music configuration information
└── AUIPlayStatus.java                              playback status
```

## API

### <span>**`service interface`**</span>

* **Basic service abstract class ->** [IAUICommonService](../auikit-service/src/main/java/io/agora/auikit/service/IAUICommonService.java)
  | method | annotation |
  | :- | :- |
  | bindRespDelegate | Bind response events |
  | unbindRespDelegate | unbind response event |
  | getContext | Get room public configuration information |
  | getChannelName | Get the current channel name |


* **Room Management**

Room management abstract class -> [IAUIRoomManager](../auikit-service/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)
Agora room management class -> [AUIRoomManagerImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIRoomServiceImpl.kt)

| method | annotation |
| :- | :- |
| createRoom | Create a room (called by the owner), if the room does not exist, the system will automatically create a new room |
| destroyRoom | Destroy a room (called by the homeowner) |
| enterRoom | enter a room (listener call) |
| exitRoom | Exit the room (listener call) |
| getRoomInfoList | Get the detailed information of the specified room id list, if the room id list is empty, get the information of all rooms |

Room information callback interface -> [IAUIRoomManager.AUIRoomRespObserver](../auikit-ui/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)

| method | annotation |
| :- | :- |
| onRoomDestroy | The callback when the room is destroyed |
| onRoomInfoChange | Room information change callback |

* **User Management**

User management abstract class -> [IAUIUserService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIUserService.java)
Agora user management class -> [AUIUserServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIUserServiceImpl.kt)

| method | annotation |
| :- | :- |
| getUserInfoList | Get the user information of the specified userId, if it is null, get the information of everyone in the room |
| getUserInfo | Get the user information of the specified userId |
| muteUserAudio | Mute/unmute yourself |
| muteUserVideo | Forbid/unban camera for yourself |

User information callback interface -> [IAUIUserService.AUIUserRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIUserService.java)

| method | annotation |
| :- | :- |
| onRoomUserSnapshot | All user information obtained after the user enters the room |
| onRoomUserEnter | User enters the room callback |
| onRoomUserLeave | User leaves the room callback |
| onRoomUserUpdate | User information modification |
| onUserAudioMute | Whether the user is muted |
| onUserVideoMute | Whether the user disables the camera |

* **Wheat bit management**

Wheat seat management abstract class -> [IAUIMicSeatService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)
Agora wheat seat management class -> [AUIMicSeatServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIMicSeatServiceImpl.kt)

| method | annotation |
| :- | :- |
| enterSeat | Take the initiative to enter the microphone (both the listener and the host can call) |
| autoEnterSeat | Take the initiative to enter the microphone, obtain a wheat seat to perform the microphone (both the listener and the host can call) |
| leaveSeat | Take the initiative to mic (call by the anchor) |
| pickSeat | Pick Seat (call by homeowner) |
| kickSeat | Kick a person under the mic (call by the homeowner) |
| muteAudioSeat | Mute/unmute a microphone (call by homeowner) |
| muteVideoSeat | Turn off/on the microphone camera |
| closeSeat | Block/unblock a seat (call by the homeowner) |
| getMicSeatInfo | Get the specified microphone seat information |

Microphone information callback interface -> [IAUIMicSeatService.AUIMicSeatRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)

| method | annotation |
| :- | :- |
| onSeatListChange | Change of full seat list |
| onAnchorEnterSeat | A member joins the mic (takes the initiative to mic/the owner hugs someone to mic) |
| onAnchorLeaveSeat | A member leaves the mic (actively leaves the mic / the host kicks someone to leave the mic) |
| onSeatAudioMute | Homeowner Mute |
| onSeatVideoMute | Homeowner bans cameras |
| onSeatClose | Homeowner closes wheat |

* **Voice Management**

Juke management abstract class -> [IAUIJukeboxService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)
Agora juke management class -> [AUIJukeboxServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIJukeboxServiceImpl.kt)

| method | annotation |
| :- | :- |
| getMusicList | Get song list |
| searchMusic | Search Songs |
| getAllChooseSongList | Get the current song list |
| chooseSong | Order a song |
| removeSong | Remove a song you ordered |
| pingSong | Top songs |
| updatePlayStatus | Update Play Status |

Juke information callback interface -> [IAUIJukeboxService.AUIJukeboxRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)

| method | annotation |
| :- | :- |
| onAddChooseSong | add a song callback |
| onRemoveChooseSong | Delete a song song callback |
| onUpdateChooseSong | Update a song callback (eg pin) |
| onUpdateAllChooseSongs | Callback to update all songs (e.g. pin) |

* **Chorus Management**

Chorus management abstract class -> [IAUIChorusService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIChorusService.java)
Agora chorus management class -> [AUIChorusServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIChorusServiceImpl.kt)

| method | annotation |
| :- | :- |
| getChoristersList | Get the list of choristers |
| joinChorus | Join Chorus |
| leaveChorus | leave the chorus |
| switchSingerRole | switch role |

Chorus information callback interface -> [IAUIChorusService.AUIChorusRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIChorusService.java)

| method | annotation |
| :- | :- |
| onChoristerDidEnter | Chorus joins |
| onChoristerDidLeave | Chorus leaves |
| onSingerRoleChanged | Role switching callback |
| onChoristerDidChanged | Chorus change notification |

* **Play Management**

Play management abstract class -> [IAUIMusicPlayerService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)
Agora playback management class -> [AUIMusicPlayerServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIMusicPlayerServiceImpl.kt)

| method | annotation |
| :- | :- |
| loadMusic | Asynchronously load songs, only one song can be loaded at a time, and the result of loadSong will be notified to the business layer through callback |
| startSing | start playing a song |
| stopSing | Stop playing a song |
| resumeSing | resume playing a song |
| pauseSing | Pause playing song |
| seekSing | Adjust playback progress |
| adjustMusicPlayerPlayoutVolume | Adjust the volume of music played locally |
| adjustMusicPlayerPublishVolume | Adjust the volume of music played remotely |
| adjustRecordingSignal | Adjust the volume of the remote accompaniment vocal played locally (both lead singer & accompaniment can be adjusted) |
| selectMusicPlayerTrackMode | Select audio track, original and accompaniment |
| getPlayerPosition | Get playback progress |
| getPlayerDuration | Get the playback duration |
| setAudioPitch | pitch up and down |
| setAudioEffectPreset | Audio Effect Preset |
| effectProperties | sound mapping key |
| enableEarMonitoring | Ear monitor on and off |

Chorus information callback interface -> [IAUIMusicPlayerService.AUIPlayerRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)

| method | annotation |
| :- | :- |
| onPreludeDidAppear | Prelude starts to load |
| onPreludeDidDisappear | Prelude ends loading |
| onPosludeDidAppear | The ending starts to load |
| onPosludeDidDisappear | end of the ending |
| onPlayerPositionDidChange | Callback for playback position information |
| onPitchDidChange | Pitch change callback |
| onPlayerStateChanged | Play state change |


### <span>**`Data Structure`**</span>

* **Common configuration class ->** [AUICommonConfig](../auikit-service/src/main/java/io/agora/auikit/model/AUICommonConfig.java)

| field | comment |
| :- | :- |
| context | Android context |
| appId | Agora APP ID |
| userId | local user Id |
| userName | local username |
| userAvatar | local user avatar |

* **Room configuration information ->** [AUIRoomConfig](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomConfig.java)

| field | comment |
| :- | :- |
| channelName | main channel |
| ktvChannelName | The name of the channel used by the jukebox |
| ktvChorusChannelName | channel name used by chorus |
| tokenMap | All token tables used internally |

* **Room Context ->** [AUIRoomContext](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomContext.java)

| field | comment |
| :- | :- |
| currentUserInfo | Cached local user information |
| roomConfig | room configuration information |
| roomInfoMap | List of all rooms joined |

* **Create room information ->** [AUICreateRoomInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUICreateRoomInfo.java)

| field | comment |
| :- | :- |
| roomName | room name |
| thumbnail | thumbnail on room list |
| seatCount | Number of seats |
| password | Room password |

* **Room Information ->** [AUIRoomInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomInfo.java)

| field | comment |
| :- | :- |
| roomId | room ID |
| roomOwner | Room owner user information |
| onlineUsers | Number of people in the room |
| createTime | room creation time |

* **Basic user information ->** [AUIUserThumbnailInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIUserThumbnailInfo.java)

| field | comment |
| :- | :- |
| userId | user ID |
| userName | username |
| userAvatar | User Avatar |

* **Complete user information ->** [AUIUserInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIUserInfo.java)

| field | comment |
| :- | :- |
| userId | user ID |
| userName | username |
| userAvatar | User Avatar |
| muteAudio | Mute or not |
| muteVideo | Whether to turn off the video state |

* **Microseat Information ->** [AUIMicSeatInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIMicSeatInfo.java)

| field | comment |
| :- | :- |
| user | user information |
| seatIndex | seat index |
| seatStatus | seat status (idle: space, used: in use, locked: locked) |
| muteAudio | Mic disable sound, 0: no, 1: yes |
| muteVideo | Mic disable video, 0: no, 1: yes |

* **Sing song information ->** [AUIMusicModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIMusicModel.java)

| field | comment |
| :- | :- |
| songCode | song id, mcc corresponds to songCode |
| name | song name |
| singer |
| poster | song cover poster |
| releaseTime | release time |
| duration | song length, in seconds |
| musicUrl | song url, mcc is empty |
| lrcUrl | lyrics url, mcc is empty |

* **Selected song information ->** [AUIChooseMusicModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIChooseMusicModel.java)

| field | comment |
| :- | :- |
| songCode | song id, mcc corresponds to songCode |
| name | song name |
| singer |
| poster | song cover poster |
| releaseTime | release time |
| duration | song length, in seconds |
| musicUrl | song url, mcc is empty |
| lrcUrl | lyrics url, mcc is empty |
| owner | Song user |
| pinAt | pinAt the time of the top song, the time difference from 19700101, in ms, if it is 0, there is no pinAt operation |
| createAt | song request time, the time difference from 19700101, in ms |
| status | Playing status, 0 is waiting to play, 1 is playing |


* ** Chorus information ->** [AUIChoristerModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIChoristerModel.java)

| field | comment |
| :- | :- |
| userId | user id of lead singer |
| chorusSongNo | Chorus singing a song |
| owner | chorus information |

* **Play audio information ->** [AUIEffectVoiceInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIEffectVoiceInfo.java)

| field | comment |
| :- | :- |
| id | The unique identifier of the sound effect |
| effectId | sound effect id |
| resId | icon resource Id |
| name | name resource Id |

* **Play loading music configuration ->** [AUILoadMusicConfiguration](../auikit-service/src/main/java/io/agora/auikit/model/AUILoadMusicConfiguration.java)

| field | comment |
| :- | :- |
| autoPlay | Whether to play automatically |
| mainSingerUid | main singer user id |
| loadMusicMode | load music mode, 0: LOAD Music Only, 1: audience, 2: lead singer |

* **Play music configuration information ->** [AUIMusicSettingInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIMusicSettingInfo.java)

| field | comment |
| :- | :- |
| isEar | ear return |
| signalVolume | vocal volume |
| musicVolume | music volume |
| pitch |
| effectId | sound effect |

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../../LICENSE).
