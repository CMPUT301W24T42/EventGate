package com.example.eventgate.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventgate.R;

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

        String title = getArguments().getString("title", "");
        String message = getArguments().getString("message", "");

        // find textviews and buttons
        TextView titleTextView = view.findViewById(R.id.confirm_title);
        TextView messageTextView = view.findViewById(R.id.confirm_message);
        Button cancelButton = view.findViewById(R.id.confirm_cancel_button);
        Button deleteButton = view.findViewById(R.id.delete_button);

        titleTextView.setText(title);
        messageTextView.setText(message);

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

    /**
     * creates a new instance of ConfirmDeleteDialog with a custom title and message
     * @param title the title of the dialog
     * @param message the message of the dialog
     * @return an instance of ConfirmDeleteDialog
     */
    public static ConfirmDeleteDialog newInstance(String title, String message) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        dialog.setArguments(args);
        return dialog;
    }

}
