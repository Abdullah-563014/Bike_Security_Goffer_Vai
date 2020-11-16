package com.bike.security.bd.about_us;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bike.security.bd.R;
import com.bike.security.bd.databinding.ActivityAboutUsBinding;

public class AboutUsActivity extends AppCompatActivity {

    private ActivityAboutUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        
    }
}