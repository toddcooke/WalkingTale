package com.talkingwhale

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.talkingwhale.repository.PostRepository
import com.talkingwhale.repository.UserRepository
import com.talkingwhale.vo.Post
import com.talkingwhale.vo.Resource
import com.talkingwhale.vo.User


class MainViewModel : ViewModel() {

    private val postRepository = PostRepository
    private val userRepository = UserRepository
    var currentUser: User? = null
    var localPosts: LiveData<Resource<List<Post>>> = MutableLiveData()
    private var getPosts: MutableLiveData<PostRepository.CornerLatLng> = MutableLiveData()

    init {
        localPosts = Transformations.switchMap(getPosts, Function {
            return@Function postRepository.getNearbyPosts(it)
        })
    }

    fun getUser(userId: String): LiveData<Resource<User>> {
        return userRepository.loadUser(userId)
    }

    fun putUser(user: User): LiveData<Resource<Unit>> {
        return userRepository.putUser(user)
    }

    fun putPost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.addPost(post)
    }

    fun putFile(pair: Pair<Post, Context>): LiveData<Resource<Post>> {
        return postRepository.putFile(pair)
    }

    fun getNewPosts(cornerLatLng: PostRepository.CornerLatLng) {
        getPosts.value = cornerLatLng
    }

    fun deletePost(post: Post): LiveData<Resource<Unit>> {
        return postRepository.deletePost(post)
    }
}