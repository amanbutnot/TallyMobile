package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable

expect fun sharePdf(filePath:String)

@Composable
expect fun shareText(text:String)