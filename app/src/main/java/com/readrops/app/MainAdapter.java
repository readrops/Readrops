package com.readrops.app;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.readrops.readropslibrary.PageParser;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    List<RSSItem> items;
    private Context context;

    public MainAdapter(Context context, List<RSSItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.compact_list_element, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        RSSItem item = items.get(i);
        viewHolder.bind(item);

        Thread thread = new Thread(() -> {
            String imageUrl = PageParser.getOGImageLink(item.getLink());
            Glide.with(context).load(imageUrl).into(viewHolder.itemImage);
        });


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private ImageView itemImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemTitle = itemView.findViewById(R.id.item_title);
            itemImage = itemView.findViewById(R.id.item_image);
        }

        private void bind(RSSItem item) {
            itemTitle.setText(item.getTitle());
        }
    }
}
