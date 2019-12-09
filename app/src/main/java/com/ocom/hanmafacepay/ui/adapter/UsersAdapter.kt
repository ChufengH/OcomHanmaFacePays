package com.ocom.hanmafacepay.ui.adapter

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
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.User
import com.ocom.hanmafacepay.ui.adapter.UsersAdapter.MyViewHolder
import com.ocom.hanmafacepay.util.extension.base64ToByteArray
import java.util.*

/**
 * Created by ravi on 16/11/17.
 * 用户信息适配器
 */
class UsersAdapter(
    private val context: Context,
    private val contactList: List<User>,
    private val listener: ContactsAdapterListener
) : RecyclerView.Adapter<MyViewHolder>(), Filterable {
    private var contactListFiltered: List<User>

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView
        var phone: TextView
        var thumbnail: ImageView

        init {
            name = view.findViewById(R.id.name)
            phone = view.findViewById(R.id.phone)
            thumbnail = view.findViewById(R.id.thumbnail)
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
        holder.name.text = name
        holder.phone.text = userid
        val byteArray = userid.base64ToByteArray() ?: byteArrayOf()
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        Glide
            .with(context)
            .load(bitmap ?: R.drawable.icon_smile)
            .apply(RequestOptions().error(R.drawable.icon_smile))
            .into(holder.thumbnail)
    }

    override fun getItemCount(): Int {
        return contactListFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                contactListFiltered = if (charString.isEmpty()) {
                    contactList
                } else {
                    val filteredList: MutableList<User> =
                        ArrayList()
                    for (row in contactList) { // name match condition. this might differ depending on your requirement
// here we are looking for name or phone number match
                        if (row.name.toLowerCase().contains(charString.toLowerCase()) || row.userid.contains(
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

    interface ContactsAdapterListener {
        fun onContactSelected(contact: User?)
    }

    init {
        contactListFiltered = contactList
    }
}