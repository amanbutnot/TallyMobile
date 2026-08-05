package org.prime.easykarobar.ui.shared.composables

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun QuantityTextField(
    quantity: Double,
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit = { it() }
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isFocused by remember { mutableStateOf(false) }

    fun formatQuantity(q: Double): String {
        return if (q == 0.0) "" else if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()
    }

    LaunchedEffect(quantity, isFocused) {
        if (!isFocused) {
            val targetText = formatQuantity(quantity)
            textFieldValue = TextFieldValue(
                text = targetText,
                selection = TextRange(targetText.length)
            )
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            // Allow only numbers and a single decimal point
            if (newValue.text.isEmpty() || newValue.text.matches(Regex("""^\d*\.?\d*$"""))) {
                textFieldValue = newValue
                val parsed = newValue.text.toDoubleOrNull() ?: 0.0
                onQuantityChange(parsed)
            }
        },
        modifier = modifier.onFocusChanged { focus ->
            isFocused = focus.isFocused
            if (focus.isFocused) {
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(0, textFieldValue.text.length)
                )
            }
        },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        decorationBox = decorationBox
    )
}
