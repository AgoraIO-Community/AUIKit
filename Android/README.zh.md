# UiKit-Android

*[English](README.md) | 中文*

AUIKit是一套场景化应用的脚手架，提供Ui组件以及Service组件，方便开发者快速搭建起自己的场景化应用。

## 特性
- [AUIKit](auikit)
    - [Service](auikit-ui/src/main/java/io/agora/auikit/service)**([使用指南](doc/AUIKit-Service.md))**
        - [AUIRoomManager](auikit-ui/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)
        - [AUIUserService](auikit-ui/src/main/java/io/agora/auikit/service/IAUIUserService.java)
        - [AUIMicSeatService](auikit-ui/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)
        - [AUIMusicPlayerService](auikit-ui/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)
        - [AUIChorusService](auikit-ui/src/main/java/io/agora/auikit/service/IAUIChorusService.java)
        - [AUIJukeboxService](auikit-ui/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)
    - [UI](auikit-ui/src/main/java/io/agora/auikit/ui)**([使用指南](doc/AUIKit-UI.md))**
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
            - ...

## 快速跑通

### 1. 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

---

### 2. 运行示例

- 用 Android Studio 运行项目即可开始您的体验

## 许可证
版权所有 Agora, Inc. 保留所有权利。
使用 [MIT 许可证](../LICENSE)
