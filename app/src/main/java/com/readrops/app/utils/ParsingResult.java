package com.readrops.app.utils;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.readrops.app.R;
import com.readrops.db.entities.Feed;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ParsingResult extends AbstractItem<ParsingResult, ParsingResult.ParsingResultViewHolder> {

    private String url;

    private String label;

    private boolean checked;

    private Integer folderId;

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

    public static List<ParsingResult> toParsingResults(List<Feed> feeds) {
        List<ParsingResult> parsingResults = new ArrayList<>();

        for (Feed feed : feeds) {
            ParsingResult parsingResult = new ParsingResult(feed.getUrl(), null);
            parsingResult.setFolderId(feed.getFolderId());
            parsingResults.add(parsingResult);
        }

        return parsingResults;
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

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
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
        public void bindView(@NotNull ParsingResult item, List<Object> payloads) {
            if (!payloads.isEmpty()) {
                ParsingResult newItem = (ParsingResult) payloads.get(0);

                checkBox.setChecked(newItem.isChecked());
            } else {
                if (item.getLabel() != null && !item.getLabel().isEmpty())
                    feedLabel.setText(item.getLabel());
                else
                    feedLabel.setVisibility(View.GONE);

                feedUrl.setText(item.getUrl());

                checkBox.setChecked(item.isChecked());
                checkBox.setClickable(false);
            }


        }

        @Override
        public void unbindView(@NotNull ParsingResult item) {
            // not useful
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        else if (!(o instanceof ParsingResult))
            return false;
        else {
            ParsingResult parsingResult = (ParsingResult) o;

            return parsingResult.getUrl().equals(this.getUrl());
        }
    }
}
