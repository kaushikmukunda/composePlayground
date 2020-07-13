package com.km.compose_tutorial.actionbuttongroup

import androidx.compose.Composable
import androidx.compose.Stable
import androidx.ui.core.Modifier
import com.km.compose_tutorial.actionbutton.ActionButtonComposer
import com.km.compose_tutorial.buttongroup.ButtonGroupComposer


/** Composable button group container for injecting dependencies. */
@Stable
class ActionButtonGroupComposer(actionButtonComposer: ActionButtonComposer) :
    ButtonGroupComposer(actionButtonComposer) {

    @Composable
    fun compose(model: ActionButtonGroupUiModel, modifier: Modifier = Modifier) {
        compose(model.buttonGroupUiModel, modifier)
    }
}
