package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

/** The one thing to do on a screen: paper on ink, 48 dp tall. */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = WashiCorners.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = colors.onSurface,
                contentColor = colors.surface,
                disabledContainerColor = colors.outlineVariant,
                disabledContentColor = colors.onSurfaceVariant,
            ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
    }
}

/** Something to do that isn't the main thing: ink on paper inside a rule. */
@Composable
fun RuleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = WashiCorners.Button,
        border = BorderStroke(Washi.ruleWidth, colors.outlineVariant),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.onSurface,
            ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
    }
}

/**
 * A quiet action in the seal's colour, like a link: Remove, or with an [icon], Add another folder,
 * which lines its icon up with the list above it. TalkBack says [description] in place of [text]
 * when the text alone wouldn't say what it acts on, as with "Remove Books".
 */
@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    description: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = WashiCorners.Button,
        contentPadding = PaddingValues(start = if (icon == null) 12.dp else 0.dp, end = 12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = color),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    if (description == null) Modifier
                    else Modifier.semantics { contentDescription = description },
            )
        }
    }
}
