package com.example.eventgate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * this is a dialog that confirms whether the user actually wants to delete an item
 */
public class ConfirmDeleteDialog extends DialogFragment {
    /**
     * this is an interface for delete button clicks
     */
    public interface OnDeleteClickListener {
        void onDelete();
    }
    /**
     * this is a listener for the delete button click listener
     */
    private OnDeleteClickListener mListener;

    /**
     * sets up a listener to be notified when the delete button is clicked in the ConfirmDeleteDialog
     * @param listener an instance of OnDeleteClickListener
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mListener = listener;
    }

    /**
     * Called to create the dialog shown in this fragment.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return A new Dialog instance to be displayed by the fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // inflate view
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.confirm_delete_popup, null);

        // find buttons
        Button cancelButton = view.findViewById(R.id.confirm_cancel_button);
        Button deleteButton = view.findViewById(R.id.delete_button);

        // handle button clicks
        cancelButton.setOnClickListener(v -> dismiss());
        deleteButton.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDelete();
            }
            dismiss();
        });

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }

}
