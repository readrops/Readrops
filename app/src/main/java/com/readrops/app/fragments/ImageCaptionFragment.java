package com.readrops.app.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.readrops.app.R;

public class ImageCaptionFragment extends DialogFragment {

    public static ImageCaptionFragment newInstance(CharSequence title, CharSequence message) {
        ImageCaptionFragment f = new ImageCaptionFragment();

        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putCharSequence("message", message);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        CharSequence title = getArguments().getCharSequence("title");
        CharSequence message = getArguments().getCharSequence("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        return builder.create();
    }
}
