package com.android.example.github;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.android.example.github.repository.StoryRepository;
import com.android.example.github.repository.UserRepository;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import java.io.File;

import javax.inject.Inject;

import kotlin.Pair;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    @Inject
    MainViewModel(StoryRepository repository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.storyRepository = repository;
    }

    LiveData<Resource<User>> getUser(String userId) {
        return userRepository.loadUser(userId);
    }

    LiveData<Resource<Void>> createUser(User user) {
        return userRepository.putUser(user);
    }

    LiveData<Resource<String>> putFileInS3(Pair<String, File> stringFilePair) {
        return storyRepository.putFileInS3(stringFilePair);
    }
}
