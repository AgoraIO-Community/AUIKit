# AUiKit Service

*English | [中文](AUiKit-Service.zh.md)*

AUiKit Service provides a set of common service interfaces that can be used for data interaction. This set of interfaces can be combined with rtm2.0 or other serverless cloud services to implement a complete set of services


## Directory Structure
```
service interface:
auikit/src/main/java/io/agora/auikit/service
├── IAUiCommonService.java                          basic service abstract class
├── IAUiRoomManager.java                            room management
├── IAUiUserService.java                            user management
├── IAUiMicSeatService.java                         microphone seat management
├── IAUiJukeboxService.java                         jukebox management
├── IAUiChorusService.java                          chorus management
├── IAUiMusicPlayerService.java                     Play management
├── impl                                            Agora implementation
└── callback                                        callback interface

data structure:
auikit/src/main/java/io/agora/auikit/model
├── AUiCommonConfig.java                            public configuration class
├── AUiRoomConfig.java                              room configuration
├── AUiRoomContext.java                             room context
├── AUiCreateRoomInfo.java                          Create room information
├── AUiRoomInfo.java                                room information
├── AUiUserThumbnailInfo.java                       basic user information
├── AUiUserInfo.java                                complete user information
├── AUiMicSeatInfo.java                             Wheat information
├── AUiMicSeatStatus.java                           Mic position status
├── AUiMusicModel.java                              song information
├── AUiChooseMusicModel.java                        song information
├── AUiChoristerModel.java                          Chorus information
├── AUiEffectVoiceInfo.java                         Play sound effect information
├── AUiLoadMusicConfiguration.java                  Play and load music configuration
├── AUiMusicSettingInfo.java                        Play music configuration information
└── AUiPlayStatus.java                              playback status
```

## API

### <span>**`service interface`**</span>

* **Basic service abstract class ->** [IAUiCommonService](../auikit/src/main/java/io/agora/auikit/service/IAUiCommonService.java)
  | method | annotation |
  | :- | :- |
  | bindRespDelegate | Bind response events |
  | unbindRespDelegate | unbind response event |
  | getContext | Get room public configuration information |
  | getChannelName | Get the current channel name |


* **Room Management**

Room management abstract class -> [IAUiRoomManager](../auikit/src/main/java/io/agora/auikit/service/IAUiRoomManager.java)
Agora room management class -> [AUiRoomManagerImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiRoomServiceImpl.kt)

| method | annotation |
| :- | :- |
| createRoom | Create a room (called by the owner), if the room does not exist, the system will automatically create a new room |
| destroyRoom | Destroy a room (called by the homeowner) |
| enterRoom | enter a room (listener call) |
| exitRoom | Exit the room (listener call) |
| getRoomInfoList | Get the detailed information of the specified room id list, if the room id list is empty, get the information of all rooms |

Room information callback interface -> [IAUiRoomManager.AUiRoomRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiRoomManager.java)

| method | annotation |
| :- | :- |
| onRoomDestroy | The callback when the room is destroyed |
| onRoomInfoChange | Room information change callback |

* **User Management**

User management abstract class -> [IAUiUserService](../auikit/src/main/java/io/agora/auikit/service/IAUiUserService.java)
Agora user management class -> [AUiUserServiceImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiUserServiceImpl.kt)

| method | annotation |
| :- | :- |
| getUserInfoList | Get the user information of the specified userId, if it is null, get the information of everyone in the room |
| getUserInfo | Get the user information of the specified userId |
| muteUserAudio | Mute/unmute yourself |
| muteUserVideo | Forbid/unban camera for yourself |

User information callback interface -> [IAUiUserService.AUiUserRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiUserService.java)

| method | annotation |
| :- | :- |
| onRoomUserSnapshot | All user information obtained after the user enters the room |
| onRoomUserEnter | User enters the room callback |
| onRoomUserLeave | User leaves the room callback |
| onRoomUserUpdate | User information modification |
| onUserAudioMute | Whether the user is muted |
| onUserVideoMute | Whether the user disables the camera |

* **Wheat bit management**

Wheat seat management abstract class -> [IAUiMicSeatService](../auikit/src/main/java/io/agora/auikit/service/IAUiMicSeatService.java)
Agora wheat seat management class -> [AUiMicSeatServiceImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiMicSeatServiceImpl.kt)

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

Microphone information callback interface -> [IAUiMicSeatService.AUiMicSeatRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiMicSeatService.java)

| method | annotation |
| :- | :- |
| onSeatListChange | Change of full seat list |
| onAnchorEnterSeat | A member joins the mic (takes the initiative to mic/the owner hugs someone to mic) |
| onAnchorLeaveSeat | A member leaves the mic (actively leaves the mic / the host kicks someone to leave the mic) |
| onSeatAudioMute | Homeowner Mute |
| onSeatVideoMute | Homeowner bans cameras |
| onSeatClose | Homeowner closes wheat |

* **Voice Management**

Juke management abstract class -> [IAUiJukeboxService](../auikit/src/main/java/io/agora/auikit/service/IAUiJukeboxService.java)
Agora juke management class -> [AUiJukeboxServiceImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiJukeboxServiceImpl.kt)

| method | annotation |
| :- | :- |
| getMusicList | Get song list |
| searchMusic | Search Songs |
| getAllChooseSongList | Get the current song list |
| chooseSong | Order a song |
| removeSong | Remove a song you ordered |
| pingSong | Top songs |
| updatePlayStatus | Update Play Status |

Juke information callback interface -> [IAUiJukeboxService.AUiJukeboxRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiJukeboxService.java)

| method | annotation |
| :- | :- |
| onAddChooseSong | add a song callback |
| onRemoveChooseSong | Delete a song song callback |
| onUpdateChooseSong | Update a song callback (eg pin) |
| onUpdateAllChooseSongs | Callback to update all songs (e.g. pin) |

* **Chorus Management**

Chorus management abstract class -> [IAUiChorusService](../auikit/src/main/java/io/agora/auikit/service/IAUiChorusService.java)
Agora chorus management class -> [AUiChorusServiceImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiChorusServiceImpl.kt)

| method | annotation |
| :- | :- |
| getChoristersList | Get the list of choristers |
| joinChorus | Join Chorus |
| leaveChorus | leave the chorus |
| switchSingerRole | switch role |

Chorus information callback interface -> [IAUiChorusService.AUiChorusRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiChorusService.java)

| method | annotation |
| :- | :- |
| onChoristerDidEnter | Chorus joins |
| onChoristerDidLeave | Chorus leaves |
| onSingerRoleChanged | Role switching callback |
| onChoristerDidChanged | Chorus change notification |

* **Play Management**

Play management abstract class -> [IAUiMusicPlayerService](../auikit/src/main/java/io/agora/auikit/service/IAUiMusicPlayerService.java)
Agora playback management class -> [AUiMusicPlayerServiceImpl](../auikit/src/main/java/io/agora/auikit/service/imp/AUiMusicPlayerServiceImpl.kt)

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

Chorus information callback interface -> [IAUiMusicPlayerService.AUiPlayerRespDelegate](../auikit/src/main/java/io/agora/auikit/service/IAUiMusicPlayerService.java)

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

* **Common configuration class ->** [AUiCommonConfig](../auikit/src/main/java/io/agora/auikit/model/AUiCommonConfig.java)

| field | comment |
| :- | :- |
| context | Android context |
| appId | Agora APP ID |
| userId | local user Id |
| userName | local username |
| userAvatar | local user avatar |

* **Room configuration information ->** [AUiRoomConfig](../auikit/src/main/java/io/agora/auikit/model/AUiRoomConfig.java)

| field | comment |
| :- | :- |
| channelName | main channel |
| ktvChannelName | The name of the channel used by the jukebox |
| ktvChorusChannelName | channel name used by chorus |
| tokenMap | All token tables used internally |

* **Room Context ->** [AUiRoomContext](../auikit/src/main/java/io/agora/auikit/model/AUiRoomContext.java)

| field | comment |
| :- | :- |
| currentUserInfo | Cached local user information |
| roomConfig | room configuration information |
| roomInfoMap | List of all rooms joined |

* **Create room information ->** [AUiCreateRoomInfo](../auikit/src/main/java/io/agora/auikit/model/AUiCreateRoomInfo.java)

| field | comment |
| :- | :- |
| roomName | room name |
| thumbnail | thumbnail on room list |
| seatCount | Number of seats |
| password | Room password |

* **Room Information ->** [AUiRoomInfo](../auikit/src/main/java/io/agora/auikit/model/AUiRoomInfo.java)

| field | comment |
| :- | :- |
| roomId | room ID |
| roomOwner | Room owner user information |
| onlineUsers | Number of people in the room |
| createTime | room creation time |

* **Basic user information ->** [AUiUserThumbnailInfo](../auikit/src/main/java/io/agora/auikit/model/AUiUserThumbnailInfo.java)

| field | comment |
| :- | :- |
| userId | user ID |
| userName | username |
| userAvatar | User Avatar |

* **Complete user information ->** [AUiUserInfo](../auikit/src/main/java/io/agora/auikit/model/AUiUserInfo.java)

| field | comment |
| :- | :- |
| userId | user ID |
| userName | username |
| userAvatar | User Avatar |
| muteAudio | Mute or not |
| muteVideo | Whether to turn off the video state |

* **Microseat Information ->** [AUiMicSeatInfo](../auikit/src/main/java/io/agora/auikit/model/AUiMicSeatInfo.java)

| field | comment |
| :- | :- |
| user | user information |
| seatIndex | seat index |
| seatStatus | seat status (idle: space, used: in use, locked: locked) |
| muteAudio | Mic disable sound, 0: no, 1: yes |
| muteVideo | Mic disable video, 0: no, 1: yes |

* **Sing song information ->** [AUiMusicModel](../auikit/src/main/java/io/agora/auikit/model/AUiMusicModel.java)

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

* **Selected song information ->** [AUiChooseMusicModel](../auikit/src/main/java/io/agora/auikit/model/AUiChooseMusicModel.java)

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


* ** Chorus information ->** [AUiChoristerModel](../auikit/src/main/java/io/agora/auikit/model/AUiChoristerModel.java)

| field | comment |
| :- | :- |
| userId | user id of lead singer |
| chorusSongNo | Chorus singing a song |
| owner | chorus information |

* **Play audio information ->** [AUiEffectVoiceInfo](../auikit/src/main/java/io/agora/auikit/model/AUiEffectVoiceInfo.java)

| field | comment |
| :- | :- |
| id | The unique identifier of the sound effect |
| effectId | sound effect id |
| resId | icon resource Id |
| name | name resource Id |

* **Play loading music configuration ->** [AUiLoadMusicConfiguration](../auikit/src/main/java/io/agora/auikit/model/AUiLoadMusicConfiguration.java)

| field | comment |
| :- | :- |
| autoPlay | Whether to play automatically |
| mainSingerUid | main singer user id |
| loadMusicMode | load music mode, 0: LOAD Music Only, 1: audience, 2: lead singer |

* **Play music configuration information ->** [AUiMusicSettingInfo](../auikit/src/main/java/io/agora/auikit/model/AUiMusicSettingInfo.java)

| field | comment |
| :- | :- |
| isEar | ear return |
| signalVolume | vocal volume |
| musicVolume | music volume |
| pitch |
| effectId | sound effect |

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).
