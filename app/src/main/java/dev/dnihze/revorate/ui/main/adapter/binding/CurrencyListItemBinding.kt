package dev.dnihze.revorate.ui.main.adapter.binding

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.emoji.widget.EmojiAppCompatTextView
import dev.dnihze.revorate.R

class CurrencyListItemBinding(
    parentView: View
) {
    val emoji: EmojiAppCompatTextView = parentView.findViewById(R.id.emoji_text_view)
    val title: AppCompatTextView = parentView.findViewById(R.id.title)
    val subtitle: AppCompatTextView = parentView.findViewById(R.id.subtitle)
    val input: AppCompatEditText = parentView.findViewById(R.id.input)
}