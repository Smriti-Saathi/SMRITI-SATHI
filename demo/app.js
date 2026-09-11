// Smriti Sathi — Interactive Showcase Logic (Sections A through G)

// State Store
const state = {
    activeRole: 'patient',
    activeScreen: 'screenPatientDashboard',
    
    // Patient Data
    patient: {
        name: "Ramesh Sharma",
        age: 72,
        inviteCode: "SM7K9P",
        caregiverName: "Priya Sharma (Daughter)"
    },

    // Reminders
    reminders: [
        { id: '1', title: 'Blood Pressure Medicine', time: '2:00 PM', dosage: '1 Tablet with water after lunch', status: 'Pending' },
        { id: '2', title: 'Gentle Afternoon Walk', time: '4:30 PM', dosage: '15 mins in garden with caregiver', status: 'Pending' },
        { id: '3', title: 'Evening Memory Exercise', time: '6:00 PM', dosage: 'Play Simon Says or Memory Match', status: 'Pending' }
    ],

    // Memory Moments (Reminiscence Therapy)
    memories: [
        {
            id: 'm1',
            name: 'Priya Sharma',
            relationship: 'Your Loving Daughter ❤️',
            story: 'Taken during Diwali celebrations at your home in Jaipur. You lit the diyas together!',
            imageUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80',
            revealed: false
        },
        {
            id: 'm2',
            name: 'Aarav Sharma',
            relationship: 'Your 9-year-old Grandson 🌟',
            story: 'Aarav won his school art prize and couldn\'t wait to bring his painting to show Dadaji!',
            imageUrl: 'https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=500&auto=format&fit=crop&q=80',
            revealed: false
        },
        {
            id: 'm3',
            name: 'Sunita Sharma',
            relationship: 'Your Beloved Wife 🌸',
            story: 'Visiting the botanical garden in Bengaluru during your 40th wedding anniversary.',
            imageUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=80',
            revealed: false
        }
    ],

    // Assessment History (Last 7 Sessions for MPAndroidChart)
    assessments: [
        { session: 1, score: 70, memory: 72, attention: 68, recognition: 75, tier: 'Moderate' },
        { session: 2, score: 74, memory: 75, attention: 70, recognition: 78, tier: 'Moderate' },
        { session: 3, score: 72, memory: 70, attention: 74, recognition: 80, tier: 'Moderate' },
        { session: 4, score: 79, memory: 80, attention: 76, recognition: 84, tier: 'Difficult' },
        { session: 5, score: 76, memory: 78, attention: 72, recognition: 82, tier: 'Difficult' },
        { session: 6, score: 82, memory: 84, attention: 78, recognition: 86, tier: 'Difficult' },
        { session: 7, score: 78, memory: 82, attention: 74, recognition: 88, tier: 'Moderate' }
    ],

    // Active Game Variables
    game: {
        type: null,
        startTime: 0,
        attempts: 0,
        mistakes: 0,
        score: 0,
        simonSequence: [],
        playerSequence: [],
        patternRound: 1
    }
};

// Web Audio API for Simon Says & Games
let audioCtx = null;
function playTone(freq, durationMs = 250) {
    try {
        if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.frequency.value = freq;
        osc.type = 'sine';
        gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + durationMs / 1000);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start();
        osc.stop(audioCtx.currentTime + durationMs / 1000);
    } catch (e) {
        // Audio fallback
    }
}

// ================= INITIALIZATION =================
document.addEventListener('DOMContentLoaded', () => {
    renderReminders();
    renderMemoryMoments();
    renderCaregiverViews();
    updateLiveClock();
    setInterval(updateLiveClock, 30000);
});

function updateLiveClock() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    document.getElementById('liveDeviceTime').textContent = timeStr;
}

// ================= NAVIGATION =================
function switchRole(role) {
    state.activeRole = role;
    document.getElementById('tabPatientMode').classList.toggle('active', role === 'patient');
    document.getElementById('tabCaregiverMode').classList.toggle('active', role === 'caregiver');

    if (role === 'patient') {
        navigateToScreen('screenPatientDashboard');
    } else {
        navigateToScreen('screenCaregiverDashboard');
    }
}

function navigateToScreen(screenId) {
    state.activeScreen = screenId;
    document.querySelectorAll('.screen-view').forEach(s => s.classList.remove('active'));
    
    const target = document.getElementById(screenId);
    if (target) {
        target.classList.add('active');
    }

    // Update sidebar active button
    document.querySelectorAll('.nav-item').forEach(btn => {
        const onClickAttr = btn.getAttribute('onclick') || '';
        btn.classList.toggle('active', onClickAttr.includes(screenId));
    });

    if (screenId === 'screenPatientAnalytics') {
        setTimeout(drawCognitiveTrajectoryChart, 100);
    }
}

// ================= 1. MEMORY MATCH GAME =================
const memoryCards = ['🌸', '🦚', '🪔', '🥭', '🐘', '☀️'];
let flippedIndices = [];
let matchedIndices = [];

function startMemoryMatchGame() {
    state.game.type = 'memoryMatch';
    state.game.startTime = Date.now();
    state.game.attempts = 0;
    state.game.mistakes = 0;
    flippedIndices = [];
    matchedIndices = [];

    document.getElementById('activeGameTitle').textContent = 'Memory Match 🌸';
    document.getElementById('gameLiveStats').textContent = 'Attempts: 0 | Mistakes: 0';
    
    // Duplicate & shuffle
    const deck = [...memoryCards, ...memoryCards].sort(() => Math.random() - 0.5);
    state.game.deck = deck;

    const arena = document.getElementById('gameInteractiveArea');
    arena.innerHTML = `
        <div class="memory-grid" id="memGrid">
            ${deck.map((item, idx) => `
                <div class="memory-card-btn" id="mcard_${idx}" onclick="flipMemoryCard(${idx})">
                    <span class="card-inner" style="display:none;">${item}</span>
                </div>
            `).join('')}
        </div>
    `;

    navigateToScreen('screenActiveGame');
}

function flipMemoryCard(idx) {
    if (flippedIndices.length >= 2 || flippedIndices.includes(idx) || matchedIndices.includes(idx)) return;

    playTone(480, 150);
    flippedIndices.push(idx);

    const card = document.getElementById(`mcard_${idx}`);
    card.classList.add('revealed');
    card.querySelector('.card-inner').style.display = 'block';

    if (flippedIndices.length === 2) {
        state.game.attempts++;
        const [idx1, idx2] = flippedIndices;
        const match = state.game.deck[idx1] === state.game.deck[idx2];

        if (match) {
            playTone(720, 280);
            matchedIndices.push(idx1, idx2);
            document.getElementById(`mcard_${idx1}`).classList.add('matched');
            document.getElementById(`mcard_${idx2}`).classList.add('matched');
            flippedIndices = [];

            if (matchedIndices.length === state.game.deck.length) {
                // Game Completed! Compute scoring via Section D formula
                setTimeout(() => finishGame('memoryMatch'), 500);
            }
        } else {
            state.game.mistakes++;
            playTone(280, 200);
            setTimeout(() => {
                document.getElementById(`mcard_${idx1}`).classList.remove('revealed');
                document.getElementById(`mcard_${idx2}`).classList.remove('revealed');
                document.getElementById(`mcard_${idx1}`).querySelector('.card-inner').style.display = 'none';
                document.getElementById(`mcard_${idx2}`).querySelector('.card-inner').style.display = 'none';
                flippedIndices = [];
            }, 900);
        }

        document.getElementById('gameLiveStats').textContent = 
            `Attempts: ${state.game.attempts} | Mistakes: ${state.game.mistakes}`;
    }
}

// ================= 2. OBJECT RECALL GAME =================
const recallTargets = ['🥭', '🦚', '🪔', '🐘', '🌸'];
const recallDistractors = ['⭐', '🔔', '🍃', '☀️'];
let recallSelected = [];

function startObjectRecallGame() {
    state.game.type = 'objectRecall';
    state.game.startTime = Date.now();
    recallSelected = [];

    document.getElementById('activeGameTitle').textContent = 'Object Recall 🥭';
    document.getElementById('gameLiveStats').textContent = 'Memorize the 5 objects!';

    const arena = document.getElementById('gameInteractiveArea');
    arena.innerHTML = `
        <div class="recall-preview-box" id="recallBox">
            <div class="countdown-big" id="recallTimer">5</div>
            <p>Memorize these 5 objects carefully:</p>
            <div class="recall-targets-display">${recallTargets.join(' ')}</div>
        </div>
        <div id="recallPrompt" style="display:none;">
            <p style="font-size:1.1rem; font-weight:700; text-align:center; margin-bottom:12px;">Tap all 5 objects you saw earlier:</p>
            <div class="recall-selection-grid" id="recallGrid"></div>
            <button class="btn-primary-action green" style="margin-top:16px;" onclick="submitObjectRecall()">Done Selecting ✓</button>
        </div>
    `;

    navigateToScreen('screenActiveGame');

    // 5-second countdown timer
    let count = 5;
    const timer = setInterval(() => {
        count--;
        playTone(520, 100);
        const timerEl = document.getElementById('recallTimer');
        if (timerEl) timerEl.textContent = count;

        if (count <= 0) {
            clearInterval(timer);
            document.getElementById('recallBox').style.display = 'none';
            document.getElementById('recallPrompt').style.display = 'block';
            document.getElementById('gameLiveStats').textContent = 'Select the 5 memorized objects';
            renderRecallGrid();
        }
    }, 1000);
}

function renderRecallGrid() {
    const allItems = [...recallTargets, ...recallDistractors].sort(() => Math.random() - 0.5);
    const grid = document.getElementById('recallGrid');
    grid.innerHTML = allItems.map((item, idx) => `
        <div class="recall-item-btn" id="ritem_${idx}" onclick="toggleRecallItem('${item}', ${idx})">${item}</div>
    `).join('');
}

function toggleRecallItem(item, idx) {
    const el = document.getElementById(`ritem_${idx}`);
    playTone(440, 120);
    if (recallSelected.includes(item)) {
        recallSelected = recallSelected.filter(x => x !== item);
        el.classList.remove('selected');
    } else {
        recallSelected.push(item);
        el.classList.add('selected');
    }
}

function submitObjectRecall() {
    let correctCount = 0;
    let falsePositives = 0;

    recallSelected.forEach(item => {
        if (recallTargets.includes(item)) correctCount++;
        else falsePositives++;
    });

    const netScore = Math.max(0, correctCount - falsePositives);
    const accuracy = (netScore / 5) * 100;
    state.game.accuracy = accuracy;
    finishGame('objectRecall');
}

// ================= 3. PATTERN RECOGNITION =================
const patternSequences = [
    { seq: ['🔵', '🔺', '🔵'], ans: '🔺', choices: ['🔺', '🟢', '⭐'] },
    { seq: ['⭐', '⭐', '☀️'], ans: '☀️', choices: ['🌸', '☀️', '🔵'] },
    { seq: ['🪔', '🌸', '🪔'], ans: '🌸', choices: ['🪔', '🌸', '🍃'] },
    { seq: ['🐘', '🦚', '🐘'], ans: '🦚', choices: ['🦚', '🥭', '🐘'] },
    { seq: ['🟢', '🟢', '🔵'], ans: '🔵', choices: ['🟢', '🔺', '🔵'] }
];

function startPatternGame() {
    state.game.type = 'pattern';
    state.game.startTime = Date.now();
    state.game.patternRound = 0;
    state.game.correctRounds = 0;
    loadPatternRound();
    navigateToScreen('screenActiveGame');
}

function loadPatternRound() {
    const roundIdx = state.game.patternRound;
    if (roundIdx >= patternSequences.length) {
        finishGame('pattern');
        return;
    }

    const curr = patternSequences[roundIdx];
    document.getElementById('activeGameTitle').textContent = `Pattern Logic (Round ${roundIdx + 1}/5)`;
    document.getElementById('gameLiveStats').textContent = `Score: ${state.game.correctRounds}/${roundIdx}`;

    const arena = document.getElementById('gameInteractiveArea');
    arena.innerHTML = `
        <div class="pattern-sequence-box">
            <p style="font-size:1.05rem; font-weight:700;">What shape comes next in the sequence?</p>
            <div class="pattern-row">
                <span>${curr.seq[0]}</span>
                <span>${curr.seq[1]}</span>
                <span>${curr.seq[2]}</span>
                <div class="pattern-blank">?</div>
            </div>
        </div>
        <p style="font-size:1rem; font-weight:700; margin:14px 0 6px;">Choose the matching shape:</p>
        <div class="pattern-choices-row">
            ${curr.choices.map(choice => `
                <button class="pattern-choice-btn" onclick="choosePatternOption('${choice}')">${choice}</button>
            `).join('')}
        </div>
    `;
}

function choosePatternOption(choice) {
    const curr = patternSequences[state.game.patternRound];
    if (choice === curr.ans) {
        playTone(680, 200);
        state.game.correctRounds++;
    } else {
        playTone(260, 200);
    }
    state.game.patternRound++;
    setTimeout(loadPatternRound, 400);
}

// ================= 4. SIMON SAYS (Sensory Working Memory) =================
const simonColors = ['green', 'red', 'yellow', 'blue'];
const simonFrequencies = { green: 330, red: 440, yellow: 554, blue: 659 };

function startSimonSaysGame() {
    state.game.type = 'simonSays';
    state.game.startTime = Date.now();
    state.game.simonSequence = [];
    state.game.playerSequence = [];
    state.game.simonScore = 0;

    document.getElementById('activeGameTitle').textContent = 'Simon Says 🎵';
    document.getElementById('gameLiveStats').textContent = 'Watch the flashing sequence!';

    const arena = document.getElementById('gameInteractiveArea');
    arena.innerHTML = `
        <div class="simon-grid">
            <button class="simon-btn green" id="simonBtn_green" onclick="tapSimonColor('green')"></button>
            <button class="simon-btn red" id="simonBtn_red" onclick="tapSimonColor('red')"></button>
            <button class="simon-btn yellow" id="simonBtn_yellow" onclick="tapSimonColor('yellow')"></button>
            <button class="simon-btn blue" id="simonBtn_blue" onclick="tapSimonColor('blue')"></button>
        </div>
        <p id="simonPrompt" style="text-align:center; font-size:1.15rem; font-weight:700; margin-top:16px; color:var(--primary);">
            Get Ready...
        </p>
    `;

    navigateToScreen('screenActiveGame');
    setTimeout(nextSimonRound, 1000);
}

function nextSimonRound() {
    state.game.playerSequence = [];
    const nextColor = simonColors[Math.floor(Math.random() * simonColors.length)];
    state.game.simonSequence.push(nextColor);

    document.getElementById('simonPrompt').textContent = 'Watch the sequence... 🌸';
    document.getElementById('gameLiveStats').textContent = `Sequence Length: ${state.game.simonSequence.length}`;

    // Play sequence
    state.game.simonSequence.forEach((color, i) => {
        setTimeout(() => flashSimonButton(color), (i + 1) * 750);
    });

    setTimeout(() => {
        document.getElementById('simonPrompt').textContent = 'Now repeat the sequence! Tap the buttons:';
    }, (state.game.simonSequence.length + 1) * 750);
}

function flashSimonButton(color) {
    playTone(simonFrequencies[color], 300);
    const btn = document.getElementById(`simonBtn_${color}`);
    if (btn) {
        btn.classList.add('lit');
        setTimeout(() => btn.classList.remove('lit'), 350);
    }
}

function tapSimonColor(color) {
    flashSimonButton(color);
    state.game.playerSequence.push(color);

    const step = state.game.playerSequence.length - 1;
    if (state.game.playerSequence[step] !== state.game.simonSequence[step]) {
        // Mistake!
        playTone(200, 400);
        document.getElementById('simonPrompt').textContent = 'Good try! That was a lovely sequencing exercise!';
        setTimeout(() => finishGame('simonSays'), 800);
        return;
    }

    if (state.game.playerSequence.length === state.game.simonSequence.length) {
        // Sequence Complete!
        playTone(880, 250);
        if (state.game.simonSequence.length >= 4) {
            document.getElementById('simonPrompt').textContent = 'Fantastic working memory & rhythm! 🌟';
            setTimeout(() => finishGame('simonSays'), 800);
        } else {
            document.getElementById('simonPrompt').textContent = 'Great rhythm! Adding one more note... 🎵';
            setTimeout(nextSimonRound, 1200);
        }
    }
}

// ================= GAME COMPLETION & SCORING SERVICE (SECTION D & E) =================
function finishGame(gameType) {
    const durationMs = Date.now() - state.game.startTime;
    let accuracy = 85;

    if (gameType === 'memoryMatch') {
        accuracy = Math.round((6 / Math.max(6, state.game.attempts)) * 100);
    } else if (gameType === 'objectRecall') {
        accuracy = Math.round(state.game.accuracy || 80);
    } else if (gameType === 'pattern') {
        accuracy = Math.round((state.game.correctRounds / 5) * 100);
    } else if (gameType === 'simonSays') {
        accuracy = Math.min(100, Math.round((state.game.simonSequence.length / 4) * 100));
    }

    // SECTION D: Spring Boot ScoringService Rule Formula:
    // 40% accuracy + 25% reaction time + 20% consistency + 15% completion rate
    const normalizedReaction = Math.max(0, 100 - Math.min((durationMs / 1000) * 8, 100));
    const consistency = 90; // Default for MVP
    const completionRate = 100;
    
    const overallScore = Math.round(
        (0.40 * accuracy) +
        (0.25 * normalizedReaction) +
        (0.20 * consistency) +
        (0.15 * completionRate)
    );

    const tier = overallScore >= 75 ? 'Difficult' : (overallScore >= 45 ? 'Moderate' : 'Easy');

    // Record into assessment history (Section E Retrofit Bridge equivalent)
    state.assessments.push({
        session: state.assessments.length + 1,
        score: overallScore,
        memory: accuracy,
        attention: normalizedReaction,
        recognition: Math.min(100, overallScore + 5),
        tier: tier
    });

    // Positive Encouraging Overlay (Always positive, never negative)
    const positiveMessages = [
        "Your memory and focus are bright like a blooming garden! 🌸",
        "Wonderful rhythm and attention today! Every practice keeps your mind sharp! ☀️",
        "Brilliant effort! Your dedication to your wellness is truly inspiring! 🌟",
        "Great job! Smiling and exercising your brain brings joy to your day! 🌺"
    ];
    const randomMsg = positiveMessages[Math.floor(Math.random() * positiveMessages.length)];

    document.getElementById('overlayTitle').textContent = 'Wonderful Effort! 🌸';
    document.getElementById('overlayMessage').textContent = randomMsg;
    document.getElementById('overlayScoreBadge').textContent = 
        `Cognitive Wellness Score: ${overallScore}/100 (${tier} Tier)`;

    document.getElementById('modalBackdrop').style.display = 'block';
    document.getElementById('modalPositiveOverlay').style.display = 'flex';
}

function closePositiveOverlay() {
    closeAllModals();
    navigateToScreen('screenPatientDashboard');
    showToast('Exercise saved to your cognitive health trajectory! 🌿');
}

function confirmExitGame() {
    if (confirm("Would you like to take a gentle rest and return to dashboard?")) {
        navigateToScreen('screenPatientDashboard');
    }
}

// ================= REMINDERS CRUD =================
function renderReminders() {
    const container = document.getElementById('remindersListContainer');
    if (!container) return;

    container.innerHTML = state.reminders.map(rem => `
        <div class="reminder-item-card ${rem.status === 'Completed' ? 'completed' : ''}" id="rem_${rem.id}">
            <div class="rem-top-row">
                <span class="rem-title">${rem.title}</span>
                <span class="rem-status-tag ${rem.status === 'Completed' ? 'completed' : ''}">${rem.status}</span>
            </div>
            <div class="rem-time">⏰ Scheduled for ${rem.time}</div>
            <div class="rem-dosage">${rem.dosage}</div>
            ${rem.status !== 'Completed' ? `
                <div class="rem-action-row">
                    <button class="btn-rem-done" onclick="markReminderDone('${rem.id}')">Done ✓</button>
                    <button class="btn-rem-later" onclick="remindLater('${rem.id}')">Remind Later ⏰</button>
                </div>
            ` : ''}
        </div>
    `).join('');
}

function markReminderDone(id) {
    const rem = state.reminders.find(r => r.id === id);
    if (rem) {
        rem.status = 'Completed';
        playTone(600, 200);
        showToast(`Marked ${rem.title} as completed! Great job! 🌸`);
        renderReminders();
    }
}

function remindLater(id) {
    const rem = state.reminders.find(r => r.id === id);
    if (rem) {
        rem.time = 'In 15 minutes';
        rem.status = 'Postponed (+15m)';
        playTone(450, 150);
        showToast(`Remind later set: ${rem.title} in 15 minutes! ⏰`);
        renderReminders();
    }
}

function openAddReminderModal() {
    const title = prompt("Enter reminder title (e.g. Evening Medicine):", "Multivitamin Tablet");
    if (title) {
        state.reminders.push({
            id: String(Date.now()),
            title: title,
            time: '8:00 PM',
            dosage: '1 Tablet after dinner',
            status: 'Pending'
        });
        renderReminders();
        showToast('New daily reminder scheduled! ⏰');
    }
}

// ================= MEMORY MOMENTS (Tap to Reveal) =================
function renderMemoryMoments() {
    const container = document.getElementById('memoryMomentsListContainer');
    if (!container) return;

    container.innerHTML = state.memories.map(m => `
        <div class="memory-moment-card" onclick="toggleRevealMemory('${m.id}')">
            <div class="photo-frame">
                <img src="${m.imageUrl}" alt="${m.name}" class="photo-img" onerror="this.src='https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500'">
            </div>
            <div class="tap-reveal-container">
                ${!m.revealed ? `
                    <div class="unrevealed-prompt">
                        <span>Tap to reveal who this is 🌸</span>
                        <span>❤️</span>
                    </div>
                ` : `
                    <div class="revealed-content">
                        <span class="revealed-name">${m.name}</span>
                        <span class="revealed-relationship">${m.relationship}</span>
                        <p class="revealed-story">${m.story}</p>
                        <span style="font-size:0.85rem; color:#94A3B8; margin-top:4px;">Tap again to hide</span>
                    </div>
                `}
            </div>
        </div>
    `).join('');
}

function toggleRevealMemory(id) {
    const mem = state.memories.find(m => m.id === id);
    if (mem) {
        mem.revealed = !mem.revealed;
        playTone(mem.revealed ? 640 : 400, 180);
        renderMemoryMoments();
    }
}

function openAddMemoryModal() {
    const name = prompt("Enter family member's name:", "Kabir Sharma");
    const relation = prompt("Enter relationship (e.g. Grandson):", "Your Youngest Grandson 🌟");
    if (name && relation) {
        state.memories.unshift({
            id: String(Date.now()),
            name: name,
            relationship: relation,
            story: 'Visiting during family reunion last Sunday.',
            imageUrl: 'https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=500',
            revealed: false
        });
        renderMemoryMoments();
        showToast('Memory Moment saved to family album! 📸');
    }
}

// ================= TALK TO SATHI (VOICE COMPANION) =================
let isRecording = false;

function toggleVoiceRecording() {
    isRecording = !isRecording;
    const btn = document.getElementById('btnLiveMic');
    const label = document.getElementById('voiceStatusLabel');

    if (isRecording) {
        playTone(600, 150);
        btn.style.background = '#EF4444';
        label.textContent = 'Listening... Please speak now 🌸';

        // Check if browser SpeechRecognition is available
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (SpeechRecognition) {
            const recognition = new SpeechRecognition();
            recognition.lang = 'en-US';
            recognition.start();
            recognition.onresult = (event) => {
                const query = event.results[0][0].transcript;
                executeVoiceQuery(query);
            };
            recognition.onerror = () => {
                simulateVoiceIntent('when is my medicine');
            };
        } else {
            // Simulated voice input for demo environment
            setTimeout(() => {
                executeVoiceQuery('when is my medicine');
            }, 2000);
        }
    } else {
        btn.style.background = '#F59E0B';
        label.textContent = 'Tap microphone or tap a question below';
    }
}

function simulateVoiceIntent(query) {
    executeVoiceQuery(query);
}

function executeVoiceQuery(query) {
    isRecording = false;
    document.getElementById('btnLiveMic').style.background = '#F59E0B';
    document.getElementById('textUserSpoken').textContent = `"${query}"`;

    const text = query.toLowerCase();
    let responseText = "";

    // 1. "what do I have to do today"
    if (text.includes("what do i have to do") || text.includes("today") || text.includes("schedule")) {
        responseText = "Today you have 3 reminders scheduled! First is Blood Pressure Medicine at 2:00 PM. You also have a brain exercise waiting for you! Would you like to start your memory game? 🌸";
    }
    // 2. "when is my medicine"
    else if (text.includes("medicine") || text.includes("medication") || text.includes("pill")) {
        responseText = "Your next medicine is Blood Pressure Tablet, scheduled for 2:00 PM. The dosage is 1 tablet with a full glass of water. Take care! 💊";
    }
    // 3. "start my memory game"
    else if (text.includes("memory game") || text.includes("memory match")) {
        responseText = "Starting your Memory Match game now! Have fun matching the pairs! 🧠";
        setTimeout(startMemoryMatchGame, 1800);
    }
    // 4. "play simon says"
    else if (text.includes("simon says") || text.includes("simon")) {
        responseText = "Let's play Simon Says! Watch the colorful lights and repeat the melody! 🎵";
        setTimeout(startSimonSaysGame, 1800);
    }
    else {
        responseText = `I heard: "${query}". You can ask me: "What do I have to do today?", "When is my medicine?", "Start my memory game", or "Play Simon Says".`;
    }

    document.getElementById('textSathiSpoken').textContent = `"${responseText}"`;
    document.getElementById('voiceStatusLabel').textContent = 'Sathi replied:';
    speakVoiceResponse(responseText);
}

function speakVoiceResponse(text) {
    if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        const cleanText = text.replace(/[🌸💊🧠🎵☀️🌟🌺]/g, '');
        const utter = new SpeechSynthesisUtterance(cleanText);
        utter.rate = 0.92; // Slightly slower for elderly comprehension
        utter.pitch = 1.05;
        window.speechSynthesis.speak(utter);
    }
}

function replaySathiAudio() {
    const text = document.getElementById('textSathiSpoken').textContent;
    speakVoiceResponse(text);
}

// ================= CAREGIVER VIEWS =================
function renderCaregiverViews() {
    // 1. Render Linked Patients
    const patContainer = document.getElementById('patientListContainer');
    if (patContainer) {
        const patients = [
            { name: 'Ramesh Sharma', age: 72, lastActive: '15 mins ago', score: 78, dot: 'green', statusText: 'Flourishing 🌸' },
            { name: 'Kamla Verma', age: 68, lastActive: '3 hours ago', score: 62, dot: 'amber', statusText: 'Moderate Practice 🌟' },
            { name: 'Harish Patel', age: 76, lastActive: 'Yesterday', score: 42, dot: 'red', statusText: 'Attention Needed 🌿' }
        ];

        patContainer.innerHTML = patients.map(p => `
            <div class="card-elderly" style="padding:14px; margin-bottom:10px;" onclick="navigateToScreen('screenPatientAnalytics')">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <h4 style="font-size:1.15rem; font-weight:700;">${p.name} (${p.age} yrs)</h4>
                        <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">Last active: ${p.lastActive}</p>
                        <p style="font-size:0.9rem; font-weight:700; color:var(--primary); margin-top:4px;">Status: ${p.statusText}</p>
                    </div>
                    <div style="text-align:center;">
                        <span class="status-dot-large ${p.dot}"></span>
                        <span style="font-size:0.85rem; font-weight:800; display:block; margin-top:4px;">${p.score}</span>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // 2. Render Caregiver Alerts
    const alertContainer = document.getElementById('caregiverAlertsContainer');
    if (alertContainer) {
        const alerts = [
            {
                type: 'SCORE_DROP',
                severity: 'CRITICAL',
                badgeColor: '#FEE2E2',
                textColor: '#DC2626',
                title: 'Score Drop > 15% in 7 Days',
                patient: 'Harish Patel',
                desc: 'Cognitive assessment dropped from 58 to 42 over the past week. Please schedule a gentle review.',
                time: 'Detected 2 hours ago'
            },
            {
                type: 'MISSED_MEDICINE',
                severity: 'WARNING',
                badgeColor: '#FEF3C7',
                textColor: '#D97706',
                title: 'Missed Medicine in Last 24h',
                patient: 'Kamla Verma',
                desc: '1 scheduled blood pressure reminder from yesterday afternoon is still marked Pending.',
                time: 'Detected 5 hours ago'
            },
            {
                type: 'INACTIVITY',
                severity: 'WARNING',
                badgeColor: '#FEF3C7',
                textColor: '#D97706',
                title: 'Zero Brain Games in 3 Days',
                patient: 'Harish Patel',
                desc: 'No cognitive exercises or memory games recorded in over 3 days. Encourage a brief 5-minute round today.',
                time: 'Detected yesterday'
            }
        ];

        alertContainer.innerHTML = alerts.map(a => `
            <div class="card-elderly" style="padding:16px; margin-bottom:12px; border-left: 4px solid ${a.textColor};">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-size:0.8rem; font-weight:800; background:${a.badgeColor}; color:${a.textColor}; padding:2px 8px; border-radius:8px;">
                        ${a.severity}
                    </span>
                    <span style="font-size:0.75rem; color:var(--text-tertiary);">${a.time}</span>
                </div>
                <h4 style="font-size:1.1rem; font-weight:700; margin-top:8px;">${a.title}</h4>
                <p style="font-size:0.9rem; font-weight:700; color:var(--primary); margin-top:2px;">Patient: ${a.patient}</p>
                <p style="font-size:0.95rem; color:var(--text-secondary); margin-top:6px; line-height:1.4;">${a.desc}</p>
            </div>
        `).join('');
    }
}

// ================= CHART VISUALIZATION (MPAndroidChart equivalent) =================
function drawCognitiveTrajectoryChart() {
    const canvas = document.getElementById('canvasCognitiveChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.clearRect(0, 0, width, height);

    // Padding
    const pTop = 20;
    const pBottom = 30;
    const pLeft = 30;
    const pRight = 20;

    const chartW = width - pLeft - pRight;
    const chartH = height - pTop - pBottom;

    const scores = state.assessments.map(a => a.score);
    const minVal = 40;
    const maxVal = 100;

    // Draw Grid Lines
    ctx.strokeStyle = '#E2E8F0';
    ctx.lineWidth = 1;
    for (let g = 0; g <= 4; g++) {
        const y = pTop + (chartH / 4) * g;
        ctx.beginPath();
        ctx.moveTo(pLeft, y);
        ctx.lineTo(width - pRight, y);
        ctx.stroke();

        const labelVal = maxVal - g * 15;
        ctx.fillStyle = '#94A3B8';
        ctx.font = '10px Plus Jakarta Sans';
        ctx.fillText(labelVal, 6, y + 3);
    }

    // Coordinates
    const points = scores.map((score, i) => {
        const x = pLeft + (chartW / (scores.length - 1)) * i;
        const y = pTop + chartH - ((score - minVal) / (maxVal - minVal)) * chartH;
        return { x, y, score };
    });

    // Draw Gradient Area under Curve
    const grad = ctx.createLinearGradient(0, pTop, 0, height - pBottom);
    grad.addColorStop(0, 'rgba(26, 95, 122, 0.4)');
    grad.addColorStop(1, 'rgba(224, 242, 254, 0.05)');

    ctx.beginPath();
    ctx.moveTo(points[0].x, height - pBottom);
    points.forEach((pt, i) => {
        if (i === 0) ctx.lineTo(pt.x, pt.y);
        else {
            const prev = points[i - 1];
            const cpX1 = prev.x + (pt.x - prev.x) / 2;
            ctx.bezierCurveTo(cpX1, prev.y, cpX1, pt.y, pt.x, pt.y);
        }
    });
    ctx.lineTo(points[points.length - 1].x, height - pBottom);
    ctx.closePath();
    ctx.fillStyle = grad;
    ctx.fill();

    // Draw Cubic-Bezier Curve Line
    ctx.beginPath();
    ctx.strokeStyle = '#1A5F7A';
    ctx.lineWidth = 3;
    points.forEach((pt, i) => {
        if (i === 0) ctx.moveTo(pt.x, pt.y);
        else {
            const prev = points[i - 1];
            const cpX1 = prev.x + (pt.x - prev.x) / 2;
            ctx.bezierCurveTo(cpX1, prev.y, cpX1, pt.y, pt.x, pt.y);
        }
    });
    ctx.stroke();

    // Draw Points & Tooltips
    points.forEach(pt => {
        ctx.beginPath();
        ctx.arc(pt.x, pt.y, 5, 0, Math.PI * 2);
        ctx.fillStyle = '#159895';
        ctx.fill();
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 2;
        ctx.stroke();

        // Label above point
        ctx.fillStyle = '#0F172A';
        ctx.font = 'bold 11px Plus Jakarta Sans';
        ctx.fillText(pt.score, pt.x - 7, pt.y - 10);
    });
}

// ================= LINK PATIENT MODAL =================
function openLinkPatientModal() {
    const code = prompt("Enter patient's 6-character invite code (e.g. SM7K9P):", "SM7K9P");
    if (code) {
        if (code.trim().toUpperCase() === "SM7K9P") {
            showToast("Successfully linked to Ramesh Sharma! 🌸");
            navigateToScreen('screenPatientList');
        } else {
            alert("Invalid invite code. Please verify the 6-character code with your patient.");
        }
    }
}

// ================= TOAST =================
function showToast(msg) {
    const toast = document.getElementById('appToast');
    if (toast) {
        toast.textContent = msg;
        toast.style.display = 'block';
        setTimeout(() => toast.style.display = 'none', 3200);
    }
}

function closeAllModals() {
    document.getElementById('modalBackdrop').style.display = 'none';
    document.getElementById('modalPositiveOverlay').style.display = 'none';
}
