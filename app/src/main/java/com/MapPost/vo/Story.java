package com.MapPost.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.MapPost.db.WalkingTaleTypeConverters;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

@Entity(indices = {@Index("id")}, primaryKeys = {"id"})
@TypeConverters(WalkingTaleTypeConverters.class)
@DynamoDBTable(tableName = "walkingtale-mobilehub-466729221-Stories")
public class Story {

    public String userId;
    @NonNull
    public String id;
    @SerializedName("name")
    @NonNull
    public String storyName;
    @SerializedName("description")
    public String description;
    @SerializedName("chapters")
    public List<Chapter> chapters;
    @SerializedName("genre")
    public String genre;
    @SerializedName("tags")
    public List<String> tags;
    @SerializedName("duration")
    public int duration;
    @SerializedName("rating")
    public Double rating;
    @SerializedName("story_image")
    public String story_image;
    @SerializedName("username")
    public String username;

    public Story() {
    }

    public Story(@NonNull String id, @NonNull String storyName, String description, List<Chapter> chapters,
                 String genre, List<String> tags, int duration, Double rating, String story_image, String username, String userId) {
        this.id = id;
        this.storyName = storyName;
        this.description = description;
        this.chapters = chapters;
        this.genre = genre;
        this.tags = tags;
        this.duration = duration;
        this.rating = rating;
        this.story_image = story_image;
        this.username = username;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Story) {
            if (((Story) o).chapters.equals(this.chapters)) {
                if (Objects.equals(((Story) o).id, this.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    @DynamoDBRangeKey(attributeName = "id")
    @DynamoDBAttribute(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }


    @NonNull
    @DynamoDBAttribute(attributeName = "storyName")
    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(@NonNull String storyName) {
        this.storyName = storyName;
    }

    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBAttribute(attributeName = "chapter")
    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    @DynamoDBAttribute(attributeName = "genre")
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @DynamoDBAttribute(attributeName = "tags")
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @DynamoDBAttribute(attributeName = "duration")
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @DynamoDBAttribute(attributeName = "rating")
    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @DynamoDBAttribute(attributeName = "story_image")
    public String getStory_image() {
        return story_image;
    }

    public void setStory_image(String story_image) {
        this.story_image = story_image;
    }

    @DynamoDBAttribute(attributeName = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(final String _userId) {
        this.userId = _userId;
    }
}