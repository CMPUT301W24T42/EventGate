package com.example.eventgate.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for displaying fragments within a viewPager in AdminActivity
 */
public class AdminViewPagerAdapter extends FragmentStateAdapter {
    /**
     * constructs a new AdminViewPagerAdapter
     * @param fragmentActivity the fragment activity that the adapter is used for
     */
    public AdminViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Provide a new EventsFragment or AttendeesFragment associated with the specified position.
     * @param position the specified position
     * @return a new fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new EventsFragment();
            case 1: return new AttendeesFragment();
            default: return new EventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
