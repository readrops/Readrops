package com.readrops.app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.items.ModelAbstractItem;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.GlideApp;

import java.util.List;

public class FeedWithFolderItem extends ModelAbstractItem<FeedWithFolder, FeedWithFolderItem, FeedWithFolderItem.ViewHolder> {

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
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        FeedWithFolder feedWithFolder = getModel();

        if (feedWithFolder.getFeed().getIconUrl() != null) {
            GlideApp.with(holder.itemView.getContext())
                    .load(feedWithFolder.getFeed().getIconUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_rss_feed)
                    .into(holder.feedIcon);
        }

        holder.feedName.setText(feedWithFolder.getFeed().getName());
        if (feedWithFolder.getFeed().getDescription() != null) {
            holder.feedDescription.setVisibility(View.VISIBLE);
            holder.feedDescription.setText(feedWithFolder.getFeed().getDescription());
        } else
            holder.feedDescription.setVisibility(View.GONE);

        holder.folderName.setText(feedWithFolder.getFolder().getName());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView feedIcon;
        private TextView feedName;
        private TextView feedDescription;
        private TextView folderName;


        public ViewHolder(View itemView) {
            super(itemView);

            feedIcon = itemView.findViewById(R.id.feed_layout_icon);
            feedName = itemView.findViewById(R.id.feed_layout_name);
            feedDescription = itemView.findViewById(R.id.feed_layout_description);
            folderName = itemView.findViewById(R.id.feed_layout_folder);
        }
    }
}
