package com.kazumaproject.markdownhelperkeyboard.groq_voice

sealed interface RecordingState {
    data object Idle : RecordingState
    data object Recording : RecordingState
    data object Transcribing : RecordingState
    data class Error(val message: String) : RecordingState
}

sealed interface RecordingEvent {
    data object MicTapped : RecordingEvent
    data class TranscriptionSucceeded(val text: String) : RecordingEvent
    data class TranscriptionFailed(val message: String) : RecordingEvent
    data object ErrorAcknowledged : RecordingEvent
}

object RecordingStateMachine {

    fun reduce(state: RecordingState, event: RecordingEvent): RecordingState {
        return when (state) {
            is RecordingState.Idle -> when (event) {
                is RecordingEvent.MicTapped -> RecordingState.Recording
                else -> state
            }

            is RecordingState.Recording -> when (event) {
                is RecordingEvent.MicTapped -> RecordingState.Transcribing
                else -> state
            }

            is RecordingState.Transcribing -> when (event) {
                is RecordingEvent.TranscriptionSucceeded -> RecordingState.Idle
                is RecordingEvent.TranscriptionFailed -> RecordingState.Error(event.message)
                else -> state
            }

            is RecordingState.Error -> when (event) {
                is RecordingEvent.MicTapped -> RecordingState.Recording
                is RecordingEvent.ErrorAcknowledged -> RecordingState.Idle
                else -> state
            }
        }
    }
}
