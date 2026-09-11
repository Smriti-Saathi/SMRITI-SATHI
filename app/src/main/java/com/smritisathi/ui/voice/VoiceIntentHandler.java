package com.smritisathi.ui.voice;

import java.util.Locale;

/**
 * Keyword-matching VoiceIntent parser for Smriti Sathi voice assistant.
 * Handles the 4 core memory-care voice queries:
 * 1. "what do I have to do today"
 * 2. "when is my medicine"
 * 3. "start my memory game"
 * 4. "play simon says"
 * Includes robust natural variations for elderly speech.
 */
public class VoiceIntentHandler {

    public enum VoiceIntent {
        TODAY_AGENDA,
        NEXT_MEDICINE,
        START_MEMORY_GAME,
        PLAY_SIMON_SAYS,
        UNKNOWN
    }

    public static VoiceIntent parseIntent(String spokenText) {
        if (spokenText == null || spokenText.trim().isEmpty()) {
            return VoiceIntent.UNKNOWN;
        }

        String text = spokenText.toLowerCase(Locale.ENGLISH).trim();

        // 1. "what do I have to do today"
        if (text.contains("what do i have to do today")
                || text.contains("what do i have to do")
                || text.contains("what to do today")
                || text.contains("what should i do today")
                || text.contains("my schedule")
                || text.contains("today's schedule")
                || text.contains("today schedule")
                || text.contains("today's tasks")
                || text.contains("what is my agenda")
                || text.contains("my tasks today")) {
            return VoiceIntent.TODAY_AGENDA;
        }

        // 2. "when is my medicine"
        if (text.contains("when is my medicine")
                || text.contains("when is my medication")
                || text.contains("when is my next medicine")
                || text.contains("medicine time")
                || text.contains("when to take medicine")
                || text.contains("when should i take my medicine")
                || text.contains("when are my pills")
                || text.contains("when is my pill")
                || text.contains("my medicine")
                || text.contains("next medicine")) {
            return VoiceIntent.NEXT_MEDICINE;
        }

        // 3. "start my memory game"
        if (text.contains("start my memory game")
                || text.contains("start memory game")
                || text.contains("play memory game")
                || text.contains("start memory match")
                || text.contains("play memory match")
                || text.contains("memory game")
                || text.contains("memory match")
                || text.contains("start the memory game")) {
            return VoiceIntent.START_MEMORY_GAME;
        }

        // 4. "play simon says"
        if (text.contains("play simon says")
                || text.contains("start simon says")
                || text.contains("simon says")
                || text.contains("play simon")
                || text.contains("start simon")
                || text.contains("sequencing game")
                || text.contains("color sequence game")) {
            return VoiceIntent.PLAY_SIMON_SAYS;
        }

        return VoiceIntent.UNKNOWN;
    }
}
