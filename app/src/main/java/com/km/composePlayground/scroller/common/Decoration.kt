package com.km.composePlayground.scroller.common

/** Marker interface denoting decoration to be applied to an item in scroller ui. */
interface Decoration

/** Container padding decoration to be applied to an item in scroller ui. */
object ContainerPaddingDecoration : Decoration

/** Content frame decoration which applies outer framing style to store content. */
object ContentFrameDecoration : Decoration
