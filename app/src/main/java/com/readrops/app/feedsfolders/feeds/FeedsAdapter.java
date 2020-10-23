package com.readrops.app.feedsfolders.feeds;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.readrops.app.R;
import com.readrops.app.databinding.FeedLayoutBinding;
import com.readrops.app.utils.GlideRequests;
import com.readrops.db.pojo.FeedWithFolder;

import org.koin.java.KoinJavaComponent;

import java.util.List;

public class FeedsAdapter extends ListAdapter<FeedWithFolder, FeedsAdapter.FeedViewHolder> {

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

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        FeedLayoutBinding binding = FeedLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);

        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder viewHolder, int i) {
        FeedWithFolder feedWithFolder = getItem(i);

        if (feedWithFolder.getFeed().getIconUrl() != null) {
            KoinJavaComponent.get(GlideRequests.class)
                    .load(feedWithFolder.getFeed().getIconUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_rss_feed_grey)
                    .into(viewHolder.binding.feedLayoutIcon);
        } else
            viewHolder.binding.feedLayoutIcon.setImageResource(R.drawable.ic_rss_feed_grey);

        viewHolder.binding.feedLayoutName.setText(feedWithFolder.getFeed().getName());
        if (feedWithFolder.getFeed().getDescription() != null) {
            viewHolder.binding.feedLayoutDescription.setVisibility(View.VISIBLE);
            viewHolder.binding.feedLayoutDescription.setText(feedWithFolder.getFeed().getDescription());
        } else
            viewHolder.binding.feedLayoutDescription.setVisibility(View.GONE);

        if (feedWithFolder.getFolder() != null)
            viewHolder.binding.feedLayoutFolder.setText(feedWithFolder.getFolder().getName());
        else
            viewHolder.binding.feedLayoutFolder.setText(R.string.no_folder);

        viewHolder.itemView.setOnClickListener(v -> listener.onEdit(feedWithFolder));
        viewHolder.itemView.setOnLongClickListener(v -> {
            listener.onOpenLink(feedWithFolder);
            return true;
        });
    }


    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            FeedWithFolder feedWithFolder = (FeedWithFolder) payloads.get(0);

            holder.binding.feedLayoutName.setText(feedWithFolder.getFeed().getName());

            if (feedWithFolder.getFolder() != null)
                holder.binding.feedLayoutName.setText(feedWithFolder.getFolder().getName());
            else
                holder.binding.feedLayoutName.setText(R.string.no_folder);

        } else
            onBindViewHolder(holder, position);
    }

    public interface ManageFeedsListener {
        void onOpenLink(FeedWithFolder feedWithFolder);

        void onEdit(FeedWithFolder feedWithFolder);
    }


    protected class FeedViewHolder extends RecyclerView.ViewHolder {

        private FeedLayoutBinding binding;

        public FeedViewHolder(FeedLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}
