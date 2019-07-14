package com.readrops.app.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.readrops.app.R;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.GlideApp;

import java.util.List;

public class FeedsAdapter extends ListAdapter<FeedWithFolder, FeedsAdapter.ViewHolder> {

    private ManageFeedsListener listener;

    private static final DiffUtil.ItemCallback<FeedWithFolder> DIFF_CALLBACK = new DiffUtil.ItemCallback<FeedWithFolder>() {
        @Override
        public boolean areItemsTheSame(@NonNull FeedWithFolder feedWithFolder, @NonNull FeedWithFolder t1) {
            return feedWithFolder.getFeed().getId() == t1.getFeed().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeedWithFolder feedWithFolder, @NonNull FeedWithFolder t1) {
            boolean folder = false;
            if (feedWithFolder.getFolder() != null && t1.getFolder() != null)
                folder = feedWithFolder.getFolder().getName().equals(t1.getFolder().getName());

            return feedWithFolder.getFeed().getName().equals(t1.getFeed().getName())
                    && folder;
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull FeedWithFolder oldItem, @NonNull FeedWithFolder newItem) {
            return newItem;
        }
    };

    public FeedsAdapter(ManageFeedsListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
    }

    public FeedWithFolder getItemAt(int position) {
        return getItem(position);
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

        if (feedWithFolder.getFolder() != null)
            viewHolder.folderName.setText(feedWithFolder.getFolder().getName());
        else
            viewHolder.folderName.setText(R.string.no_folder);

        viewHolder.itemView.setOnClickListener(v -> listener.onEdit(getItem(i)));
        viewHolder.itemView.setOnLongClickListener(v -> {
            listener.onOpenLink(getItem(i));
            return true;
        });
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            FeedWithFolder feedWithFolder = (FeedWithFolder) payloads.get(0);

            holder.feedName.setText(feedWithFolder.getFeed().getName());

            if (feedWithFolder.getFolder() != null)
                holder.folderName.setText(feedWithFolder.getFolder().getName());
            else
                holder.folderName.setText(R.string.no_folder);

        } else
            onBindViewHolder(holder, position);
    }

    public interface ManageFeedsListener {
        void onOpenLink(FeedWithFolder feedWithFolder);
        void onEdit(FeedWithFolder feedWithFolder);
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {

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
