package com.example.presentation.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: MultiplayerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiplayer Mode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is MultiplayerUiState.Idle -> {
                    LoginView(
                        onAnonymous = { viewModel.signInAnonymously() },
                        onGoogle = { 
                            // Note: Real Client ID needed here
                            viewModel.signInWithGoogle("YOUR_WEB_CLIENT_ID")
                        }
                    )
                }
                is MultiplayerUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is MultiplayerUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.signOut() }) {
                            Text("Retry")
                        }
                    }
                }
                is MultiplayerUiState.Authenticated -> {
                    ProfileDashboard(
                        profile = state.profile,
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginView(
    onAnonymous: () -> Unit,
    onGoogle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Leaderboard,
            contentDescription = "Multiplayer",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Join the global leaderboard!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Sync your progress, compete with friends, and backup to the cloud.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onAnonymous,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play Anonymously")
        }
    }
}

@Composable
fun ProfileDashboard(
    profile: UserProfile,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Profile") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Leaderboard") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Friends") })
        }
        
        when (selectedTab) {
            0 -> ProfileTab(profile, onSignOut)
            1 -> LeaderboardTab()
            2 -> FriendsTab()
        }
    }
}

@Composable
fun ProfileTab(profile: UserProfile, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = profile.displayName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Cloud Sync: Active • Offline Ready",
            fontSize = 12.sp,
            color = Color(0xFF10B981)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Global Rank", "#${if(profile.globalRank > 0) profile.globalRank else "-"}")
            StatCard("Total Score", profile.totalScore.toString())
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Weekly Rank", "#${if(profile.weeklyRank > 0) profile.weeklyRank else "-"}")
            StatCard("Monthly Rank", "#${if(profile.monthlyRank > 0) profile.monthlyRank else "-"}")
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}

@Composable
fun LeaderboardTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Global Leaderboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(10) { index ->
                LeaderboardItem(rank = index + 1, name = "Player ${index + 1}", score = 10000 - (index * 500))
                Divider()
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, name: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#$rank", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text("$score pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun FriendsTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Friends", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Connect with friends to compare scores and achievements.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Find Friends")
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.width(140.dp).height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
