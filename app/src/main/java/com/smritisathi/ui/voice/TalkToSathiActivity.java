package com.smritisathi.ui.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.smritisathi.R;
import com.smritisathi.data.FirestoreHelper;
import com.smritisathi.data.SessionManager;
import com.smritisathi.model.Reminder;
import com.smritisathi.ui.games.MemoryMatchActivity;
import com.smritisathi.ui.games.SimonSaysActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * TalkToSathiActivity — Voice Companion for elderly and dementia care.
 * Built with native Android SpeechRecognizer and TextToSpeech.
 *
 * Implements VoiceIntents keyword-matching for:
 * 1. "what do I have to do today"
 * 2. "when is my medicine"
 * 3. "start my memory game"
 * 4. "play simon says"
 */
public class TalkToSathiActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 101;

    private MaterialButton btnBack;
    private View viewMicPulse;
    private MaterialButton btnMicToggle;
    private TextView tvVoiceStatus;
    private TextView tvUserSpokenText;
    private TextView tvSathiResponseText;
    private MaterialButton btnReplaySpeech;

    private MaterialButton chipAgenda;
    private MaterialButton chipMedicine;
    private MaterialButton chipMemoryGame;
    private MaterialButton chipSimonSays;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private boolean isListening = false;
    private String lastSpokenResponse = "";

    private FirestoreHelper firestoreHelper;
    private SessionManager sessionManager;
    private String patientId;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_to_sathi);

        firestoreHelper = FirestoreHelper.getInstance();
        sessionManager = new SessionManager(this);
        patientId = sessionManager.getUserId();

        initViews();
        setupListeners();
        initTts();
        initSpeechRecognizer();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackFromVoice);
        viewMicPulse = findViewById(R.id.viewMicPulse);
        btnMicToggle = findViewById(R.id.btnMicToggle);
        tvVoiceStatus = findViewById(R.id.tvVoiceStatus);
        tvUserSpokenText = findViewById(R.id.tvUserSpokenText);
        tvSathiResponseText = findViewById(R.id.tvSathiResponseText);
        btnReplaySpeech = findViewById(R.id.btnReplaySpeech);

        chipAgenda = findViewById(R.id.chipAgenda);
        chipMedicine = findViewById(R.id.chipMedicine);
        chipMemoryGame = findViewById(R.id.chipMemoryGame);
        chipSimonSays = findViewById(R.id.chipSimonSays);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMicToggle.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                checkPermissionAndStartListening();
            }
        });

        btnReplaySpeech.setOnClickListener(v -> {
            if (!lastSpokenResponse.isEmpty()) {
                speak(lastSpokenResponse);
            }
        });

        // Quick Suggestion Chips
        chipAgenda.setOnClickListener(v -> executeVoiceIntent("what do I have to do today"));
        chipMedicine.setOnClickListener(v -> executeVoiceIntent("when is my medicine"));
        chipMemoryGame.setOnClickListener(v -> executeVoiceIntent("start my memory game"));
        chipSimonSays.setOnClickListener(v -> executeVoiceIntent("play simon says"));
    }

    private void initTts() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.US);
            }
            isTtsReady = true;
            // Welcome greeting
            String greeting = "Hello! I am Sathi, your memory companion. Tap the microphone and tell me what you need.";
            tvSathiResponseText.setText(greeting);
            speak(greeting);
        }
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    tvVoiceStatus.setText("Listening... Please speak now 🌸");
                    viewMicPulse.setAlpha(0.6f);
                }

                @Override
                public void onBeginningOfSpeech() {
                    tvVoiceStatus.setText("Hearing your voice...");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Micro-pulse animation based on voice volume
                    float scale = 1.0f + Math.max(0f, Math.min(0.4f, rmsdB / 25f));
                    viewMicPulse.setScaleX(scale);
                    viewMicPulse.setScaleY(scale);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                    tvVoiceStatus.setText("Thinking & understanding...");
                    resetMicVisuals();
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    resetMicVisuals();
                    String message = "Tap the microphone to try again";
                    if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                        message = "I couldn't hear clearly. Please tap and try again 🌸";
                    }
                    tvVoiceStatus.setText(message);
                }

                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    resetMicVisuals();
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String query = matches.get(0);
                        executeVoiceIntent(query);
                    } else {
                        tvVoiceStatus.setText("Tap the microphone to speak");
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    private void checkPermissionAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO
            );
        } else {
            startListening();
        }
    }

    private void startListening() {
        if (speechRecognizer == null) {
            initSpeechRecognizer();
        }

        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening to you...");

            speechRecognizer.startListening(intent);
            tvVoiceStatus.setText("Preparing microphone...");
        } else {
            Toast.makeText(this, "Voice recognition unavailable on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        isListening = false;
        resetMicVisuals();
        tvVoiceStatus.setText("Tap the microphone to speak");
    }

    private void resetMicVisuals() {
        viewMicPulse.setAlpha(0.25f);
        viewMicPulse.setScaleX(1.0f);
        viewMicPulse.setScaleY(1.0f);
    }

    /**
     * Dispatches query to VoiceIntentHandler and executes the corresponding action.
     */
    private void executeVoiceIntent(String spokenText) {
        tvUserSpokenText.setText(spokenText);
        VoiceIntentHandler.VoiceIntent intent = VoiceIntentHandler.parseIntent(spokenText);

        switch (intent) {
            case TODAY_AGENDA:
                handleTodayAgenda();
                break;

            case NEXT_MEDICINE:
                handleNextMedicine();
                break;

            case START_MEMORY_GAME:
                handleStartMemoryGame();
                break;

            case PLAY_SIMON_SAYS:
                handlePlaySimonSays();
                break;

            case UNKNOWN:
            default:
                handleUnknownQuery(spokenText);
                break;
        }
    }

    /**
     * Intent 1: "what do I have to do today"
     */
    private void handleTodayAgenda() {
        tvVoiceStatus.setText("Checking your reminders for today...");

        firestoreHelper.getRemindersForPatient(patientId)
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        int pendingCount = 0;
                        String nextReminderTitle = "";
                        String nextReminderTime = "";

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Reminder r = doc.toObject(Reminder.class);
                            if (r != null && "Pending".equalsIgnoreCase(r.getStatus())) {
                                pendingCount++;
                                if (nextReminderTitle.isEmpty()) {
                                    nextReminderTitle = r.getTitle() != null ? r.getTitle() : "Medication";
                                    if (r.getScheduledTime() > 0) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                                        nextReminderTime = sdf.format(new Date(r.getScheduledTime()));
                                    }
                                }
                            }
                        }

                        String response;
                        if (pendingCount > 0) {
                            response = "Today you have " + pendingCount + " pending reminder" + (pendingCount > 1 ? "s" : "")
                                    + ". Your next is " + nextReminderTitle
                                    + (nextReminderTime.isEmpty() ? "" : " at " + nextReminderTime)
                                    + ". You also have a brain game waiting for you! Would you like to start your memory game?";
                        } else {
                            response = "You are all caught up on your reminders for today! 🌸 Don't forget to play a friendly brain game to keep your mind sharp.";
                        }
                        displayAndSpeakResponse(response);
                    } else {
                        displayAndSpeakResponse("You have no pending reminders for today! Everything is all clear and peaceful 🌸");
                    }
                })
                .addOnFailureListener(e -> {
                    displayAndSpeakResponse("You have your daily brain exercise to play today, and your loved ones are keeping you in their thoughts 🌸");
                });
    }

    /**
     * Intent 2: "when is my medicine"
     */
    private void handleNextMedicine() {
        tvVoiceStatus.setText("Checking your medication schedule...");

        firestoreHelper.getNextPendingReminder(patientId)
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && !snapshot.isEmpty()) {
                        Reminder r = snapshot.getDocuments().get(0).toObject(Reminder.class);
                        if (r != null) {
                            String medName = r.getMedicineName() != null && !r.getMedicineName().isEmpty()
                                    ? r.getMedicineName() : (r.getTitle() != null ? r.getTitle() : "Medication");

                            String timeStr = "today";
                            if (r.getScheduledTime() > 0) {
                                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                                timeStr = sdf.format(new Date(r.getScheduledTime()));
                            }

                            String dosageStr = r.getDosage() != null && !r.getDosage().isEmpty()
                                    ? " The dosage is " + r.getDosage() + "." : "";

                            String response = "Your next medicine is " + medName + ", scheduled for " + timeStr + "."
                                    + dosageStr + " Remember to drink a glass of water when taking it! 💊";
                            displayAndSpeakResponse(response);
                            return;
                        }
                    }
                    displayAndSpeakResponse("You have no upcoming medicine scheduled right now. You are all up to date! 🌸");
                })
                .addOnFailureListener(e -> {
                    displayAndSpeakResponse("You have no pending medicine reminders at this time. Take care and stay hydrated!");
                });
    }

    /**
     * Intent 3: "start my memory game"
     */
    private void handleStartMemoryGame() {
        String response = "Starting your Memory Match game now! Have fun matching the pictures! 🧠";
        displayAndSpeakResponse(response);

        mainHandler.postDelayed(() -> {
            Intent intent = new Intent(this, MemoryMatchActivity.class);
            startActivity(intent);
        }, 1600);
    }

    /**
     * Intent 4: "play simon says"
     */
    private void handlePlaySimonSays() {
        String response = "Let's play Simon Says! Watch the lights carefully and repeat the sequence! 🎵";
        displayAndSpeakResponse(response);

        mainHandler.postDelayed(() -> {
            Intent intent = new Intent(this, SimonSaysActivity.class);
            startActivity(intent);
        }, 1600);
    }

    /**
     * Fallback for unrecognized phrases
     */
    private void handleUnknownQuery(String query) {
        String response = "I heard you say: \"" + query + "\". You can ask me: \"What do I have to do today?\", \"When is my medicine?\", \"Start my memory game\", or \"Play Simon Says\".";
        displayAndSpeakResponse(response);
    }

    private void displayAndSpeakResponse(String response) {
        tvVoiceStatus.setText("Sathi replied:");
        tvSathiResponseText.setText(response);
        btnReplaySpeech.setVisibility(View.VISIBLE);
        speak(response);
    }

    private void speak(String text) {
        lastSpokenResponse = text;
        if (isTtsReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sathi_voice_" + System.currentTimeMillis());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Microphone permission is needed to talk with Sathi", Toast.LENGTH_LONG).show();
                tvVoiceStatus.setText("Microphone access needed. You can also tap the questions below!");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
