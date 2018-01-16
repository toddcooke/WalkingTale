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

package com.android.example.github;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.android.example.github.api.GithubService;
import com.android.example.github.api.RepoSearchResponse;
import com.android.example.github.util.LiveDataCallAdapterFactory;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.Chapter;
import com.android.example.github.walkingTale.ExampleRepo;
import com.android.example.github.walkingTale.Exposition;
import com.android.example.github.walkingTale.ExpositionType;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AwsTest {

    private Context context = InstrumentationRegistry.getTargetContext();
    private String TAG = this.getClass().getSimpleName();
    private String accessToken;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://godtbigaai.execute-api.us-east-1.amazonaws.com/dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(new LiveDataCallAdapterFactory())
            .build();
    private GithubService githubService = retrofit.create(GithubService.class);
    private CognitoUserPool userPool = new CognitoUserPool(context,
            "us-east-1_5Jew8wIVQ",
            "2aj12760enoi6kvs3bun08dsfg",
            "1tr05oa80sre89di3an5je81k5o78jq6thblkmcgrgo0mljb3li2");
    private AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            accessToken = userSession.getIdToken().getJWTToken();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, "Passw0rd!", null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
        }

        @Override
        public void onFailure(Exception exception) {
        }
    };

    @Before
    public void init() {
        // Get cognito accessToken
        userPool.getUser("todd").getSession(authenticationHandler);
        assertNotNull(accessToken);
    }

    @Test
    public void testListStories() throws IOException {
        // Make GET request to list endpoint
        Response<RepoSearchResponse> response = githubService.getAllRepos(accessToken).execute();
        assertEquals(200, response.code());
        assertNotNull(response.body().getItems());
    }

    @Test
    public void testPutStory() throws IOException {
        Repo repo = ExampleRepo.Companion.getRandomRepo();
        repo.name = "name";
        repo.description = "desc";
        repo.genre = "genre";
        repo.tags = "tags";
        repo.story_image = "image";
        repo.username = "user";
        repo.chapters = new ArrayList<>();

        Exposition exposition = new Exposition(ExpositionType.TEXT, "content", 0);
        Chapter chapter = new Chapter(new ArrayList<>(), "chapter name", new LatLng(1.1, 1.1), 0, 5);
        ArrayList<Exposition> expositions = new ArrayList<>();
        expositions.add(exposition);
        chapter.setExpositions(expositions);
        repo.chapters.add(chapter);

        Response<Repo> response = githubService.putStory(accessToken, repo).execute();
        Log.i(TAG, "message " + response.message());
        Log.i(TAG, "body " + response.body());
        Log.i(TAG, "headers " + response.headers());
        Log.i(TAG, "error body " + response.errorBody());
        Log.i(TAG, "repo " + repo);

        assertEquals(200, response.code());
    }

    @Test
    public void testS3Upload() throws IOException {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File file = File.createTempFile("prefix", "extension", outputDir);
    }
}