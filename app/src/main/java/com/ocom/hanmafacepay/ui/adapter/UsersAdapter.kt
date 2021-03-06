package com.ocom.hanmafacepay.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.User
import com.ocom.hanmafacepay.ui.adapter.UsersAdapter.MyViewHolder
import com.ocom.hanmafacepay.util.extension.base64ToByteArray
import java.util.*
import kotlin.text.toLowerCase as toLowerCase1

/**
 * Created by ravi on 16/11/17.
 * 用户信息适配器
 */
class UsersAdapter(
    private val context: Context,
    private val contactList: MutableList<User>,
    private val listener: ContactsAdapterListener
) : RecyclerView.Adapter<MyViewHolder>(), Filterable {
    private var contactListFiltered: MutableList<User>

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mAvatar: String = ""
        var name: TextView = view.findViewById(R.id.name)
        var phone: TextView = view.findViewById(R.id.id_tv)
        var cardNo: TextView = view.findViewById(R.id.card_no_tv)
        var thumbnail: ImageView = view.findViewById(R.id.thumbnail)

        init {
            view.setOnClickListener {
                // send selected contact in callback
                listener.onContactSelected(contactListFiltered[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_row_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (_, userid, name) = contactListFiltered[position]
        if (holder.mAvatar == userid)
            return
        holder.name.text = "姓名: ${name}"
        holder.phone.text = "ID: ${userid}"
        if (contactListFiltered[position].card.isNotEmpty()) {
            holder.cardNo.text = "卡号: ${contactListFiltered[position].card}"
        }
        val byteArray = userid.base64ToByteArray() ?: byteArrayOf()
//        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        Glide
            .with(context)
            .asBitmap()
            .load(byteArray)
            .apply(RequestOptions().circleCrop().error(R.drawable.icon_smile))
            .into(holder.thumbnail)
    }

    override fun getItemCount(): Int {
        return contactListFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            @SuppressLint("DefaultLocale")
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                contactListFiltered = if (charString.isEmpty()) {
                    contactList
                } else {
                    val filteredList: MutableList<User> =
                        ArrayList()
                    for (row in contactList) {
                        if (row.name.toLowerCase1().contains(charString.toLowerCase1()) || row.userid.contains(
                                charSequence
                            )
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = contactListFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                contactListFiltered =
                    filterResults.values as ArrayList<User>
                notifyDataSetChanged()
            }
        }
    }

    fun switchData(users: List<User>) {
        contactList.clear()
        contactList.addAll(users)
        notifyDataSetChanged()
    }

    interface ContactsAdapterListener {
        fun onContactSelected(contact: User?)
    }

    init {
        contactListFiltered = contactList
    }
}