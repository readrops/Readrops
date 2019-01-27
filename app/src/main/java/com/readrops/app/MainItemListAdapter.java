package com.readrops.app;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.readrops.app.database.entities.Item;

public class MainItemListAdapter extends ListAdapter<Item, MainItemListAdapter.ViewHolder> {

    private RequestManager manager;

    public MainItemListAdapter(RequestManager manager) {
        super(DIFF_CALLBACK);
        this.manager = manager;
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item item, @NonNull Item t1) {
            return item.getId() == t1.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item item, @NonNull Item t1) {
            return item.getTitle().equals(t1.getTitle()) &&
                    item.getContent().equals(t1.getContent());
        }
    };

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

        // displaying image with some round corners
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

        if (item.getImageLink() != null)
            manager.load(item.getImageLink()).apply(requestOptions).into(viewHolder.itemImage);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private ImageView itemImage;
        private TextView date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

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
