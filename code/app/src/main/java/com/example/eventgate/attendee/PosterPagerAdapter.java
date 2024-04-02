package com.example.eventgate.attendee;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;
/**
 *Adapter for displaying posters in viewPager
 */

public class PosterPagerAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;

    public PosterPagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    /**
     * returns total number of pictures inside adapter
     * @return total number of pictures
     */
    @Override
    public int getCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    /**
     * @param view   Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     * @return
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get().load(imageUrls.get(position)).fit().centerCrop().into(imageView);
        container.addView(imageView);
        return imageView;
    }

    /**
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by
     *                  {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }

    public void removePoster(int position) {
        imageUrls.remove(position);
        notifyDataSetChanged();
    }
}
