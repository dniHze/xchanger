package dev.dnihze.revorate.ui.main.adapter

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.ui.main.adapter.binding.CurrencyListItemBinding
import dev.dnihze.revorate.ui.main.adapter.diffutil.CurrencyDiffUtilPayload
import dev.dnihze.revorate.ui.main.delegate.AdapterActionsDelegate
import dev.dnihze.revorate.ui.main.util.CharsAfterDotInputFilter
import dev.dnihze.revorate.ui.main.util.ZeroInputFilter
import dev.dnihze.revorate.utils.ext.showKeyboard

class CurrencyHolder(
    parent: ViewGroup,
    private val dataAccessor: CurrencyDataAccessor,
    private val delegate: AdapterActionsDelegate
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)
), View.OnClickListener {


    private val viewBinding = CurrencyListItemBinding(itemView)
    private val zeroInputFilter = ZeroInputFilter()
    private val afterDotInputFilter = CharsAfterDotInputFilter()
    private val adapterData: CurrencyDisplayItem
        get() {
            return dataAccessor(adapterPosition)
        }

    init {
        itemView.setOnClickListener(this)
        viewBinding.dummyClickView.setOnClickListener(this)
        viewBinding.input.filters = viewBinding.input.filters + afterDotInputFilter + zeroInputFilter

        viewBinding.input.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            if (!isAdapterPositionValid()) return@addTextChangedListener

            val data = adapterData
            if (data.inputEnabled) {
                val rawInput = text.stringValue()
                if (!zeroInputFilter.doubleCheck(rawInput)) {
                    viewBinding.input.setText(rawInput)
                    return@addTextChangedListener
                }
                delegate.onNewInput(rawInput, data.amount)
            }
        })
    }

    override fun onClick(v: View) {
        if (!isAdapterPositionValid()) return
        if (!adapterData.inputEnabled) {
            delegate.onNewCurrency(adapterData.amount)
        } else {
            if (adapterData.inputEnabled) {
                itemView.post {
                    if (!viewBinding.input.hasFocus()) {
                        viewBinding.input.setSelection(viewBinding.input.stringValue().length)
                    }
                    viewBinding.input.showKeyboard()
                }
            }
        }
    }

    fun bind() {
        val data = adapterData
        viewBinding.title.text = data.amount.currency.isoName

        viewBinding.emoji.setText(data.currencyFlagEmojiId)
        viewBinding.subtitle.setText(data.currencyFullNameId)

        bindInputState(data)
        bindSum(data)

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

            if (payload.sumChanged) {
                bindSum(data)
            }

            if (payload.inputEnabledChanged) {
                bindInputState(data)
                viewBinding.input.isEnabled = data.inputEnabled

                if (data.inputEnabled) {
                    viewBinding.input.post {
                        viewBinding.input.showKeyboard()
                    }
                }
            }
        }
    }

    private fun bindInputState(data: CurrencyDisplayItem) {
        if (data.inputEnabled) {
            viewBinding.dummyClickView.isVisible = false

            if (!viewBinding.input.isEnabled)
                viewBinding.input.isEnabled = true

            viewBinding.input.setSelection(viewBinding.input.stringValue().length)
        } else {
            viewBinding.dummyClickView.isVisible = true

            if (viewBinding.input.isEnabled)
                viewBinding.input.isEnabled = false
        }

        zeroInputFilter.enabled = data.inputEnabled
        afterDotInputFilter.enabled = data.inputEnabled
    }

    private fun bindSum(data: CurrencyDisplayItem) {
        if (!data.inputEnabled) {
            viewBinding.input.setText(data.displayAmount)
        } else if (data.freeInput != null && data.freeInput != (viewBinding.input.stringValue())) {
            viewBinding.input.setText(data.freeInput)
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