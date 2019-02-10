package com.example.android.android_me;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BodyPartFragment extends Fragment {


    
    public BodyPartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_body_part, container, false);

        ImageView img = (ImageView)view.findViewById(R.id.img_body_part);

        img.setImageResource(AndroidImageAssets.getHeads().get(0));
        return view;
    }

}
