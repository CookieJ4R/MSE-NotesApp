package notesapp.mse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                for (i in 0..60){
                    var priority = NotePriority.DEFAULT
                    if (i % 3 == 1){
                        priority = NotePriority.IMPORTANT
                    }else if(i % 3 == 2){
                        priority = NotePriority.ARCHIVED
                    }
                        nodeList.add(Note("Titel $i", "This is a longer note with multiple lines and is note number $i!", priority=priority, image = if(i % 10 == 0) R.drawable.ic_launcher_foreground else null))
                }
                NotesScaffold(nodeList)
            }
        }
    }
}

/*
Scaffold Composeable
 */
@Composable
fun NotesScaffold(notes: List<Note>){
    Scaffold(
        backgroundColor = BG,
        topBar = { TopAppBar(title = {Text("Best Notes App Ever", color = Color.White)},backgroundColor = AppBarBG)  },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = ({
            FloatingActionButton(backgroundColor = FABBG, onClick = { /*Not used right now*/ }) {
            }
        }),
        isFloatingActionButtonDocked = true,
        // Wrapper Box to use innerPadding to prevent bars from overlapping notes composeable
        content = { innerPadding -> Box(modifier = Modifier.padding(innerPadding)) {NotesList(notes = notes)} },
        bottomBar = { BottomAppBar(backgroundColor = AppBarBG) {  } }
    )
}

/*
Notelist Composeable
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
                                contentDescription = "example picture"
                            )
                        }
                    }
                }
            }
        }
    }
}

fun String.important(): String{
    return "!!!$this!!!";
}