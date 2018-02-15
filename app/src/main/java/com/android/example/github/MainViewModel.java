package com.android.example.github;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.repository.UserRepository;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    private UserRepository userRepository;

    @Inject
    MainViewModel(RepoRepository repository, UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    LiveData<Resource<User>> getUser(String userId) {
        return userRepository.loadUser(userId);
    }

    LiveData<Resource<Void>> createUser(User user) {
        return userRepository.putUser(user);
    }
}
