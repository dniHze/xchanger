package dev.dnihze.revorate.ui.main.adapter

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import androidx.emoji.widget.EmojiAppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.ui.main.util.AdapterActionsDelegate
import dev.dnihze.revorate.utils.ext.showKeyboard

class CurrencyHolder(
    parent: ViewGroup,
    private val dataAccessor: CurrencyDataAccessor,
    private val delegate: AdapterActionsDelegate
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)
) {

    private val emoji: EmojiAppCompatTextView = itemView.findViewById(R.id.emoji_text_view)

    private val title: AppCompatTextView = itemView.findViewById(R.id.title)
    private val subtitle: AppCompatTextView = itemView.findViewById(R.id.subtitle)

    private val input: AppCompatEditText = itemView.findViewById(R.id.input)

    private val adapterData: CurrencyDisplayItem
        get() {
            return dataAccessor(adapterPosition)
        }

    init {
        itemView.setOnClickListener {
            if (!isAdapterPositionValid()) return@setOnClickListener

            if (!adapterData.inputEnabled) {
                delegate.onNewCurrency(adapterData.amount)
                if (!input.hasFocus())
                    input.requestFocus()
                input.showKeyboard()
            } else {
                if (!input.hasFocus())
                    input.requestFocus()
                input.showKeyboard()
            }
        }

        input.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            if (!isAdapterPositionValid()) return@addTextChangedListener
            val data = adapterData
            if (data.inputEnabled) {
                val number = text?.toString()?.toDoubleOrNull() ?: 0.0
                if (number != data.amount.amount) {
                    val newAmount = CurrencyAmount(number, currency = data.amount.currency)
                    delegate.onNewCurrency(newAmount)
                }
            }
        })
    }

    fun bind() {
        val data = adapterData
        title.text = data.amount.currency.isoName

        emoji.setText(data.currencyFlagEmojiId)
        subtitle.setText(data.currencyFullNameId)

        if (!input.isFocused || !data.inputEnabled) {
            input.setText(data.displayAmount)
            input.setSelection(data.displayAmount.length)
        }

        input.isEnabled = data.inputEnabled

        if (data.amount.currency.digitsAfterSeparator != 0 && input.inputType != FLAG_DECIMAL_NUMBER) {
            input.inputType = FLAG_DECIMAL_NUMBER
        } else if (data.amount.currency.digitsAfterSeparator == 0 && input.inputType != FLAG_NUMBER) {
            input.inputType = FLAG_NUMBER
        }
    }

    fun bind(payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bind()
            return
        }
    }


    private fun isAdapterPositionValid(): Boolean {
        return adapterPosition != RecyclerView.NO_POSITION
    }

    private companion object {
        private const val FLAG_NUMBER = InputType.TYPE_CLASS_NUMBER
        private const val FLAG_DECIMAL_NUMBER = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
}