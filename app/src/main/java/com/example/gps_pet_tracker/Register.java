package com.example.gps_pet_tracker;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Register#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Register extends Fragment {
    Button btn_backPage; // Corrected button name
    Slider meter_value;
    TextView meter_val;
    AutoCompleteTextView category_pet;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public Register() {
        // Required empty public constructor
    }

    public static Register newInstance(String param1, String param2) {
        Register fragment = new Register();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize views
        category_pet = view.findViewById(R.id.category);
        meter_val = view.findViewById(R.id.meter_val);
        meter_value = view.findViewById(R.id.meter_value);
        btn_backPage = view.findViewById(R.id.btn_backPage  ); // Corrected button reference

        // Set the custom thumb drawable programmatically
        Drawable thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.custom_slider_thumb);
        if (thumbDrawable != null) {
            // Set the custom thumb tint programmatically
            meter_value.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttn)));
        }

        // Listener for notification button click
        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // Listener for slider value changes
        meter_value.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                meter_val.setText(String.valueOf((int) value));
            }
        });

        return view;
    }
}
