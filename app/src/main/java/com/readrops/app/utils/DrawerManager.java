package com.readrops.app.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.ColorInt;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.readrops.app.R;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.readrops.app.utils.Utils.drawableWithColor;

public class DrawerManager {

    public static final int ARTICLES_ITEM_ID = 1;
    public static final int READ_LATER_ID = 2;

    private Drawer drawer;

    public DrawerManager(Drawer drawer) {
        this.drawer = drawer;
    }

    public void updateDrawer(Context context, Map<Folder, List<Feed>> folderListMap) {
        drawer.removeAllItems();
        addDefaultPlaces(context);

        List<SecondaryDrawerItem> feedsWithoutFolder = new ArrayList<>();

        for (Folder folder : folderListMap.keySet()) {
            if (folder.getId() != 1) {
                ExpandableBadgeDrawerItem badgeDrawerItem = new ExpandableBadgeDrawerItem()
                        .withName(folder.getName())
                        .withIdentifier(folder.getId())
                        .withIcon(context.getDrawable(R.drawable.ic_folder_grey));

                List<IDrawerItem> secondaryDrawerItems = new ArrayList<>();

                for (Feed feed : folderListMap.get(folder)) {
                    int color = feed.getTextColor();

                    SecondaryDrawerItem secondaryDrawerItem = new SecondaryDrawerItem()
                            .withName(feed.getName())
                            .withIcon(color != 0 ? drawableWithColor(color) : drawableWithColor(context.getResources().getColor(R.color.colorPrimary)))
                            .withIdentifier(feed.getId());

                    secondaryDrawerItems.add(secondaryDrawerItem);
                }

                if (secondaryDrawerItems.size() > 0) {
                    badgeDrawerItem.withSubItems(secondaryDrawerItems);
                    drawer.addItem(badgeDrawerItem);
                }
            } else { // no folder case, items to add after the folders
                for (Feed feed : folderListMap.get(folder)) {
                    int color = feed.getTextColor();

                    SecondaryDrawerItem primaryDrawerItem = new SecondaryDrawerItem()
                            .withName(feed.getName())
                            .withIcon(color != 0 ? drawableWithColor(color) : drawableWithColor(context.getResources().getColor(R.color.colorPrimary)))
                            .withIdentifier(feed.getId());

                    feedsWithoutFolder.add(primaryDrawerItem);
                }
            }
        }

        // work-around as MaterialDrawer doesn't accept an item list
        for (SecondaryDrawerItem primaryDrawerItem : feedsWithoutFolder) {
            drawer.addItem(primaryDrawerItem);
        }
    }

    private void addDefaultPlaces(Context context) {
        PrimaryDrawerItem articles = new PrimaryDrawerItem()
                .withName(context.getString(R.string.articles))
                .withIcon(context.getDrawable(R.drawable.ic_rss_feed_grey))
                .withIdentifier(ARTICLES_ITEM_ID);

        PrimaryDrawerItem toReadLater = new PrimaryDrawerItem()
                .withName(context.getString(R.string.read_later))
                .withIcon(context.getDrawable(R.drawable.ic_read_later_grey))
                .withIdentifier(READ_LATER_ID);

        drawer.addItem(articles);
        drawer.addItem(toReadLater);
        drawer.addItem(new DividerDrawerItem());
    }
}
