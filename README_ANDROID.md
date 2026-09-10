# Smriti Sathi — Native Android (Java)

This is the native Android Studio conversion of the supplied React Native/Expo mobile app. The Android layer is written in **Java** and keeps the supplied Node.js/Express + MongoDB backend as a separate `backend/` project.

## Open in Android Studio
1. Extract this folder.
2. Open the **SmritiSathiAndroid** folder in Android Studio.
3. Let Gradle sync and install any requested SDK components.
4. Create/start an Android Emulator (API 35 recommended).
5. Start the backend first (instructions below).
6. Run the `app` configuration.

## Backend
```bash
cd backend
npm install
# create .env with MONGO_URI and JWT_SECRET (and Cloudinary values if using photo upload)
npm start
```
The Android emulator reaches a backend running on your computer through `http://10.0.2.2:5000`.
For a physical phone, change `ApiClient.BASE_URL` in `app/src/main/java/com/smritisathi/app/core/ApiClient.java` to your computer's LAN IP, e.g. `http://192.168.1.10:5000/api`.

## Included native features
- Login/register and secure local session storage via SharedPreferences
- Patient dashboard with time-based greeting and rotating daily game
- Memory Match (12 cards / 6 pairs)
- Object Recall (5-second memorize phase + 9-item recall grid)
- Pattern Recognition (5 rounds)
- Game result submission and offline SQLite queue
- Automatic sync when connectivity returns
- Progress/latest cognitive assessment
- Reminder list and Android notifications with Done/Snooze actions
- Talk to Sathi using Android SpeechRecognizer + TextToSpeech
- Family Memory Moments retrieval
- Caregiver portal, patient overview and alerts
- Invite-code patient linking
- English/Assamese phrase resources can be expanded in `core/Localization.java` if needed

## Important source-project notes
The supplied files contain a few endpoint/model inconsistencies. The Android client uses the actual route mounted by the supplied backend (`/api/moments`, `/api/sessions`, `/api/reminders/.../status`) rather than the inconsistent URLs used in some original React Native files.

The source upload did not include the six Memory Match images, object images, or pattern icon images referenced by the React Native screens. The native version therefore uses text/emoji representations for those game items so the app remains runnable. Replace them with the original images in `res/drawable` when available.

The supplied caregiver patient-list and alert/analytics UI refers to endpoints that are not present in the uploaded backend. The native app retains the screens and uses the existing session statistics endpoint where available, with safe demo fallback content for the missing caregiver endpoints.

The supplied legal disclaimer is retained conceptually: this platform supports cognitive engagement and monitoring and is not a diagnostic tool.
