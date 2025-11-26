# EmailWatchdog

EmailWatchdog is a small Android app (Jetpack Compose) that displays short summaries of emails stored in Firestore and delivers push notifications using Firebase Cloud Messaging (FCM).

The project uses:
- Kotlin + Jetpack Compose UI
- Firebase Firestore for storing email summaries
- Firebase Cloud Messaging for push notifications
- Android Navigation + ViewModel for app flow
- Markdown renderer (dev.jeziellago.compose.markdowntext) for rich summary display

## Features
- Real-time list of email summaries driven by Firestore snapshot listeners
- Detail screen with rendered markdown summary and actions: share (TODO), delete
- Push notifications via FCM (service registered in `AndroidManifest.xml`) — app subscribes to the `new_summary` topic
- Runtime notification permission request on Android 13+ (POST_NOTIFICATIONS)

## Architecture (high-level)

- UI: Jetpack Compose (single Activity pattern) with `NavHost` handling two routes: summaries list and summary detail.
- State: `SummaryViewModel` holds a `MutableStateFlow<List<EmailSummary>>` and updates it from Firestore.
- Data: Firestore collection `emails_summaries` stores summary documents. Each document is mapped to `EmailSummary`.
- Messaging: `MyFirebaseMessagingService` handles incoming messages and shows notifications; `MainActivity` subscribes the device to a topic (`new_summary`).

### Data model example (Firestore document)

A document in `emails_summaries` should contain fields compatible with `EmailSummary`:

```json
{
  "Summary": "Short summary text or markdown...",
  "TimeDate": "2025-11-26 10:32",
}
```

When a document is read, the app stores the Firestore `document.id` in the `id` field of `EmailSummary`.

## Prerequisites

- Android Studio Flamingo or newer (or command-line Gradle)
- JDK 11
- Android SDK compile/target 36
- A Firebase project with Firestore and Cloud Messaging enabled

## Quick setup

1. Clone the repo and open it in Android Studio.

```bash
git clone <repo-url>
cd emailwatchdog
```

2. Create a Firebase project (console.firebase.google.com) and enable Firestore and Cloud Messaging.

3. Download `google-services.json` for your Android app package id (`app.web.jiniyede.email_watchdog`) and place it in the `app/` directory.

4. (Firestore) Create a collection named `emails_summaries` and add a few documents using the example schema above. Adjust security rules for development (do NOT use open rules in production).

5. Build and run from Android Studio. Or use Gradle from the repo root:

```bash
# assemble a debug build
./gradlew assembleDebug
# install on a connected device
./gradlew installDebug
```

## Run & behavior notes

- On first run the app will request `POST_NOTIFICATIONS` permission on Android 13+.
- The app subscribes to the `new_summary` FCM topic in `MainActivity`. You can send topic messages from the Firebase console or server to notify all subscribers.
- Notifications are handled by `MyFirebaseMessagingService` which creates a notification channel for Android O+.

## FCM quick test (Firebase console)

- Open Firebase console → Cloud Messaging → Compose message → Target: Topic → `new_summary` and send. Devices running the app (and subscribed) should receive a notification.

## Troubleshooting

- Missing `google-services.json`: build will fail. Ensure file is present under `app/`.
- No summaries shown: verify Firestore documents exist in `emails_summaries` and that the app's Firebase credentials match the Firestore instance.
- Notifications not received: check that the device has a network connection, that FCM is enabled in your Firebase project, and that the app successfully subscribed to the topic (logcat shows subscription status).
- Firestore rules: for local testing you can relax rules, but lock them down for production. If the app gets permission errors, check Firestore rules and the app's authentication state.

