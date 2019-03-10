package com.readrops.app.utils;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.readrops.app.R;

import java.util.List;

public class ParsingResult extends AbstractItem<ParsingResult, ParsingResult.ParsingResultViewHolder> {

    private String url;

    private String label;

    private boolean checked;

    public ParsingResult(String url, String label) {
        this.url = url;
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @NonNull
    @Override
    public ParsingResultViewHolder getViewHolder(View v) {
        return new ParsingResultViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.add_feed_main_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.add_feed_item;
    }

    class ParsingResultViewHolder extends FastAdapter.ViewHolder<ParsingResult> {

        private TextView feedLabel;
        private TextView feedUrl;
        private CheckBox checkBox;

        public ParsingResultViewHolder(View itemView) {
            super(itemView);

            feedLabel = itemView.findViewById(R.id.add_feed_item_label);
            feedUrl = itemView.findViewById(R.id.add_feed_item_url);
            checkBox = itemView.findViewById(R.id.add_feed_checkbox);
        }

        @Override
        public void bindView(ParsingResult item, List<Object> payloads) {
            if (item.getLabel() != null)
                feedLabel.setText(item.getLabel());
            else
                feedLabel.setVisibility(View.GONE);

            feedUrl.setText(item.getUrl());

            checkBox.setChecked(item.isChecked());
            checkBox.setClickable(false);
        }

        @Override
        public void unbindView(ParsingResult item) {

        }
    }
}
