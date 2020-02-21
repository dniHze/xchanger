package dev.dnihze.revorate.ui.main.util

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
        return old.amount.amount == new.amount.amount &&
                old.amount.currency == new.amount.currency &&
                old.inputEnabled == new.inputEnabled
    }
}