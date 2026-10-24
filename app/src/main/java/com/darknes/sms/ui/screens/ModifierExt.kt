package com.darknes.sms.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** Uzun basma ile menu acmak icin kisa yardimci. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.combinedClickableCompat(onLongClick: () -> Unit): Modifier = composed {
    this.combinedClickable(onClick = {}, onLongClick = onLongClick)
}
