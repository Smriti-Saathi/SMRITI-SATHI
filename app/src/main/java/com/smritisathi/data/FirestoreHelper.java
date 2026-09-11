package com.smritisathi.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smritisathi.model.User;

import java.security.SecureRandom;

/**
 * Helper class providing references and operations for Firestore collections:
 * - users
 * - gameSessions
 * - reminders
 * - memoryMoments
 * - cognitiveAssessments
 */
public class FirestoreHelper {

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_GAME_SESSIONS = "gameSessions";
    public static final String COLLECTION_REMINDERS = "reminders";
    public static final String COLLECTION_MEMORY_MOMENTS = "memoryMoments";
    public static final String COLLECTION_COGNITIVE_ASSESSMENTS = "cognitiveAssessments";

    // Characters for invite code: uppercase alphanumeric excluding ambiguous chars 0, O, 1, I
    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static FirestoreHelper getInstance() {
        return new FirestoreHelper();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_USERS);
    }

    public CollectionReference getGameSessionsCollection() {
        return db.collection(COLLECTION_GAME_SESSIONS);
    }

    public CollectionReference getRemindersCollection() {
        return db.collection(COLLECTION_REMINDERS);
    }

    public CollectionReference getMemoryMomentsCollection() {
        return db.collection(COLLECTION_MEMORY_MOMENTS);
    }

    public CollectionReference getCognitiveAssessmentsCollection() {
        return db.collection(COLLECTION_COGNITIVE_ASSESSMENTS);
    }

    /**
     * Saves or updates a User document in 'users' collection using user's UID as doc ID.
     */
    public Task<Void> saveUser(User user) {
        return getUsersCollection().document(user.getUid()).set(user);
    }

    /**
     * Retrieves a user document by UID.
     */
    public Task<DocumentSnapshot> getUser(String uid) {
        return getUsersCollection().document(uid).get();
    }

    /**
     * Finds a patient document by their 6-character invite code.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> findPatientByInviteCode(String inviteCode) {
        if (inviteCode == null) return null;
        String cleanCode = inviteCode.trim().toUpperCase();
        return getUsersCollection()
                .whereEqualTo("inviteCode", cleanCode)
                .whereEqualTo("role", "patient")
                .limit(1)
                .get();
    }

    /**
     * Atomically links a caregiver to a patient by setting the patient's caregiverId.
     */
    public Task<Void> linkCaregiverToPatient(String patientUid, String caregiverUid) {
        return getUsersCollection().document(patientUid).update("caregiverId", caregiverUid);
    }

    /**
     * Queries the next pending reminder for a given patient, sorted by scheduled time ascending.
     * Required for Section B Patient Dashboard preview.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getNextPendingReminder(String patientId) {
        return getRemindersCollection()
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("status", "Pending")
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .limit(1)
                .get();
    }

    /**
     * Queries the last 10 game sessions for a patient, sorted by timestamp descending.
     * Used by the Section E Retrofit bridge to send recent session history to the scoring service.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getLastTenGameSessions(String patientId) {
        return getGameSessionsCollection()
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get();
    }

    /**
     * Saves a new cognitive assessment result document into the 'cognitiveAssessments' collection.
     */
    public Task<com.google.firebase.firestore.DocumentReference> saveCognitiveAssessment(com.smritisathi.model.CognitiveAssessment assessment) {
        return getCognitiveAssessmentsCollection().add(assessment);
    }

    /**
     * Queries all reminders for a patient.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getRemindersForPatient(String patientId) {
        return getRemindersCollection()
                .whereEqualTo("patientId", patientId)
                .orderBy("scheduledTime", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get();
    }

    /**
     * Updates reminder status ("Completed", "Remind Later", "Pending") and rescheduled time.
     */
    public Task<Void> updateReminderStatus(String reminderId, String status, long newScheduledTime) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", status);
        if (newScheduledTime > 0) {
            updates.put("scheduledTime", newScheduledTime);
        }
        return getRemindersCollection().document(reminderId).update(updates);
    }

    /**
     * Adds a new reminder document to Firestore.
     */
    public Task<com.google.firebase.firestore.DocumentReference> addReminder(com.smritisathi.model.Reminder reminder) {
        return getRemindersCollection().add(reminder);
    }

    /**
     * Queries memory moments for a patient.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getMemoryMomentsForPatient(String patientId) {
        return getMemoryMomentsCollection()
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Adds a new memory moment photo card to Firestore.
     */
    public Task<com.google.firebase.firestore.DocumentReference> addMemoryMoment(com.smritisathi.model.MemoryMoment moment) {
        return getMemoryMomentsCollection().add(moment);
    }

    /**
     * Queries all patients linked to a specific caregiver.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getLinkedPatients(String caregiverUid) {
        return getUsersCollection()
                .whereEqualTo("caregiverId", caregiverUid)
                .whereEqualTo("role", "patient")
                .get();
    }

    /**
     * Queries the last 7 cognitive assessments for chart visualization.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getLastSevenAssessments(String patientId) {
        return getCognitiveAssessmentsCollection()
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .limit(7)
                .get();
    }

    /**
     * Queries the single latest cognitive assessment for a patient.
     */
    public Task<com.google.firebase.firestore.QuerySnapshot> getLatestAssessment(String patientId) {
        return getCognitiveAssessmentsCollection()
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get();
    }

    /**
     * Generates a random 6-character alphanumeric uppercase invite code for patients.
     * Example: "SM7K9P"
     */
    public static String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(INVITE_CHARS.length());
            sb.append(INVITE_CHARS.charAt(index));
        }
        return sb.toString();
    }
}
