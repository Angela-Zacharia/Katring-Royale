package ca.uwaterloo.kartingroyale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.LogRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.UserRepository
import ca.uwaterloo.kartingroyale.data.repository.UserRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.usecase.AddLogEntry
import ca.uwaterloo.kartingroyale.domain.usecase.FollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.GetAllFollowings
import ca.uwaterloo.kartingroyale.domain.usecase.GetFriendsActivity
import ca.uwaterloo.kartingroyale.domain.usecase.GetLeaderboard
import ca.uwaterloo.kartingroyale.domain.usecase.GetLogEntry
import ca.uwaterloo.kartingroyale.domain.usecase.GetRecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetTracks
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import ca.uwaterloo.kartingroyale.presentation.AuthViewModel
import ca.uwaterloo.kartingroyale.presentation.HomeView
import ca.uwaterloo.kartingroyale.presentation.HomeViewModel
import ca.uwaterloo.kartingroyale.presentation.LeaderboardView
import ca.uwaterloo.kartingroyale.presentation.LeaderboardViewModel
import ca.uwaterloo.kartingroyale.presentation.LogView
import ca.uwaterloo.kartingroyale.presentation.LogViewModel
import ca.uwaterloo.kartingroyale.presentation.RecordView
import ca.uwaterloo.kartingroyale.presentation.RecordViewModel
import ca.uwaterloo.kartingroyale.presentation.SocialView
import ca.uwaterloo.kartingroyale.presentation.SocialViewModel
import ca.uwaterloo.kartingroyale.presentation.theme.KartingroyaleTheme
import ca.uwaterloo.kartingroyale.domain.usecase.DeleteLogEntry
import ca.uwaterloo.kartingroyale.domain.usecase.EditLogEntry
import ca.uwaterloo.kartingroyale.domain.usecase.UpdateUserName
import ca.uwaterloo.kartingroyale.presentation.UserView
import ca.uwaterloo.kartingroyale.presentation.UserViewModel
import ca.uwaterloo.kartingroyale.presentation.theme.Asphalt
import ca.uwaterloo.kartingroyale.presentation.theme.ChromeWhite
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTeal
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTealVariant
import ca.uwaterloo.kartingroyale.presentation.theme.SilverGrey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cloudStorage = CloudStorage()
        val sharedRepository = LogRepositoryImpl()
        val repo = LogRepositoryImpl()

        val authViewModel = AuthViewModel(cloudStorage)

        val socialRepositoryImpl = SocialRepositoryImpl(cloudStorage)

        val leaderboardRepositoryImpl = LeaderboardRepositoryImpl(cloudStorage)
        val getUserName = GetUserName(leaderboardRepositoryImpl)
        val homeViewModel = HomeViewModel(
            getUserName,
            GetRecentRaces(leaderboardRepositoryImpl),
            GetFriendsActivity(socialRepositoryImpl)
        )
        val userViewModel = UserViewModel(UpdateUserName(UserRepositoryImpl()), GetUserName(leaderboardRepositoryImpl))

        val getAllFollowings = GetAllFollowings(socialRepositoryImpl)
        val searchUsers = SearchUsers(socialRepositoryImpl)
        val followUser = FollowUser(socialRepositoryImpl)
        val unfollowUser = UnfollowUser(socialRepositoryImpl)
        val socialViewModel =
            SocialViewModel(getAllFollowings, searchUsers, followUser, unfollowUser)

        val getTracks = GetTracks(leaderboardRepositoryImpl)
        val logViewModel = LogViewModel(AddLogEntry(sharedRepository), cloudStorage, getTracks)
        val getLeaderboard = GetLeaderboard(leaderboardRepositoryImpl)
        val leaderboardViewModel = LeaderboardViewModel(getTracks, getLeaderboard, getUserName)


        val recordViewModel = RecordViewModel(
            getLogEntries = GetLogEntry(repo),
            editLogEntry = EditLogEntry(repo),
            deleteLogEntry = DeleteLogEntry(repo),
            getTracks = getTracks,
            cloudStorage = cloudStorage
        )

        enableEdgeToEdge()
        setContent {
            KartingroyaleTheme {
                MainApp(
                    homeViewModel,
                    logViewModel,
                    recordViewModel,
                    authViewModel,
                    socialViewModel,
                    leaderboardViewModel,
                    userViewModel
                )
            }
        }
    }
}

@Composable
fun MainApp(
    homeViewModel: HomeViewModel,
    logViewModel: LogViewModel,
    recordViewModel: RecordViewModel,
    authViewModel: AuthViewModel,
    socialViewModel: SocialViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home_root") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("google_login") {
            GoogleLoginScreen(
                onLoginClicked = {
                    navController.navigate("home_root") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home_root") {
            BottomNavScreen(
                homeViewModel,
                logViewModel,
                recordViewModel,
                socialViewModel,
                leaderboardViewModel,
                userViewModel,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate("login") {
                            popUpTo("home_root") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavScreen(
    homeViewModel: HomeViewModel,
    logViewModel: LogViewModel,
    recordViewModel: RecordViewModel,
    socialViewModel: SocialViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("log") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {

            NavigationBar (
                containerColor = Asphalt,
                contentColor = ChromeWhite){
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry.value?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RacingTeal,
                        selectedTextColor = RacingTeal,
                        indicatorColor = RacingTealVariant,
                        unselectedIconColor = SilverGrey,
                        unselectedTextColor = SilverGrey
                    ),
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Insights, contentDescription = "Record") },
                    label = { Text("Record") },
                    selected = currentRoute == "records",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RacingTeal,
                        selectedTextColor = RacingTeal,
                        indicatorColor = RacingTealVariant,
                        unselectedIconColor = SilverGrey,
                        unselectedTextColor = SilverGrey
                    ),
                    onClick = {
                        navController.navigate("records") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Social") },
                    label = { Text("Social") },
                    selected = currentRoute == "social",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RacingTeal,
                        selectedTextColor = RacingTeal,
                        indicatorColor = RacingTealVariant,
                        unselectedIconColor = SilverGrey,
                        unselectedTextColor = SilverGrey
                    ),
                    onClick = {
                        navController.navigate("social") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Friends") },
                    label = { Text("Leaderboard") },
                    selected = currentRoute == "leaderboard",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RacingTeal,
                        selectedTextColor = RacingTeal,
                        indicatorColor = RacingTealVariant,
                        unselectedIconColor = SilverGrey,
                        unselectedTextColor = SilverGrey
                    ),
                    onClick = {
                        navController.navigate("leaderboard") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )


            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("social") {
                SocialView(viewModel = socialViewModel)
            }
            composable("home") {
                HomeView(
                    homeViewModel,
                    onProfileClick = { navController.navigate("user") },
                    onRecentRacesClick = { navController.navigate("records") },
                    onFriendsActivityClick = { navController.navigate("social") },
                    onLogout = onLogout
                )
            }
            composable("leaderboard") {
                LeaderboardView(viewModel = leaderboardViewModel)
            }
            composable("log") {
                LogView(viewModel = logViewModel)
            }
            composable("records") {
                RecordView(viewModel = recordViewModel)
            }
            composable("user") {
                UserView(viewModel = userViewModel)
            }
        }
    }

}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )

            Text("Welcome! Please Login", modifier = Modifier.padding(bottom = 24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (isSignUp) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            authViewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (isSignUp) {
                        authViewModel.signUp(email, password, displayName, onLoginSuccess)
                    } else {
                        authViewModel.login(email, password, onLoginSuccess)
                    }
                },
                enabled = !authViewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                if (authViewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isSignUp) "Sign Up" else "Login")
                }
            }

            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(
                    if (isSignUp) "Already have an account? Login"
                    else "Don't have an account? Sign Up"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}

@Composable
fun GoogleLoginScreen(
    onLoginClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Sign in with Google",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onLoginClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Login with Google",
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val repo = remember { LogRepositoryImpl() }
    val logVM = remember { LogViewModel(AddLogEntry(repo), CloudStorage()) }

    val userVM = remember { UserViewModel(
        UpdateUserName(UserRepositoryImpl()),
        GetUserName(LeaderboardRepositoryImpl())
    ) }
    val authVM = remember { AuthViewModel(CloudStorage()) }
    val socialVM = remember {
        SocialViewModel(
            GetAllFollowings(SocialRepositoryImpl()),
            SearchUsers(SocialRepositoryImpl()),
            FollowUser(SocialRepositoryImpl()),
            UnfollowUser(SocialRepositoryImpl())
        )
    }
    val homeVM = remember {
        HomeViewModel(
            GetUserName(LeaderboardRepositoryImpl()), GetRecentRaces(
                LeaderboardRepositoryImpl()
            ), GetFriendsActivity(SocialRepositoryImpl())
        )
    }
    val leaderboardVM = remember {
        LeaderboardViewModel(
            GetTracks(LeaderboardRepositoryImpl()),
            GetLeaderboard(LeaderboardRepositoryImpl()),
            GetUserName(LeaderboardRepositoryImpl())
        )
    }
    val recordVM = remember {
        RecordViewModel(
            getLogEntries = GetLogEntry(repo),
            editLogEntry = EditLogEntry(repo),
            deleteLogEntry = DeleteLogEntry(repo),
            getTracks = GetTracks(LeaderboardRepositoryImpl()),
            cloudStorage = CloudStorage()
        )
    }
    KartingroyaleTheme {
        MainApp(
            homeViewModel = homeVM,
            logViewModel = logVM,
            recordViewModel = recordVM,
            authViewModel = authVM,
            socialViewModel = socialVM,
            leaderboardViewModel = leaderboardVM,
            userViewModel = userVM
        )
    }
}