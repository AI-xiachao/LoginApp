# LoginApp 项目架构文档

## 项目概述
Android 登录应用，采用 Clean Architecture + MVVM 架构，使用 Jetpack Compose 构建 UI。

## 技术栈
- **UI**: Jetpack Compose + Material3
- **架构**: Clean Architecture + MVVM
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp + Gson
- **本地存储**: Room Database
- **图片加载**: Coil
- **异步**: Kotlin Coroutines + Flow

## 项目结构

```
app/src/main/java/com/example/loginapp/
├── data/                          # 数据层
│   ├── di/
│   │   └── DataModule.kt          # Hilt 依赖注入配置
│   ├── datasource/                # 数据源（AccountManager）
│   ├── local/                     # 本地数据源（Room）
│   ├── remote/                    # 远程数据源
│   │   ├── api/                   # API 接口
│   │   └── dto/                   # 数据传输对象
│   └── repository/                # 仓库实现
├── domain/                        # 领域层
│   ├── model/                     # 领域模型
│   ├── repository/                # 仓库接口
│   └── usecase/                   # 用例
├── presentation/                  # 表现层
│   ├── base/
│   │   └── BaseViewModel.kt       # MVI 基础 ViewModel
│   ├── navigation/                # 导航配置
│   ├── theme/                     # Compose 主题
│   ├── login/                     # 登录模块
│   └── home/                      # 首页模块（含天气、随机用户）
└── MainActivity.kt
```

## 架构模式

### MVI 模式 (BaseViewModel)
```kotlin
abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>
```
- **State**: UI 状态（数据驱动 UI）
- **Event**: 用户事件（点击、刷新等）
- **Effect**: 一次性副作用（导航、Toast、Snackbar）

### 数据流
```
UI → Event → ViewModel → UseCase → Repository → (Local/Remote) → State → UI
```

## 关键文件参考

### 首页模块 (Home)
| 文件 | 职责 |
|------|------|
| `HomeScreen.kt` | Compose UI，显示天气卡片、用户信息、随机用户卡片 |
| `HomeViewModel.kt` | 处理认证状态、天气加载/刷新、随机用户加载 |
| `HomeState` | user, randomUser, weather, isLoading, isRefreshing 等 |

### 天气模块
| 文件 | 职责 |
|------|------|
| `WeatherApiService.kt` | open-meteo API 接口（温度、UV、降雨） |
| `IpLocationService.kt` | ipwho.is IP 定位服务（粗略定位） |
| `WeatherRepository.kt/Impl` | 天气数据获取 + Room 缓存（1小时有效期） |
| `GetWeatherUseCase.kt` | 获取缓存天气数据流 |
| `FetchWeatherUseCase.kt` | 强制从网络获取天气 |
| `Weather.kt` | 领域模型（温度、UV、降雨概率、出行建议） |
| `WeatherCacheEntity.kt` | Room 缓存实体 |
| `WeatherCacheDao.kt` | 缓存数据库访问 |

### 网络层
| 文件 | 职责 |
|------|------|
| `DataModule.kt` | Retrofit、OkHttp、Room、Repository 注入配置 |
| `RandomUserApiService.kt` | 随机用户 API 接口示例 |
| `RandomUserRepositoryImpl.kt` | 网络请求 + 本地缓存（Room） |

### UseCase 模式
```kotlin
class FetchXxxUseCase @Inject constructor(private val repository: XxxRepository) {
    suspend operator fun invoke(): Result<Xxx> = repository.fetch()
}
```

## 添加新功能的最佳实践

1. **新增 API**: 在 `data/remote/api/` 创建接口，在 `DataModule.kt` 提供实例
2. **新增仓库**: 先定义接口（domain/repository/），再实现（data/repository/），在 DataModule bind
3. **新增 UseCase**: 在 `domain/usecase/` 创建，遵循单一职责原则
4. **ViewModel 扩展**: 注入 UseCase，在 init/load 中调用，通过 State 驱动 UI
5. **UI 扩展**: 在 Screen Composable 中添加 Section，保持卡片式布局风格

## 依赖添加方式
在 `gradle/libs.versions.toml` 中定义版本和库，在 `app/build.gradle.kts` 中引用。

## 权限配置
AndroidManifest.xml 已配置：
- `INTERNET` / `ACCESS_NETWORK_STATE` - 网络
- `AUTHENTICATE_ACCOUNTS` 等 - AccountManager

## Room 数据库迁移
开发阶段使用破坏性迁移（`fallbackToDestructiveMigration()`），在 `DataModule.kt` 中配置。
