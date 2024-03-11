package com.wozavez.fmr.phoneappcompose

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class SampleOngoingCallViewModel: PreviewParameterProvider<OngoingCallViewModel> {
    private val sample = OngoingCallViewModel
    override val values: Sequence<OngoingCallViewModel>
        get() = sequenceOf(sample)

}
