package com.readrops.app.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.readrops.app.utils.Utils.drawableWithColor;

public class DrawerManager {

    public static final int ARTICLES_ITEM_ID = -5;
    public static final int READ_LATER_ID = -6;
    public static final int ACCOUNT_SETTINGS_ID = -3;
    public static final int ADD_ACCOUNT_ID = -4;

    private Activity activity;
    private Toolbar toolbar;
    private Drawer drawer;

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

    public Drawer buildDrawer(List<Account> accounts) {
        createAccountHeader(accounts);

        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .withSelectedItem(DrawerManager.ARTICLES_ITEM_ID)
                .withOnDrawerItemClickListener(listener)
                .build();

        addDefaultPlaces();

        return drawer;
    }

    public void updateDrawer(Map<Folder, List<Feed>> folderListMap) {
        drawer.removeAllItems();
        addDefaultPlaces();

        List<SecondaryDrawerItem> feedsWithoutFolder = new ArrayList<>();

        for (Folder folder : folderListMap.keySet()) {
            if (folder.getId() != 0) {
                // no identifier for badge items, but if needed, be aware of not getting conflicts
                // with secondary item identifiers (folder and feed ids can be the same)
                ExpandableBadgeDrawerItem badgeDrawerItem = new ExpandableBadgeDrawerItem()
                        .withName(folder.getName())
                        .withIcon(R.drawable.ic_folder_grey);

                List<IDrawerItem> secondaryDrawerItems = new ArrayList<>();
                int expandableUnreadCount = 0;

                for (Feed feed : folderListMap.get(folder)) {
                    expandableUnreadCount += feed.getUnreadCount();

                    SecondaryDrawerItem secondaryDrawerItem = createSecondaryItem(feed);
                    secondaryDrawerItems.add(secondaryDrawerItem);
                }

                if (secondaryDrawerItems.size() > 0) {
                    badgeDrawerItem.withSubItems(secondaryDrawerItems);
                    badgeDrawerItem.withBadge(String.valueOf(expandableUnreadCount));
                    drawer.addItem(badgeDrawerItem);
                }
            } else { // no folder case, items to add after the folders
                for (Feed feed : folderListMap.get(folder)) {
                    SecondaryDrawerItem primaryDrawerItem = createSecondaryItem(feed);

                    feedsWithoutFolder.add(primaryDrawerItem);
                }
            }
        }

        // work-around as MaterialDrawer doesn't accept an item list
        for (SecondaryDrawerItem primaryDrawerItem : feedsWithoutFolder) {
            drawer.addItem(primaryDrawerItem);
        }
    }

    private void createAccountHeader(List<Account> accounts) {
        ProfileDrawerItem[] profileItems = new ProfileDrawerItem[accounts.size()];
        int currentAccountId = 1;

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);

            if (account.isCurrentAccount())
                currentAccountId = account.getId();

            ProfileDrawerItem profileItem = createProfileItem(account);
            profileItems[i] = profileItem;
        }

        ProfileSettingDrawerItem profileSettingsItem = new ProfileSettingDrawerItem()
                .withName(R.string.account_settings)
                .withIcon(R.drawable.ic_account)
                .withIdentifier(ACCOUNT_SETTINGS_ID);

        ProfileSettingDrawerItem addAccountSettingsItem = new ProfileSettingDrawerItem()
                .withName(R.string.add_account)
                .withIcon(R.drawable.ic_add_account_grey)
                .withIdentifier(ADD_ACCOUNT_ID);

        header = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(profileItems)
                .addProfiles(profileSettingsItem, addAccountSettingsItem)
                .withDividerBelowHeader(false)
                .withAlternativeProfileHeaderSwitching(true)
                .withCurrentProfileHiddenInList(true)
                .withTextColorRes(R.color.colorBackground)
                .withHeaderBackground(R.drawable.header_background)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withOnAccountHeaderListener(headerListener)
                .build();

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

        SecondaryDrawerItem secondaryDrawerItem = new SecondaryDrawerItem()
                .withName(feed.getName())
                .withBadge(String.valueOf(feed.getUnreadCount()))
                .withIcon(color != 0 ? drawableWithColor(color) : drawableWithColor(activity.getResources().getColor(R.color.colorPrimary)))
                .withIdentifier(feed.getId());

        Glide.with(activity)
                .load(feed.getIconUrl())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        drawer.updateIcon(secondaryDrawerItem.getIdentifier(), new ImageHolder(resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        return secondaryDrawerItem;
    }

    private void addDefaultPlaces() {
        PrimaryDrawerItem articles = new PrimaryDrawerItem()
                .withName(R.string.articles)
                .withIcon(R.drawable.ic_rss_feed_grey)
                .withSelectable(true)
                .withIdentifier(ARTICLES_ITEM_ID);

        PrimaryDrawerItem toReadLater = new PrimaryDrawerItem()
                .withName(R.string.read_later)
                .withIcon(R.drawable.ic_read_later_grey)
                .withSelectable(true)
                .withIdentifier(READ_LATER_ID);

        drawer.addItem(articles);
        drawer.addItem(toReadLater);
        drawer.addItem(new DividerDrawerItem());
    }

    public void addAccount(Account account) {
        ProfileDrawerItem profileItem = createProfileItem(account);

        header.addProfiles(profileItem);
        header.setActiveProfile(profileItem.getIdentifier());
    }

    public void resetItems() {
        drawer.removeAllItems();
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
}
