package com.readrops.app.views;

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
import com.readrops.app.databinding.FolderLayoutBinding;

import java.util.List;

public class FoldersAdapter extends ListAdapter<Folder, FoldersAdapter.FolderViewHolder> {

    private ManageFoldersListener listener;

    public FoldersAdapter(ManageFoldersListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
    }


    private static final DiffUtil.ItemCallback<Folder> DIFF_CALLBACK = new DiffUtil.ItemCallback<Folder>() {
        @Override
        public boolean areItemsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return TextUtils.equals(oldItem.getName(), newItem.getName());
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull Folder oldItem, @NonNull Folder newItem) {
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
            Folder folder = (Folder) payloads.get(0);

            holder.binding.folderName.setText(folder.getName());
        } else
            onBindViewHolder(holder, position);

    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = getItem(position);

        holder.binding.folderName.setText(folder.getName());

        holder.itemView.setOnClickListener(v -> listener.onClick(folder));
    }

    public Folder getFolder(int position) {
        return getItem(position);
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
