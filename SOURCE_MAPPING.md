# React Native -> Native Android (Java) mapping

| Supplied source | Native Android replacement |
|---|---|
| App.js / AppNavigator.js | LoginActivity + Activity navigation |
| AuthContext.js | core/Session.java |
| NetworkContext.js | BaseActivity connectivity banner |
| localDb.js | core/OfflineDb.java |
| syncManager.js | core/SyncManager.java |
| notificationService.js | reminders/NotificationScheduler.java + ReminderReceiver.java |
| voiceIntents.js | voice/VoiceIntents.java |
| phrases.js | core/Localization.java |
| PatientDashboardScreen.js | PatientDashboardActivity.java |
| MemoryMatchScreen.js | games/MemoryMatchActivity.java |
| ObjectRecallScreen.js | games/ObjectRecallActivity.java |
| PatternRecognitionScreen.js | games/PatternRecognitionActivity.java |
| ProgressScreen.js | progress/ProgressActivity.java |
| ReminderScreen.js | reminders/ReminderActivity.java |
| TalkToSathiScreen.js | voice/TalkToSathiActivity.java |
| VoiceAssistantScreen.js | voice/VoiceAssistantActivity.java |
| MemoryMomentsScreen.js | memory/MemoryMomentsActivity.java |
| CaregiverPatientListScreen.js | caregiver/CaregiverPatientListActivity.java |
| CaregiverAlertsScreen.js | caregiver/CaregiverAlertsActivity.java |
| PatientDetailScreen.js | caregiver/PatientDetailActivity.java |
| GameShell.js | games/BaseGameActivity.java + GameBase.java |
| Node/Express backend | backend/ (kept server-side; Android calls its REST API) |
