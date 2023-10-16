# AUIKit Service

*[English](AUIKit-Service.md) | 中文*

AUIKit Service提供一套通用的服务接口，可用于数据交互。这套接口可以结合rtm2.0或者其他serverless云服务实现一套完整的服务


## 目录结构
```
service接口：
auikit-service/src/main/java/io/agora/auikit/service
├── IAUICommonService.java                  基础服务抽象类
├── IAUIRoomManager.java                    房间管理
├── IAUIUserService.java                    用户管理
├── IAUIMicSeatService.java                 麦位管理
├── IAUIJukeboxService.java                 点唱管理
├── IAUIChorusService.java                  合唱管理
├── IAUIMusicPlayerService.java             播放管理
├── impl                                    Agora实现
└── callback                                回调接口

数据结构：
auikit-service/src/main/java/io/agora/auikit/model
├── AUICommonConfig.java                    公共配置类
├── AUIRoomConfig.java                      房间配置
├── AUIRoomContext.java                     房间上下文
├── AUICreateRoomInfo.java                  创建房间信息
├── AUIRoomInfo.java                        房间信息
├── AUIUserThumbnailInfo.java               基础用户信息
├── AUIUserInfo.java                        完整用户信息
├── AUIMicSeatInfo.java                     麦位信息
├── AUIMicSeatStatus.java                   麦位状态
├── AUIMusicModel.java                      点唱歌曲信息
├── AUIChooseMusicModel.java                已点唱歌曲信息
├── AUIChoristerModel.java                  合唱者信息
├── AUIEffectVoiceInfo.java                 播放音效信息
├── AUILoadMusicConfiguration.java          播放加载音乐配置
├── AUIMusicSettingInfo.java                播放音乐配置信息
└── AUIPlayStatus.java                      播放状态
```

## API

### <span>**`service接口`**</span>

* **基础服务抽象类 ->** [IAUICommonService](../auikit-service/src/main/java/io/agora/auikit/service/IAUICommonService.java)
| 方法 | 注释 |
| :- | :- |
| bindRespDelegate | 绑定响应事件 |
| unbindRespDelegate | 解绑响应事件 |
| getContext | 获取房间公共配置信息 |
| getChannelName | 获取当前频道名 |


* **房间管理**

房间管理抽象类 -> [IAUIRoomManager](../auikit-service/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)
Agora房间管理类 -> [AUIRoomManagerImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIRoomServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| createRoom | 创建房间（房主调用），若房间不存在，系统将自动创建一个新房间 |
| destroyRoom | 销毁房间（房主调用） |
| enterRoom | 进入房间（听众调用） |
| exitRoom | 退出房间（听众调用） |
| getRoomInfoList | 获取指定房间id列表的详细信息，如果房间id列表为空，则获取所有房间的信息 |

房间信息回调接口 -> [IAUIRoomManager.AUIRoomRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)

| 方法 | 注释 |
| :- | :- |
| onRoomDestroy | 房间被销毁的回调 |
| onRoomInfoChange | 房间信息变更回调 |

* **用户管理**

用户管理抽象类 -> [IAUIUserService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIUserService.java)
Agora用户管理类 -> [AUIUserServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIUserServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| getUserInfoList | 获取指定 userId 的用户信息，如果为 null，则获取房间内所有人的信息 |
| getUserInfo | 获取指定 userId 的用户信息 |
| muteUserAudio | 对自己静音/解除静音 |
| muteUserVideo | 对自己禁摄像头/解禁摄像头 |

用户信息回调接口 -> [IAUIUserService.AUIUserRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIUserService.java)

| 方法 | 注释 |
| :- | :- |
| onRoomUserSnapshot | 用户进入房间后获取到的所有用户信息 |
| onRoomUserEnter | 用户进入房间回调 |
| onRoomUserLeave | 用户离开房间回调 |
| onRoomUserUpdate | 用户信息修改 |
| onUserAudioMute | 用户是否静音 |
| onUserVideoMute | 用户是否禁用摄像头 |

* **麦位管理**

麦位管理抽象类 -> [IAUIMicSeatService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)
Agora麦位管理类 -> [AUIMicSeatServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIMicSeatServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| enterSeat | 主动上麦（听众端和房主均可调用） |
| autoEnterSeat | 主动上麦, 获取一个小麦位进行上麦（听众端和房主均可调用） |
| leaveSeat | 主动下麦（主播调用） |
| pickSeat | 抱人上麦（房主调用） |
| kickSeat | 踢人下麦（房主调用） |
| muteAudioSeat | 静音/解除静音某个麦位（房主调用） |
| muteVideoSeat | 关闭/打开麦位摄像头 |
| closeSeat | 封禁/解禁某个麦位（房主调用） |
| getMicSeatInfo | 获取指定麦位信息 |

麦位信息回调接口 -> [IAUIMicSeatService.AUIMicSeatRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)

| 方法 | 注释 |
| :- | :- |
| onSeatListChange | 全量的麦位列表变化 |
| onAnchorEnterSeat | 有成员上麦（主动上麦/房主抱人上麦） |
| onAnchorLeaveSeat | 有成员下麦（主动下麦/房主踢人下麦） |
| onSeatAudioMute | 房主禁麦 |
| onSeatVideoMute | 房主禁摄像头 |
| onSeatClose | 房主封麦 |

* **点唱管理**

点唱管理抽象类 -> [IAUIJukeboxService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)
Agora点唱管理类 -> [AUIJukeboxServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIJukeboxServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| getMusicList | 获取歌曲列表 |
| searchMusic | 搜索歌曲 |
| getAllChooseSongList | 获取当前点歌列表 |
| chooseSong | 点一首歌 |
| removeSong | 移除一首自己点的歌 |
| pingSong | 置顶歌曲 |
| updatePlayStatus | 更新播放状态 |

点唱信息回调接口 -> [IAUIJukeboxService.AUIJukeboxRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)

| 方法 | 注释 |
| :- | :- |
| onAddChooseSong | 新增一首歌曲回调 |
| onRemoveChooseSong | 删除一首歌歌曲回调 |
| onUpdateChooseSong | 更新一首歌曲回调（例如pin） |
| onUpdateAllChooseSongs | 更新所有歌曲回调（例如pin） |

* **合唱管理**

合唱管理抽象类 -> [IAUIChorusService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIChorusService.java)
Agora合唱管理类 -> [AUIChorusServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIChorusServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| getChoristersList | 获取合唱者列表 |
| joinChorus | 加入合唱 |
| leaveChorus | 退出合唱 |
| switchSingerRole | 切换角色 |

合唱信息回调接口 -> [IAUIChorusService.AUIChorusRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIChorusService.java)

| 方法 | 注释 |
| :- | :- |
| onChoristerDidEnter | 合唱者加入 |
| onChoristerDidLeave | 合唱者离开 |
| onSingerRoleChanged | 角色切换回调 |
| onChoristerDidChanged | 合唱者改变通知 |

* **播放管理**

播放管理抽象类 -> [IAUIMusicPlayerService](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)
Agora播放管理类 -> [AUIMusicPlayerServiceImpl](../auikit-service/src/main/java/io/agora/auikit/service/imp/AUIMusicPlayerServiceImpl.kt)

| 方法 | 注释 |
| :- | :- |
| loadMusic | 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层 |
| startSing | 开始播放歌曲 |
| stopSing | 停止播放歌曲 |
| resumeSing | 恢复播放歌曲 |
| pauseSing | 暂停播放歌曲 |
| seekSing | 调整播放进度 |
| adjustMusicPlayerPlayoutVolume | 调整音乐本地播放的声音 |
| adjustMusicPlayerPublishVolume | 调整音乐远端播放的声音 |
| adjustRecordingSignal | 调整本地播放远端伴唱人声音量的大小（主唱 && 伴唱都可以调整） |
| selectMusicPlayerTrackMode | 选择音轨，原唱、伴唱 |
| getPlayerPosition | 获取播放进度 |
| getPlayerDuration | 获取播放时长 |
| setAudioPitch | 升降调 |
| setAudioEffectPreset | 音效设置 |
| effectProperties | 音效映射key |
| enableEarMonitoring | 耳返开启关闭 |

合唱信息回调接口 -> [IAUIMusicPlayerService.AUIPlayerRespObserver](../auikit-service/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)

| 方法 | 注释 |
| :- | :- |
| onPreludeDidAppear | 前奏开始加载 |
| onPreludeDidDisappear | 前奏结束加载 |
| onPostludeDidAppear | 尾奏开始加载 |
| onPostludeDidDisappear | 尾奏结束 |
| onPlayerPositionDidChange | 播放位置信息回调 |
| onPitchDidChange | 音调变化回调 |
| onPlayerStateChanged | 播放状态变化 |


### <span>**`数据结构`**</span>

* **公共配置类 ->** [AUICommonConfig](../auikit-service/src/main/java/io/agora/auikit/model/AUICommonConfig.java)

| 字段 | 注释 |
| :- | :- |
| context | Android上下文 |
| appId | Agora APP ID |
| userId | 本地用户Id |
| userName | 本地用户名 |
| userAvatar | 本地用户头像 |

* **房间配置信息 ->** [AUIRoomConfig](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomConfig.java)

| 字段 | 注释 |
| :- | :- |
| channelName | 主频道 |
| ktvChannelName | 点唱所使用频道名 |
| ktvChorusChannelName | 合唱所使用频道名 |
| tokenMap | 内部使用到的所有token表 |

* **房间上下文 ->** [AUIRoomContext](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomContext.java)

| 字段 | 注释 |
| :- | :- |
| currentUserInfo | 缓存的本地用户信息 |
| roomConfig | 房间配置信息 |
| roomInfoMap | 加入的所有房间列表 |

* **创建房间信息 ->** [AUICreateRoomInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUICreateRoomInfo.java)

| 字段 | 注释 |
| :- | :- |
| roomName | 房间名称 |
| thumbnail | 房间列表上的缩略图 |
| seatCount | 麦位个数 |
| password | 房间密码 |

* **房间信息 ->** [AUIRoomInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIRoomInfo.java)

| 字段 | 注释 |
| :- | :- |
| roomId | 房间ID |
| roomOwner | 房主用户信息 |
| onlineUsers | 房间内人数 |
| createTime | 房间创建时间 |

* **基础用户信息 ->** [AUIUserThumbnailInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIUserThumbnailInfo.java)

| 字段 | 注释 |
| :- | :- |
| userId | 用户ID |
| userName | 用户名 |
| userAvatar | 用户头像 |

* **完整用户信息 ->** [AUIUserInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIUserInfo.java)

| 字段 | 注释 |
| :- | :- |
| userId | 用户ID |
| userName | 用户名 |
| userAvatar | 用户头像 |
| muteAudio | 是否静音状态 |
| muteVideo | 是否关闭视频状态 |

* **麦位信息 ->** [AUIMicSeatInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIMicSeatInfo.java)

| 字段 | 注释 |
| :- | :- |
| user | 用户信息 |
| seatIndex | 麦位索引 |
| seatStatus | 麦位状态（idle：空间，used：使用中，locked：锁定） |
| muteAudio | 麦位禁用声音，0：否，1：是 |
| muteVideo | 麦位禁用视频，0：否，1：是 |

* **点唱歌曲信息 ->** [AUIMusicModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIMusicModel.java)

| 字段 | 注释 |
| :- | :- |
| songCode | 歌曲id，mcc则对应songCode |
| name | 歌曲名称 |
| singer | 演唱者 |
| poster | 歌曲封面海报 |
| releaseTime | 发布时间 |
| duration | 歌曲长度，单位秒 |
| musicUrl | 歌曲url，mcc则为空 |
| lrcUrl | 歌词url，mcc则为空 |

* **已点歌曲信息 ->** [AUIChooseMusicModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIChooseMusicModel.java)

| 字段 | 注释 |
| :- | :- |
| songCode | 歌曲id，mcc则对应songCode |
| name | 歌曲名称 |
| singer | 演唱者 |
| poster | 歌曲封面海报 |
| releaseTime | 发布时间 |
| duration | 歌曲长度，单位秒 |
| musicUrl | 歌曲url，mcc则为空 |
| lrcUrl | 歌词url，mcc则为空 |
| owner | 点歌用户 |
| pinAt | 置顶歌曲时间，与19700101的时间差，单位ms，为0则无置顶操作 |
| createAt | 点歌时间，与19700101的时间差，单位ms |
| status | 播放状态，0 待播放，1 播放中 |


* **合唱者信息 ->** [AUIChoristerModel](../auikit-service/src/main/java/io/agora/auikit/model/AUIChoristerModel.java)

| 字段 | 注释 |
| :- | :- |
| userId | 主唱者用户id |
| chorusSongNo | 合唱者演唱歌曲 |
| owner | 合唱者信息 |

* **播放音效信息 ->** [AUIEffectVoiceInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIEffectVoiceInfo.java)

| 字段 | 注释 |
| :- | :- |
| id | 音效唯一标识 |
| effectId | 音效id |
| resId | 图标资源Id |
| name | 名称资源Id |

* **播放加载音乐配置 ->** [AUILoadMusicConfiguration](../auikit-service/src/main/java/io/agora/auikit/model/AUILoadMusicConfiguration.java)

| 字段 | 注释 |
| :- | :- |
| autoPlay | 是否自动播放 |
| mainSingerUid | 主唱用户id |
| loadMusicMode | 加载音乐模式，0：LOAD Music Only，1：观众，2：主唱 |

* **播放音乐配置信息 ->** [AUIMusicSettingInfo](../auikit-service/src/main/java/io/agora/auikit/model/AUIMusicSettingInfo.java)

| 字段 | 注释 |
| :- | :- |
| isEar | 耳返 |
| signalVolume | 人声音量 |
| musicVolume | 音乐音量 |
| pitch | 升降调 |
| effectId | 音效 |

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../../LICENSE).
