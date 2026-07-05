package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMindAppShell(viewModel: MainViewModel) {
    val selectedScreen by viewModel.selectedScreen.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val userEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val userName by viewModel.currentUserName.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dynamic ticking for Pomodoro
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.tickPomodoro()
        }
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        if (selectedScreen == AppScreen.AUTH) {
            AuthScreen(onLogin = { name, email ->
                viewModel.registerOrLogin(name, email)
                Toast.makeText(context, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
            })
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        tonalElevation = 8.dp
                    ) {
                        val items = listOf(
                            Triple(AppScreen.DASHBOARD, Icons.Default.Dashboard, "dashboard"),
                            Triple(AppScreen.CHAT, Icons.Default.Chat, "chat_tutor"),
                            Triple(AppScreen.NOTES, Icons.Default.Book, "notes_pdf"),
                            Triple(AppScreen.FLASHCARDS, Icons.Default.Style, "flashcards"),
                            Triple(AppScreen.AI_TOOLS, Icons.Default.AutoAwesome, "ai_tools")
                        )
                        items.forEach { (screen, icon, translationKey) ->
                            val isSelected = selectedScreen == screen || 
                                (screen == AppScreen.CHAT && selectedScreen == AppScreen.CHAT) ||
                                (screen == AppScreen.NOTES && selectedScreen == AppScreen.NOTES)
                            
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectScreen(screen) },
                                icon = { 
                                    Icon(
                                        icon, 
                                        contentDescription = Translations.getText(translationKey, language)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        Translations.getText(translationKey, language),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 10.sp
                                    ) 
                                },
                                modifier = Modifier.testTag("nav_btn_${translationKey}")
                            )
                        }
                    }
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = "App Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        Translations.getText("app_title", language),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        "created by mesum abbas mir",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        },
                        actions = {
                            // Study Streak badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "$streak",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 13.sp
                                )
                            }

                            // Pro Badge / Store Button
                            IconButton(
                                onClick = { viewModel.selectScreen(AppScreen.UPGRADE) },
                                modifier = Modifier.testTag("upgrade_btn")
                            ) {
                                Icon(
                                    Icons.Default.WorkspacePremium,
                                    contentDescription = "Upgrade to Pro",
                                    tint = if (isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectScreen(AppScreen.SETTINGS) },
                                modifier = Modifier.testTag("settings_btn")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        )
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        )
                ) {
                    when (selectedScreen) {
                        AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                        AppScreen.NOTES -> NotesScreen(viewModel = viewModel)
                        AppScreen.FLASHCARDS -> FlashcardsScreen(viewModel = viewModel)
                        AppScreen.POMODORO -> PomodoroScreen(viewModel = viewModel)
                        AppScreen.AI_TOOLS -> AiToolsScreen(viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        AppScreen.UPGRADE -> UpgradeScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// --- AUTH SCREEN ---
@Composable
fun AuthScreen(onLogin: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C3E50),
                        Color(0xFF0F2027)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEC1A252C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "Logo",
                    modifier = Modifier.size(72.dp),
                    tint = Color(0xFF00E5FF)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "StudyMind AI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        "created by mesum abbas mir",
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontStyle = FontStyle.Italic
                    )
                }

                Text(
                    "Your personal AI Tutor and Spaced Learning Hub",
                    textAlign = TextAlign.Center,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF00E5FF),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF00E5FF),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            onLogin(name, email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isSignUp) "Sign Up" else "Log In",
                        color = Color(0xFF0F2027),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.testTag("auth_toggle_btn")
                ) {
                    Text(
                        if (isSignUp) "Already have an account? Log In" else "Create a new account? Sign Up",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Divider(color = Color.DarkGray, thickness = 1.dp)

                // Google Sign In Mock Button
                OutlinedButton(
                    onClick = { onLogin("Google Learner", "learner@gmail.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_auth_btn"),
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Google Icon",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Continue with Google", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// --- DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val userName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val dailyChatCount by viewModel.dailyChatCount.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val todayGoals by viewModel.todayGoals.collectAsStateWithLifecycle()

    var goalText by remember { mutableStateOf("") }
    var goalMinutes by remember { mutableStateOf("30") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.firstOrNull()?.uppercase() ?: "S",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Column {
                            Text(
                                "${Translations.getText("welcome", language)}, $userName!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                if (isPremium) Translations.getText("pro_badge", language) else Translations.getText("free_badge", language),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "\"The beautiful thing about learning is that no one can take it away from you.\" — B.B. King",
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Streak & Usage Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                Translations.getText("streak", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${viewModel.streak.value} ${Translations.getText("days", language)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Daily Limit Progress Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DataUsage,
                                contentDescription = "Usage",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                Translations.getText("daily_limit", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isPremium) {
                            Text("Unlimited", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                        } else {
                            Text("$dailyChatCount / 10", fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(Translations.getText("chats_used", language), fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    Translations.getText("quick_actions", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val actions = listOf(
                        Quadruple(AppScreen.POMODORO, Icons.Default.Timer, "pomodoro_timer", Color(0xFFE91E63)),
                        Quadruple(AppScreen.CHAT, Icons.Default.School, "chat_tutor", Color(0xFF2196F3)),
                        Quadruple(AppScreen.NOTES, Icons.Default.Description, "notes_pdf", Color(0xFF4CAF50)),
                        Quadruple(AppScreen.FLASHCARDS, Icons.Default.Layers, "flashcards", Color(0xFF9C27B0)),
                        Quadruple(AppScreen.AI_TOOLS, Icons.Default.Calculate, "math_solver", Color(0xFFFF9800))
                    )
                    items(actions) { action ->
                        Button(
                            onClick = { viewModel.selectScreen(action.first) },
                            colors = ButtonDefaults.buttonColors(containerColor = action.fourth.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .testTag("quick_act_${action.third}")
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, action.fourth.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(action.second, contentDescription = null, tint = action.fourth)
                                Text(
                                    Translations.getText(action.third, language),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Study Planner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        Translations.getText("study_planner", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = goalText,
                            onValueChange = { goalText = it },
                            placeholder = { Text(Translations.getText("enter_goal", language), fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("planner_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = goalMinutes,
                            onValueChange = { goalMinutes = it },
                            placeholder = { Text("Min", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(70.dp)
                                .testTag("planner_min_input"),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (goalText.trim().isNotEmpty()) {
                                    val mins = goalMinutes.toIntOrNull() ?: 30
                                    viewModel.addStudyGoal(goalText, mins)
                                    goalText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("add_goal_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (todayGoals.isEmpty()) {
                        Text(
                            Translations.getText("no_goals", language),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayGoals.forEach { goal ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = goal.isCompleted,
                                        onCheckedChange = { viewModel.toggleGoalCompleted(goal) },
                                        modifier = Modifier.testTag("goal_chk_${goal.id}")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            goal.task,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (goal.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "${goal.spentMinutes} / ${goal.targetMinutes} ${Translations.getText("minutes", language)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteGoal(goal.id) },
                                        modifier = Modifier.testTag("del_goal_${goal.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Chats & Notes list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    Translations.getText("recent_chats", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (sessions.isEmpty()) {
                    Text("No recent sessions yet. Tap 'AI Chat Tutor' to start studying!", fontSize = 12.sp, color = Color.Gray)
                } else {
                    sessions.take(3).forEach { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectChatSession(session.id) }
                                .testTag("session_item_${session.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(session.subject, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Ads Section if Free Account
        if (!isPremium) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "SPONSORED ADVERTISEMENT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tired of ads and limits? Upgrade to StudyMind Pro for unlimited learning support and instant expert study tools!",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.selectScreen(AppScreen.UPGRADE) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Remove Ads", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- CHAT SCREEN ---
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Computer Science") }
    val listState = rememberLazyListState()

    // Scroll to bottom when message list changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (activeSessionId == null) {
        // Chat Directory Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                Translations.getText("new_chat", language),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            // Subject selector buttons
            val subjects = listOf("Computer Science", "Mathematics", "Physics", "Chemistry", "English Grammar", "Islamic Studies")
            Text("Select Study Subject:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subject ->
                    FilterChip(
                        selected = selectedSubject == subject,
                        onClick = { selectedSubject = subject },
                        label = { Text(subject) }
                    )
                }
            }

            Button(
                onClick = { viewModel.startNewChat("Study Session: $selectedSubject", selectedSubject) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_chat_session"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.School, contentDescription = null)
                    Text("Start AI Tutor Session")
                }
            }

            Divider()

            Text(
                Translations.getText("recent_chats", language),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sessions) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectChatSession(session.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.title, fontWeight = FontWeight.Bold)
                                Text(session.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteChat(session.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Individual Chat Bubble Layout
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectChatSession(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    val activeSession = sessions.find { it.id == activeSessionId }
                    Text(activeSession?.title ?: "AI Tutor Session", fontWeight = FontWeight.Bold)
                    Text(activeSession?.subject ?: "StudyMind AI", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isUser = message.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.82f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 16.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    message.text,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    if (!isUser) {
                                        IconButton(
                                            onClick = { viewModel.toggleMessageFavorite(message.id, !message.isFavorite) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                if (message.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Favorite",
                                                tint = if (message.isFavorite) Color(0xFFFFD700) else Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message Box Text Entry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .windowInsetsPadding(WindowInsets.ime),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text(Translations.getText("type_message", language)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val activeSub = sessions.find { it.id == activeSessionId }?.subject ?: "General"
                        viewModel.sendChatMessage(textInput, activeSub)
                        textInput = ""
                    })
                )
                IconButton(
                    onClick = {
                        val activeSub = sessions.find { it.id == activeSessionId }?.subject ?: "General"
                        viewModel.sendChatMessage(textInput, activeSub)
                        textInput = ""
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(48.dp)
                        .testTag("chat_send_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

// --- NOTES & PDF SUMMARIZER ---
@Composable
fun NotesScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val isSummarizing by viewModel.isSummarizing.collectAsStateWithLifecycle()
    val summarizedResult by viewModel.summarizedResult.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val quizQuestions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val quizIdx by viewModel.quizCurrentIndex.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()

    var notesTitle by remember { mutableStateOf("") }
    var notesContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("Physics") }

    // Mock PDF metadata state
    var selectedPdfName by remember { mutableStateOf<String?>(null) }
    var selectedPdfSize by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                Translations.getText("notes_pdf", language),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Simulating PDF upload
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analyze & Summarize PDFs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Select any study notes PDF from your device.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedPdfName != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFF44336))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedPdfName!!, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text(selectedPdfSize!!, fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                selectedPdfName = null
                                selectedPdfSize = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                // Simulate picking a PDF notes file
                                val mockNames = listOf("Biology_Cell_Structure_Notes.pdf", "Introduction_to_Economics_Ch1.pdf", "Quantum_Physics_Intro.pdf")
                                val mockSizes = listOf("2.4 MB - 14 Pages", "1.8 MB - 9 Pages", "3.1 MB - 22 Pages")
                                val idx = (0..2).random()
                                selectedPdfName = mockNames[idx]
                                selectedPdfSize = mockSizes[idx]

                                // Fill notes content with simulated extracted content so AI can actually summarize it!
                                notesTitle = mockNames[idx].replace(".pdf", "")
                                notesContent = "This PDF discusses advanced academic topics corresponding to $notesTitle. Cell organelles, photosynthesis pathways, economic supply & demand curves, elasticities, quantum states, wave-particle duality and Schrödinger equations. Summary should include overview, clear sections, bullet points and definitions."
                            },
                            modifier = Modifier.testTag("pdf_pick_btn")
                        ) {
                            Text(Translations.getText("upload_pdf", language))
                        }

                        if (selectedPdfName != null) {
                            Button(
                                onClick = {
                                    viewModel.summarizeTextNotes(notesTitle, notesContent, noteSubject, true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("pdf_summarize_btn")
                            ) {
                                Text("Summarize PDF")
                            }
                        }
                    }
                }
            }
        }

        // Custom notes summarizing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Manually Add / Summarize Text Notes", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = notesTitle,
                        onValueChange = { notesTitle = it },
                        label = { Text("Notes Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notesContent,
                        onValueChange = { notesContent = it },
                        label = { Text(Translations.getText("enter_notes", language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("note_content_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subject: ", fontWeight = FontWeight.SemiBold)
                        val subjects = listOf("Physics", "Biology", "Economics", "Chemistry")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subjects) { sub ->
                                FilterChip(
                                    selected = noteSubject == sub,
                                    onClick = { noteSubject = sub },
                                    label = { Text(sub) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (notesTitle.trim().isNotEmpty() && notesContent.trim().isNotEmpty()) {
                                viewModel.summarizeTextNotes(notesTitle, notesContent, noteSubject, true)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("summarize_btn")
                    ) {
                        if (isSummarizing) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(Translations.getText("summarize_btn", language))
                        }
                    }
                }
            }
        }

        // AI Quiz generator trigger from Notes
        if (notesContent.trim().isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Generate Practice Quiz!", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("Create a 5-question multiple choice test instantly from these notes.", fontSize = 11.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.generateQuizFromNotes(notesContent, notesTitle, noteSubject)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.testTag("quiz_gen_btn")
                        ) {
                            if (viewModel.isQuizGenerating.value) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(24.dp))
                            } else {
                                Text(Translations.getText("generate_quiz", language))
                            }
                        }
                    }
                }
            }
        }

        // Interactive Quiz view if generated
        if (quizQuestions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Interactive AI Quiz", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${quizIdx.coerceAtMost(quizQuestions.size)} / ${quizQuestions.size}", fontWeight = FontWeight.Bold)
                        }

                        if (quizIdx < quizQuestions.size) {
                            val question = quizQuestions[quizIdx]
                            Text(question.question, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                            question.options.forEachIndexed { optIdx, option ->
                                OutlinedButton(
                                    onClick = { viewModel.answerQuizQuestion(optIdx) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quiz_opt_$optIdx"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(option, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(56.dp))
                                Text("Quiz Completed!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Your Score: $score / ${quizQuestions.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Button(onClick = { viewModel.generateQuizFromNotes(notesContent, notesTitle, noteSubject) }) {
                                    Text("Retake Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Offline Notes Directory
        item {
            Text(
                Translations.getText("offline_section", language),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (notes.isEmpty()) {
            item {
                Text("No saved study summaries offline yet.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(note.title, fontWeight = FontWeight.Bold)
                                Text(note.subject, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row {
                                IconButton(onClick = { viewModel.toggleNoteFav(note.id, !note.isFavorite) }) {
                                    Icon(
                                        if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (note.isFavorite) Color(0xFFFFD700) else Color.Gray
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(note.content, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

// --- FLASHCARDS SCREEN ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FlashcardsScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val decks by viewModel.allDeckNames.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcardsInSelectedDeck.collectAsStateWithLifecycle()
    val activeDeck by viewModel.selectedDeck.collectAsStateWithLifecycle()

    var cardFront by remember { mutableStateOf("") }
    var cardBack by remember { mutableStateOf("") }
    var newDeckName by remember { mutableStateOf("") }

    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                Translations.getText("flashcards", language),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Selected Deck Header / Review Game
        if (activeDeck == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(Translations.getText("add_card", language), fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = newDeckName,
                            onValueChange = { newDeckName = it },
                            label = { Text(Translations.getText("deck_name", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_deck_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cardFront,
                            onValueChange = { cardFront = it },
                            label = { Text(Translations.getText("front_side", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_front_input")
                        )

                        OutlinedTextField(
                            value = cardBack,
                            onValueChange = { cardBack = it },
                            label = { Text(Translations.getText("back_side", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_back_input")
                        )

                        Button(
                            onClick = {
                                if (newDeckName.trim().isNotEmpty() && cardFront.trim().isNotEmpty() && cardBack.trim().isNotEmpty()) {
                                    viewModel.addFlashcard(newDeckName, cardFront, cardBack)
                                    cardFront = ""
                                    cardBack = ""
                                    Toast.makeText(viewModel.getApplication(), "Flashcard added!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_card_btn")
                        ) {
                            Text(Translations.getText("save", language))
                        }
                    }
                }
            }

            item {
                Text("Your Flashcard Decks:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (decks.isEmpty()) {
                item {
                    Text("No flashcard decks created yet.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                items(decks) { deck ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedDeck.value = deck },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Layers, contentDescription = null)
                                Text(deck, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        } else {
            // Flashcard Review Activity
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectedDeck.value = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(activeDeck!!, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${currentCardIndex + 1} / ${flashcards.size}", fontSize = 12.sp)
                }
            }

            if (flashcards.isEmpty()) {
                item {
                    Text("This deck has no flashcards yet.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                val card = flashcards.getOrNull(currentCardIndex)
                if (card != null) {
                    item {
                        // Flip Card Animation with Spring rotation!
                        val rotation by animateFloatAsState(
                            targetValue = if (isFlipped) 180f else 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clickable { isFlipped = !isFlipped }
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12 * density
                                }
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                                .background(
                                    if (isFlipped) MaterialTheme.colorScheme.tertiaryContainer 
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .graphicsLayer {
                                        // Avoid mirroring contents when rotated
                                        if (rotation > 90f) rotationY = 180f
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isFlipped) "ANSWER / جواب" else "QUESTION / سوال",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFlipped) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f) 
                                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isFlipped) card.back else card.front,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (isFlipped) MaterialTheme.colorScheme.onTertiaryContainer 
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(Translations.getText("rate_recall", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                (1..5).forEach { score ->
                                    Button(
                                        onClick = {
                                            viewModel.rateFlashcard(card, score)
                                            isFlipped = false
                                            if (currentCardIndex < flashcards.size - 1) {
                                                currentCardIndex += 1
                                            } else {
                                                currentCardIndex = 0
                                                Toast.makeText(viewModel.getApplication(), "Deck review cycle finished!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when (score) {
                                                1 -> Color(0xFFF44336)
                                                2 -> Color(0xFFFF5722)
                                                3 -> Color(0xFFFF9800)
                                                4 -> Color(0xFF4CAF50)
                                                else -> Color(0xFF00C853)
                                            }
                                        ),
                                        modifier = Modifier.weight(1f).padding(2.dp).testTag("card_rate_$score")
                                    ) {
                                        Text("$score", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteCard(card.id)
                                if (currentCardIndex > 0) currentCardIndex--
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.fillMaxWidth().testTag("del_card_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Flashcard")
                        }
                    }
                }
            }
        }
    }
}

// --- POMODORO TIMER SCREEN ---
@Composable
fun PomodoroScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val mode by viewModel.pomodoroMode.collectAsStateWithLifecycle()

    val totalSeconds = if (mode == "Study") 25 * 60 else 5 * 60
    val progress = secondsLeft.toFloat() / totalSeconds.toFloat()

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            Translations.getText("pomodoro_timer", language),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stunning countdown progress circle using Canvas drawing!
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            val progressColor = if (mode == "Study") MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
            val trackColor = progressColor.copy(alpha = 0.2f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    formattedTime,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    if (mode == "Study") "FOCUSING / پڑھائی" else "BREAK / وقفہ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = progressColor
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Presets
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(15, 25, 45).forEach { min ->
                FilterChip(
                    selected = (totalSeconds / 60) == min && mode == "Study",
                    onClick = { viewModel.setPomodoroTime(min) },
                    label = { Text("$min min") }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.togglePomodoro() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("pom_toggle"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isRunning) Translations.getText("pause", language) else Translations.getText("start", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = { viewModel.resetPomodoro() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("pom_reset")
            ) {
                Text(
                    Translations.getText("reset", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// --- AI HELPERS / TOOLS SCREEN ---
@Composable
fun AiToolsScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isLoading by viewModel.isToolLoading.collectAsStateWithLifecycle()

    val explRes by viewModel.explanationResult.collectAsStateWithLifecycle()
    val mathRes by viewModel.mathResult.collectAsStateWithLifecycle()
    val gramRes by viewModel.grammarResult.collectAsStateWithLifecycle()
    val transRes by viewModel.translationResult.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }

    var topicInput by remember { mutableStateOf("") }
    var mathInput by remember { mutableStateOf("") }
    var grammarInput by remember { mutableStateOf("") }
    var translationInput by remember { mutableStateOf("") }
    var transToUrdu by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = activeTab) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text(Translations.getText("explain_topic", language)) })
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Math Step-Solver") })
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text(Translations.getText("grammar_checker", language)) })
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("English <-> Urdu") })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            when (activeTab) {
                0 -> { // Explain Difficult Topic
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Explain Difficult Topics in Simple Terms", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = topicInput,
                                    onValueChange = { topicInput = it },
                                    label = { Text("Enter a difficult concept (e.g., Photosynthesis, Black Holes, Inflation)") },
                                    modifier = Modifier.fillMaxWidth().testTag("explain_topic_input")
                                )
                                Button(
                                    onClick = { viewModel.explainTopic(topicInput) },
                                    modifier = Modifier.fillMaxWidth().testTag("explain_topic_btn")
                                ) {
                                    Text("Explain Simple Language")
                                }
                            }
                        }
                    }
                    if (explRes != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Explanation:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(explRes!!, fontSize = 14.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                }
                1 -> { // Math Step Solver
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Step-by-Step Math & Homework Solver", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = mathInput,
                                    onValueChange = { mathInput = it },
                                    label = { Text("Enter mathematical equation or word problem") },
                                    modifier = Modifier.fillMaxWidth().testTag("math_solver_input")
                                )
                                Button(
                                    onClick = { viewModel.solveMath(mathInput) },
                                    modifier = Modifier.fillMaxWidth().testTag("math_solver_btn")
                                ) {
                                    Text("Solve Problem")
                                }
                            }
                        }
                    }
                    if (mathRes != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Step-by-Step Solution:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(mathRes!!, fontSize = 14.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                }
                2 -> { // Grammar Checker
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Instant Academic Grammar Checker", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = grammarInput,
                                    onValueChange = { grammarInput = it },
                                    label = { Text("Paste text to analyze and polish") },
                                    modifier = Modifier.fillMaxWidth().testTag("grammar_checker_input")
                                )
                                Button(
                                    onClick = { viewModel.checkGrammar(grammarInput) },
                                    modifier = Modifier.fillMaxWidth().testTag("grammar_checker_btn")
                                ) {
                                    Text("Check Grammar")
                                }
                            }
                        }
                    }
                    if (gramRes != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Grammar Corrections:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(gramRes!!, fontSize = 14.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }
                }
                3 -> { // Bilingual translation English Urdu
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("English <-> Urdu Bilingual Translator", fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = transToUrdu, onClick = { transToUrdu = true })
                                    Text("English to Urdu")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    RadioButton(selected = !transToUrdu, onClick = { transToUrdu = false })
                                    Text("Urdu to English")
                                }
                                OutlinedTextField(
                                    value = translationInput,
                                    onValueChange = { translationInput = it },
                                    label = { Text("Enter text to translate") },
                                    modifier = Modifier.fillMaxWidth().testTag("translator_input")
                                )
                                Button(
                                    onClick = { viewModel.translate(translationInput, transToUrdu) },
                                    modifier = Modifier.fillMaxWidth().testTag("translator_btn")
                                ) {
                                    Text("Translate")
                                }
                            }
                        }
                    }
                    if (transRes != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Translation / ترجمہ:", fontWeight = FontWeight.Bold, color = Color(0xFF00838F))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(transRes!!, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                Translations.getText("settings", language),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Language toggle
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Translations.getText("language_select", language), fontWeight = FontWeight.Bold)
                        Text("Switch languages in real time", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = language == "en",
                            onClick = { viewModel.changeLanguage("en") },
                            label = { Text("English") },
                            modifier = Modifier.testTag("lang_en")
                        )
                        FilterChip(
                            selected = language == "ur",
                            onClick = { viewModel.changeLanguage("ur") },
                            label = { Text("اردو") },
                            modifier = Modifier.testTag("lang_ur")
                        )
                    }
                }
            }
        }

        // Dark Theme toggle
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Translations.getText("theme_toggle", language), fontWeight = FontWeight.Bold)
                        Text("Comfortable eye safety mode", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_switch")
                    )
                }
            }
        }

        // Pro store shortcut
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectScreen(AppScreen.UPGRADE) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("StudyMind Pro Status", fontWeight = FontWeight.Bold)
                        Text(if (isPremium) "Premium fully activated!" else "Unlock unlimited summaries and chats", fontSize = 12.sp)
                    }
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = if (isPremium) Color(0xFFFFD700) else Color.Gray)
                }
            }
        }

        // Policy docs
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    TextButton(onClick = { showPrivacy = true }, modifier = Modifier.testTag("privacy_btn")) {
                        Text(Translations.getText("privacy_policy", language))
                    }
                    Divider()
                    TextButton(onClick = { showTerms = true }, modifier = Modifier.testTag("terms_btn")) {
                        Text(Translations.getText("terms", language))
                    }
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_btn")
            ) {
                Text("Log Out Account")
            }
        }
    }

    // Modal dialogs
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            confirmButton = { Button(onClick = { showPrivacy = false }) { Text("Close") } },
            title = { Text("Privacy Policy") },
            text = { Text("At StudyMind AI, we secure your local chats, notes, and study statistics directly in your device database. Gemini API communication respects private academic text parsing parameters. No personalized user tracking is sold.") }
        )
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            confirmButton = { Button(onClick = { showTerms = false }) { Text("Close") } },
            title = { Text("Terms of Service") },
            text = { Text("StudyMind AI is designed to assist academic learning. Free tier is provided with reasonable limits. Spaced repetition flashcards do not guarantee examination performance. Usage is restricted to fair intellectual integrity guidelines.") }
        )
    }
}

// --- PRO UPGRADE STORE SCREEN ---
@Composable
fun UpgradeScreen(viewModel: MainViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2111E25))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFFD700)
                )

                Text(
                    "StudyMind Pro",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color.White
                )

                Text(
                    "Supercharge your study velocity and break academic barriers.",
                    textAlign = TextAlign.Center,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Benefit List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BenefitRow(text = Translations.getText("unlimited_chat", language))
                    BenefitRow(text = Translations.getText("ad_free", language))
                    BenefitRow(text = Translations.getText("adv_quiz", language))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.togglePremium()
                        Toast.makeText(viewModel.getApplication(), if (!isPremium) "Pro upgraded successfully!" else "Premium deactivated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("upgrade_pro_action"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isPremium) "Downgrade Account" else Translations.getText("premium_upgrade_button", language),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                TextButton(onClick = { viewModel.selectScreen(AppScreen.DASHBOARD) }) {
                    Text("Maybe Later", color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

// Simple Helper Quadruple
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
