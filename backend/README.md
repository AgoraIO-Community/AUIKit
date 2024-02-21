# UIKit 后台服务

## 项目简介
- 本项目基于Spring Boot框架开发, 依赖 Redis 和 MongoDB 中间件。
    - Redis, 主要用于提供分布式缓存和分布式锁方式保证数据更新的一致性
    - MongoDB，主要用于维护房间列表

## 服务能力
- 提供大厅房间列表管理、语聊房管理、用户踢人接口和Token创建服务

## 服务部署
### 快速体验
- > 采用Docker部署服务, 服务安装环境需要提前安装好Docker环境, 并安装最新版[docker-compose](https://docs.docker.com/compose/)部署工具
    - 可以下载安装[Docker Desktop](https://www.docker.com/products/docker-desktop/), 并已默认安装docker-compose
- 本地部署
    - > 本地启动服务前, 需要先在声网申请的[appId和Secret](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)、[RESTful API](https://console.shengwang.cn/settings/restfulApi)以及环信 IM 相关的配置[开通配置环信即时通讯 IM 服务](https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html)
    - > 本地启动服务前, 需要在项目根目录创建`.env`文件, 并且填入以下字段，用于配置白名单使用：
        - WHITELIST_TOKEN_APP_ID=< Your WHITELIST_TOKEN_APP_ID >
        - WHITELIST_TOKEN_APP_CERT=< Your WHITELIST_TOKEN_APP_CERT >
        - WHITELIST_CHAT_ROOM_APP_ID=< Your WHITELIST_CHAT_ROOM_APP_ID >
        - WHITELIST_CHAT_ROOM_ORG_NAME=< Your WHITELIST_CHAT_ROOM_ORG_NAME >
        - WHITELIST_CHAT_ROOM_APP_NAME=< Your WHITELIST_CHAT_ROOM_APP_NAME >
        - WHITELIST_CHAT_ROOM_CLIENT_ID=< Your WHITELIST_CHAT_ROOM_CLIENT_ID >
        - WHITELIST_CHAT_ROOM_CLIENT_SECRET=< Your WHITELIST_CHAT_ROOM_CLIENT_SECRET >
        - WHITELIST_RTC_KICK_OUT_AUTH_APP_ID=< Your WHITELIST_RTC_KICK_OUT_AUTH_APP_ID >
        - WHITELIST_RTC_KICK_OUT_AUTH_BASIC_AUTH=< Your WHITELIST_RTC_KICK_OUT_AUTH_BASIC_AUTH >

    - 在当前项目根目录下执行 `docker compose up -d --build`, 会拉取相关镜像启动Redis、MongoDB服务，同时会对业务服务进行编译。如镜像拉取失败, 可配置国内镜像源解决
    - 服务启动后, 可使用 curl `http://localhost:8080/health/check` 测试
    - 如果使用App调试本地服务, 需要在App上替换对应后端服务域名为http://服务机器IP:8080, 替换域名后可以使用App体验相关服务
    - 停止服务, 执行 `docker compose down`

### 本地开发
- Java版本推荐>=11
- 编辑器可采用 [Visual Studio Code](https://code.visualstudio.com/), 安装以下插件
    - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- vscode打开项目目录
- 修改配置文件[application.yml](src/main/resources/application.yml)
    - spring.data.mongodb.uri
    - spring.redis.host
    - spring.redis.password
    - whitelist.token.appId
    - whitelist.token.appCert
    - whitelist.chatRoom.appId
    - whitelist.chatRoom.orgName
    - whitelist.chatRoom.appName
    - whitelist.chatRoom.clientId
    - whitelist.chatRoom.clientSecret
    - whitelist.rtcKickOutAuth.appId
    - whitelist.rtcKickOutAuth.basicAuth

### 上线部署
- 正式上线前, 需要调整Redis/MongoDB等配置, 并将服务部署在网关后, 网关可提供鉴权/限流等能力, 本服务不带网关能力
- 指标收集, https://您的域名:9090/metrics/prometheus, 可根据需要收集相应指标监控服务
- 服务可以部署在云平台, 比如[阿里云容器服务ACK](https://www.alibabacloud.com/zh/product/kubernetes)

## 目录结构
```
.
├── Dockerfile                                                          // 项目构建镜像
├── HELP.md
├── README.md
├── README_zh.md
├── docker-compose.yaml                                                 // 本地一键部署
├── init-mongo.js                                                       // MongoDB初始化
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io
│   │   │       └── agora
│   │   │           └── uikit
│   │   │               ├── Application.java                            // 启动文件
│   │   │               ├── bean                                        // 对象类
│   │   │               │   ├── domain                                  // 实体类
│   │   │               │   ├── dto                                     // 传输类
│   │   │               │   ├── entity                                  // DB实体类
│   │   │               │   ├── enums                                   // 枚举
│   │   │               │   ├── exception                               // 异常处理
│   │   │               │   │   └── BusinessException.java
│   │   │               │   ├── req                                     // 请求类
│   │   │               │   └── valid                                   // 校验
│   │   │               │       ├── EnumValid.java
│   │   │               │       └── EnumValidator.java
│   │   │               ├── config                                      // 配置
│   │   │               │   ├── GlobalExceptionHandler.java             // 全局异常捕获
│   │   │               │   ├── RedisConfig.java                        // Redis配置
│   │   │               │   ├── ChatRoomAPIClient.java                  // 环信 IM 配置
│   │   │               │   ├── RtcChannelAPIClient.java                // RTC 踢人配置
│   │   │               │   ├── WhitelistConfig.java                    // 白名单配置
│   │   │               │   └── WebMvcConfig.java                       // MVC配置
│   │   │               ├── controller                                  // 控制器
│   │   │               │   ├── HealthController.java                   // 健康检查
│   │   │               │   ├── ChatRoomV2Controller.java               // 语言聊房
│   │   │               │   ├── RoomV2Controller.java                   // 房间管理
│   │   │               │   ├── UserController.java                     // 用户管理，提供踢人能力
│   │   │               │   └── TokenV2Controller.java                  // Token管理
│   │   │               ├── interceptor                                 // 拦截器
│   │   │               │   ├── PrometheusMetricInterceptor.java        // 指标拦截器
│   │   │               │   └── TraceIdInterceptor.java                 // 链路追踪
│   │   │               ├── metric                                      // 指标上报
│   │   │               │   └── PrometheusMetric.java
│   │   │               │── repository                                  // DB访问层
│   │   │               │   └── RoomListV2Repository.java
│   │   │               ├── service                                     // 服务层
│   │   │               │   ├── IRoomV2Service.java                     // 房间
│   │   │               │   ├── IChatRoomV2Service.java                 // 语聊房服务
│   │   │               │   ├── IEMAPIService.java                      // 语聊房接口 feign 定义
│   │   │               │   ├── IChatRoomAPIService.java                // 语聊房接口
│   │   │               │   ├── IUserService.java                       // 用户管理接口
│   │   │               │   ├── IRtcChannlAPIService.java               // 踢人接口 feign 定义
│   │   │               │   ├── IRtcChannlService.java                  // 踢人接口
│   │   │               │   ├── ITokenV2Service.java                    // Token
│   │   │               │   └── impl
│   │   │               │       ├── RoomV2ServiceImpl.java
│   │   │               │       ├── ChatRoomV2ServiceImpl.java
│   │   │               │       ├── ChatRoomAPIServiceImpl.java
│   │   │               │       ├── RtcChannelServiceImpl.java
│   │   │               │       ├── UserServiceImpl.java
│   │   │               │       └── TokenV2ServiceImpl.java
│   │   │               └── utils                                       // 工具类
│   │   │                   ├── RedisUtil.java                          // Redis操作
│   │   │                   └── TokenUtil.java                          // Token操作
│   │   └── resources
│   │       ├── application.yml                                         // 配置文件
│   │       └── logback-spring.xml                                      // 日志配置
```


