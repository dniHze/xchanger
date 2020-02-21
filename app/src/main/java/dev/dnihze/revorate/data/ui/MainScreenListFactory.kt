package dev.dnihze.revorate.data.ui

import dev.dnihze.revorate.data.ui.mapper.DisplayAmountMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyFlagMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyNameMapper
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import javax.inject.Inject

class MainScreenListFactory @Inject constructor(
    private val displayAmountMapper: DisplayAmountMapper,
    private val flagMapper: DisplayCurrencyFlagMapper,
    private val nameMapper: DisplayCurrencyNameMapper
) {

    fun create(
        table: ExchangeTable,
        amount: CurrencyAmount,
        baseCurrencyFreeInputAmount: CharSequence?
    ): List<CurrencyDisplayItem> {
        if (table.isEmpty()) {
            return emptyList()
        }

        val baseCurrency = table.baseCurrency
        assert(baseCurrency == amount.currency)

        val amountList = mutableListOf(amount)

        table.mapTo(amountList) { rate ->
            rate * amount
        }

        return amountList.mapIndexed { index, currencyAmount ->
            val displayAmount = displayAmountMapper.map(currencyAmount)
            val freeInputAmount = if (currencyAmount.currency == baseCurrency) {
                baseCurrencyFreeInputAmount ?: displayAmount
            } else {
                null
            }
            CurrencyDisplayItem(
                displayAmount = displayAmount,
                freeInput = freeInputAmount,
                currencyFullNameId = nameMapper.map(currencyAmount.currency),
                currencyFlagEmojiId = flagMapper.map(currencyAmount.currency),
                amount = currencyAmount,
                inputEnabled = index == 0
            )
        }


    }
}