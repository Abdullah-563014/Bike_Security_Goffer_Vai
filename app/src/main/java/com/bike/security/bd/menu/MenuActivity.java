package com.bike.security.bd.menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bike.security.bd.Constants;
import com.bike.security.bd.R;
import com.bike.security.bd.about_us.AboutUsActivity;
import com.bike.security.bd.databinding.ActivityMenuBinding;
import com.bike.security.bd.more_option.MoreOptionActivity;
import com.bike.security.bd.user_manual.UserManualActivity;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMenuBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        initAll();



    }


    private void initAll() {
        binding.moreOptionActivityAboutUsButton.setOnClickListener(this);
        binding.moreOptionActivityUserManualButton.setOnClickListener(this);
        binding.moreOptionActivityFacebookPageButton.setOnClickListener(this);
        binding.moreOptionActivityHelpLineButton.setOnClickListener(this);
    }

    private void openUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(Intent.createChooser(i, "Please select a browser"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to open url for " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.moreOptionActivityAboutUsButton:
                intent=new Intent(MenuActivity.this, AboutUsActivity.class);
                startActivity(intent);
                break;

            case R.id.moreOptionActivityUserManualButton:
                intent=new Intent(MenuActivity.this, UserManualActivity.class);
                startActivity(intent);
                break;

            case R.id.moreOptionActivityFacebookPageButton:
                openUrl("https://www.facebook.com");
                break;

            case R.id.moreOptionActivityHelpLineButton:
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Constants.helpLineNumber));
                startActivity(intent);
                break;
        }
    }


}