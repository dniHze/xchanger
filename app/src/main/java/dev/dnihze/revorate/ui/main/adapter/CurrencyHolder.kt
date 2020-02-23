package dev.dnihze.revorate.ui.main.adapter

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.ui.main.adapter.binding.CurrencyListItemBinding
import dev.dnihze.revorate.ui.main.delegate.AdapterActionsDelegate
import dev.dnihze.revorate.ui.main.adapter.diffutil.CurrencyDiffUtilPayload
import dev.dnihze.revorate.utils.ext.showKeyboard

class CurrencyHolder(
    parent: ViewGroup,
    private val dataAccessor: CurrencyDataAccessor,
    private val delegate: AdapterActionsDelegate
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)
) {

    private val viewBinding = CurrencyListItemBinding(itemView)

    private val adapterData: CurrencyDisplayItem
        get() {
            return dataAccessor(adapterPosition)
        }

    init {
        itemView.setOnClickListener {
            if (!isAdapterPositionValid()) return@setOnClickListener

            if (!adapterData.inputEnabled) {
                delegate.onNewCurrency(adapterData.amount)
            } else {
                itemView.post {
                    viewBinding.input.showKeyboard()
                }
            }
        }

        viewBinding.input.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            if (!isAdapterPositionValid()) return@addTextChangedListener
            val data = adapterData
            if (data.inputEnabled) {
                delegate.onNewInput(text.stringValue(), data.amount)
            }
        })
    }

    fun bind() {
        val data = adapterData
        viewBinding.title.text = data.amount.currency.isoName

        viewBinding.emoji.setText(data.currencyFlagEmojiId)
        viewBinding.subtitle.setText(data.currencyFullNameId)

        bindSum(data)

        viewBinding.input.isEnabled = data.inputEnabled

        if (data.amount.currency.digitsAfterSeparator != 0 && viewBinding.input.inputType != FLAG_DECIMAL_NUMBER) {
            viewBinding.input.inputType = FLAG_DECIMAL_NUMBER
        } else if (data.amount.currency.digitsAfterSeparator == 0 && viewBinding.input.inputType != FLAG_NUMBER) {
            viewBinding.input.inputType = FLAG_NUMBER
        }
    }

    fun bind(payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bind()
        }

        val payload = payloads.firstOrNull() as? CurrencyDiffUtilPayload
        if (payload != null) {
            val data = adapterData

            if (payload.inputEnabledChanged) {
                viewBinding.input.isEnabled = data.inputEnabled

                if (data.inputEnabled) {
                    viewBinding.input.post {
                        viewBinding.input.showKeyboard()
                    }
                }
            }

            if (payload.sumChanged) {
                bindSum(data)
            }

        }
    }

    private fun bindSum(data: CurrencyDisplayItem) {
        if (!data.inputEnabled) {
            viewBinding.input.setText(data.displayAmount)
            viewBinding.input.setSelection(data.displayAmount.length)
        } else if (data.freeInput != null && data.freeInput != (viewBinding.input.stringValue())) {
            viewBinding.input.setText(data.freeInput)
            viewBinding.input.setSelection(data.freeInput.length)
        }
    }

    private fun AppCompatEditText.stringValue() = text?.toString() ?: ""

    private fun CharSequence?.stringValue() = this?.toString() ?: ""

    private fun isAdapterPositionValid(): Boolean {
        return adapterPosition != RecyclerView.NO_POSITION
    }

    private companion object {
        private const val FLAG_NUMBER = InputType.TYPE_CLASS_NUMBER
        private const val FLAG_DECIMAL_NUMBER = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
}