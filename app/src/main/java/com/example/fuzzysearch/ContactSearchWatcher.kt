package com.example.fuzzysearch

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.util.*

class FuzzyAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, items.toMutableList()), Filterable {

    private val filter = FuzzyFilter()

    override fun getFilter(): Filter {
        return filter
    }

    inner class FuzzyFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint != null && constraint.isNotEmpty()) {
                val searchQuery = constraint.toString()
                val filteredContacts = FuzzySearch.extractAll(searchQuery, items)
                    .sortedByDescending { it.score }
                    .map { it.string }
                    .take(3) // limit to 3 results
                results.values = filteredContacts
                results.count = filteredContacts.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                clear()
                addAll(results.values as List<String>)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (view is TextView) {
            view.text = getItem(position)
        } else {
            throw UnsupportedOperationException("Unsupported view type: ${view?.javaClass?.simpleName}")
        }
        return view
    }


}



class ContactSearchWatcher(private val context: Context, private val autoCompleteTextView: AutoCompleteTextView) : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        val searchTerm = s.toString().trim()
        if (searchTerm.isEmpty()) {
            autoCompleteTextView.dismissDropDown()
            return
        }
        val contacts = searchContacts(context, searchTerm)
        if (contacts.isEmpty()) {
            autoCompleteTextView.dismissDropDown()
            return
        }
        val adapter = FuzzyAdapter(context, contacts)

        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.showDropDown()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not used
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Not used
    }

    private fun searchContacts(context: Context, searchTerm: String): List<String> {
        val contacts = ArrayList<String>()
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$searchTerm%")
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                contacts.add(name)
            }
            cursor.close()
        }
        return contacts
    }
}
