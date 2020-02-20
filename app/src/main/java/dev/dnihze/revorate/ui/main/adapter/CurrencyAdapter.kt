package dev.dnihze.revorate.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.emoji.widget.EmojiAppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.ui.main.util.AdapterActionsDelegate

typealias CurrencyDataAccessor = (index: Int) -> CurrencyDisplayItem

class CurrencyAdapter(
    private val delegate: AdapterActionsDelegate
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyHolder>(), CurrencyDataAccessor {

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

    override fun getItemId(position: Int) = this(position).id

    override fun invoke(index: Int): CurrencyDisplayItem {
        return data[index]
    }

    fun setData(data: List<CurrencyDisplayItem>) {
        setDataSilently(data)
        notifyDataSetChanged()
    }

    fun setDataSilently(data: List<CurrencyDisplayItem>) {
        this.data.clear()
        this.data += data
    }

    fun getData(): List<CurrencyDisplayItem> {
        return data
    }

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

        private val input: AppCompatTextView = itemView.findViewById(R.id.input)

        private val adapterData: CurrencyDisplayItem
            get() { return dataAccessor(adapterPosition) }

        init {
            itemView.setOnClickListener {
                if (!isAdapterPositionValid()) return@setOnClickListener

                if (!adapterData.inputEnabled) {
                    delegate.onNewCurrency(adapterData.amount)
                }
            }
        }

        fun bind() {
            title.text = adapterData.amount.currency.isoName

            emoji.setText(adapterData.currencyFlagEmojiId)
            subtitle.setText(adapterData.currencyFullNameId)

            input.setText(adapterData.displayAmount)
        }

        private fun isAdapterPositionValid(): Boolean {
            return adapterPosition != RecyclerView.NO_POSITION
        }

    }
}