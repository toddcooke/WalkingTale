package com.talkingwhale.ui;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.talkingwhale.R;
import com.talkingwhale.pojos.Post;
import com.talkingwhale.pojos.PostType;

import static com.talkingwhale.pojos.PostKt.getDrawableForPost;

public class MapPostBindingAdapters {

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, Post post) {
        if (post == null) return;
        if (post.getType() == PostType.PICTURE || post.getType() == PostType.VIDEO) {
            Context context = imageView.getContext();
            Glide.with(context).load(context.getResources().getString(R.string.s3_hostname) + post.getContent()).into(imageView);
        }
    }

    @BindingAdapter("imageType")
    public static void setImageIcon(ImageView imageView, Post post) {
        imageView.setImageResource(getDrawableForPost(post));
    }
}