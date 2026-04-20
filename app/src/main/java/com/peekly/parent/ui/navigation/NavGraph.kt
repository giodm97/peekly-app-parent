package com.peekly.parent.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN       = "main"
    const val CHILD_DASH = "child_dash/{childId}/{childName}"
    const val ADD_CHILD  = "add_child"
    const val PAIRING    = "pairing/{childId}"
    const val DIGEST     = "digest/{childId}/{childName}"

    const val DASHBOARD = MAIN

    fun childDash(childId: Long, childName: String) =
        "child_dash/$childId/${android.net.Uri.encode(childName)}"
    fun digest(childId: Long, childName: String) =
        "digest/$childId/${android.net.Uri.encode(childName)}"
    fun pairing(childId: Long) = "pairing/$childId"
}
