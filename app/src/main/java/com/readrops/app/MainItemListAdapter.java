package com.readrops.app;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
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
import com.readrops.app.database.entities.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainItemListAdapter extends ListAdapter<Item, MainItemListAdapter.ViewHolder> implements ListPreloader.PreloadModelProvider<String> {

    private RequestManager manager;
    private OnItemClickListener listener;
    private ViewPreloadSizeProvider preloadSizeProvider;

    public MainItemListAdapter(RequestManager manager, ViewPreloadSizeProvider preloadSizeProvider) {
        super(DIFF_CALLBACK);

        this.manager = manager;
        this.preloadSizeProvider = preloadSizeProvider;
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item item, @NonNull Item t1) {
            return item.getId() == t1.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item item, @NonNull Item t1) {
            return item.getTitle().equals(t1.getTitle()) &&
                    item.getDescription().equals(t1.getDescription());
        }
    };

    private static final DrawableCrossFadeFactory fadeFactory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.image_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = getItem(i);
        viewHolder.bind(item);

        preloadSizeProvider.setView(viewHolder.itemImage);

        // displaying image with some round corners
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

        if (item.getImageLink() != null)
            manager.load(item.getImageLink())
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade(fadeFactory))
                    .into(viewHolder.itemImage);
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        String url = getItem(position).getImageLink();

        return Collections.singletonList(url);
    }

    @Nullable
    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull String url) {
        return manager.load(url);
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private ImageView itemImage;
        private TextView date;

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
        }

        private void bind(Item item) {
            itemTitle.setText(item.getTitle());
            date.setText(DateUtils.formatedDateByLocal(item.getFormatedDate()));
        }
    }
}
