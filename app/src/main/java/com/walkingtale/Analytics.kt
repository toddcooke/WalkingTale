package com.walkingtale

import android.content.Context
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    private lateinit var instance: Analytics
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    fun init(context: Context) {
        instance = Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(eventType: EventType, TAG: String, message: String) {
        val bundle = Bundle()
        bundle.putString(TAG, message)
        bundle.putString("cognito user id", MainActivity.getCognitoId())
        mFirebaseAnalytics.logEvent(eventType.toString(), bundle)
    }

    fun logEvent(eventType: EventType, TAG: String) {
        logEvent(eventType, TAG, "")
    }

    enum class EventType {
        CreatedUser,
        UserLogin,
        PlayedStory,
        CreatedStory
    }
}
