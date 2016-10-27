package com.example.savagavran.criminalintent;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;

import static android.R.attr.bitmap;

public class PhotoFragment extends DialogFragment {


    private Bitmap mBitmap;
    private File mPhotoFile;
    private ImageView mPhotoView;
    private static final String ARG_PICTURE = "picture";

    static PhotoFragment newInstance(File pic) {
        PhotoFragment f = new PhotoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PICTURE, pic);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FULLSCREEN_DIALOG);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.photo_fragment, null);

        mPhotoView = (ImageView) v.findViewById(R.id.photo_display);
        mPhotoFile = (File) getArguments().getSerializable(ARG_PICTURE);
        mBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
        mBitmap = PictureUtils.rotateBitmap(mBitmap, 90);
        mPhotoView.setImageBitmap(mBitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }
}
