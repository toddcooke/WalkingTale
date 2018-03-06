package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.Story

class GetOneStoryTask(val storyKey: StoryKey, val db: WalkingTaleDb) : AbstractTask<StoryKey, Story>(storyKey, db) {

    override fun run() {
        val response = dynamoDBMapper.load(Story::class.java, storyKey.userId, storyKey.storyId)
        if (response != null) {
            result.postValue(Resource(Status.SUCCESS, response, null))
            db.beginTransaction()
            db.storyDao().insert(response)
            db.endTransaction()
        } else {
            result.postValue(Resource(Status.ERROR, null, null))
        }
    }
}

data class StoryKey(val userId: String, val storyId: String)