package notesapp.mse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import notesapp.mse.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MSENotesAppTheme {
                /*
                    Generate static notes list: 0-60 (61 Notes)
                    Each third Note starting at 0 is of priority default. (0, 3, 6, 9, ...)
                    Each third Note starting at 1 is of priority important. (1, 4, 7, 10, ...)
                    Each third Note starting at 2 is of priority archived. (2, 5, 8, 11, ...)
                    Each tenth Note starting at 0 will haven an image attached.(0, 10, 20, ...)
                 */
                val nodeList : MutableList<Note> = mutableListOf()
                for (i in 0..30){
                    var priority = NotePriority.DEFAULT
                    if (i % 3 == 1){
                        priority = NotePriority.IMPORTANT
                    }else if(i % 3 == 2){
                        priority = NotePriority.ARCHIVED
                    }
                        nodeList.add(Note("Titel $i", "This is a longer note with multiple lines and is note number $i!", priority=priority, image = if(i % 10 == 0) R.drawable.ic_launcher_foreground else null))
                }
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { NotesScaffold(nodeList, navController) }
                    composable("create") { CreateNote(nodeList, navController) }
                    /*...*/
                }
            }
        }
    }
}

/*
Scaffold Composable
 */
@Composable
fun NotesScaffold(notes: List<Note>, navController: NavHostController){
    Scaffold(
        backgroundColor = BG,
        topBar = { TopAppBar(title = {Text(stringResource(R.string.app_title), color = Color.White)},
            backgroundColor = AppBarBG)  },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(backgroundColor = FABBG, onClick = {
                navController.navigate("create")
            }) {
                Icon(
                    Icons.Filled.Add,
                    stringResource(R.string.add_button_content_description),
                    modifier = Modifier.scale(1.5f),
                    tint = Color.White
                )
            }
        },
        isFloatingActionButtonDocked = true,
        // Wrapper Box to use innerPadding to prevent bars from overlapping notes composable
        content = { innerPadding -> Box(modifier = Modifier.padding(innerPadding)) {NotesList(notes = notes)} },
        bottomBar = { BottomAppBar(backgroundColor = AppBarBG) {  } }
    )
}

/*
Notelist Composable
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NotesList(notes: List<Note>) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(3),
        modifier = Modifier.padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        // filter out archived notes
        items(notes.filter { it.priority != NotePriority.ARCHIVED }) { note ->
            Box {
                var extendedState by remember { mutableStateOf(false) }
                Card(
                    elevation = 10.dp,
                    onClick = {
                        extendedState = !extendedState
                    },
                    backgroundColor = if (note.priority == NotePriority.DEFAULT) NoteBG else ImportantBG
                ) {
                    Column {
                        Text(text = note.title, modifier = Modifier.padding(10.dp))
                        /* Use different configured Composeable based on extendedState.
                        Little unclean implementation caused by not being able to
                        disable maxLines instead of outright removing it.
                        */
                        if (extendedState)
                            Text(
                                text = if (note.priority == NotePriority.IMPORTANT) note.text.important() else note.text,
                                modifier = Modifier.padding(10.dp),
                                overflow = TextOverflow.Ellipsis
                            )
                        else
                            Text(
                                text = if (note.priority == NotePriority.IMPORTANT) note.text.important() else note.text,
                                modifier = Modifier.padding(10.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        // Paint Image
                        if (note.image != null && extendedState) {
                            Image(
                                painter = painterResource(id = note.image),
                                contentDescription = stringResource(R.string.picture_content_description)
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
Create new note screen composable
 */
@Composable
fun CreateNote(notes: MutableList<Note>, navController: NavHostController){
    Scaffold(
        topBar = { TopAppBar(title = {Text(stringResource(R.string.create_note_title), color = Color.White)},backgroundColor = AppBarBG)  },
        content = {
            Column(modifier=Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
                var titelTextstate by remember { mutableStateOf(TextFieldValue("")) }
                var noteTextstate by remember { mutableStateOf(TextFieldValue("")) }
                var important by remember {mutableStateOf(false)}
                var useImage by remember {mutableStateOf(false)}
                TextField(
                    modifier = Modifier
                        .height(50.dp)
                        .width(250.dp),
                    value = titelTextstate,
                    placeholder = {Text(stringResource(R.string.note_title_placeholder))},
                    onValueChange = { titelTextstate = it }
                )
                TextField(
                    modifier = Modifier
                        .height(200.dp)
                        .width(250.dp),
                    value = noteTextstate,
                    placeholder = {Text(stringResource(R.string.note_text_placeholder))},
                    onValueChange = { noteTextstate = it }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.important_text))
                    Checkbox(checked = important, onCheckedChange = { important = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.use_image_toggle_text))
                    Checkbox(checked = useImage, onCheckedChange = { useImage = it })
                    Box(modifier = Modifier
                        .padding(start = 20.dp)
                        .border(2.dp, Color.DarkGray)
                        .height(100.dp)
                        .width(100.dp)
                        .background(BG)){
                        if(useImage){
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = stringResource(R.string.chosen_picture_content_description),
                            )
                        }
                    }

                }
                Button(onClick = {
                    val priority = if(important) NotePriority.IMPORTANT else NotePriority.DEFAULT
                    val note = if(useImage)
                        Note(titelTextstate.text, noteTextstate.text, R.drawable.ic_launcher_foreground, priority)
                    else
                        Note(titelTextstate.text, noteTextstate.text, null, priority)
                    notes.add(note)
                    navController.popBackStack()}){
                    Text(stringResource(R.string.save_note_text))
                }
            }
        }
    )
}

fun String.important(): String{
    return "!!!$this!!!";
}