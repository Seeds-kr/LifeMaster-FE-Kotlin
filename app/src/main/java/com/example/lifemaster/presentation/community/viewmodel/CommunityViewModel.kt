package com.example.lifemaster.presentation.community.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.community.model.CommunityItem

class CommunityViewModel : ViewModel() {

    private val _items = MutableLiveData<MutableList<CommunityItem>>(mutableListOf())
    val items: LiveData<MutableList<CommunityItem>> get() = _items

    private val likedPosts = mutableSetOf<String>()

    fun addItemAtTop(item: CommunityItem) {
        val list = _items.value ?: mutableListOf()
        list.add(0, item)
        _items.value = list
    }

    fun getById(id: String?): CommunityItem? {
        return _items.value?.find { it.id == id }
    }

    fun updateItem(
        id: String,
        title: String,
        content: String,
        shareCalendar: Boolean,
        fileUri: String? = null
    ) {
        val list = _items.value ?: return
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val old = list[idx]
            list[idx] = old.copy(
                title = title,
                content = content,
                shareCalendar = shareCalendar,
                fileUri = fileUri
            )
            _items.value = list
        }
    }

    fun deleteById(id: String) {
        val list = _items.value ?: return
        list.removeAll { it.id == id }
        _items.value = list
        likedPosts.remove(id)
    }

    fun isPostLiked(id: String): Boolean {
        return likedPosts.contains(id)
    }

    fun togglePostLike(id: String): Int {
        val list = _items.value ?: return 0
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val item = list[idx]
            if (likedPosts.contains(id)) {
                likedPosts.remove(id)
                list[idx] = item.copy(likes = (item.likes - 1).coerceAtLeast(0))
            } else {
                likedPosts.add(id)
                list[idx] = item.copy(likes = item.likes + 1)
            }
            _items.value = list
            return list[idx].likes
        }
        return 0
    }
}