# LoginApp

一个基于 Android 原生开发的登录认证示例应用，采用 Clean Architecture 架构

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络请求**: Retrofit2 + OkHttp
- **异步处理**: Kotlin Coroutines + Flow
- **账户管理**: Android AccountManager

## 项目结构

```
app/src/main/java/com/example/loginapp/
├── data/                           # 数据层
│   ├── datasource/                 # 数据源（AccountManager 封装）
│   ├── device/                     # 设备相关（OAID 管理）
│   ├── di/                         # 依赖注入模块
│   ├── remote/                     # 网络层
│   │   ├── api/                    # API 接口
│   │   └── dto/                    # 数据传输对象
│   └── repository/                 # 仓库实现
├── domain/                         # 领域层
│   ├── model/                      # 领域模型
│   ├── repository/                 # 仓库接口
│   └── usecase/                    # 用例
├── presentation/                   # 表现层
│   ├── base/                       # 基础组件
│   ├── home/                       # 首页功能
│   ├── login/                      # 登录功能
│   ├── navigation/                 # 导航配置
│   └── theme/                      # 主题配置
└── MainActivity.kt                 # 主入口
```

## 核心功能

### 1. 登录认证
- 支持 Flyme 账号登录（自动补全 `@flyme.cn` 后缀）
- 密码显隐切换
- 表单验证和错误提示
- 登录状态管理

### 2. 会话管理
- **自动恢复**: 应用启动时自动恢复会话
- **安全存储**: 使用 AccountManager 安全存储 Token
- **退出登录**: 清除账户和会话数据

### 3. 设备标识
- 获取设备唯一标识用于埋点分析

### 4. 架构特点

#### Clean Architecture 分层
- **Domain 层**: 纯 Kotlin，无 Android 依赖，包含业务逻辑
- **Data 层**: 处理数据获取（网络、本地存储）
- **Presentation 层**: UI 逻辑和状态管理

#### 状态管理
- 使用 `StateFlow` 管理 UI 状态
- 使用 `SharedFlow` 处理一次性事件（导航、Toast）
- 单向数据流：`Event -> ViewModel -> State -> UI`

## 构建与运行

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35
- minSdk 24

### 构建命令

```bash
# 调试构建
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 运行测试
./gradlew test
```

### 测试账号
应用当前使用 Mock 数据，可使用以下测试账号：
- 用户名: `test`
- 密码: `123456`

## 权限说明

```xml
<!-- 网络权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 账户管理权限 -->
<uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
```

## 扩展计划

- [ ] 接入真实 Flyme 登录 API
- [ ] 添加手机号验证码登录
- [ ] 实现扫码登录功能
- [ ] 添加生物识别认证（指纹/人脸）
- [ ] 埋点数据上报

## 开源协议

[MIT License](LICENSE)

---

> 本项目为魅族 Android 开发技术示例，展示了 Modern Android Development 的最佳实践。
