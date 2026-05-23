# Android Mobile Design — Home Screen Reference

Source: https://developer.android.com/design/ui/mobile/guides/home-screen/

---

## Table of Contents
1. [Notifications](#1-notifications)
2. [App Widgets](#2-app-widgets)

---

## 1. Notifications

Source: https://developer.android.com/design/ui/mobile/guides/home-screen/notifications

### Notification Anatomy
```
┌─────────────────────────────────────────────────────┐
│ [App Icon]  App Name  •  Time              [Actions]│
│─────────────────────────────────────────────────────│
│             Title (bold)                            │
│             Body text (up to 2 lines collapsed)     │
│             [Media image - optional]                │
│─────────────────────────────────────────────────────│
│  [Action 1]          [Action 2]         [Action 3] │
└─────────────────────────────────────────────────────┘
```

### Notification Styles
| Style | When to Use | Class |
|---|---|---|
| **Default** | Simple title + body | `NotificationCompat.Builder` |
| **BigText** | Long body text (expand to show) | `BigTextStyle` |
| **BigPicture** | Image-rich content (article, media) | `BigPictureStyle` |
| **Inbox** | List of messages/items | `InboxStyle` |
| **Messaging** | Conversations between people | `MessagingStyle` |
| **MediaPlayer** | Audio playback controls | `MediaStyle` |
| **CallStyle** | Incoming/ongoing calls | `CallStyle` |
| **Progress** | Background task progress | Builder `setProgress()` |

```kotlin
// Basic notification
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)  // Required — shown in status bar
    .setContentTitle("New message")
    .setContentText("You have a new message from Alice")
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setAutoCancel(true)  // dismiss on tap
    .setContentIntent(pendingIntent)  // tap action
    .build()

// Messaging style (for chat apps)
val person = Person.Builder()
    .setName("Alice")
    .setIcon(IconCompat.createWithBitmap(avatar))
    .build()

val style = NotificationCompat.MessagingStyle(myPerson)
    .addMessage("Hey!", timestamp1, person)
    .addMessage("Are you free tonight?", timestamp2, person)

NotificationCompat.Builder(context, CHANNEL_ID)
    .setStyle(style)
    .setSmallIcon(R.drawable.ic_message)
    .build()
```

### Notification Channels (Required API 26+)
```kotlin
// Create channel (call in Application.onCreate or before first notification)
val channel = NotificationChannel(
    "messages",
    "Messages",
    NotificationManager.IMPORTANCE_DEFAULT
).apply {
    description = "Chat message notifications"
    enableLights(true)
    lightColor = Color.BLUE
    enableVibration(true)
}
val notificationManager = getSystemService(NotificationManager::class.java)
notificationManager.createNotificationChannel(channel)
```

### Importance Levels
| Importance | Behavior | Use For |
|---|---|---|
| `IMPORTANCE_HIGH` (Urgent) | Sound + heads-up popup | Calls, alarms, time-critical |
| `IMPORTANCE_DEFAULT` | Sound, no popup | Messages, reminders |
| `IMPORTANCE_LOW` | No sound/popup; shows in shade | Background status, progress |
| `IMPORTANCE_MIN` | No sound, collapsed | Silent background info |
| `IMPORTANCE_NONE` | Blocked | — |

**Rules:**
- Never use IMPORTANCE_HIGH for non-urgent notifications — users will block your channel
- Match importance to user expectation; err low rather than high

### Notification Permissions (Android 13+)
```kotlin
// Request POST_NOTIFICATIONS permission
val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (!isGranted) {
        // Show rationale or gracefully degrade
    }
}

if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != GRANTED) {
    requestPermissionLauncher.launch(POST_NOTIFICATIONS)
}
```

### Notification Actions
```kotlin
// Direct reply action (inline reply)
val remoteInput = RemoteInput.Builder("reply_key")
    .setLabel("Reply")
    .build()

val replyAction = NotificationCompat.Action.Builder(
    R.drawable.ic_reply,
    "Reply",
    replyPendingIntent
).addRemoteInput(remoteInput).build()

NotificationCompat.Builder(context, CHANNEL_ID)
    .addAction(replyAction)
    .build()
```

### Notification Design Rules
| DO | DON'T |
|---|---|
| Use for user-relevant, timely info | Send marketing/promotional notifications without explicit consent |
| Provide clear, actionable content | Use vague titles like "Update available" |
| Match small icon to app brand | Use full-color images as small icon (must be alpha-only) |
| Group related notifications | Flood with many separate notifications |
| Allow users to configure channels | Use single channel for all notification types |
| Use MessagingStyle for chats | Build custom chat notification from scratch |
| Auto-cancel when tapped | Leave notifications persisted unless showing active status |

### Grouping Notifications
```kotlin
// Group summary
val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setStyle(NotificationCompat.InboxStyle()
        .addLine("Alice: Hey!")
        .addLine("Bob: Can we meet?")
        .setSummaryText("2 new messages"))
    .setGroup(GROUP_KEY)
    .setGroupSummary(true)
    .build()

// Individual notifications in group
val notification1 = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("Alice")
    .setContentText("Hey!")
    .setGroup(GROUP_KEY)
    .build()
```

### Notification Testing Checklist
- [ ] Displays correctly in collapsed state
- [ ] Expands correctly with BigText/BigPicture/MessagingStyle
- [ ] Actions work (including inline reply)
- [ ] Tap opens correct in-app destination
- [ ] Channel configured with appropriate importance
- [ ] Small icon is alpha-only (white with transparency)
- [ ] Works with notification permission denied
- [ ] Groups properly when multiple notifications arrive

---

## 2. App Widgets

Source: https://developer.android.com/design/ui/mobile/guides/home-screen/app-widgets

### What App Widgets Are
- Mini app UIs on the home screen
- Update periodically or in response to events
- Tap areas launch app or trigger actions
- Cannot use arbitrary Views — limited to `RemoteViews` (Views) or Glance (Compose-like)

### Widget Categories
| Type | Description | Example |
|---|---|---|
| **Informational** | Displays data, updates automatically | Weather, news, stocks |
| **Control** | Triggers app actions | Music controls, smart home |
| **Hybrid** | Both info and controls | Calendar with add button |
| **Collection** | Scrollable list of items | Email list, tasks |

### Widget Sizes & Grid System
Android home screen uses a grid (typically 4×5 or 5×6 columns):
| Size Name | Min Columns × Rows | dp Approximate |
|---|---|---|
| Small | 2×1 | 110×40dp |
| Medium | 2×2 | 110×110dp |
| Medium-wide | 4×2 | 270×110dp |
| Large | 4×4 | 270×270dp |

Widgets should support **resizing** — define `minWidth/minHeight` and `maxResizeWidth/maxResizeHeight` in config.

### Widget Design Principles
1. **Glanceable:** Info readable in < 1 second; no interaction needed to get value
2. **Relevant:** Surface the most important content, not everything
3. **Branded:** Use app colors + shapes; recognizable
4. **Timely:** Update at appropriate intervals; stale data is worse than no data
5. **Tappable:** Make it clear what happens when tapped; launch app to appropriate screen

### Glance (Compose for Widgets) — Recommended
```kotlin
// Gradle
implementation("androidx.glance:glance-appwidget:1.x.x")
implementation("androidx.glance:glance-material3:1.x.x")

// Widget receiver
class MyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MyWidgetContent()
            }
        }
    }
}

@Composable
fun MyWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(16.dp)
            .appWidgetBackground()
    ) {
        Text(
            "Widget Title",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            "Content here",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
        )
        Button(
            text = "Open App",
            onClick = actionStartActivity<MainActivity>()
        )
    }
}

// Receiver declaration in manifest
class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MyWidget()
}
```

```xml
<!-- AndroidManifest.xml -->
<receiver android:name=".MyWidgetReceiver"
          android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/my_widget_info"/>
</receiver>
```

```xml
<!-- res/xml/my_widget_info.xml -->
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:targetCellWidth="2"
    android:targetCellHeight="1"
    android:maxResizeWidth="250dp"
    android:maxResizeHeight="110dp"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="1800000"  <!-- 30 minutes — minimum: 30 min -->
    android:widgetCategory="home_screen"
    android:previewLayout="@layout/my_widget_preview"
    android:description="@string/widget_description"/>
```

### Update Strategies
| Method | When |
|---|---|
| `updatePeriodMillis` | Periodic, battery-friendly (min 30 min) |
| `WorkManager` | Precise scheduling, background data refresh |
| `AppWidgetManager.updateAppWidget()` | On-demand from app |
| `PendingIntent` | User interaction triggers update |

```kotlin
// Manual update from WorkManager
class WidgetUpdateWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        MyWidget().updateAll(applicationContext)
        return Result.success()
    }
}
```

### Widget Configuration Activity
Optional — shown when user adds widget:
```kotlin
class WidgetConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: return finish()

        // Set result CANCELED by default (if user presses back)
        setResult(RESULT_CANCELED)

        // Show configuration UI
        setContent {
            ConfigScreen(
                onConfirm = { config ->
                    saveConfig(appWidgetId, config)
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    })
                    finish()
                }
            )
        }
    }
}
```

### Widget Design Checklist
- [ ] Glanceable at a glance — key info visible without interaction
- [ ] Works at minimum size (2×1 or configured min)
- [ ] Scales well to larger sizes
- [ ] Supports light and dark mode (`GlanceTheme` handles automatically)
- [ ] Rounded corners match Android's system widget style
- [ ] Tappable areas are clear (open app or trigger action)
- [ ] Update interval is appropriate — not too frequent (battery), not too stale
- [ ] Preview image set in `appwidget-provider` for widget picker
- [ ] Content description on all interactive elements

### Widget DO / DON'T
| DO | DON'T |
|---|---|
| Show most relevant/timely data | Replicate full app UI |
| Use system `GlanceTheme` for colors | Hardcode colors that break dark mode |
| Provide tap-to-open-app CTA | Make widget require complex interaction |
| Handle loading/error states gracefully | Show blank widget when data fails |
| Support dynamic resizing | Lock to single size |
| Set appropriate update frequency | Update every minute (huge battery drain) |
