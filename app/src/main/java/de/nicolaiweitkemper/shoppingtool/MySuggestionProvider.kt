package de.nicolaiweitkemper.shoppingtool

import android.content.SearchRecentSuggestionsProvider

class MySuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "de.nicolaiweitkemper.shoppingtool.MySuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}