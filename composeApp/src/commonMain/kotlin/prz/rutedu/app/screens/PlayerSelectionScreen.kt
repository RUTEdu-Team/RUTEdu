package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.Database
import prz.rutedu.app.Player
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.add_player
import rutedu.composeapp.generated.resources.back
import rutedu.composeapp.generated.resources.cancel
import rutedu.composeapp.generated.resources.choose_player_input
import rutedu.composeapp.generated.resources.choose_player_nick_exist
import rutedu.composeapp.generated.resources.create_first_player
import rutedu.composeapp.generated.resources.create_player
import rutedu.composeapp.generated.resources.error_creating_player
import rutedu.composeapp.generated.resources.fragment_choose_player_instruction
import rutedu.composeapp.generated.resources.high_score_games
import rutedu.composeapp.generated.resources.ic_add_black_24dp
import rutedu.composeapp.generated.resources.ic_star_yellow_24dp
import rutedu.composeapp.generated.resources.nick_empty
import rutedu.composeapp.generated.resources.nickname
import rutedu.composeapp.generated.resources.no_players_yet
import rutedu.composeapp.generated.resources.save
import rutedu.composeapp.generated.resources.star

/**
 * Leaderboard player-selection screen shown before a solo or PvP game session.
 *
 * Lists all player profiles from the database as [PlayerCard]s. A FAB opens a "Create player"
 * dialog; new players are persisted to the database and the list refreshes immediately.
 *
 * @param navController    Navigation controller for the back-stack pop.
 * @param database         [Database] instance used to read/write player records.
 * @param onPlayerSelected Called with the chosen [Player] when a card is tapped; also saves the
 *                         current player ID to the database.
 */
@Composable
fun PlayerSelectionScreen(
    navController: NavController,
    database: Database,
    onPlayerSelected: (Player) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlayerNickname by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    // Load players from database
    LaunchedEffect(Unit) {
        try {
            players = database.databaseQueries.getAllPlayers().executeAsList()
        } catch (e: Exception) {
            players = emptyList()
        }
    }

    // Get localized strings
    val instructionText = stringResource(Res.string.fragment_choose_player_instruction)
    val inputPrompt = stringResource(Res.string.choose_player_input)
    val nickExistsError = stringResource(Res.string.choose_player_nick_exist)
    val nickEmptyError = stringResource(Res.string.nick_empty)
    val cancelText = stringResource(Res.string.cancel)
    val saveText = stringResource(Res.string.save)
    val backText = stringResource(Res.string.back)
    val noPlayersYetText = stringResource(Res.string.no_players_yet)
    val createFirstPlayerText = stringResource(Res.string.create_first_player)
    val createPlayerText = stringResource(Res.string.create_player)
    val addPlayerText = stringResource(Res.string.add_player)
    val nicknameText = stringResource(Res.string.nickname)
    val errorCreatingPlayerText = stringResource(Res.string.error_creating_player)
    val starText = stringResource(Res.string.star)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = instructionText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (players.isEmpty()) {
            // No players exist - show create prompt
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = noPlayersYetText,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = createFirstPlayerText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showCreateDialog = true }) {
                    Text(createPlayerText)
                }
            }
        } else {
            // Show list of players
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players) { player ->
                    PlayerCard(
                        player = player,
                        isSelected = selectedPlayer?.id == player.id,
                        highScoreGamesFormat = stringResource(Res.string.high_score_games, player.high_score.toInt(), player.games_played.toInt()),
                        starContentDescription = starText,
                        onClick = {
                            selectedPlayer = player
                            coroutineScope.launch {
                                try {
                                    database.databaseQueries.setCurrentPlayerId(player.id.toString())
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                            onPlayerSelected(player)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text(backText)
            }
            
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_black_24dp),
                    contentDescription = addPlayerText,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    // Create Player Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newPlayerNickname = ""
                errorMessage = null
            },
            title = { Text(inputPrompt) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlayerNickname,
                        onValueChange = {
                            newPlayerNickname = it
                            errorMessage = null
                        },
                        label = { Text(nicknameText) },
                        isError = errorMessage != null,
                        singleLine = true
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlayerNickname.isBlank()) {
                            errorMessage = nickEmptyError
                        } else if (players.any { it.nickname.equals(newPlayerNickname, ignoreCase = true) }) {
                            errorMessage = nickExistsError
                        } else {
                            coroutineScope.launch {
                                try {
                                    database.databaseQueries.createPlayer(newPlayerNickname.trim())
                                    players = database.databaseQueries.getAllPlayers().executeAsList()
                                    showCreateDialog = false
                                    newPlayerNickname = ""
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = errorCreatingPlayerText
                                }
                            }
                        }
                    }
                ) {
                    Text(saveText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        newPlayerNickname = ""
                        errorMessage = null
                    }
                ) {
                    Text(cancelText)
                }
            }
        )
    }
}

/**
 * A player list item card in [PlayerSelectionScreen].
 *
 * Highlights with `primaryContainer` background when [isSelected]. Shows the player's nickname,
 * [highScoreGamesFormat] subtitle, and a star icon when the player's high score is non-zero.
 *
 * @param player                 The player model to display.
 * @param isSelected             Whether this card should show the selected highlight.
 * @param highScoreGamesFormat   Pre-formatted "score · games" string (avoids nested resource calls).
 * @param starContentDescription Accessibility label for the star icon.
 * @param onClick                Called when the card is tapped.
 */
@Composable
fun PlayerCard(
    player: Player,
    isSelected: Boolean,
    highScoreGamesFormat: String,
    starContentDescription: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = player.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = highScoreGamesFormat,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (player.high_score > 0) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star_yellow_24dp),
                    contentDescription = starContentDescription,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}