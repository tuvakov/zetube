package com.tuvakov.zetube.android.ui.channel

sealed class LiveDataState

object Success : LiveDataState()

object InProgress : LiveDataState()

object EmptyList : LiveDataState()

class Error(val exception: Exception? = null) : LiveDataState()

class ImmatureSyncException : Exception()