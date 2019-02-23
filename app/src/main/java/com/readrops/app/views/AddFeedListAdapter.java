package com.readrops.app.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.readrops.app.R;
import com.readrops.readropslibrary.ParsingResult;

import java.util.List;

public class AddFeedListAdapter extends RecyclerView.Adapter<AddFeedListAdapter.AddFeedViewHolder> {

    private List<ParsingResult> results;
    private OnItemClickListener listener;

    public AddFeedListAdapter(List<ParsingResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public AddFeedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_feed_item, viewGroup, false);

        return new AddFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFeedViewHolder addFeedViewHolder, int i) {
        addFeedViewHolder.bind(results.get(i));

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ParsingResult parsingResult);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class AddFeedViewHolder extends RecyclerView.ViewHolder {

        private TextView label;
        private TextView url;

        public AddFeedViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener((view) -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION)
                    listener.onItemClick(results.get(position));
            });

            label = itemView.findViewById(R.id.add_feed_item_label);
            url = itemView.findViewById(R.id.add_feed_item_url);
        }

        public void bind(ParsingResult parsingResult) {
            label.setText(parsingResult.getLabel());
            url.setText(parsingResult.getUrl());
        }
    }
}
