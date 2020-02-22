package dev.dnihze.revorate.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.ui.main.delegate.AdapterActionsDelegate

class CurrencyAdapter(
    private val delegate: AdapterActionsDelegate
) : RecyclerView.Adapter<CurrencyHolder>(), CurrencyDataAccessor {

    init {
        setHasStableIds(true)
    }

    private val data: MutableList<CurrencyDisplayItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHolder {
        return CurrencyHolder(parent, this, delegate)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int, payloads: List<Any>) {
        holder.bind(payloads)
    }

    override fun getItemId(position: Int) = this(position).id

    override fun invoke(index: Int): CurrencyDisplayItem {
        return data[index]
    }

    fun setDataSilently(data: List<CurrencyDisplayItem>) {
        this.data.clear()
        this.data += data
    }

    fun getData(): List<CurrencyDisplayItem> {
        return data
    }
}