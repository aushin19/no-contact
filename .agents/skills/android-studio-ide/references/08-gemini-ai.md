# Android Studio — Gemini in Android Studio

**Sources:**
- https://developer.android.com/gemini-in-android
- https://developer.android.com/studio/preview/gemini

---

## Overview

Gemini is Google's AI assistant integrated into Android Studio. Powered by Gemini models. Available in Android Studio Iguana (2023.2.1)+ as a stable feature.

**Sign in required:** Profile icon in toolbar → sign in with Google account.

---

## Gemini Features

### 1. AI Code Completion (Inline Suggestions)
- Context-aware multi-line code suggestions as you type
- Shows as gray ghost text — press **Tab** to accept
- Press **Escape** to dismiss
- Partial acceptance: `Ctrl+→` / `Cmd+→` to accept word-by-word

**What it suggests:**
- Complete function bodies from signature + docstring
- Boilerplate (data classes, ViewModels, Compose layouts)
- Test cases from function under test
- Android-specific patterns (Composables, Flows, coroutines)

**Enable/disable:**
Settings > Editor > General > Code Completion > **Enable Gemini Code Completion**

---

### 2. Gemini Chat Panel

**Open:** Toolbar Gemini icon (sparkle ✨) or **View > Tool Windows > Gemini**

Chat interface embedded in IDE. Has full context of your open project.

**Capabilities:**
- Explain selected code
- Generate new code from description
- Debug errors (paste stack trace)
- Refactor suggestions
- Code review feedback
- Explain Android concepts
- Generate unit tests

**Context-aware:** Gemini can read your open files, selected code, error messages from build output.

**Use `@` to reference:**
- `@file` — attach current file
- `@project` — reference project context
- `@error` — reference current build error

### Example Prompts
```
# Code generation
"Create a ViewModel for a shopping cart with add, remove, and total price functions using StateFlow"

"Write a Compose screen for a login form with email/password fields and validation"

"Generate a Room DAO for a User entity with CRUD operations"

# Explanation
"Explain what this code does" (with code selected)

"Why does this cause a NullPointerException?" (paste stack trace)

# Refactoring
"Refactor this to use coroutines instead of callbacks"

"Convert this RecyclerView code to Compose LazyColumn"

# Testing
"Write unit tests for this ViewModel using MockK and Turbine"

"Generate Espresso tests for this login screen"
```

---

### 3. Code Transformation

**Right-click selected code > Gemini > ...**:
- **Explain this code** — detailed explanation in chat
- **Add tests** — generates test file for selected class
- **Find potential issues** — code review analysis
- **Generate documentation** — add KDoc/Javadoc
- **Add code** — extend selected code

Or from the editor, after selecting code: look for Gemini lightbulb / sparkle icon in gutter.

---

### 4. Crash Insights (App Quality Insights Integration)

**View > Tool Windows > App Quality Insights** (requires Firebase Crashlytics + signed in)

On a crash report:
- **Gemini "Explain crash"** button → AI analysis of stack trace
- Explains likely cause + suggests fix
- Links to relevant documentation

---

### 5. Studio Bot (Conversational Development)

Full-page chat for complex questions:
- Multi-turn conversations
- Maintains context across follow-up questions
- Can generate complete features step-by-step

**Access:** Gemini panel → maximize → full conversation mode

---

### 6. Rename Suggestions

When refactoring (Shift+F6 to rename):
- Gemini suggests meaningful names based on variable usage/context
- Accept with Tab or type your own

---

### 7. Commit Message Generation

**VCS > Commit** (`Ctrl+K`) → **Generate Commit Message** (Gemini icon in commit message field)

Analyzes diff → suggests conventional commit message.

---

## Gemini API Integration (In-App AI)

For building AI-powered features into your Android app:

### Setup
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
```

```kotlin
// Get API key from https://aistudio.google.com/app/apikey
// Store in local.properties (not in source):
// generativeai.api_key=YOUR_KEY

val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY,
    generationConfig = generationConfig {
        temperature = 0.7f
        maxOutputTokens = 1000
    }
)
```

### Text Generation
```kotlin
suspend fun generateText(prompt: String): String {
    val response = generativeModel.generateContent(prompt)
    return response.text ?: ""
}

// Streaming
generativeModel.generateContentStream(prompt).collect { chunk ->
    print(chunk.text)
}
```

### Multimodal (Text + Image)
```kotlin
val bitmap: Bitmap = // your image
val inputContent = content {
    image(bitmap)
    text("Describe what you see in this image")
}
val response = generativeModel.generateContent(inputContent)
```

### Chat Session
```kotlin
val chat = generativeModel.startChat()

val response1 = chat.sendMessage("Hello, I'm building a fitness app")
val response2 = chat.sendMessage("What features should I prioritize?")
// Chat maintains conversation history automatically
```

---

## Firebase Genkit (Agent Workflows)

For more complex AI workflows with tools, retrieval, etc.:
```kotlin
// Firebase Genkit SDK for Android (preview)
// https://firebase.google.com/docs/genkit
```

---

## Gemini Nano (On-Device)

Available on Pixel 8+ and other AICore-supported devices:
```kotlin
// AICore — on-device Gemini Nano
// Requires: Android 14+, AICore system service
// Use for: private, offline AI processing

val textClassifier = TextClassification.create(context)
```

**Android Studio integration:** Code completion can suggest Gemini Nano APIs when relevant.

---

## Tips for Effective Gemini Use in Studio

**Be specific:**
- ❌ "Write a screen"
- ✅ "Write a Compose screen for displaying a list of products with name, price, and image, using LazyColumn, and a loading/error/success state using sealed class"

**Provide context:**
- Select relevant code before asking
- Paste error messages directly in chat
- Mention libraries you're using (Hilt, Room, Retrofit, etc.)

**Iterate:**
- Ask follow-up questions to refine generated code
- "Make it handle the error case"
- "Add loading state"
- "Write tests for this"

**Verify generated code:**
- Always review AI-generated code before committing
- Check for: hardcoded values, missing null safety, incorrect API usage
- Run tests on generated code

**Use for learning:**
- "Explain why this pattern is used"
- "What are the tradeoffs of this approach?"
- "Show me a better way to do this"
