package com.readrops.app.feedsfolders.folders;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.readrops.app.R;
import com.readrops.app.databinding.FolderLayoutBinding;
import com.readrops.db.entities.Folder;
import com.readrops.db.pojo.FolderWithFeedCount;

import java.util.List;

public class FoldersAdapter extends ListAdapter<FolderWithFeedCount, FoldersAdapter.FolderViewHolder> {

    private ManageFoldersListener listener;
    private int totalFeedCount;

    public FoldersAdapter(ManageFoldersListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
    }

    public void setTotalFeedCount(int totalFeedCount) {
        this.totalFeedCount = totalFeedCount;
    }

    private static final DiffUtil.ItemCallback<FolderWithFeedCount> DIFF_CALLBACK = new DiffUtil.ItemCallback<FolderWithFeedCount>() {
        @Override
        public boolean areItemsTheSame(@NonNull FolderWithFeedCount oldItem, @NonNull FolderWithFeedCount newItem) {
            return oldItem.getFolder().getId() == newItem.getFolder().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FolderWithFeedCount oldItem, @NonNull FolderWithFeedCount newItem) {
            return TextUtils.equals(oldItem.getFolder().getName(), newItem.getFolder().getName()) &&
                    oldItem.getFeedCount() == newItem.getFeedCount();
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull FolderWithFeedCount oldItem, @NonNull FolderWithFeedCount newItem) {
            return newItem;
        }
    };

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FolderLayoutBinding binding = FolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);

        return new FolderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            FolderWithFeedCount folderWithFeedCount = (FolderWithFeedCount) payloads.get(0);

            holder.bind(folderWithFeedCount);
        } else
            onBindViewHolder(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderWithFeedCount folderWithFeedCount = getItem(position);

        holder.bind(folderWithFeedCount);
        holder.itemView.setOnClickListener(v -> listener.onClick(folderWithFeedCount.getFolder()));
    }

    public interface ManageFoldersListener {
        void onClick(Folder folder);
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {

        private FolderLayoutBinding binding;

        public FolderViewHolder(FolderLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        private void bind(FolderWithFeedCount folderWithFeedCount) {
            binding.folderName.setText(folderWithFeedCount.getFolder().getName());

            int stringRes = folderWithFeedCount.getFeedCount() > 1 ? R.string.feeds_number : R.string.feed_number;
            binding.folderFeedsCount.setText(itemView.getContext().getString(stringRes, String.valueOf(folderWithFeedCount.getFeedCount())));

            binding.folderProgressBar.setMax(totalFeedCount);
            binding.folderProgressBar.setProgress(folderWithFeedCount.getFeedCount());
        }
    }
}
