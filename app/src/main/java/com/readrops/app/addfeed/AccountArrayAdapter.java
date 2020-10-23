package com.readrops.app.addfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.R;
import com.readrops.db.entities.account.Account;

import java.util.List;

public class AccountArrayAdapter extends ArrayAdapter<Account> {

    public AccountArrayAdapter(@NonNull Context context, @NonNull List<Account> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_type_item, parent, false);
        }

        Account account = getItem(position);

        ImageView accountIcon = convertView.findViewById(R.id.account_type_logo);
        TextView accountName = convertView.findViewById(R.id.account_type_name);

        accountIcon.setImageResource(account.getAccountType().getIconRes());
        accountName.setText(account.getAccountType().getName());

        return convertView;
    }
}
