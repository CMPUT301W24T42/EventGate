package com.example.eventgate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// citations
//      Notification bell icon made by Freepik - Flaticon from https://www.flaticon.com/free-icons/notification

/**
 * a fragment for requesting notification permission
 */
public class NotificationPermissionDialog extends DialogFragment {
    /**
     * this is the launcher that requests permission to receive notifications
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // display a message confirming that user has chosen not to receive notifications
                    Toast.makeText(requireContext(), "EventGate will not send notifications", Toast.LENGTH_SHORT).show();
                }
            });

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.notification_permission_popup, null);

        Button noThanksButton = view.findViewById(R.id.no_thanks_button);
        Button okButton = view.findViewById(R.id.ok_button);
        CheckBox doNotAskCheckbox = view.findViewById(R.id.permission_checkbox);

        // dismiss dialog if user clicks no thanks button
        noThanksButton.setOnClickListener(v -> {
            boolean doNotAsk = doNotAskCheckbox.isChecked();
            checkDoNotAsk(doNotAsk);
            dismiss();
        });

        // request permission for notification if the user agrees to receiving them
        okButton.setOnClickListener(v -> {
            boolean doNotAsk = doNotAskCheckbox.isChecked();
            checkDoNotAsk(doNotAsk);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                dismiss();
            }
        });

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view); // Set the custom layout
        return builder.create();
    }

    /**
     * this edits the user's sharedpreferences so that the app will know not to ask the user for
     *      notification permissions
     * @param doNotAsk true if the user has checked the checkbox confirming that they no longer
     *                 want to be asked for notification permissions, false otherwise
     */
    private void checkDoNotAsk(boolean doNotAsk) {
        if (doNotAsk) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dont_ask_again", true);
            editor.apply();
        }
    }
}
