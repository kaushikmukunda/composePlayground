package com.km.composePlayground.components.common

const val NEVER_EQUAL_ID = "NEVER_EQUAL_ID"

/**
 * Interface implemented by an MVVM contract layer entity that can be uniquely identified.
 *
 * It can be used to compare two items when diffing (i.e. between an old and new lists of UiModels).
 *
 * If the data identity of two entities is the same, then they are considered as same. This allows
 * rendering optimizations so that only the items that have changed are re-rendered. If the string
 * returned in dataId is [NEVER_EQUAL_ID] or if an item is not an instance of [Identifiable], then
 * the item is not considered to be identical to any other item in a new list.
 */
interface Identifiable {
  val dataId: String
}