package com.readrops.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.readrops.app.R;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.databinding.ListItemBinding;
import com.readrops.app.utils.DateUtils;
import com.readrops.app.utils.GlideRequests;
import com.readrops.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class MainItemListAdapter extends PagedListAdapter<ItemWithFeed, MainItemListAdapter.ItemViewHolder> implements ListPreloader.PreloadModelProvider<String> {

    private GlideRequests glideRequests;
    private OnItemClickListener listener;
    private ViewPreloadSizeProvider preloadSizeProvider;

    private LinkedHashSet<Integer> selection;

    public MainItemListAdapter(GlideRequests glideRequests, ViewPreloadSizeProvider preloadSizeProvider) {
        super(DIFF_CALLBACK);

        this.glideRequests = glideRequests;
        this.preloadSizeProvider = preloadSizeProvider;
        selection = new LinkedHashSet<>();
    }

    private static final DiffUtil.ItemCallback<ItemWithFeed> DIFF_CALLBACK = new DiffUtil.ItemCallback<ItemWithFeed>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemWithFeed item, @NonNull ItemWithFeed t1) {
            return item.getItem().getId() == t1.getItem().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ItemWithFeed itemWithFeed, @NonNull ItemWithFeed t1) {
            Item item = itemWithFeed.getItem();
            Item item1 = t1.getItem();

            boolean folder = false;
            if (itemWithFeed.getFolder() != null && t1.getFolder() != null)
                folder = itemWithFeed.getFolder().getName().equals(t1.getFolder().getName());

            return item.getTitle().equals(item1.getTitle()) &&
                    itemWithFeed.getFeedName().equals(t1.getFeedName()) &&
                    folder &&
                    item.isRead() == item1.isRead() &&
                    item.isReadItLater() == item1.isReadItLater() &&
                    itemWithFeed.getColor() == t1.getColor() &&
                    itemWithFeed.getBgColor() == t1.getBgColor();
        }

        @Override
        public Object getChangePayload(@NonNull ItemWithFeed oldItem, @NonNull ItemWithFeed newItem) {
            return newItem;
        }
    };

    private static final DrawableCrossFadeFactory FADE_FACTORY = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private static final RequestOptions REQUEST_OPTIONS = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(16));

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()));

        ItemViewHolder viewHolder = new ItemViewHolder(binding);
        preloadSizeProvider.setView(binding.itemImage);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.size() > 0) {
            ItemWithFeed itemWithFeed = (ItemWithFeed) payloads.get(0);

            holder.bind(itemWithFeed);
            holder.applyColors(itemWithFeed);

            if (itemWithFeed.getFolder() != null)
                holder.binding.itemFolderName.setText(itemWithFeed.getFolder().getName());
            else
                holder.binding.itemFolderName.setText(R.string.no_folder);

            holder.setReadState(itemWithFeed.getItem().isRead());
            holder.setSelected(selection.contains(position));
        } else
            onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int i) {
        ItemWithFeed itemWithFeed = getItem(i);
        if (itemWithFeed == null)
            return;

        viewHolder.bind(itemWithFeed);
        viewHolder.setImages(itemWithFeed);
        viewHolder.applyColors(itemWithFeed);

        int minutes = (int) Math.round(itemWithFeed.getItem().getReadTime());
        if (minutes < 1)
            viewHolder.binding.itemReadtime.setText(R.string.read_time_lower_than_1);
        else if (minutes > 1)
            viewHolder.binding.itemReadtime.setText(viewHolder.itemView.getContext().
                    getString(R.string.read_time, String.valueOf(minutes)));
        else
            viewHolder.binding.itemReadtime.setText(R.string.read_time_one_minute);

        if (itemWithFeed.getFolder() != null)
            viewHolder.binding.itemFolderName.setText(itemWithFeed.getFolder().getName());
        else
            viewHolder.binding.itemFolderName.setText(R.string.no_folder);

        viewHolder.setReadState(itemWithFeed.getItem().isRead());
        viewHolder.setSelected(selection.contains(viewHolder.getAdapterPosition()));
    }


    @Override
    public long getItemId(int position) {
        return getItem(position).getItem().getId();
    }

    public void toggleSelection(int position) {
        if (selection.contains(position))
            selection.remove(position);
        else
            selection.add(position);

        notifyItemChanged(position, getItem(position));
    }

    public void clearSelection() {
        LinkedHashSet<Integer> localSelection = new LinkedHashSet<>(selection);
        selection.clear();

        for (int position : localSelection) {
            notifyItemChanged(position, getItem(position));
        }
    }

    public LinkedHashSet<Integer> getSelection() {
        return selection;
    }

    public void updateSelection(boolean read) {
        for (int position : selection) {
            ItemWithFeed itemWithFeed = getItem(position);
            itemWithFeed.getItem().setRead(read);
            notifyItemChanged(position, itemWithFeed);
        }
    }

    public void selectAll() {
        selection.clear();
        for (int i = 0; i < getItemCount(); i++) {
            selection.add(i);
        }

        notifyDataSetChanged();
    }

    public void unselectAll() {
        selection.clear();
        notifyDataSetChanged();
    }

    public List<ItemWithFeed> getSelectedItems() {
        List<ItemWithFeed> items = new ArrayList<>();

        for (int i : selection) {
            items.add(getItem(i));
        }

        return items;
    }

    public void clearData() {
        submitList(null);
    }

    public ItemWithFeed getItemWithFeed(int i) {
        return getItem(i);
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        if (getItem(position).getItem().hasImage()) {
            String url = getItem(position).getItem().getImageLink();
            return Collections.singletonList(url);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull String url) {
        return glideRequests
                .load(url)
                .centerCrop()
                .apply(REQUEST_OPTIONS)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(FADE_FACTORY));
    }

    public interface OnItemClickListener {
        void onItemClick(ItemWithFeed itemWithFeed, int position);

        void onItemLongClick(ItemWithFeed itemWithFeed, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ListItemBinding binding;
        View[] alphaViews;

        ItemViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener((view -> {
                int position = getAdapterPosition();

                if (listener != null && position != RecyclerView.NO_POSITION)
                    listener.onItemClick(getItem(position), position);
            }));

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();

                if (listener != null && position != RecyclerView.NO_POSITION)
                    listener.onItemLongClick(getItem(position), position);

                return true;
            });

            alphaViews = new View[]{
                    binding.itemDate,
                    binding.itemFolderName,
                    binding.itemFeedIcon,
                    binding.itemFeedName,
                    binding.itemDescription,
                    binding.itemTitle,
                    binding.itemImage,
                    binding.itemReadtimeLayout
            };
        }

        private void bind(ItemWithFeed itemWithFeed) {
            Item item = itemWithFeed.getItem();

            binding.itemTitle.setText(item.getTitle());
            binding.itemDate.setText(DateUtils.formattedDateByLocal(item.getPubDate()));
            binding.itemFeedName.setText(itemWithFeed.getFeedName());

            if (item.getCleanDescription() != null) {
                binding.itemDescription.setVisibility(View.VISIBLE);
                binding.itemDescription.setText(item.getCleanDescription());
            } else {
                binding.itemDescription.setVisibility(View.GONE);
                if (itemWithFeed.getItem().hasImage())
                    binding.itemTitle.setMaxLines(4);
            }
        }

        private void setImages(ItemWithFeed itemWithFeed) {
            if (itemWithFeed.getItem().hasImage()) {
                binding.itemImage.setVisibility(View.VISIBLE);

                glideRequests
                        .load(itemWithFeed.getItem().getImageLink())
                        .centerCrop()
                        .apply(REQUEST_OPTIONS)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transition(DrawableTransitionOptions.withCrossFade(FADE_FACTORY))
                        .into(binding.itemImage);
            } else
                binding.itemImage.setVisibility(View.GONE);

            if (itemWithFeed.getFeedIconUrl() != null) {
                glideRequests.
                        load(itemWithFeed.getFeedIconUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_rss_feed_grey)
                        .into(binding.itemFeedIcon);
            } else
                binding.itemFeedIcon.setImageResource(R.drawable.ic_rss_feed_grey);
        }

        private void applyColors(ItemWithFeed itemWithFeed) {
            Resources resources = itemView.getResources();

            if (itemWithFeed.getBgColor() != 0) {
                binding.itemFeedName.setTextColor(itemWithFeed.getBgColor());
                Utils.setDrawableColor(binding.itemDate.getBackground(), itemWithFeed.getBgColor());

            } else if (itemWithFeed.getColor() != 0) {
                binding.itemFeedName.setTextColor(itemWithFeed.getColor());
                Utils.setDrawableColor(binding.itemDate.getBackground(), itemWithFeed.getColor());

            } else if (itemWithFeed.getBgColor() == 0 && itemWithFeed.getColor() == 0) {
                binding.itemFeedName.setTextColor(resources.getColor(android.R.color.tab_indicator_text));
                Utils.setDrawableColor(binding.itemDate.getBackground(),
                        ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            }
        }

        private void setReadState(boolean isRead) {
            float alpha = isRead ? 0.5f : 1.0f;
            for (View view : alphaViews) {
                view.setAlpha(alpha);
            }
        }

        private void setSelected(boolean selected) {
            Context context = itemView.getContext();

            if (selected) {
                itemView.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.selected_background)));
            } else {
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackground, outValue, true);

                itemView.setBackgroundResource(outValue.resourceId);
            }
        }

        public ImageView getItemImage() {
            return binding.itemImage;
        }
    }
}
