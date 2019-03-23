package com.readrops.app.views;

import android.content.res.Resources;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.DateUtils;
import com.readrops.app.utils.GlideRequests;
import com.readrops.app.utils.Utils;

import java.util.Collections;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class MainItemListAdapter extends ListAdapter<ItemWithFeed, MainItemListAdapter.ItemViewHolder> implements ListPreloader.PreloadModelProvider<String> {

    private GlideRequests glideRequests;
    private OnItemClickListener listener;
    private ViewPreloadSizeProvider preloadSizeProvider;

    public MainItemListAdapter(GlideRequests glideRequests, ViewPreloadSizeProvider preloadSizeProvider) {
        super(DIFF_CALLBACK);

        this.glideRequests = glideRequests;
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
                    itemWithFeed.getFeedName().equals(t1.getFeedName()) &&
                    itemWithFeed.getFolder().getName().equals(t1.getFolder().getName());
        }
    };

    private static final DrawableCrossFadeFactory FADE_FACTORY = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private static final RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(16));

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.image_item, viewGroup, false);

        ItemViewHolder viewHolder = new ItemViewHolder(view);
        preloadSizeProvider.setView(viewHolder.itemImage);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int i) {
        ItemWithFeed itemWithFeed = getItem(i);
        viewHolder.bind(itemWithFeed);

        if (itemWithFeed.getItem().hasImage()) {
            viewHolder.itemImage.setVisibility(View.VISIBLE);

            glideRequests
                    .load(itemWithFeed.getItem().getImageLink())
                    .centerCrop()
                    .apply(requestOptions)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade(FADE_FACTORY))
                    .into(viewHolder.itemImage);
        } else
            viewHolder.itemImage.setVisibility(View.GONE);

        if (itemWithFeed.getFeedIconUrl() != null) {
            glideRequests.load(itemWithFeed.getFeedIconUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_rss_feed)
                    .into(viewHolder.feedIcon);
        } else
            viewHolder.feedIcon.setImageResource(R.drawable.ic_rss_feed);

        Resources resources = viewHolder.itemView.getResources();
        if (itemWithFeed.getColor() != 0) {
            viewHolder.feedName.setTextColor(itemWithFeed.getColor());
            Utils.setDrawableColor(viewHolder.dateLayout.getBackground(), itemWithFeed.getColor());
        } else
            viewHolder.feedName.setTextColor(resources.getColor(android.R.color.tab_indicator_text));

        if (itemWithFeed.getBgColor() != 0)
            Utils.setDrawableColor(viewHolder.dateLayout.getBackground(), itemWithFeed.getBgColor());

        int minutes = (int)Math.round(itemWithFeed.getItem().getReadTime());
        if (minutes < 1)
            viewHolder.itemReadTime.setText(resources.getString(R.string.read_time_lower_than_1));
        else if (minutes > 1)
            viewHolder.itemReadTime.setText(resources.getString(R.string.read_time, String.valueOf(minutes)));
        else
            viewHolder.itemReadTime.setText(resources.getString(R.string.read_time_one_minute));

        if (itemWithFeed.getFolder().getId() != 1)
            viewHolder.itemFolderName.setText(itemWithFeed.getFolder().getName());
        else
            viewHolder.itemFolderName.setText(resources.getString(R.string.no_folder));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItem().getId();
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
                .apply(requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(FADE_FACTORY));
    }

    public interface OnItemClickListener {
        void onItemClick(ItemWithFeed itemWithFeed);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private TextView date;
        private TextView feedName;
        private TextView itemDescription;
        private ImageView feedIcon;
        private ImageView itemImage;
        private TextView itemReadTime;
        private TextView itemFolderName;
        private RelativeLayout dateLayout;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener((view -> {
                int position = getAdapterPosition();

                if (listener != null && position != RecyclerView.NO_POSITION)
                    listener.onItemClick(getItem(position));
            }));

            itemTitle = itemView.findViewById(R.id.item_title);
            date = itemView.findViewById(R.id.item_date);
            feedName = itemView.findViewById(R.id.item_feed_title);
            itemDescription = itemView.findViewById(R.id.item_description);
            feedIcon = itemView.findViewById(R.id.item_feed_icon);
            itemImage = itemView.findViewById(R.id.item_image);
            itemReadTime = itemView.findViewById(R.id.item_readtime);
            itemFolderName = itemView.findViewById(R.id.item_folder_name);
            dateLayout = itemView.findViewById(R.id.item_date_layout);
        }

        private void bind(ItemWithFeed itemWithFeed) {
            Item item = itemWithFeed.getItem();

            itemTitle.setText(item.getTitle());
            date.setText(DateUtils.formatedDateByLocal(item.getPubDate()));
            feedName.setText(itemWithFeed.getFeedName());

            if (item.getCleanDescription() != null) {
                itemDescription.setVisibility(View.VISIBLE);
                itemDescription.setText(item.getCleanDescription());
            } else
                itemDescription.setVisibility(View.GONE);
        }

        public ImageView getItemImage() {
            return itemImage;
        }
    }
}
