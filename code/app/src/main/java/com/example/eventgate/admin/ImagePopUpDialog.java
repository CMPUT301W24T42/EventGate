package com.example.eventgate.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;
import com.squareup.picasso.Picasso;

public class ImagePopUpDialog extends DialogFragment {
    // Define a listener interface
    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    private OnImageDeleteListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // inflate view
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.image_popup, null);

        // get layout references
        ImageView image = view.findViewById(R.id.popup_image_view);
        Button deleteButton = view.findViewById(R.id.image_delete_button);

        Bundle args = getArguments();
        String imageUrl;
        if (args != null) {
            // get image info and load it into the imageview
            imageUrl = args.getString("imageUrl");
            Picasso.get().load(imageUrl).resize(1000, 1000).centerInside().into(image);
            // give time for image to load in before showing the dialog fragment
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        deleteButton.setOnClickListener(v -> {
            // Notify the listener that delete button is clicked
            mListener.onImageDelete(getArguments().getInt("position"));
            dismiss();
        });

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }

    /**
     * setter method for OnImageDeleteListener
     * @param listener the listener to be set
     */
    public void setOnImageDeleteListener(OnImageDeleteListener listener) {
        mListener = listener;
    }
}
