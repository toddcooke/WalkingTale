package com.walkingtale.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story

class GetFeedStoriesTask(val nothing: String, val db: WalkingTaleDb) : AbstractTask<String, MutableList<Story>>(nothing, db) {

    override fun run() {
        val response = dynamoDBMapper.scan(Story::class.java, DynamoDBScanExpression())
        db.beginTransaction()
        response.forEach {
            db.storyDao().insert(it)
        }
        db.endTransaction()
        result.postValue(Resource(Status.SUCCESS, response, null))
    }
}