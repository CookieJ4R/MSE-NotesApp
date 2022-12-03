package notesapp.mse


enum class NotePriority {
    IMPORTANT, DEFAULT, ARCHIVED
}

class Note(val title: String, val text: String = "",
           val image: Int? = null,
           val priority: NotePriority = NotePriority.DEFAULT)