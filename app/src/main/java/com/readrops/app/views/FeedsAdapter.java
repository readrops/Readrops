package com.readrops.app.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.readrops.app.R;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.GlideApp;

import java.util.List;

public class FeedsAdapter extends ListAdapter<FeedWithFolder, FeedsAdapter.ViewHolder> {

    public static final String FEED_NAME_KEY = "name";
    public static final String FOLDER_NAME_KEY = "folderName";

    private ManageFeedsListener listener;

    private static final DiffUtil.ItemCallback<FeedWithFolder> DIFF_CALLBACK = new DiffUtil.ItemCallback<FeedWithFolder>() {
        @Override
        public boolean areItemsTheSame(@NonNull FeedWithFolder feedWithFolder, @NonNull FeedWithFolder t1) {
            Log.d("", "areItemsTheSame: ");
            return feedWithFolder.getFeed().getId() == t1.getFeed().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeedWithFolder feedWithFolder, @NonNull FeedWithFolder t1) {
            return feedWithFolder.getFeed().getName().equals(t1.getFeed().getName())
                    && feedWithFolder.getFolder().getName().equals(t1.getFolder().getName());
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull FeedWithFolder oldItem, @NonNull FeedWithFolder newItem) {
            Bundle bundle = new Bundle();

            if (!oldItem.getFeed().getName().equals(newItem.getFeed().getName()))
                bundle.putString(FeedsAdapter.FEED_NAME_KEY, newItem.getFeed().getName());

            if (!oldItem.getFolder().getName().equals(newItem.getFolder().getName()))
                bundle.putString(FeedsAdapter.FOLDER_NAME_KEY, newItem.getFolder().getName());

            if (bundle.size() > 0)
                return bundle;
            else
                return null;

        }
    };

    public FeedsAdapter(ManageFeedsListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        FeedWithFolder feedWithFolder = getItem(i);

        if (feedWithFolder.getFeed().getIconUrl() != null) {
            GlideApp.with(viewHolder.itemView.getContext())
                    .load(feedWithFolder.getFeed().getIconUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_rss_feed)
                    .into(viewHolder.feedIcon);
        } else
            viewHolder.feedIcon.setImageResource(R.drawable.ic_rss_feed);

        viewHolder.feedName.setText(feedWithFolder.getFeed().getName());
        if (feedWithFolder.getFeed().getDescription() != null) {
            viewHolder.feedDescription.setVisibility(View.VISIBLE);
            viewHolder.feedDescription.setText(feedWithFolder.getFeed().getDescription());
        } else
            viewHolder.feedDescription.setVisibility(View.GONE);

        if (feedWithFolder.getFolder().getId() != 1)
            viewHolder.folderName.setText(feedWithFolder.getFolder().getName());
        else
            viewHolder.folderName.setText(viewHolder.itemView.getResources().getString(R.string.no_folder));

        viewHolder.editFeed.setOnClickListener(v -> listener.onEdit(getItem(i)));
        viewHolder.deleteFeed.setOnClickListener(v -> listener.onDelete(getItem(i)));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Bundle bundle = (Bundle) payloads.get(0);

            if (bundle.getString(FEED_NAME_KEY) != null) {
                holder.feedName.setText(bundle.getString(FEED_NAME_KEY));
            }

            if (bundle.getString(FOLDER_NAME_KEY) != null) {
                holder.folderName.setText(bundle.getString(FOLDER_NAME_KEY));
            }
        } else
            onBindViewHolder(holder, position);
    }

    public interface ManageFeedsListener {
        void onEdit(FeedWithFolder feedWithFolder);
        void onDelete(FeedWithFolder feedWithFolder);
    }



    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView feedIcon;
        private TextView feedName;
        private TextView feedDescription;
        private TextView folderName;

        private ImageView editFeed;
        private ImageView deleteFeed;

        public ViewHolder(View itemView) {
            super(itemView);

            feedIcon = itemView.findViewById(R.id.feed_layout_icon);
            feedName = itemView.findViewById(R.id.feed_layout_name);
            feedDescription = itemView.findViewById(R.id.feed_layout_description);
            folderName = itemView.findViewById(R.id.feed_layout_folder);
            editFeed = itemView.findViewById(R.id.feed_layout_edit);
            deleteFeed = itemView.findViewById(R.id.feed_layout_delete);
        }
    }
}