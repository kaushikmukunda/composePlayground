package com.km.composePlayground.modifiers


import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableContract
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * Shorthand for remember { mutableStateOf(init()) } which was removed from the Compose API.
 * See [remember] and [MutableState] for more details.
 */
@Composable
inline fun <T> rememberState(
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    init: @ComposableContract(preventCapture = true) () -> T
): MutableState<T> = remember { mutableStateOf(init(), policy) }

/**
 * Shorthand for remember(v1) { mutableStateOf(init()) } which was removed from the Compose API.
 * See [remember] and [MutableState] for more details.
 */
@Composable
inline fun <T, /*reified*/ V1> rememberStateFor(
    v1: V1,
    init: @ComposableContract(preventCapture = true) () -> T
): MutableState<T> = remember(v1) { mutableStateOf(init()) }

/**
 * Shorthand for remember(v1, v2) { mutableStateOf(init()) } which was removed from the Compose API.
 * See [remember] and [MutableState] for more details.
 */
@Composable
inline fun <T, reified V1, reified V2> rememberStateFor(
    v1: V1,
    v2: V2,
    init: @ComposableContract(preventCapture = true) () -> T
): MutableState<T> = remember(v1, v2) { mutableStateOf(init()) }

/**
 * Shorthand for remember(*inputs) { mutableStateOf(init()) } which was removed from Compose API.
 * See [remember] and [MutableState] for more details.
 */
@Composable
inline fun <T> rememberStateFor(
    vararg inputs: Any?,
    init: @ComposableContract(preventCapture = true) () -> T
): MutableState<T> = remember(*inputs) { mutableStateOf(init()) }
