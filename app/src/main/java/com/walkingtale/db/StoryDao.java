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

package com.walkingtale.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.walkingtale.vo.RepoSearchResult;
import com.walkingtale.vo.Story;

import java.util.List;

/**
 * Interface for database access on Story related operations.
 */
@Dao
public abstract class StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Story... stories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertStories(List<Story> repositories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long createStoryIfNotExists(Story story);

    @Query("SELECT * FROM Story WHERE id = :id")
    public abstract LiveData<Story> load(String id);

    @Query("SELECT * FROM Story "
            + "WHERE id = :id")
    public abstract LiveData<List<Story>> loadRepositories(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(RepoSearchResult result);

    @Query("SELECT * FROM RepoSearchResult WHERE query = :query")
    public abstract LiveData<RepoSearchResult> search(String query);

    @Query("SELECT * FROM Story WHERE id in (:repoIds)")
    protected abstract LiveData<List<Story>> loadById(List<Integer> repoIds);

    @Query("SELECT * FROM RepoSearchResult WHERE query = :query")
    public abstract RepoSearchResult findSearchResult(String query);

    @Query("SELECT * FROM Story")
    public abstract LiveData<List<Story>> loadAll();
}