package com.readrops.app.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.fastadapter.items.ModelAbstractItem;
import com.readrops.app.R;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.GlideApp;

import java.util.List;

public class FeedWithFolderItem extends ModelAbstractItem<FeedWithFolder, FeedWithFolderItem, FeedWithFolderItem.ViewHolder> {

    public static final String FEED_NAME_KEY = "name";
    public static final String FOLDER_NAME_KEY = "folder_name";

    private ManageFeedsListener listener;

    public FeedWithFolderItem(FeedWithFolder feedWithFolder) {
        super(feedWithFolder);
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.feed_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.feed_layout;
    }

    @Override
    public long getIdentifier() {
        return getModel().getFeed().getId();
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        if (!payloads.isEmpty()) {
            Bundle bundle = (Bundle)payloads.get(0);

            if (bundle.getString(FEED_NAME_KEY) != null)
                holder.feedName.setText(bundle.getString(FEED_NAME_KEY));

            if (bundle.getString(FOLDER_NAME_KEY) != null)
                holder.folderName.setText(bundle.getString(FOLDER_NAME_KEY));
        } else {
            FeedWithFolder feedWithFolder = getModel();

            if (feedWithFolder.getFeed().getIconUrl() != null) {
                GlideApp.with(holder.itemView.getContext())
                        .load(feedWithFolder.getFeed().getIconUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_rss_feed)
                        .into(holder.feedIcon);
            } else
                holder.feedIcon.setImageResource(R.drawable.ic_rss_feed);

            holder.feedName.setText(feedWithFolder.getFeed().getName());
            if (feedWithFolder.getFeed().getDescription() != null) {
                holder.feedDescription.setVisibility(View.VISIBLE);
                holder.feedDescription.setText(feedWithFolder.getFeed().getDescription());
            } else
                holder.feedDescription.setVisibility(View.GONE);

            if (feedWithFolder.getFolder().getId() != 1)
                holder.folderName.setText(feedWithFolder.getFolder().getName());
            else
                holder.folderName.setText(holder.itemView.getResources().getString(R.string.no_folder));

            holder.editFeed.setOnClickListener(v -> listener.onEdit(feedWithFolder));
            holder.deleteFeed.setOnClickListener(v -> listener.onDelete(feedWithFolder));
        }


    }

    public void setListener(ManageFeedsListener listener) {
        this.listener = listener;
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
