package com.readrops.app.itemslist;

import static com.readrops.app.utils.Utils.drawableWithColor;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.readrops.app.R;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.customviews.CustomExpandableBadgeDrawerItem;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawerManager {

    public static final int ARTICLES_ITEM_ID = -5;
    public static final int READ_LATER_ID = -6;
    public static final int STARS_ID = -10;
    public static final int ADD_ACCOUNT_ID = -4;
    public static final int ABOUT_ID = -7;
    public static final int SETTINGS_ID = -8;
    public static final int ACCOUNT_SETTINGS_ID = -9;

    private Activity activity;
    private Toolbar toolbar;
    private Drawer drawer;
    private FastAdapter<IDrawerItem> adapter;

    private AccountHeader header;
    private Drawer.OnDrawerItemClickListener listener;
    private AccountHeader.OnAccountHeaderListener headerListener;

    public DrawerManager(Activity activity, Toolbar toolbar, Drawer.OnDrawerItemClickListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.toolbar = toolbar;
    }

    public void setHeaderListener(AccountHeader.OnAccountHeaderListener headerListener) {
        this.headerListener = headerListener;
    }

    public Drawer buildDrawer(List<Account> accounts, int currentAccountId) {
        createAccountHeader(accounts, currentAccountId);

        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .withSelectedItem(DrawerManager.ARTICLES_ITEM_ID)
                .withOnDrawerItemClickListener(listener)
                .build();

        adapter = drawer.getAdapter();
        buildFastAdapter();

        addDefaultPlaces();

        return drawer;
    }

    public void buildFastAdapter() {
        // Folder click
        adapter.withEventHook(new ClickEventHook<IDrawerItem>() {
            @Override
            public void onClick(@NonNull View v, int position, @NonNull FastAdapter<IDrawerItem> fastAdapter, @NonNull IDrawerItem item) {
                SelectExtension selectExtension = adapter.getExtension(SelectExtension.class);

                selectExtension.deselect(selectExtension.getSelections());

                if (!item.isSelected()) {
                    selectExtension.select(position);
                }

                listener.onItemClick(v, position, item);
            }

            @Override
            public List<View> onBindMany(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof CustomExpandableBadgeDrawerItem.ViewHolder) {
                    CustomExpandableBadgeDrawerItem.ViewHolder expandableViewHolder = (CustomExpandableBadgeDrawerItem.ViewHolder) viewHolder;

                    return Arrays.asList(new View[]{
                            expandableViewHolder.itemView.findViewById(R.id.expandable_item_container),
                            expandableViewHolder.itemView.findViewById(R.id.material_drawer_icon),
                            expandableViewHolder.itemView.findViewById(R.id.material_drawer_name),
                            expandableViewHolder.itemView.findViewById(R.id.material_drawer_description)
                    }.clone());

                } else {
                    return Collections.emptyList();
                }
            }
        });

        // Expandable click
        adapter.withEventHook(new ClickEventHook<IDrawerItem>() {
            @Override
            public void onClick(@NonNull View v, int position, @NonNull FastAdapter<IDrawerItem> fastAdapter, @NonNull IDrawerItem item) {
                ExpandableExtension expandableExtension = adapter.getExtension(ExpandableExtension.class);

                expandableExtension.toggleExpandable(position);
            }

            @Override
            public List<View> onBindMany(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof CustomExpandableBadgeDrawerItem.ViewHolder) {
                    CustomExpandableBadgeDrawerItem.ViewHolder expandableViewHolder = (CustomExpandableBadgeDrawerItem.ViewHolder) viewHolder;

                    return Arrays.asList(new View[]{
                            expandableViewHolder.badge,
                            expandableViewHolder.badgeContainer,
                            expandableViewHolder.arrow,
                            expandableViewHolder.itemView.findViewById(R.id.material_drawer_arrow_container)
                    }.clone());

                } else {
                    return Collections.emptyList();
                }
            }
        });
    }

    public void updateDrawer(Map<Folder, List<Feed>> folderListMap) {
        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        addDefaultPlaces();

        Map<SecondaryDrawerItem, Feed> feedsWithoutFolder = new HashMap<>();
        boolean showFeedsWithNoUnreadItems = Boolean.parseBoolean(SharedPreferencesManager
                .readString(SharedPreferencesManager.SharedPrefKey.HIDE_SHOW_FEEDS));

        for (Map.Entry<Folder, List<Feed>> entry : folderListMap.entrySet()) {
            Folder folder = entry.getKey();
            if (folder != null) {
                CustomExpandableBadgeDrawerItem badgeDrawerItem = new CustomExpandableBadgeDrawerItem()
                        .withIdentifier(folder.getId() * 1000L) // to avoid any id conflict with other items
                        .withName(folder.getName())
                        .withIcon(R.drawable.ic_folder_grey);

                List<IDrawerItem> secondaryDrawerItems = new ArrayList<>();
                int expandableUnreadCount = 0;

                for (Feed feed : entry.getValue()) {
                    expandableUnreadCount += feed.getUnreadCount();

                    SecondaryDrawerItem secondaryDrawerItem = createSecondaryItem(feed);

                    if (!showFeedsWithNoUnreadItems) {
                        if (feed.getUnreadCount() > 0) {
                            secondaryDrawerItems.add(secondaryDrawerItem);
                        }
                    } else {
                        secondaryDrawerItems.add(secondaryDrawerItem);
                    }

                    loadItemIcon(secondaryDrawerItem, feed);
                }

                boolean showItem;
                if (!showFeedsWithNoUnreadItems) {
                    showItem = expandableUnreadCount > 0;
                } else {
                    showItem = true;
                }

                if (!secondaryDrawerItems.isEmpty() && showItem) {
                    badgeDrawerItem.withSubItems(secondaryDrawerItems);
                    badgeDrawerItem.withBadge(String.valueOf(expandableUnreadCount));
                    drawer.addItem(badgeDrawerItem);
                }
            } else { // no folder case, items to add after the folders
                for (Feed feed : folderListMap.get(folder)) {
                    SecondaryDrawerItem secondaryItem = createSecondaryItem(feed);
                    feedsWithoutFolder.put(secondaryItem, feed);
                }
            }
        }

        // work-around as MaterialDrawer doesn't accept an item list
        for (Map.Entry<SecondaryDrawerItem, Feed> entry : feedsWithoutFolder.entrySet()) {
            drawer.addItem(entry.getKey());
            loadItemIcon(entry.getKey(), entry.getValue());
        }
    }

    private void createAccountHeader(List<Account> accounts, int currentAccountId) {
        ProfileDrawerItem[] profileItems = new ProfileDrawerItem[accounts.size()];

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);

            // if currentAccount > 0, it means that the current account is no longer
            if (account.isCurrentAccount() && currentAccountId == 0)
                currentAccountId = account.getId();

            ProfileDrawerItem profileItem = createProfileItem(account);
            profileItems[i] = profileItem;
        }

        header = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(profileItems)
                .withDividerBelowHeader(false)
                .withAlternativeProfileHeaderSwitching(true)
                .withCurrentProfileHiddenInList(true)
                .withTextColorRes(R.color.colorBackground)
                .withHeaderBackground(R.drawable.header_background)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withOnAccountHeaderListener(headerListener)
                .build();

        addProfileSettingItems();

        header.setActiveProfile(currentAccountId);
    }

    private ProfileDrawerItem createProfileItem(Account account) {
        return new ProfileDrawerItem()
                .withIcon(account.getAccountType().getIconRes())
                .withName(account.getDisplayedName())
                .withEmail(account.getAccountName())
                .withIdentifier(account.getId());
    }

    private SecondaryDrawerItem createSecondaryItem(Feed feed) {
        int color = feed.getTextColor();

        return new SecondaryDrawerItem()
                .withName(feed.getName())
                .withBadge(String.valueOf(feed.getUnreadCount()))
                .withIcon(color != 0 ? drawableWithColor(color) : drawableWithColor(activity.getResources().getColor(R.color.colorPrimary)))
                .withIdentifier(feed.getId());
    }

    private void loadItemIcon(SecondaryDrawerItem secondaryDrawerItem, Feed feed) {
        Glide.with(activity)
                .load(feed.getIconUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        drawer.updateIcon(secondaryDrawerItem.getIdentifier(), new ImageHolder(resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // no need of this method
                    }
                });
    }

    private void addDefaultPlaces() {
        PrimaryDrawerItem articles = new PrimaryDrawerItem()
                .withName(R.string.articles)
                .withIcon(R.drawable.ic_rss_feed_grey)
                .withSelectable(true)
                .withIdentifier(ARTICLES_ITEM_ID);

        PrimaryDrawerItem toReadLater = new PrimaryDrawerItem()
                .withName(R.string.read_later)
                .withIcon(R.drawable.ic_read_later)
                .withSelectable(true)
                .withIdentifier(READ_LATER_ID);

        PrimaryDrawerItem favorites = new PrimaryDrawerItem()
                .withName(R.string.favorites)
                .withIcon(R.drawable.ic_star)
                .withSelectable(true)
                .withIdentifier(STARS_ID);

        PrimaryDrawerItem aboutItem = new PrimaryDrawerItem()
                .withName(R.string.about)
                .withIcon(R.drawable.ic_about_grey)
                .withSelectable(false)
                .withIdentifier(ABOUT_ID);

        PrimaryDrawerItem settingsItem = new PrimaryDrawerItem()
                .withName(R.string.settings)
                .withIcon(R.drawable.ic_settings)
                .withSelectable(false)
                .withIdentifier(SETTINGS_ID);

        drawer.addStickyFooterItem(settingsItem);
        drawer.addStickyFooterItem(aboutItem);

        drawer.addItem(articles);
        drawer.addItem(favorites);
        drawer.addItem(toReadLater);
        drawer.addItem(new DividerDrawerItem());
    }

    private void addProfileSettingItems() {
        ProfileSettingDrawerItem accountSettingsItem = new ProfileSettingDrawerItem()
                .withName(R.string.account_settings)
                .withIcon(R.drawable.ic_settings)
                .withIdentifier(ACCOUNT_SETTINGS_ID);

        ProfileSettingDrawerItem addAccountSettingsItem = new ProfileSettingDrawerItem()
                .withName(R.string.add_account)
                .withIcon(R.drawable.ic_add_account_grey)
                .withIdentifier(ADD_ACCOUNT_ID);

        header.addProfiles(accountSettingsItem, addAccountSettingsItem);
    }

    public void addAccount(Account account, boolean currentProfile) {
        ProfileDrawerItem profileItem = createProfileItem(account);

        header.addProfiles(profileItem);

        if (currentProfile)
            header.setActiveProfile(profileItem.getIdentifier());
    }

    public void setAccount(int accountId) {
        header.setActiveProfile(accountId);
    }

    public void updateHeader(List<Account> accounts) {
        header.clear();
        addProfileSettingItems();

        for (Account account : accounts) {
            addAccount(account, account.isCurrentAccount());
        }
    }

    public int getNumberOfProfiles() {
        List<IProfile> profiles = header.getProfiles();

        int number = 0;
        for (IProfile profile : profiles) {
            if (profile instanceof ProfileDrawerItem)
                number++;
        }

        return number;
    }

    public void resetItems() {
        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();
        addDefaultPlaces();
    }

    public void disableAccountSelection() {
        List<IProfile> profiles = header.getProfiles();

        for (IProfile profile : profiles) {
            if (profile.getIdentifier() != header.getActiveProfile().getIdentifier() && !(profile instanceof ProfileSettingDrawerItem)) {
                profile.withSelectable(false);
                header.updateProfile(profile);
            }
        }
    }

    public void enableAccountSelection() {
        List<IProfile> profiles = header.getProfiles();

        for (IProfile profile : profiles) {
            if (profile.getIdentifier() != header.getActiveProfile().getIdentifier() && !(profile instanceof ProfileSettingDrawerItem)) {
                profile.withSelectable(true);
                header.updateProfile(profile);
            }
        }
    }

    public void setDrawerSelection(long identifier) {
        drawer.setSelection(identifier);
    }

    public long getCurrentSelection() {
        return drawer.getCurrentSelection();
    }
}
