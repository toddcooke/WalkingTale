/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.github.ui.common;

import com.android.example.github.MainActivity;
import com.android.example.github.R;
import com.android.example.github.ui.repo.RepoFragment;
import com.android.example.github.ui.search.SearchFragment;
import com.android.example.github.ui.storycreate.StoryCreateFragment;
import com.android.example.github.ui.storyfeed.StoryFeedFragment;
import com.android.example.github.ui.storyreader.StoryPlayFragment;
import com.android.example.github.ui.user.UserFragment;

import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

/**
 * A utility class that handles navigation in {@link MainActivity}.
 */
public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void navigateToSearch() {
        SearchFragment searchFragment = new SearchFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, searchFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToRepo(String owner, String name) {
        RepoFragment fragment = RepoFragment.create(owner, name);
        String tag = "repo" + "/" + owner + "/" + name;
        fragmentManager.beginTransaction()
                .replace(containerId, fragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToUser(String login) {
        String tag = "user" + "/" + login;
        UserFragment userFragment = UserFragment.create(login);
        fragmentManager.beginTransaction()
                .replace(containerId, userFragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToCreateStory() {
        StoryCreateFragment storyCreateFragment = new StoryCreateFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, storyCreateFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void navigateToStoryFeed() {
        StoryFeedFragment storyFeedFragment = new StoryFeedFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, storyFeedFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToStoryPlay(String owner, String name) {
        String tag = "repo" + "/" + owner + "/" + name;
        StoryPlayFragment storyPlayFragment = StoryPlayFragment.create(owner, name);
        fragmentManager.beginTransaction()
                .replace(containerId, storyPlayFragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
