package com.example.flashgig.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.flashgig.R;
import com.example.flashgig.databinding.ActivityMainBinding;
import com.example.flashgig.fragments.HomeFragment;
import com.example.flashgig.fragments.MyJobsFragment;
import com.example.flashgig.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        replaceFragment(new HomeFragment(), "home", "LtoR");


        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            assert curFragment != null;
            String curFragmentTag = curFragment.getTag();
            assert curFragmentTag != null : "Set a fragment tag!";

            switch (item.getItemId()) {
                case R.id.home:
                    if (!curFragmentTag.equals("home")) {
                        replaceFragment(new HomeFragment(), "home", "LtoR");
                    }
                    break;
                case R.id.messages:
                    if (!curFragmentTag.equals("messages")) {
                        if (!curFragmentTag.equals("home")) {
                            startActivity(new Intent(this, InboxChat.class));
//                            replaceFragment(new MessagesFragment(), "messages", "LtoR");
                        } else {
                            startActivity(new Intent(this, InboxChat.class));
//                            replaceFragment(new MessagesFragment(), "messages", "RtoL");
                        }
                    }
                    break;
                case R.id.myJobs:
                    if (!curFragmentTag.equals("myJobs")) {
                        if (!curFragmentTag.equals("profile")) {
                            replaceFragment(new MyJobsFragment(), "myJobs", "RtoL");
                        } else {
                            replaceFragment(new MyJobsFragment(), "myJobs", "LtoR");
                        }
                    }
                    break;
                case R.id.profile:
                    if (!curFragmentTag.equals("profile")) {
                        replaceFragment(new ProfileFragment(), "profile", "RtoL");
                    }
                    break;
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if(!(curFragment instanceof HomeFragment)){
            super.onBackPressed();
            return;
        }

        SearchView searchBar = findViewById(R.id.searchviewHome);
        if(searchBar != null) {
            if (!searchBar.isIconified()) {
                searchBar.setIconified(true);
                searchBar.onActionViewCollapsed();
                return;
            }
        }
        this.moveTaskToBack(true);
    }

    private void replaceFragment(Fragment fragment, String tag, String animDirection) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (animDirection.equals("LtoR")) {
            fragmentTransaction
                    .setCustomAnimations(
                            R.anim.slide_in_left, //enter
                            R.anim.slide_out_right, //exit
                            R.anim.slide_in_right, //pop enter
                            R.anim.slide_out_left //pop exit
                    )
                    .replace(R.id.frameLayout, fragment, tag)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentTransaction
                    .setCustomAnimations(R.anim.slide_in_right, //enter
                            R.anim.slide_out_left, //exit
                            R.anim.slide_in_left, //pop enter
                            R.anim.slide_out_right //pop exit
                    )
                    .replace(R.id.frameLayout, fragment, tag)
                    .addToBackStack(null)
                    .commit();
        }
    }
}