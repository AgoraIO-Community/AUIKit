# UiKit-Android

*English | [中文](README.zh.md)*

AUIKit is a set of scaffolding for scenario-based applications. It provides UI components and Service components to facilitate developers to quickly build their own scenario-based applications.

## Features
- [AUIKit](auikit)
  - [Service](auikit/src/main/java/io/agora/auikit/service)**([Document](doc/AUIKit-Service.md))**
    - [AUIRoomManager](auikit/src/main/java/io/agora/auikit/service/IAUIRoomManager.java)
    - [AUIUserService](auikit/src/main/java/io/agora/auikit/service/IAUIUserService.java)
    - [AUIMicSeatService](auikit/src/main/java/io/agora/auikit/service/IAUIMicSeatService.java)
    - [AUIMusicPlayerService](auikit/src/main/java/io/agora/auikit/service/IAUIMusicPlayerService.java)
    - [AUIChorusService](auikit/src/main/java/io/agora/auikit/service/IAUIChorusService.java)
    - [AUIJukeboxService](auikit/src/main/java/io/agora/auikit/service/IAUIJukeboxService.java)
  - [UI](auikit/src/main/java/io/agora/auikit/ui)**([Document](doc/AUIKit-UI.md))**
    - [Feature UI Widgets](auikit/src/main/java/io/agora/auikit/ui)
      - [AUIMicSeatsView](auikit/src/main/java/io/agora/auikit/ui/micseats/IMicSeatsView.java)
      - [AUIJukeboxView](auikit/src/main/java/io/agora/auikit/ui/jukebox/IAUIJukeboxView.java)
      - [AUIMusicPlayerView](auikit/src/main/java/io/agora/auikit/ui/musicplayer/IMusicPlayerView.java)
      - [AUIMemberView](auikit/src/main/java/io/agora/auikit/ui/member/IMemberListView.java)
    - [Basic UI Widgets](auikit/src/main/java/io/agora/auikit/ui/basic)
      - [AUIButton](auikit/src/main/java/io/agora/auikit/ui/basic/AUIButton.java)
      - [AUIBottomDialog](auikit/src/main/java/io/agora/auikit/ui/basic/AUIBottomDialog.java)
      - [AUIAlertDialog](auikit/src/main/java/io/agora/auikit/ui/basic/AUIAlertDialog.java)
      - [AUITabLayout](auikit/src/main/java/io/agora/auikit/ui/basic/AUITabLayout.java)
      - [AUIEditText](auikit/src/main/java/io/agora/auikit/ui/basic/AUIEditText.java)
      - ...

## Quick Start

### 1. Environment Setup

- <mark>Minimum Compatibility with Android 5.0</mark>（SDK API Level 21）
- Android Studio 3.5 and above versions.
- Mobile devices with Android 5.0 and above.

---

### 2. Running the Example
- Obtain Agora SDK
  Download [the rtc sdk with rtm 2.0](https://download.agora.io/null/Agora_Native_SDK_for_Android_rel.v4.1.1.30_49294_FULL_20230512_1606_264137.zip) and then unzip it to the directions belows:
  [AUIKitKaraoke/auikit/libs](auikit/libs) : agora-rtc-sdk.jar
  [AUIKitKaraoke/auikit/src/main/jniLibs](auikit/src/main/jniLibs) : so(arm64-v8a/armeabi-v7a/x86/x86_64)

- Run the project with Android Studio to begin your experience.

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).