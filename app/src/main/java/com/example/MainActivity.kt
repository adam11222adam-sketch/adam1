package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Note
import com.example.ui.NoteViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Modern Pastel Color Palette
data class NoteColor(
    val lightColor: Color,
    val darkColor: Color,
    val name: String
)

val NoteColors = listOf(
    NoteColor(Color(0xFFFFF4CC), Color(0xFF332D15), "أصفر"),
    NoteColor(Color(0xFFD4EDDA), Color(0xFF1D3522), "أخضر"),
    NoteColor(Color(0xFFD1ECF1), Color(0xFF162D33), "أزرق"),
    NoteColor(Color(0xFFE8DDFC), Color(0xFF261D3A), "بنفسجي"),
    NoteColor(Color(0xFFF8D7DA), Color(0xFF381C20), "وردي"),
    NoteColor(Color(0xFFF1F3F5), Color(0xFF2D3033), "افتراضي")
)

val Categories = listOf("الكل", "شخصي", "عمل", "أفكار", "هام")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force RTL Layout specifically for perfect Arabic Notes structure
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NotesAppScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = WindowInsets.navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotesAppScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel()
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var editingNote by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    val hasPinnedNotes = notes.any { it.isPinned }

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Header with Title and Layout Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ملاحظاتي",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                    Text(
                        text = if (notes.isEmpty()) "المفكرة فارغة" else "لديك ${notes.size} ملاحظة",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Grid/List toggle action with visual feedback
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .size(48.dp)
                        .testTag("layout_toggle_button")
                ) {
                    // Inline crisp vectors for layout
                    if (isGridView) {
                        // Drawing list columns representation
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(Modifier.size(16.dp, 3.dp).background(MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(16.dp, 3.dp).background(MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(16.dp, 3.dp).background(MaterialTheme.colorScheme.primary))
                        }
                    } else {
                        // Drawing grid layout cells
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Row {
                                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary))
                                Spacer(Modifier.width(3.dp))
                                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary))
                            }
                            Spacer(Modifier.height(3.dp))
                            Row {
                                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary))
                                Spacer(Modifier.width(3.dp))
                                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar pill styled with custom background
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("بحث في ملاحظاتك...", fontSize = 15.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("search_note_input"),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontally Scrollable Category Selector Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.setSelectedCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Notes Grid or List with Animated State Handling
            if (notes.isEmpty()) {
                // Beautiful Empty State Illustration and Tip
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing custom modern notepad look using Compose primitives
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 44.dp)
                                    .border(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(3.dp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "لا توجد ملاحظات بعد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedCategory != "الكل")
                            "لم يتم العثور على أي ملاحظات تطابق معايير البحث."
                        else
                            "انقر على الزر الدائري بالأسفل لإضافة فكرتك البسيطة الأولى وحفظها محلياً!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                // Render with Pinned vs All Sections elegantly
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                onClick = { editingNote = note },
                                onDelete = { noteToDelete = note },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCardItem(
                                note = note,
                                onClick = { editingNote = note },
                                onDelete = { noteToDelete = note },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                }
            }
        }

        // Beautiful Circle Floating Action Button
        FloatingActionButton(
            onClick = {
                editingNote = Note(
                    title = "",
                    content = "",
                    category = if (selectedCategory == "الكل") "شخصي" else selectedCategory
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_note_fab"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "إضافة ملاحظة جديدة",
                modifier = Modifier.size(28.dp)
            )
        }

        // Custom Sliding Overlay Edit Sheet Container
        AnimatedVisibility(
            visible = editingNote != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(stiffness = 300f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(stiffness = 300f)
            ) + fadeOut()
        ) {
            editingNote?.let { note ->
                EditNoteOverlay(
                    note = note,
                    onDismiss = { editingNote = null },
                    onSave = { updated ->
                        viewModel.insertNote(updated)
                        editingNote = null
                    }
                )
            }
        }

        // Lovely Alert Dialog for Delete Confirmation
        noteToDelete?.let { note ->
            Dialog(onDismissRequest = { noteToDelete = null }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drawing delete alert warning icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "حذف الملاحظة؟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "هل أنت متأكد من رغبتك في حذف ملاحظة \"${note.title.ifEmpty { "بلا عنوان" }}\" نهائياً؟ لا يمكن التراجع عن هذا الإجراء.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { noteToDelete = null },
                            ) {
                                Text("إلغاء", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteNote(note)
                                    noteToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("حذف الآن", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCardItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) {
        NoteColors[note.colorIndex].darkColor
    } else {
        NoteColors[note.colorIndex].lightColor
    }

    val dateFormatted = remember(note.timestamp) {
        val date = Date(note.timestamp)
        val sdf = SimpleDateFormat("yyyy/MM/dd | hh:mm a", Locale("ar"))
        sdf.format(date)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (note.isPinned) 4.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onTogglePin
            )
            .testTag("note_item_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and Pin status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.title.isNotEmpty()) {
                        Text(
                            text = note.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF212529),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "ملاحظة بلا عنوان",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    // Pinned state trigger
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        // Drawing dynamic custom pin/star indicator
                        if (note.isPinned) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                            )
                        }
                    }

                    // Delete Note button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category snippet badge
            if (note.category.isNotEmpty() && note.category != "الكل") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = note.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF212529).copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Body preview content
            Text(
                text = note.content.ifEmpty { "لا يوجد محتوى بالداخل بعد..." },
                fontSize = 14.sp,
                color = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer dynamic timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    fontSize = 11.sp,
                    color = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )

                // Small visual tag of pin state label if any
                if (note.isPinned) {
                    Text(
                        text = "مثبتة بالقمة",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditNoteOverlay(
    note: Note,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var colorIndex by remember { mutableStateOf(note.colorIndex) }
    var category by remember { mutableStateOf(note.category) }
    var isPinned by remember { mutableStateOf(note.isPinned) }

    val isDark = isSystemInDarkTheme()
    val sheetBgColor by animateColorAsState(
        targetValue = if (isDark) {
            NoteColors[colorIndex].darkColor
        } else {
            NoteColors[colorIndex].lightColor
        },
        animationSpec = spring(stiffness = 150f),
        label = "sheetColor"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss), // Click outside to cancel
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Modal Sheet Layout container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(sheetBgColor)
                    .clickable(enabled = false) { } // Prevent clicks through to background
                    .padding(24.dp)
                    .testTag("edit_note_sheet")
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(40.dp, 5.dp)
                        .background(
                            (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f),
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                (if (isDark) Color.White else Color.Black).copy(alpha = 0.06f),
                                CircleShape
                            )
                            .size(40.dp)
                            .testTag("close_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            modifier = Modifier.size(20.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }

                    // Save with pin and customized icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isPinned = !isPinned },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    if (isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent,
                                    CircleShape
                                )
                                .size(40.dp)
                        ) {
                            // Render localized marker circle for pinning
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (isPinned) MaterialTheme.colorScheme.primary
                                        else (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f),
                                        CircleShape
                                    )
                            )
                        }

                        Button(
                            onClick = {
                                onSave(
                                    note.copy(
                                        title = title.trim(),
                                        content = content,
                                        colorIndex = colorIndex,
                                        category = category,
                                        isPinned = isPinned,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("save_note_button")
                        ) {
                            Text(
                                text = "حفظ الملاحظة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bold Title Input
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        color = if (isDark) Color.White else Color(0xFF212529),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input"),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                "العنوان",
                                color = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.35f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Category select list inside editing pane
                Text(
                    text = "التصنيف",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Categories.filter { it != "الكل" }) { item ->
                        val isSelected = category == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f)
                                )
                                .clickable { category = item }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Body content Multi-line Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else Color(0xFF212529),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("note_content_input"),
                        decorationBox = { innerTextField ->
                            if (content.isEmpty()) {
                                Text(
                                    "اكتب ملاحظتك البسيطة هنا...",
                                    color = (if (isDark) Color.White else Color(0xFF212529)).copy(alpha = 0.4f),
                                    fontSize = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Styled circular Palette Selector at bottom
                Text(
                    text = "اختر لون الخلفية",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoteColors.forEachIndexed { index, noteColor ->
                        val blockColor = if (isDark) noteColor.darkColor else noteColor.lightColor
                        val isSelected = colorIndex == index

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(blockColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { colorIndex = index }
                                .testTag("color_option_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "تم الاختيار",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
