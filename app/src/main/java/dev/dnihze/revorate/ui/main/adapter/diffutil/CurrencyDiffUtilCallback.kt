package dev.dnihze.revorate.ui.main.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem

class CurrencyDiffUtilCallback(
    private val oldList: List<CurrencyDisplayItem>,
    private val newList: List<CurrencyDisplayItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.id == new.id
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return old.displayAmount == new.displayAmount &&
                old.amount.currency == new.amount.currency &&
                old.inputEnabled == new.inputEnabled
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        val inputChanged = old.inputEnabled != new.inputEnabled
        val bindSumChanged = old.displayAmount != new.displayAmount || (inputChanged && !new.inputEnabled)

        return if (inputChanged || bindSumChanged) {
            CurrencyDiffUtilPayload(
                sumChanged = bindSumChanged,
                inputEnabledChanged = inputChanged
            )
        } else null
    }
}