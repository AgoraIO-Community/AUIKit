# UiKit-Android

AUIKit是一套场景化应用的脚手架，提供Ui组件以及Service组件，方便开发者快速搭建起自己的场景化应用。

## 特性
- [AUIKit](auikit)
    - [Service](auikit-service/src/main/java/io/agora/auikit/service)**([使用指南](auikit-service))**
        - [AUIRoomManager](auikit-service/src/main/java/io/agora/auikit/service/room/AUIRoomManager.kt)
        - [AUIUserService](auikit-service/src/main/java/io/agora/auikit/service/IAUIUserService.java)
        - [AUIMicSeatService](auikit-service/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)
        - [AUIMusicPlayerService](auikit-service/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)
        - [AUIChorusService](auikit-service/src/main/java/io/agora/auikit/service/IAUIChorusService.java)
        - [AUIJukeboxService](auikit-service/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)
    - [UI](auikit-ui/src/main/java/io/agora/auikit/ui)**([使用指南](auikit-ui))**
        - [Feature UI Widgets](auikit-ui/src/main/java/io/agora/auikit/ui)
            - [AUIMicSeatsView](auikit-ui/src/main/java/io/agora/auikit/ui/micseats/IMicSeatsView.java)
            - [AUIJukeboxView](auikit-ui/src/main/java/io/agora/auikit/ui/jukebox/IAUIJukeboxView.java)
            - [AUIMusicPlayerView](auikit-ui/src/main/java/io/agora/auikit/ui/musicplayer/IMusicPlayerView.java)
            - [AUIMemberView](auikit-ui/src/main/java/io/agora/auikit/ui/member/IMemberListView.java)
        - [Basic UI Widgets](auikit-ui/src/main/java/io/agora/auikit/ui/basic)
            - [AUIButton](auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIButton.java)
            - [AUIBottomDialog](auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIBottomDialog.java)
            - [AUIAlertDialog](auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIAlertDialog.java)
            - [AUITabLayout](auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUITabLayout.java)
            - [AUIEditText](auikit-ui/src/main/java/io/agora/auikit/ui/basic/AUIEditText.java)
        - 更多ui组件见[auikit-ui代码](auikit-ui/src/main/java/io/agora/auikit/ui)

---
## 快速跑通

### 1. 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。


### 2. 运行示例

- 用 Android Studio 运行项目即可开始您的体验

> 注意：本示例只包含ui的基本使用，不包含service的使用示例，service的使用示例代码见以下场景化代码：
> - [AUIKaraoke](https://github.com/AgoraIO-Community/AUIKaraoke)
> - [AUIVoiceRoom](https://github.com/AgoraIO-Community/AUIVoiceRoom)



---
## 反馈

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
> 
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
> 
> 方案3：扫码加入我们的微信交流群提问
> 
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">

---
## 许可证

版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
