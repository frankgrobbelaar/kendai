package io.github.frankgrobbelaar.kendai.ui.firstrun

import android.icu.text.ListFormatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.files.openAllFilesAccess
import io.github.frankgrobbelaar.kendai.files.pathOfPicked
import io.github.frankgrobbelaar.kendai.ui.kit.BookCover
import io.github.frankgrobbelaar.kendai.ui.kit.FolderRow
import io.github.frankgrobbelaar.kendai.ui.kit.InkButton
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar
import io.github.frankgrobbelaar.kendai.ui.kit.LinkButton
import io.github.frankgrobbelaar.kendai.ui.kit.Overline
import io.github.frankgrobbelaar.kendai.ui.kit.RuleButton
import io.github.frankgrobbelaar.kendai.ui.kit.TickBox
import io.github.frankgrobbelaar.kendai.ui.kit.bookCountLabel
import io.github.frankgrobbelaar.kendai.ui.theme.Literata
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

@Composable
fun FirstRunRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FirstRunViewModel = viewModel(factory = FirstRunViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { tree ->
            if (tree != null) viewModel.pick(pathOfPicked(tree))
        }
    // Android's settings are another app: look again whenever kendai comes back.
    LifecycleResumeEffect(viewModel) {
        viewModel.recheckAccess()
        onPauseOrDispose {}
    }
    BackHandler(enabled = state.step == FirstRunStep.Access || state.step == FirstRunStep.Choose) {
        viewModel.back()
    }
    FirstRunScreen(
        state = state,
        onStart = viewModel::start,
        onAllow = { openAllFilesAccess(context) },
        onBack = viewModel::back,
        onToggle = viewModel::toggle,
        onAnother = { picker.launch(null) },
        onAdd = viewModel::add,
        onOpenLibrary = onDone,
        modifier = modifier,
    )
}

/**
 * The first run, as on the design's first-run board: what kendai is, why it needs All files access,
 * the folders to add, and the books being found in them.
 */
@Composable
fun FirstRunScreen(
    state: FirstRunUiState,
    onStart: () -> Unit,
    onAllow: () -> Unit,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onAnother: () -> Unit,
    onAdd: () -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // On a tablet the first run keeps to a phone's width, in the middle.
        val column = Modifier.widthIn(max = 480.dp).fillMaxHeight()
        when (state.step) {
            FirstRunStep.Welcome -> Welcome(onStart, column)
            FirstRunStep.Access -> Access(onAllow, column)
            FirstRunStep.Choose -> Choose(state, onBack, onToggle, onAnother, onAdd, column)
            FirstRunStep.Finding -> Finding(state.progress, onOpenLibrary, column)
        }
    }
}

@Composable
private fun Welcome(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Masthead(Modifier.weight(1f))
        Actions(
            text = stringResource(R.string.welcome_start),
            onClick = onStart,
            note = stringResource(R.string.welcome_promise),
        )
    }
}

/** The seal, kendai's name and what it means, and what it's for. */
@Composable
private fun Masthead(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(colors.primary, WashiCorners.Tile),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.welcome_seal),
                color = colors.onPrimary,
                fontFamily = Literata,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.app_name),
            style =
                MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp, lineHeight = 44.sp),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.welcome_meaning),
            fontFamily = Literata,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            color = colors.onSurfaceVariant,
        )
        Spacer(
            Modifier.padding(vertical = 28.dp)
                .width(40.dp)
                .height(Washi.ruleWidth)
                .background(colors.outlineVariant)
        )
        Text(
            text = stringResource(R.string.welcome_tagline),
            fontFamily = Literata,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Access(onAllow: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Reasons(Modifier.weight(1f))
        Actions(
            text = stringResource(R.string.access_allow),
            onClick = onAllow,
            note = stringResource(R.string.access_note),
        )
    }
}

/** Why kendai needs All files access, and what it does and doesn't do with it. */
@Composable
private fun Reasons(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 40.dp)
    ) {
        Box(
            modifier =
                Modifier.size(72.dp)
                    .background(colors.surfaceContainer, WashiCorners.Tile)
                    .border(Washi.ruleWidth, colors.outlineVariant, WashiCorners.Tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                KendaiIcons.Folder,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.access_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.access_body),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Point(
            KendaiIcons.Library,
            stringResource(R.string.access_in_place_title),
            stringResource(R.string.access_in_place_body),
        )
        Point(
            KendaiIcons.FolderChosen,
            stringResource(R.string.access_chosen_title),
            stringResource(R.string.access_chosen_body),
        )
        Point(
            KendaiIcons.Private,
            stringResource(R.string.access_private_title),
            stringResource(R.string.access_private_body),
            last = true,
        )
    }
}

/** A step's one thing to do, at the foot of the screen, with a [note] under it. */
@Composable
private fun Actions(
    text: String,
    onClick: () -> Unit,
    note: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InkButton(text = text, onClick = onClick, modifier = Modifier.fillMaxWidth())
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** One promise about access, between hairlines. */
@Composable
private fun Point(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    last: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val rule = colors.outlineVariant
    val width = Washi.ruleWidth
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .drawBehind {
                    val w = width.toPx()
                    drawLine(rule, Offset(0f, w / 2), Offset(size.width, w / 2), w)
                    if (last) {
                        val y = size.height - w / 2
                        drawLine(rule, Offset(0f, y), Offset(size.width, y), w)
                    }
                }
                .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(22.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = body,
                style =
                    MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Choose(
    state: FirstRunUiState,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onAnother: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val choices = state.choices
    Column(modifier) {
        KendaiTopBar(title = stringResource(R.string.choose_title), onBack = onBack)
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp)
        ) {
            Text(
                text =
                    stringResource(
                        when {
                            choices == null -> R.string.choose_looking
                            choices.isEmpty() -> R.string.choose_none
                            else -> R.string.choose_found
                        }
                    ),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (choices != null) {
                Choices(choices, onToggle)
                Another(notOnDevice = state.notOnDevice, onAnother = onAnother)
            }
        }
        InkButton(
            text = pluralStringResource(R.plurals.choose_add, state.ticked, state.ticked),
            onClick = onAdd,
            enabled = state.ticked > 0,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        )
    }
}

/** The folders to choose from, under the device's name, each ticked or not. */
@Composable
private fun Choices(
    choices: List<FolderChoice>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (choices.isEmpty()) return
    Column(modifier) {
        Overline(
            stringResource(R.string.library_device),
            Modifier.padding(top = 18.dp, bottom = 2.dp),
        )
        choices.forEach { choice ->
            FolderRow(
                name = choice.name,
                detail = bookCountLabel(choice.count),
                modifier =
                    Modifier.toggleable(
                        value = choice.ticked,
                        role = Role.Checkbox,
                        onValueChange = { onToggle(choice.path) },
                    ),
            ) {
                TickBox(choice.ticked)
            }
        }
    }
}

/** Add another folder with Android's picker, and a word on changing them later. */
@Composable
private fun Another(notOnDevice: Boolean, onAnother: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        LinkButton(
            text = stringResource(R.string.choose_another),
            onClick = onAnother,
            icon = KendaiIcons.Add,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (notOnDevice) {
            Text(
                text = stringResource(R.string.choose_not_on_device),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
            )
        }
        Text(
            text = stringResource(R.string.choose_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun Finding(progress: Progress, onOpenLibrary: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 36.dp)
        ) {
            Text(
                text =
                    if (progress.done) {
                        pluralStringResource(R.plurals.finding_done, progress.found, progress.found)
                    } else {
                        stringResource(R.string.finding_title)
                    },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(6.dp))
            FoundSoFar(progress)
            ProgressRule(progress.fraction, Modifier.padding(top = 16.dp))
            Spacer(Modifier.height(24.dp))
            Covers(progress, Modifier.padding(bottom = 16.dp))
        }
        Column(
            modifier =
                Modifier.drawBehind {
                        drawLine(
                            colors.outlineVariant,
                            Offset.Zero,
                            Offset(size.width, 0f),
                            1.dp.toPx(),
                        )
                    }
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (progress.done) {
                InkButton(
                    text = stringResource(R.string.finding_open),
                    onClick = onOpenLibrary,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = stringResource(R.string.finding_note),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                RuleButton(
                    text = stringResource(R.string.finding_open),
                    onClick = onOpenLibrary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** "71 found so far in Books, Download and Comics", the count in ink. */
@Composable
private fun FoundSoFar(progress: Progress, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val soFar = pluralStringResource(R.plurals.finding_so_far, progress.found, progress.found)
    val places =
        progress.folders
            .takeIf { it.isNotEmpty() }
            ?.let { stringResource(R.string.finding_in, ListFormatter.getInstance().format(it)) }
    val text = buildAnnotatedString {
        if (!progress.done) {
            withStyle(SpanStyle(color = colors.onSurface, fontWeight = FontWeight.Bold)) {
                append(soFar)
            }
            if (places != null) append(" ")
        }
        if (places != null) append(places)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant,
        modifier = modifier,
    )
}

/** A hairline with a 3 dp seal rule along it as far as [fraction]. */
@Composable
private fun ProgressRule(fraction: Float, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val rule = colors.outlineVariant
    val seal = colors.primary
    Box(
        modifier.fillMaxWidth().height(3.dp).drawBehind {
            val middle = size.height / 2
            drawLine(rule, Offset(0f, middle), Offset(size.width, middle), 1.dp.toPx())
            if (fraction > 0f) {
                drawLine(
                    seal,
                    Offset(0f, middle),
                    Offset(size.width * fraction, middle),
                    size.height,
                )
            }
        }
    )
}

/** The latest covers found, three to a row, with a dashed place for the next while looking. */
@Composable
private fun Covers(progress: Progress, modifier: Modifier = Modifier) {
    val rule = MaterialTheme.colorScheme.outlineVariant
    val cells: List<FoundBook?> = progress.latest + if (progress.done) emptyList() else listOf(null)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        cells.chunked(COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(COLUMNS) { index ->
                    val cell = row.getOrNull(index)
                    when {
                        cell != null -> FoundCover(cell)
                        index == row.lastIndex ->
                            Box(
                                Modifier.width(COVER_WIDTH).height(COVER_HEIGHT).drawBehind {
                                    drawRoundRect(
                                        color = rule,
                                        style =
                                            Stroke(
                                                width = 1.dp.toPx(),
                                                pathEffect =
                                                    PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                                            ),
                                        cornerRadius = CornerRadius(3.dp.toPx()),
                                    )
                                }
                            )
                        else -> Spacer(Modifier.width(COVER_WIDTH))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoundCover(found: FoundBook, modifier: Modifier = Modifier) {
    Column(modifier.width(COVER_WIDTH), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BookCover(found.book, Modifier.width(COVER_WIDTH).height(COVER_HEIGHT))
        Text(
            text = found.book.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = found.folder,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val COLUMNS = 3
private val COVER_WIDTH = 96.dp
private val COVER_HEIGHT = 144.dp
