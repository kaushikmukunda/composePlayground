package com.km.composePlayground.scroller.common

import com.km.composePlayground.base.UiModel

/**
 * Marker interface that represents a UiModel which trails the renderable portion of a list.
 * Any UiModels that follow the first RenderBlockingUiModel (including other RenderBlockingUiModels)
 * should be ignored and not rendered.
 *
 * Example:
 *
 * Given a listOf(
 *   AUiModel,
 *   BUiModel,
 *   CRenderBlockingUiModel,
 *   DUiModel,
 *   ERenderBlockingUiModel
 * )
 * In this example, the render layer should render up to and including CRenderBlockingUiModel and
 * ignore DUiModel and ERenderBlockingUiModel.
 */
interface RenderBlockingUiModel : UiModel