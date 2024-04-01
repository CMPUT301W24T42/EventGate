package com.example.eventgate.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.eventgate.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

/**
 * an adapter for displaying images in a grid view
 */
public class AdminGridViewAdapter extends BaseAdapter {
    /**
     * holds the context
     */
    Context context;
    /**
     * a list of images urls to be displayed
     */
    ArrayList<String> imageList;

    /**
     * constructs a new AdminGridViewAdapter
     * @param context the context
     * @param imageList the list of images to be displayed
     */
    public AdminGridViewAdapter(Context context, ArrayList<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_grid_item, parent, false);
        }

        ImageView view = convertView.findViewById(R.id.grid_image);

        Picasso.get().load(imageList.get(position)).fit().centerCrop().into(view);

        return convertView;
    }
}
