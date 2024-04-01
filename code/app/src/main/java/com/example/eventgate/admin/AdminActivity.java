package com.example.eventgate.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.Button;

import com.example.eventgate.R;
import com.google.android.material.tabs.TabLayout;

// citations
// https://www.youtube.com/watch?v=LXl7D57fgOQ Elements of the following layout design,
//      (activity_admin) is from Codes Easy, Youtube, "How to Implement TabLayout in your Android App", 2023-01-23
/**
 * This is the activity for the administrator main menu.
 * It allows the administrator to view and delete any events or users
 */
public class AdminActivity extends AppCompatActivity {
    /**
     * holds the tabLayout
     */
    TabLayout tabLayout;
    /**
     * holds the viewpager used to swipe between tabs
     */
    ViewPager2 viewPager2;
    /**
     * the adapter for the viewpager
     */
    AdminViewPagerAdapter viewPager2Adapter;

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // get layout references
        tabLayout = findViewById(R.id.admin_tab_layout);
        viewPager2 = findViewById(R.id.admin_viewpager);
        // create and set adapter
        viewPager2Adapter = new AdminViewPagerAdapter(this);
        viewPager2.setAdapter(viewPager2Adapter);
        // set up the tabLayout
        setUpTabLayout();

        // sends admin back to the main menu
        Button adminActivityBackButton = findViewById(R.id.admin_back_button);
        adminActivityBackButton.setOnClickListener(v -> finish());
    }

    /**
     * this handles the logic for selecting tabs in the tabLayout
     */
    private void setUpTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }
}