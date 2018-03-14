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

package com.MapPost

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.MapPost.databinding.ActivityMainBinding
import com.MapPost.ui.audiorecord.AudioRecordActivity
import com.MapPost.ui.common.LocationLiveData
import com.MapPost.ui.common.dispatchTakePictureIntent
import com.MapPost.vo.Post
import com.MapPost.vo.PostType.*
import com.MapPost.vo.Status
import com.MapPost.vo.User
import com.amazonaws.mobile.auth.core.IdentityManager
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.s3HostName
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener {

    private val tag = this.javaClass.simpleName
    private lateinit var mMap: GoogleMap
    private var playServicesErrorDialog: Dialog? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var location: LatLng
    private var file: File? = null
    private val markers = mutableListOf<Marker>()
    private val markerWidth = 200
    private val markerHeight = 200
    private var cameraOnUserOnce = false
    private val rcAudio = 123
    private val rcPicture = 1234
    private val rcVideo = 12345
    private val visiblePosts = mutableListOf<Post>()
    private val mediaPlayer = MediaPlayer()
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private var linkMode = LinkMode.NOT_LINKING
    private var linkedPosts = mutableListOf<Post>()
    private var polyLines = mutableListOf<Polyline>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        Analytics.init(this)
        if (PermissionManager.checkLocationPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, rcLocation, "Location", "Give permission to access location?")) {
            initLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        val lld = LocationLiveData(this)
        lld.observe(this, Observer {
            if (it != null) {
                location = locationToLatLng(it)
                mMap.isMyLocationEnabled = true
                locationListener()
                userSetup()
                cameraButton()
                textButton()
                myLocationButton()
                nearbyPosts()
                videoButton()
                audioButton()
                bottomSheetClick()
                postAudioButton()
                flagButton()
                deletePostButton()
                linkButton()
                lld.removeObservers(this)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            rcLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation()
                } else {
                    Toast.makeText(this, "Please enable location permissions for this app.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            rcAudio -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audio_button.performClick()
                }
            }
            rcVideo -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    video_button.performClick()
                }
            }
        }
    }

    private fun random(min: Double, max: Double): Double {
        return ThreadLocalRandom.current().nextDouble() * (max - min) + min
    }

    private fun nearbyPosts() {
        mainViewModel.getNewPosts()
        mainViewModel.localPosts.observe(this, Observer {

            if (it?.data == null) return@Observer

            markers.map { it.remove() }
            markers.clear()
            polyLines.map { it.remove() }
            polyLines.clear()
            visiblePosts.clear()
            visiblePosts.addAll(it.data)

            for (post in it.data) {

                val iconGenerator = IconGenerator(this)
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(markerWidth, markerHeight)
                val location = LatLng(post.latitude, post.longitude)
                val markerOptions = MarkerOptions()
                        .draggable(true)
//                        .position(SphericalUtil.computeOffset(location, 20.0, random(0.0, 359.0)))
                        .position(location)
                        .title(post.postId)

                when (post.type) {
                    TEXT -> {
                        imageView.setImageResource(R.drawable.ic_textsms_black_24dp)
                        iconGenerator.setContentView(imageView)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                        markers.add(mMap.addMarker(markerOptions))
                    }
                    AUDIO -> {
                        imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp)
                        iconGenerator.setContentView(imageView)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                        markers.add(mMap.addMarker(markerOptions))
                    }
                    PICTURE ->
                        // Note: This can take a while
                        Glide.with(this)
                                .asBitmap()
                                .load(s3HostName + post.content)
                                .apply(RequestOptions().centerCrop())
                                .into(object : SimpleTarget<Bitmap>(markerWidth, markerHeight) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                                        imageView.setImageBitmap(resource)
                                        iconGenerator.setContentView(imageView)
                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                        markers.add(mMap.addMarker(markerOptions))
                                    }
                                })
                    VIDEO -> {
                        imageView.setImageResource(R.drawable.ic_videocam_black_24dp)
                        iconGenerator.setContentView(imageView)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                        markers.add(mMap.addMarker(markerOptions))
                    }
                }
            }

            // Draw links
            for (post in it.data) {
                if (post.linkedPosts.isNotEmpty()) {
                    for (link in post.linkedPosts) {
                        if (link in markers.map { it.title }) {
                            val polylineOptions = PolylineOptions()
                            polylineOptions.add(LatLng(post.latitude, post.longitude), markers.first { it.title == link }.position)
                            polyLines.add(mMap.addPolyline(polylineOptions))
                        }
                    }
                }
            }
        })
    }

    private fun textButton() {
        text_button.setOnClickListener({
            val post = Post(
                    cognitoId,
                    UUID.randomUUID().toString(),
                    Date().time.toString(),
                    location.latitude,
                    location.longitude,
                    mutableListOf(),
                    TEXT,
                    "nice",
                    mutableListOf()
            )
            mainViewModel.putPost(post).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val user = mainViewModel.currentUser!!
                    if (!user.createdPosts.contains(post.postId)) {
                        user.createdPosts.add(post.postId)
                    }
                    mainViewModel.currentUser = user
                    mainViewModel.putUser(user).observe(this, Observer {
                        if (it != null) {
                            Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                            mainViewModel.getNewPosts()
                        }
                    })
                }
            })
        })
    }

    private fun audioButton() {
        audio_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(this, Manifest.permission.RECORD_AUDIO, rcAudio, "Audio", "Give permission to record audio?")) {
                val intent = Intent(this, AudioRecordActivity::class.java)
                intent.type = "audio/mpeg4-generic"
                startActivityForResult(intent, rcAudio)
            }
        })
    }

    private fun cameraButton() {
        camera_button.setOnClickListener({
            file = dispatchTakePictureIntent(rcPicture, this, file)
        })
    }

    private fun videoButton() {
        video_button.setOnClickListener({
            if (PermissionManager.checkLocationPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, rcVideo, "Storage", "Give permission to access storage?")) {
                val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                if (takeVideoIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takeVideoIntent, rcVideo)
                }
            }
        })
    }

    private fun myLocationButton() {
        my_location_button.setOnClickListener({
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
        })
    }

    private fun linkButton() {
        link_button.setOnClickListener({
            if (linkMode == LinkMode.NOT_LINKING) {
                Toast.makeText(this, "Touch a post then another to make a link.", Toast.LENGTH_SHORT).show()
                linkMode = LinkMode.NONE_PRESSED
                link_button.size = FloatingActionButton.SIZE_MINI
            } else {
                linkMode = LinkMode.NOT_LINKING
                Toast.makeText(this, "Link mode off.", Toast.LENGTH_SHORT).show()
                link_button.size = FloatingActionButton.SIZE_NORMAL
            }
        })
    }

    private fun locationListener() {
        LocationLiveData(this).observe(this, Observer {
            if (it != null) {
                if (!cameraOnUserOnce) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
                    cameraOnUserOnce = true
                }
                location = locationToLatLng(it)
            }
        })
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private fun userSetup() {
        mainViewModel.getUser(cognitoId).observe(this, Observer { userResource ->
            if (userResource != null) {
                when (userResource.status) {
                    Status.ERROR -> createNewUser()
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        mainViewModel.currentUser = userResource.data
                        Analytics.logEvent(Analytics.EventType.UserLogin, tag)
                    }
                }
            }
        })
    }

    private fun createNewUser() {
        val user = User(cognitoId, cognitoUsername, mutableListOf(), "none", mutableListOf())
        mainViewModel.currentUser = user
        mainViewModel.putUser(user).observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, tag)
                    }
                    Status.ERROR -> {
                    }
                    Status.LOADING -> {
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Prevents user from using the app unless they have google play services installed.
     * Not having it will prevent the google map from working.
     */
    private fun checkPlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404)
                    playServicesErrorDialog!!.setCancelable(false)
                }

                if (!playServicesErrorDialog!!.isShowing)
                    playServicesErrorDialog!!.show()
            }
        }
    }

    override fun onResume() {
        if (IdentityManager.getDefaultIdentityManager() == null) {
            val intent = Intent(this, AuthenticatorActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            super.onResume()
            checkPlayServices()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        for (post in visiblePosts) {
            if (post.postId == marker!!.title) {
                binding.post = post

                if (linkMode == LinkMode.NONE_PRESSED) {
                    linkedPosts.add(post)
                    linkMode = LinkMode.ONE_PRESSED
                    Toast.makeText(this, "Touch another to finish the link.", Toast.LENGTH_SHORT).show()
                    return true
                } else if (linkMode == LinkMode.ONE_PRESSED) {
                    if (post == linkedPosts[0]) {
                        Toast.makeText(this, "Cannot link a post to itself!", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    linkedPosts.add(post)
                    val p = linkedPosts[0]
                    if (!p.linkedPosts.contains(linkedPosts[1].postId)) {
                        p.linkedPosts.add(linkedPosts[1].postId)
                    }
                    mainViewModel.putPost(p).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            Toast.makeText(this, "Link created.", Toast.LENGTH_SHORT).show()
                            mainViewModel.getNewPosts()
                        }
                    })
                    link_button.size = FloatingActionButton.SIZE_NORMAL
                    linkMode = LinkMode.NOT_LINKING
                    linkedPosts.clear()
                    return true
                }

                when (post.type) {
                    TEXT -> {
                    }
                    AUDIO -> {
                    }
                    PICTURE -> {
                        Glide.with(this).load(s3HostName + post.content).into(post_image_view)
                    }
                    VIDEO -> {
                        loopVideo(Uri.parse(s3HostName + post.content))
                    }
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
        }
        return false
    }

    private fun postAudioButton() {
        post_audio_button.setOnClickListener({
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, theme))
            } else {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(s3HostName + binding.post!!.content)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener(MediaPlayer::start)
                mediaPlayer.setOnCompletionListener {
                    post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, theme))
                }
                post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, theme))
            }
        })
    }

    private fun flagButton() {
        flag_post_button.setOnClickListener({
            // stuff
        })
    }

    private fun deletePostButton() {
        delete_post_button.setOnClickListener({
            mainViewModel.deletePost(binding.post!!).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val user = mainViewModel.currentUser
                    user!!.createdPosts.remove(binding.post!!.postId)
                    mainViewModel.putUser(user).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            mainViewModel.currentUser = user
                            mainViewModel.getNewPosts()
                            onBackPressed()
                            Toast.makeText(this, "Post deleted.", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            })
        })
    }

    private fun bottomSheetClick() {
        bottom_sheet.setOnClickListener({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onMarkerDragEnd(p0: Marker?) {
    }

    override fun onMarkerDragStart(p0: Marker?) {
    }

    override fun onMarkerDrag(p0: Marker?) {
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        val mUiSettings = mMap.uiSettings
        mUiSettings.isMapToolbarEnabled = false
        mUiSettings.isZoomControlsEnabled = true
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = false
        mUiSettings.isRotateGesturesEnabled = false
        mUiSettings.isCompassEnabled = false
        mUiSettings.isMyLocationButtonEnabled = false
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == rcPicture && resultCode == RESULT_OK) {
            val post = Post()
            post.postId = UUID.randomUUID().toString()
            post.userId = MainActivity.cognitoId
            post.content = file!!.absolutePath
            post.type = PICTURE
            post.latitude = location.latitude
            post.longitude = location.longitude
            post.dateTime = Date().time.toString()
            // Put the file in S3
            mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val newPost = it.data!!
                    // Add the post to DDB
                    mainViewModel.putPost(newPost).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            val user = mainViewModel.currentUser!!
                            if (!user.createdPosts.contains(newPost.content)) {
                                user.createdPosts.add(newPost.content)
                            }
                            // Update the users set of created posts
                            mainViewModel.putUser(user).observe(this, Observer {
                                if (it != null && it.status == Status.SUCCESS) {
                                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                                    mainViewModel.getNewPosts()
                                }
                            })
                        }
                    })
                }
            })
        } else if (requestCode == rcAudio && resultCode == RESULT_OK) {
            val post = Post(
                    cognitoId,
                    getRandomPostId(),
                    getDate(),
                    location.latitude,
                    location.longitude,
                    mutableListOf(),
                    AUDIO,
                    data!!.data.path,
                    mutableListOf()
            )
            mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val newPost = it.data!!
                    // Add the post to DDB
                    mainViewModel.putPost(newPost).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            val user = mainViewModel.currentUser!!
                            if (!user.createdPosts.contains(newPost.content)) {
                                user.createdPosts.add(newPost.content)
                            }
                            // Update the users set of created posts
                            mainViewModel.putUser(user).observe(this, Observer {
                                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                                mainViewModel.getNewPosts()
                            })
                        }
                    })
                }
            })
        } else if (requestCode == rcVideo && resultCode == RESULT_OK) {
            val videoUri = data!!.data
            val videoFile = File(UriUtil.getPath(this, videoUri))
            loopVideo(videoUri)
            val post = Post(
                    cognitoId,
                    getRandomPostId(),
                    getDate(),
                    location.latitude,
                    location.longitude,
                    mutableListOf(),
                    VIDEO,
                    videoFile.absolutePath,
                    mutableListOf()
            )
            mainViewModel.putFile(Pair(post, this)).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    val newPost = it.data!!
                    // Add the post to DDB
                    mainViewModel.putPost(newPost).observe(this, Observer {
                        if (it != null && it.status == Status.SUCCESS) {
                            val user = mainViewModel.currentUser!!
                            if (!user.createdPosts.contains(newPost.content)) {
                                user.createdPosts.add(newPost.content)
                            }
                            // Update the users set of created posts
                            mainViewModel.putUser(user).observe(this, Observer {
                                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                                mainViewModel.getNewPosts()
                            })
                        }
                    })
                }
            })
        }
    }

    private fun loopVideo(uri: Uri) {
        video_view.setVideoURI(uri)
        video_view.setOnPreparedListener({
            it.isLooping = true
            video_view.start()
        })
    }

    private enum class LinkMode {
        // Before the user clicks the link button
        NOT_LINKING,
        // After they click it once, but before they click a post
        NONE_PRESSED,
        // After they click it once, and after they click a post
        ONE_PRESSED
    }

    companion object {

        fun getDate(): String {
            return Date().time.toString()
        }

        fun getRandomPostId(): String {
            return UUID.randomUUID().toString()
        }

        fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }

        const val DEFAULT_ZOOM = 18f
        const val rcLocation = 1
        val cognitoId: String
            get() = IdentityManager.getDefaultIdentityManager().cachedUserID

        val cognitoUsername: String
            get() {
                val cognitoToken = IdentityManager.getDefaultIdentityManager().currentIdentityProvider.token
                val jwt = JWT(cognitoToken)
                val username = jwt.getClaim("cognito:username").asString()
                return username ?: jwt.getClaim("given_name").asString()!!
            }
    }
}