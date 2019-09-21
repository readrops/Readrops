package com.readrops.app.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.readrops.app.R;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FolderWithFeedCount;
import com.readrops.app.databinding.FolderLayoutBinding;

import java.util.List;

public class FoldersAdapter extends ListAdapter<FolderWithFeedCount, FoldersAdapter.FolderViewHolder> {

    private ManageFoldersListener listener;

    public FoldersAdapter(ManageFoldersListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
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
        FolderLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.folder_layout, parent, false);

        return new FolderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.size() > 0) {
            FolderWithFeedCount folder = (FolderWithFeedCount) payloads.get(0);

            holder.binding.folderName.setText(folder.getFolder().getName());

            int stringRes = folder.getFeedCount() > 1 ? R.string.feeds_number : R.string.feed_number;
            holder.binding.folderFeedsCount.setText(holder.itemView.getContext().getString(stringRes, String.valueOf(folder.getFeedCount())));
        } else
            onBindViewHolder(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderWithFeedCount folder = getItem(position);

        holder.binding.folderName.setText(folder.getFolder().getName());

        int stringRes = folder.getFeedCount() > 1 ? R.string.feeds_number : R.string.feed_number;
        holder.binding.folderFeedsCount.setText(holder.itemView.getContext().getString(stringRes, String.valueOf(folder.getFeedCount())));

        holder.itemView.setOnClickListener(v -> listener.onClick(folder.getFolder()));
    }

    public Folder getFolder(int position) {
        return getItem(position).getFolder();
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
    }
}
