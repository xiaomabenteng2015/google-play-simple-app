# EnTeacher

EnTeacher 是一个离线 Android 英语单词学习应用。当前版本内置 100 个基础单词，通过本地学习、到期复习和学习记录帮助用户维护个人进度。

## 当前功能

- 首页展示总单词数、已学习数、已掌握数、连续学习天数和待复习数量。
- 学习模式从未学习单词中选词，支持查看释义后标记“认识”或“不认识”。
- 复习模式只显示已到复习时间的单词；没有到期单词时不会用未到期内容补位。
- 单词列表展示内置词库及每个单词的当前学习状态。
- 单词状态、学习进度、每日统计和最近 30 天的会话记录保存在本机 `SharedPreferences` 中。
- 正确或错误答案会更新难度、答题计数和下次复习时间；连续学习天数只在完成有效学习或复习会话后更新。
- 系统快捷方式可直接进入学习、复习或录音页面。
- 录音页面支持长按录音，并把文件保存到应用专属目录中的 `voice_input.3gp`。

## 复习规则

应用根据单词当前难度、回答结果和复习次数计算下次复习时间。基础间隔如下：

| 难度 | 回答正确 | 回答错误 |
| --- | ---: | ---: |
| 未知 | 1 天 | 1 天 |
| 困难 | 2 天 | 1 天 |
| 中等 | 5 天 | 2 天 |
| 简单 | 10 天 | 3 天 |
| 已掌握 | 30 天 | 7 天 |

复习次数会在基础间隔上增加倍率，最高为 3 倍。错误答案会降低难度并安排更早复习。

## 技术结构

- Kotlin 与 Android XML/ViewBinding
- Activity + AndroidViewModel + LiveData
- Repository 负责单词与聚合进度
- SharedPreferences + Gson 负责本地持久化
- JUnit 4 + Robolectric 负责 JVM 回归测试
- Android Gradle Plugin 8.6.1、Gradle 8.7、compileSdk/targetSdk 35

主要入口：

- `MainActivity`：首页与学习入口
- `LearningActivity`：学习和复习流程
- `WordListActivity`：单词列表
- `VoiceInputActivity`：录音文件保存

## 构建与验证

环境要求：JDK 17 或更高版本、Android SDK 35。

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease
```

Debug APK 输出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 当前边界

- 录音功能只保存音频文件，不包含语音识别、转写或自动新增单词。
- 没有独立的统计趋势页面或图表。
- 没有词库导入、导出和用户可操作的备份恢复功能。
- 没有账号、云同步或跨设备数据迁移。
- 内置词库固定为 100 个单词，当前没有编辑入口。
