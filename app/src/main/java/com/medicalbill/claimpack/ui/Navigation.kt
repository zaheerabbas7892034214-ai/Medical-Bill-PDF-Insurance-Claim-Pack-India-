package com.medicalbill.claimpack.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object ConvertProgress : Screen("convert_progress/{fileUri}") {
        fun createRoute(fileUri: String) = "convert_progress/$fileUri"
    }
    object Preview : Screen("preview/{fileId}") {
        fun createRoute(fileId: Long) = "preview/$fileId"
    }
    object ClaimPackPreview : Screen("claim_pack_preview/{fileId}") {
        fun createRoute(fileId: Long) = "claim_pack_preview/$fileId"
    }
    object Export : Screen("export/{fileId}") {
        fun createRoute(fileId: Long) = "export/$fileId"
    }
    object Paywall : Screen("paywall")
    object Settings : Screen("settings")
}
