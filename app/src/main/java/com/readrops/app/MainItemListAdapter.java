package com.readrops.app;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;

import java.util.Collections;
import java.util.List;

public class MainItemListAdapter extends ListAdapter<ItemWithFeed, MainItemListAdapter.ViewHolder> implements ListPreloader.PreloadModelProvider<String> {

    private RequestManager manager;
    private OnItemClickListener listener;
    private ViewPreloadSizeProvider preloadSizeProvider;

    public MainItemListAdapter(RequestManager manager, ViewPreloadSizeProvider preloadSizeProvider) {
        super(DIFF_CALLBACK);

        this.manager = manager;
        this.preloadSizeProvider = preloadSizeProvider;
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

            return item.getTitle().equals(item1.getTitle()) &&
                    item.getDescription().equals(item1.getDescription());
        }
    };

    private static final DrawableCrossFadeFactory FADE_FACTORY = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.image_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ItemWithFeed itemWithFeed = getItem(i);
        viewHolder.bind(itemWithFeed);

        preloadSizeProvider.setView(viewHolder.itemImage);

        // displaying image with some round corners
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

        if (itemWithFeed.getItem().getImageLink() != null) {
            manager.load(itemWithFeed.getItem().getImageLink())
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade(FADE_FACTORY))
                    .into(viewHolder.itemImage);
        }

        if (itemWithFeed.getFeedIconUrl() != null) {
            manager.load(itemWithFeed.getFeedIconUrl())
                    .into(viewHolder.feedIcon);
        }

    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        String url = getItem(position).getItem().getImageLink();

        return Collections.singletonList(url);
    }

    @Nullable
    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull String url) {
        return manager.load(url);
    }

    public interface OnItemClickListener {
        void onItemClick(ItemWithFeed itemWithFeed);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private ImageView itemImage;
        private TextView date;
        private TextView feedName;
        private TextView itemDescription;
        private ImageView feedIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener((view -> {
                int position = getAdapterPosition();

                if (listener != null && position != RecyclerView.NO_POSITION)
                    listener.onItemClick(getItem(position));
            }));

            itemTitle = itemView.findViewById(R.id.item_title);
            itemImage = itemView.findViewById(R.id.item_image);
            date = itemView.findViewById(R.id.item_date);
            feedName = itemView.findViewById(R.id.item_feed_title);
            itemDescription = itemView.findViewById(R.id.item_description);
            feedIcon = itemView.findViewById(R.id.item_feed_icon);
        }

        private void bind(ItemWithFeed itemWithFeed) {
            Item item = itemWithFeed.getItem();

            itemTitle.setText(item.getTitle());
            date.setText(DateUtils.formatedDateByLocal(item.getPubDate()));
            feedName.setText(itemWithFeed.getFeedName());
            itemDescription.setText(item.getDescription());

            if (itemWithFeed.getColor() != 0)
                feedName.setTextColor(itemWithFeed.getColor());
        }
    }
}
