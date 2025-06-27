package com.chuckerteam.chucker.sample.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chuckerteam.chucker.internal.ui.theme.AppAccentColor
import com.chuckerteam.chucker.internal.ui.theme.ChuckerTheme

/**
 * A composable that presents a radio button with an adjacent text label and full control over
 * its accessibility semantics.
 *
 * This combines a [RadioButton] and a [Text] in a horizontal [Row], then:
 * 1. Merges their semantics into one node so TalkBack reads a single description.
 * 2. Uses `clearAndSetSemantics { … }` to declare exactly:
 *    - `role = Role.RadioButton`
 *    - A `contentDescription` of the form:
 *        “Selected Application radio button 1 of 2” or
 *        “Not selected Network radio button 2 of 2 Double tap to select”
 *    - An explicit click action with the hint “Double tap to select”
 * 3. Makes the entire row clickable—forwarding `onClick` to select the button.
 *
 * @param label       The text to display next to the radio button.
 * @param selected    Whether this radio button is currently selected.
 * @param onClick     Callback invoked when the user taps the row.
 * @param index       The 1-based position of this radio button within its group of two for accessibility purpose.
 * @param modifier    Optional [Modifier] for layout or drawing adjustments.
 */
@Composable
internal fun LabeledRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        "${if (selected) "Selected" else "Not selected"} " +
                        "$label radio button $index of 2 ${if (!selected) "Double tap to select" else ""} "
                }.clearAndSetSemantics {}
                .clickable(
                    onClick = onClick,
                    role = Role.RadioButton,
                ),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = AppAccentColor,
                ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(
    name = "Phone - Light",
    device = Devices.PIXEL_4,
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Phone - Dark",
    device = Devices.PIXEL_4,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LabeledRadioButtonPreview() {
    ChuckerTheme {
        LabeledRadioButton(
            label = "Network",
            selected = true,
            index = 1,
            onClick = {},
        )
    }
}
