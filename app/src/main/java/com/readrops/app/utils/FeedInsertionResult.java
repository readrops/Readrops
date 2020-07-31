package com.readrops.app.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.readrops.app.R;
import com.readrops.db.entities.Feed;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FeedInsertionResult extends AbstractItem<FeedInsertionResult, FeedInsertionResult.FeedInsertionViewHolder> {

    private Feed feed;

    private ParsingResult parsingResult;

    private FeedInsertionError insertionError;

    public FeedInsertionResult() {
        // empty constructor
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public ParsingResult getParsingResult() {
        return parsingResult;
    }

    public void setParsingResult(ParsingResult parsingResult) {
        this.parsingResult = parsingResult;
    }

    public FeedInsertionError getInsertionError() {
        return insertionError;
    }

    public void setInsertionError(FeedInsertionError insertionError) {
        this.insertionError = insertionError;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @NonNull
    @Override
    public FeedInsertionViewHolder getViewHolder(View v) {
        return new FeedInsertionViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.feed_insertion_result;
    }


    public enum FeedInsertionError {
        ERROR,
        NETWORK_ERROR,
        DB_ERROR,
        PARSE_ERROR,
        FORMAT_ERROR,
        UNKNOWN_ERROR
    }

    class FeedInsertionViewHolder extends FastAdapter.ViewHolder<FeedInsertionResult> {

        private TextView feedInsertionRes;
        private ImageView feedInsertionIcon;

        public FeedInsertionViewHolder(View itemView) {
            super(itemView);

            feedInsertionRes = itemView.findViewById(R.id.feed_insertion_result_text_view);
            feedInsertionIcon = itemView.findViewById(R.id.feed_insertion_result_icon);
        }

        @Override
        public void bindView(FeedInsertionResult item, List<Object> payloads) {
            if (item.getInsertionError() == null) {
                setText(R.string.feed_insertion_successfull, item.parsingResult);
                feedInsertionIcon.setImageResource(R.drawable.ic_check_green);
            } else {
                switch (item.getInsertionError()) {
                    case NETWORK_ERROR:
                        setText(R.string.feed_insertion_network_failed, item.parsingResult);
                        break;
                    case DB_ERROR:
                        break;
                    case PARSE_ERROR:
                        setText(R.string.feed_insertion_parse_failed, item.parsingResult);
                        break;
                    case FORMAT_ERROR:
                        setText(R.string.feed_insertion_wrong_format, item.parsingResult);
                        break;
                    case UNKNOWN_ERROR:
                        setText(R.string.feed_insertion_unknown_error, item.parsingResult);
                        break;
                    case ERROR:
                        setText(R.string.feed_insertion_error, item.parsingResult);
                }

                feedInsertionIcon.setImageResource(R.drawable.ic_warning_red);
            }
        }

        private void setText(@StringRes int stringRes, ParsingResult parsingResult) {
            feedInsertionRes.setText(itemView.getContext().getString(stringRes,
                    parsingResult.getLabel() != null ? parsingResult.getLabel() :
                            parsingResult.getUrl()));
        }

        @Override
        public void unbindView(@NotNull FeedInsertionResult item) {
            // not useful
        }
    }
}
