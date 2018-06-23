package com.playposse.landoftherooster.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.glide.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A {@link Fragment} that is shown after the user logs on for the first time. It explains the app
 * to the user.
 */
public class IntroductionSlide1Fragment extends Fragment {

    @BindView(R.id.decorative_icon_image_view) ImageView decorativeIconImageView;

    public IntroductionSlide1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(
                R.layout.fragment_introduction_slide1,
                container,
                false);

        ButterKnife.bind(this, rootView);

        GlideApp.with(this)
                .load(R.drawable.exit)
                .into(decorativeIconImageView);

        return rootView;
    }
}
