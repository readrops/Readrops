package com.readrops.app.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;

public class FoldersFragment extends Fragment {

    public FoldersFragment() {
        // Required empty public constructor
    }

    public static FoldersFragment newInstance(Account account) {
        FoldersFragment fragment = new FoldersFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folders, container, false);
    }

}
