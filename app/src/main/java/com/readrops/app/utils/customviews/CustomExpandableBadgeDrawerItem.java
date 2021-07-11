package com.readrops.app.utils.customviews;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.icons.MaterialDrawerFont;
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;
import com.mikepenz.materialdrawer.model.interfaces.ColorfulBadgeable;
import com.readrops.app.R;

import java.util.List;

/**
 * This a simple modification of original ExpandableBadgeDrawerItem from MaterialDrawer lib to get two click events from an expandable drawer item
 */
public class CustomExpandableBadgeDrawerItem extends BaseDescribeableDrawerItem<CustomExpandableBadgeDrawerItem,
        CustomExpandableBadgeDrawerItem.ViewHolder>
        implements ColorfulBadgeable<CustomExpandableBadgeDrawerItem>, IClickable {

    protected ColorHolder arrowColor;

    protected int arrowRotationAngleStart = 0;

    protected int arrowRotationAngleEnd = 180;

    protected StringHolder mBadge;
    protected BadgeStyle mBadgeStyle = new BadgeStyle();

    @Override
    public int getType() {
        return R.id.material_drawer_item_expandable_badge;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.custom_expandable_drawer_item;
    }

    @Override
    public void bindView(CustomExpandableBadgeDrawerItem.ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        Context ctx = viewHolder.itemView.getContext();
        //bind the basic view parts
        bindViewHelper(viewHolder);

        //set the text for the badge or hide
        boolean badgeVisible = StringHolder.applyToOrHide(mBadge, viewHolder.badge);
        //style the badge if it is visible
        if (true) {
            mBadgeStyle.style(viewHolder.badge, getTextColorStateList(getColor(ctx), getSelectedTextColor(ctx)));
            viewHolder.badgeContainer.setVisibility(View.VISIBLE);
        } else {
            viewHolder.badgeContainer.setVisibility(View.GONE);
        }

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.badge.setTypeface(getTypeface());
        }

        //make sure all animations are stopped
        if (viewHolder.arrow.getDrawable() instanceof IconicsDrawable) {
            ((IconicsDrawable) viewHolder.arrow.getDrawable()).color(this.arrowColor != null ? this.arrowColor.color(ctx) : getIconColor(ctx));
        }
        viewHolder.arrow.clearAnimation();
        if (!isExpanded()) {
            viewHolder.arrow.setRotation(this.arrowRotationAngleStart);
        } else {
            viewHolder.arrow.setRotation(this.arrowRotationAngleEnd);
        }

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    @Override
    public CustomExpandableBadgeDrawerItem withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener onDrawerItemClickListener) {
        mOnDrawerItemClickListener = null;
        return this;
    }

    @Override
    public Drawer.OnDrawerItemClickListener getOnDrawerItemClickListener() {
        return null;
    }

    @Override
    public CustomExpandableBadgeDrawerItem withBadge(StringHolder badge) {
        this.mBadge = badge;
        return this;
    }

    @Override
    public CustomExpandableBadgeDrawerItem withBadge(String badge) {
        this.mBadge = new StringHolder(badge);
        return this;
    }

    @Override
    public CustomExpandableBadgeDrawerItem withBadge(@StringRes int badgeRes) {
        this.mBadge = new StringHolder(badgeRes);
        return this;
    }

    @Override
    public CustomExpandableBadgeDrawerItem withBadgeStyle(BadgeStyle badgeStyle) {
        this.mBadgeStyle = badgeStyle;
        return this;
    }

    public StringHolder getBadge() {
        return mBadge;
    }

    public BadgeStyle getBadgeStyle() {
        return mBadgeStyle;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public IItem withOnItemPreClickListener(OnClickListener onItemPreClickListener) {
        return null;
    }

    @Override
    public OnClickListener getOnPreItemClickListener() {
        return null;
    }

    @Override
    public IItem withOnItemClickListener(OnClickListener onItemClickListener) {
        return null;
    }

    @Override
    public OnClickListener getOnItemClickListener() {
        return null;
    }

    public static class ViewHolder extends BaseViewHolder {
        public ImageView arrow;
        public View badgeContainer;
        public TextView badge;

        public ViewHolder(View view) {
            super(view);
            badgeContainer = view.findViewById(R.id.material_drawer_badge_container);
            badge = view.findViewById(R.id.material_drawer_badge);
            arrow = view.findViewById(R.id.material_drawer_arrow);
            arrow.setImageDrawable(new IconicsDrawable(view.getContext(), MaterialDrawerFont.Icon.mdf_expand_more).sizeDp(16).paddingDp(2).color(Color.BLACK));
        }
    }
}
