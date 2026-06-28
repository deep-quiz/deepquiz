package com.example

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Question
import com.example.presentation.quiz.QuizScreen
import com.example.presentation.quiz.QuizStats
import com.example.presentation.quiz.QuizViewModel
import com.example.ui.theme.MyApplicationTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

// Sound feedback helper using lightweight system ToneGenerator
class SoundFeedbackManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCorrect() {
        try {
            // High-pitched pleasant beep
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playIncorrect() {
        // Soft error double buzzer sound
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 220)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                QuizApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun QuizApp(viewModel: QuizViewModel) {
    val screenState by viewModel.currentScreen.collectAsStateWithLifecycle()
    val statsState by viewModel.stats.collectAsStateWithLifecycle()
    val aiExplanation by viewModel.aiExplanation.collectAsStateWithLifecycle()
    val aiTutorHint by viewModel.aiTutorHint.collectAsStateWithLifecycle()
    val aiPerformanceAnalysis by viewModel.aiPerformanceAnalysis.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    // Sound feedback manager lifetime synced with screen
    val soundManager = remember { SoundFeedbackManager() }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            AnimatedContent(
                targetState = screenState,
                transitionSpec = {
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(500))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(500)))
                },
                label = "ScreenTransition"
            ) { state ->
                when (state) {
                    is QuizScreen.Welcome -> {
                        WelcomeScreen(
                            categories = state.categories,
                            stats = statsState,
                            isAiLoading = isAiLoading,
                            onCategorySelect = { category ->
                                soundManager.playClick()
                                viewModel.startQuiz(category)
                            },
                            onClearStats = {
                                soundManager.playClick()
                                viewModel.clearStats()
                            },
                            onMultiplayerClicked = {
                                soundManager.playClick()
                                viewModel.navigateToMultiplayer()
                            }
                        )
                    }

                    is QuizScreen.ActiveQuiz -> {
                        QuizScreen(
                            state = state,
                            soundManager = soundManager,
                            aiExplanation = aiExplanation,
                            aiTutorHint = aiTutorHint,
                            isAiLoading = isAiLoading,
                            onGetExplanation = { viewModel.getExplanation() },
                            onGetTutorHint = { viewModel.getTutorHint() },
                            onOptionSelected = { index ->
                                viewModel.selectOption(index)
                            },
                            onNextClicked = {
                                soundManager.playClick()
                                viewModel.nextQuestion()
                            },
                            onQuitClicked = {
                                soundManager.playClick()
                                viewModel.quitQuiz()
                            },
                            onFiftyFifty = {
                                soundManager.playClick()
                                viewModel.useFiftyFifty()
                            },
                            onSkip = {
                                soundManager.playClick()
                                viewModel.useSkip()
                            },
                            onTogglePause = {
                                soundManager.playClick()
                                viewModel.togglePause()
                            }
                        )
                    }

                    is QuizScreen.Result -> {
                        ResultScreen(
                            state = state,
                            soundManager = soundManager,
                            aiPerformanceAnalysis = aiPerformanceAnalysis,
                            isAiLoading = isAiLoading,
                            onGetPerformanceAnalysis = { viewModel.getPerformanceAnalysis() },
                            onRestart = {
                                soundManager.playClick()
                                viewModel.startQuiz(state.category)
                            },
                            onHome = {
                                soundManager.playClick()
                                viewModel.quitQuiz()
                            }
                        )
                    }

                    is QuizScreen.Multiplayer -> {
                        com.example.presentation.multiplayer.MultiplayerScreen(
                            onNavigateBack = {
                                soundManager.playClick()
                                viewModel.quitQuiz() // or a separate goHome method
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    categories: List<String>,
    stats: QuizStats,
    isAiLoading: Boolean,
    onCategorySelect: (String) -> Unit,
    onClearStats: () -> Unit,
    onMultiplayerClicked: () -> Unit = {}
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
        item {
            // AI Sparkle Illustration
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "PulseAnimation")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Scale"
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .shadow(12.dp, CircleShape)
                        .background(Color(0xFF080612), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Decoration",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Deep Quiz",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Test your intelligence across multiple subjects!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Stats Dashboard Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_card")
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Stats Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your Statistics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        if (stats.totalQuizzes > 0) {
                            IconButton(
                                onClick = { showConfirmClear = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Statistics",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (stats.totalQuizzes == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No quiz history yet. Select a category below to write your first high score!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        // Level and Coins row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Coins
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${stats.coins}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            // Streak
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Streak", tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${stats.streak} Streak", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stats.totalQuizzes.toString(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Quizzes Played",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(50.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Lvl ${stats.level}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "${stats.xp} XP",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Score progress visualization
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            val animatedProgress by animateFloatAsState(
                                targetValue = stats.averageAccuracy / 100f,
                                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                                label = "progress"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = animatedProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                            )
                        }

                        if (stats.highScoresByCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Personal Bests",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                stats.highScoresByCategory.forEach { (cat, score) ->
                                    val catColor = getCategoryColor(cat)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                catColor.copy(alpha = 0.15f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$cat: $score",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = catColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Button(
                onClick = onMultiplayerClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Multiplayer Mode (Online)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Selection Grid Title
        item {
            Text(
                text = "Select Quiz Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )
        }

        // Categories List
        itemsIndexed(categories) { _, category ->
            CategoryCard(
                categoryName = category,
                isLoading = isAiLoading && category == "AI Generated",
                onClick = { if (!isAiLoading) onCategorySelect(category) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to delete all historical quiz results? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearStats()
                        showConfirmClear = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    categoryName: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val themeColor = getCategoryColor(categoryName)
    val categoryIcon = getCategoryIcon(categoryName)
    val subtitleText = when (categoryName) {
        "Mixed" -> "15 questions • Random variety challenges"
        "AI Generated" -> "Unlimited questions • Generated via Gemini API"
        else -> "10 questions • Curated $categoryName topic"
    }
    
    // Glassmorphism modifier
    val glassModifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .clickable(onClick = onClick)
        .background(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            shape = RoundedCornerShape(20.dp)
        )
        .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp)
        )
        .clip(RoundedCornerShape(20.dp))
        .padding(4.dp) // inner spacing

    Box(
        modifier = glassModifier
            .testTag("category_card_${categoryName.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                themeColor.copy(alpha = 0.2f),
                                themeColor.copy(alpha = 0.05f)
                            )
                        ),
                        CircleShape
                    )
                    .border(1.dp, themeColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = "$categoryName Category Icon",
                    tint = themeColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categoryName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow right icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizScreen(
    state: QuizScreen.ActiveQuiz,
    soundManager: SoundFeedbackManager,
    aiExplanation: String?,
    aiTutorHint: String?,
    isAiLoading: Boolean,
    onGetExplanation: () -> Unit,
    onGetTutorHint: () -> Unit,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onQuitClicked: () -> Unit,
    onFiftyFifty: () -> Unit,
    onSkip: () -> Unit,
    onTogglePause: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showQuitDialog by remember { mutableStateOf(false) }

    // Audio/haptic trigger on state changes
    LaunchedEffect(state.isAnswered) {
        if (state.isAnswered && state.selectedOptionIndex != null) {
            val isCorrect = state.selectedOptionIndex == state.currentQuestion.correctAnswerIndex
            if (isCorrect) {
                soundManager.playCorrect()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                soundManager.playIncorrect()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(20.dp)
        ) {
        // Quiz Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showQuitDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quit Quiz",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Timer
            Box(
                modifier = Modifier
                    .background(
                        if (state.timeRemaining <= 5 && !state.isAnswered) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), 
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏱ ${state.timeRemaining}s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (state.timeRemaining <= 5 && !state.isAnswered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Pause Button
            IconButton(
                onClick = onTogglePause,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))

            // Lifelines
            IconButton(
                onClick = onFiftyFifty,
                enabled = !state.fiftyFiftyUsed && !state.isAnswered,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (state.fiftyFiftyUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Text("50", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (state.fiftyFiftyUsed) Color.Gray else MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSkip,
                enabled = !state.skipUsed && !state.isAnswered,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (state.skipUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Skip",
                    tint = if (state.skipUsed) Color.Gray else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Current Question score
            Text(
                text = "Score: ${state.score}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Custom progress bar with bouncing progress
        val progressPercent = (state.currentQuestionIndex + 1).toFloat() / state.totalQuestions
        val animatedProgress by animateFloatAsState(
            targetValue = progressPercent,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "ProgressAnimation"
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Progress",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Q: ${state.currentQuestionIndex + 1} / ${state.totalQuestions}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(getCategoryColor(state.category), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question display card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("question_card")
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.currentQuestion.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options List (4 options)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.currentQuestion.options.forEachIndexed { index, option ->
                if (state.eliminatedOptions.contains(index)) {
                    // Hide this option completely or render as invisible box to keep space
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)))
                    return@forEachIndexed
                }
                
                val isSelected = state.selectedOptionIndex == index
                val isCorrectAnswer = state.currentQuestion.correctAnswerIndex == index

                // Color coding depending on answer selection
                val containerColor = when {
                    !state.isAnswered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    isCorrectAnswer -> {
                        // Green for correct option
                        Color(0xFF10B981).copy(alpha = 0.2f)
                    }
                    isSelected -> {
                        // Selected incorrect option appears red
                        Color(0xFFEF4444).copy(alpha = 0.2f)
                    }
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                }

                val borderColor = when {
                    !state.isAnswered -> {
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
                    }
                    isCorrectAnswer -> Color(0xFF10B981)
                    isSelected -> Color(0xFFEF4444)
                    else -> Color.White.copy(alpha = 0.05f)
                }

                val textColor = when {
                    !state.isAnswered -> MaterialTheme.colorScheme.onSurface
                    isCorrectAnswer -> Color(0xFF10B981)
                    isSelected -> Color(0xFFEF4444)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }

                val optionWeight = if (isCorrectAnswer || isSelected) FontWeight.Bold else FontWeight.Medium

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(enabled = !state.isAnswered, onClick = {
                            onOptionSelected(index)
                        })
                        .testTag("option_${index}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(if (state.isAnswered && (isCorrectAnswer || isSelected)) 1.5.dp else 1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Number circle (A, B, C, D)
                        val badgeBg = when {
                            !state.isAnswered -> Color.White.copy(alpha = 0.05f)
                            isCorrectAnswer -> Color(0xFF10B981).copy(alpha = 0.15f)
                            isSelected -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.02f)
                        }

                        val badgeText = when {
                            !state.isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                            isCorrectAnswer -> Color(0xFF10B981)
                            isSelected -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(badgeBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + index).toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeText
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = option,
                            fontSize = 15.sp,
                            fontWeight = optionWeight,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )

                        // Action feedback icons
                        if (state.isAnswered) {
                            if (isCorrectAnswer) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct Choice",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Incorrect Choice",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Tutor Hint (Shown before answering)
        AnimatedVisibility(
            visible = !state.isAnswered,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (aiTutorHint != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Star, contentDescription = "AI Hint", tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = aiTutorHint,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onGetTutorHint,
                        enabled = !isAiLoading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Star, contentDescription = "Get AI Hint", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AI Tutor")
                        }
                    }
                }
            }
        }

        // Animated reveal feedback box & button
        AnimatedVisibility(
            visible = state.isAnswered,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val isCorrect = state.selectedOptionIndex == state.currentQuestion.correctAnswerIndex
            val feedbackBg = if (isCorrect) {
                if (isSystemInDarkTheme()) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFE8F5E9)
            } else {
                if (isSystemInDarkTheme()) Color(0xFFC62828).copy(alpha = 0.2f) else Color(0xFFFFEBEE)
            }
            val accentColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFD32F2F)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = feedbackBg),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Result feedback header",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCorrect) "Correct! 🎉" else "Incorrect Answer!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = state.currentQuestion.explanation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // AI Explanation Section
                    if (aiExplanation != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = "AI Insights", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Explanation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiExplanation,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onGetExplanation,
                            enabled = !isAiLoading,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Get AI Explanation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNextClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getCategoryColor(state.category)
                        )
                    ) {
                        Text(
                            text = if (state.currentQuestionIndex == state.totalQuestions - 1) "View Results" else "Next Question",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next arrow icon",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    }
    
    if (state.isPaused) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onTogglePause() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Pause, contentDescription = "Paused", tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("PAUSED", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap anywhere to resume", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit Game?") },
            text = { Text("Are you sure you want to end this quiz? Your progress in this game will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onQuitClicked()
                        showQuitDialog = false
                    }
                ) {
                    Text("Quit Quiz", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ResultScreen(
    state: QuizScreen.Result,
    soundManager: SoundFeedbackManager,
    aiPerformanceAnalysis: String?,
    isAiLoading: Boolean,
    onGetPerformanceAnalysis: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val scorePercentage = (state.score.toFloat() / state.totalQuestions) * 100f

    val gradeTitle = when {
        scorePercentage == 100f -> "PERFECT SCORE! 🌟"
        scorePercentage >= 80f -> "EXCELLENT JOB! 🏆"
        scorePercentage >= 50f -> "GOOD EFFORT! 👍"
        else -> "KEEP PRACTICING! 📚"
    }

    val gradeMsg = when {
        scorePercentage == 100f -> "You are a total trivia deity! Flawless knowledge!"
        scorePercentage >= 80f -> "Outstanding performance! You really know your stuff!"
        scorePercentage >= 50f -> "Great job! A few minor errors, but you did solid!"
        else -> "Every expert was once a beginner. Keep learning and try again!"
    }

    val gradeColor = when {
        scorePercentage >= 80f -> Color(0xFF2E7D32)
        scorePercentage >= 50f -> Color(0xFFF57C00)
        else -> Color(0xFFD32F2F)
    }

    // Trigger score sounds based on results
    LaunchedEffect(Unit) {
        if (scorePercentage >= 80f) {
            soundManager.playCorrect()
        } else {
            soundManager.playIncorrect()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (scorePercentage == 100f) {
            ConfettiOverlay()
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
        item {
            Text(
                text = "Quiz Complete",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Score ring visualization
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                gradeColor.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(130.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 10.dp
                )

                // Foreground ring representing score
                CircularProgressIndicator(
                    progress = { state.score.toFloat() / state.totalQuestions },
                    modifier = Modifier.size(130.dp),
                    color = gradeColor,
                    strokeWidth = 10.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.score}/${state.totalQuestions}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${"%.0f".format(scorePercentage)}% Accuracy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = gradeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grade feedback
            Text(
                text = gradeTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = gradeColor
            )

            Text(
                text = gradeMsg,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            // AI Performance Analysis Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "AI Analysis", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Performance Coach", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (aiPerformanceAnalysis != null) {
                        Text(
                            text = aiPerformanceAnalysis,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    } else {
                        Button(
                            onClick = onGetPerformanceAnalysis,
                            enabled = !isAiLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Get AI Performance Analysis")
                            }
                        }
                    }
                }
            }
        }

        // Action controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("home_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home Button",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Home")
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("restart_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getCategoryColor(state.category)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Button",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Again", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Review title
        item {
            Text(
                text = "Question Review",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )
        }

        // Question details list
        itemsIndexed(state.answeredQuestions) { index, pair ->
            val question = pair.first
            val selectedOption = pair.second
            val isCorrect = selectedOption == question.correctAnswerIndex

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isCorrect) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("review_card_${index}")
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            if (isCorrect) Color(0xFF10B981).copy(alpha = 0.05f) else Color(0xFFEF4444).copy(alpha = 0.05f)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = if (isCorrect) "Correct" else "Wrong",
                            tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = question.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Answers breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val correctText = question.options.getOrNull(question.correctAnswerIndex) ?: ""
                        val selectedText = selectedOption?.let { question.options.getOrNull(it) } ?: "Unanswered"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Correct Answer: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = correctText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        if (!isCorrect) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Your Answer: ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = question.explanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
    }
}

// Helper styling generators
private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Science" -> Color(0xFF2E7D32)         // Emerald Green
        "Technology" -> Color(0xFF1565C0)      // Ocean Blue
        "General Knowledge" -> Color(0xFF8E24AA) // Vivid Purple
        "AI Generated" -> Color(0xFFFF8F00)      // Amber/Gold
        else -> Color(0xFFD81B60)               // Vibrant Pink / Mixed
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Science" -> Icons.Default.Star
        "Technology" -> Icons.Default.Settings
        "General Knowledge" -> Icons.Default.Info
        "AI Generated" -> Icons.Default.Star
        else -> Icons.Default.Refresh
    }
}

// FlowRow implementation for compact styling support in standard Compose
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Standard Column wrapper as a safe, highly stable fallback for flexible FlowRow layouts
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        // Simple chunking layout to display personal high scores nicely
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement
        ) {
            content()
        }
    }
}

@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_y"
    )

    Box(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        for (i in 0..30) {
            val startX = (i * 10) + (Math.random() * 20 - 10).toFloat()
            val speedMultiplier = 0.5f + (Math.random() * 1.5).toFloat()
            val color = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta).random()
            
            Box(
                modifier = Modifier
                    .offset(x = startX.dp, y = (yOffset * speedMultiplier).dp)
                    .size(8.dp)
                    .background(color, CircleShape)
            )
        }
    }
}
