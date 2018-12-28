package com.esenlermotionstar.lightnote;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.sax.Element;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hussainlobo on 26.05.2018.
 */


class LntItemViewHolder extends RecyclerView.ViewHolder {
    CN_Item relatedItem;
    public TextView title;
    public ImageView imageView;
    View ItemView;

    public LntItemViewHolder(View v) {
        super(v);
        ItemView = v;
        title = (TextView) (v.findViewById(R.id.noteTitle));
        imageView = (ImageView) (v.findViewById(R.id.noteThumbnail));
    }

    public void Minimalize() {
        final float scale = ItemView.getContext().getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams param = ItemView.getLayoutParams();
        param.height = (int) (72 * scale);

        ItemView.setLayoutParams(param);
    }

    public void FullView() {
        final float scale = ItemView.getContext().getResources().getDisplayMetrics().density;

        ViewGroup.LayoutParams param = ItemView.getLayoutParams();
        param.height = (int) (264 * scale);

        ItemView.setLayoutParams(param);
    }

    public void MatchParentWidthIfDeviceIsPhone() {
        int scrObj = (ItemView.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

        if (scrObj == Configuration.SCREENLAYOUT_SIZE_NORMAL || scrObj == Configuration.SCREENLAYOUT_SIZE_SMALL) {
           // final float scale = ItemView.getContext().getResources().getDisplayMetrics().density;
            ViewGroup.LayoutParams param = ItemView.getLayoutParams();
            Point size = new Point(0, 0);
            Display display = ((WindowManager) ItemView.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getSize(size);
            param.width = size.x;
            ItemView.setLayoutParams(param);
        }

    }

    boolean ItemSelected = false;

    void setItemSelected(Boolean bool) {
        ItemSelected = bool;
        setItemSelectedVisibility();
    }




    void setItemSelectedVisibility() {
        if (ItemSelected) {
           /*
           ANİMASYON
           ScaleAnimation anim = new ScaleAnimation(1f, 0.9f, 1f, 0.9f);
            anim.setFillAfter(true);
            anim.setDuration(200);
            itemView.startAnimation(anim);*/

            itemView.findViewById(R.id.selectedItemChk).setVisibility(View.VISIBLE);

        } else {
            /*ScaleAnimation anim = new ScaleAnimation(0.9f, 1f, 0.9f, 1f);
            anim.setFillAfter(true);
            anim.setDuration(200);
            itemView.startAnimation(anim);*/

            itemView.findViewById(R.id.selectedItemChk).setVisibility(View.INVISIBLE);
        }
    }

    Boolean Selected() {
        return ItemSelected;
    }

}

abstract class AdapterEvents {
    abstract void intoSelectionMode();

    abstract void onRemovedItem(int index);

    abstract void onExitSelectionMode();

    abstract boolean ExitSelectionModeRequest();

    public abstract void onAfterAllSelectedItemsRemoved();
}

public class LNTItemAdapter extends RecyclerView.Adapter<LntItemViewHolder> {
    ArrayList<CN_Item> SubItems;
    Context ActivityContext;
    View.OnClickListener ClickEvent;
    Boolean SelectionMode = false;
    AdapterEvents adapterEvents;
    private ArrayList<CN_Item> SelectedItems;
    private ArrayList<View> SelectedItemEnabledViews;
    boolean ElementIsHolded;
    public View snackBarViewForRemoveInfo;

    public LNTItemAdapter(ArrayList<CN_Item> SubItems_, Context ctx, View.OnClickListener clickEvent) {
        SubItems = SubItems_;
        ActivityContext = ctx;
        ClickEvent = clickEvent;

    }

    public void removeSingleItem(int subitemindex_) {

        final int subitemindex = subitemindex_;

        final CN_Item item = SubItems.get(subitemindex);


        SubItems.remove(item);

        item.RemoveInstant();
        adapterEvents.onRemovedItem(subitemindex);


        //View rootView = ((Activity) ActivityContext).getWindow().getDecorView().getRootView();
        View rootView = snackBarViewForRemoveInfo;
        String message = item.title + " " + ActivityContext.getString(R.string.x_is_removed);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        SubItems.add(subitemindex, item);
                        notifyItemInserted(subitemindex);
                        item.CancelInstantRemove();
                    }
                }).addCallback(new Snackbar.Callback() {


                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {


                            if (item instanceof CN_Note)
                            {
                                ((CN_Note) item).RemoveWorkFolder();
                                //(item).RemoveItself();
                            }
                            /*else if (item instanceof CN_Category)
                            {
                                ((CN_Category)
                            }*/


                            item.RemoveItself();



                        }


                    }

                });
        snackbar.show();

        adapterEvents.onAfterAllSelectedItemsRemoved();

    }

    @Override
    public LntItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ActivityContext).inflate(R.layout.item, parent, false);
        return new LntItemViewHolder(v);

    }



    @Override
    public void onBindViewHolder(final LntItemViewHolder holder, int position) {

        final CN_Item itm = SubItems.get(position);
        holder.relatedItem = itm;
        holder.title.setText(itm.title);
        if (itm instanceof CN_Category) {
            holder.Minimalize();
            holder.imageView.setImageDrawable(new ColorDrawable(Color.rgb(255, 255, 255)));
        } else {
            CN_Note itmAsNote = ((CN_Note) (itm));
            holder.FullView();
            ItemImageUpdater img = new ItemImageUpdater(holder.itemView);
            img.execute(new File(itmAsNote.referanceFolder + "/thumbnail/"), new File(itmAsNote.referanceFolder));

        }

        holder.ItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SelectionMode) {

                    SelectItem(holder, itm);
                }else ClickEvent.onClick(view);


            }
        });



        //holder.ItemView.setTou
        /*holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    return  true;
                }
                if (motionEvent.getAction() == MotionEvent){

                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP )
                {
                    if(ElementIsHolded)
                    {
                        if (!SelectionMode)
                        {
                            StartSelectionMode();
                            SelectItem(holder, itm);
                        }
                    }
                    else
                    {
                        if (SelectionMode)
                        {
                            SelectItem(holder, itm);
                        }
                        else ClickEvent.onClick(view);
                    }



                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
                {
                    ElementIsHolded = false;

                }


                return false;
            }
        });*/
        holder.MatchParentWidthIfDeviceIsPhone();
        setAnimation(holder.itemView, position);
        holder.ItemView.setTag(R.string.category_note_item_property, itm);

    }

    private void SelectItem(LntItemViewHolder holder, CN_Item itm) {

        boolean isSelected = holder.Selected();


        if (!SelectedItems.contains(itm)) {
            SelectedItems.add(itm);
            if (!SelectedItemEnabledViews.contains(holder.itemView)) SelectedItemEnabledViews.add(holder.ItemView);
            holder.setItemSelected(true);
        } else if (SelectedItems.indexOf(itm) > -1) {
            SelectedItems.remove(itm);

            if (SelectedItemEnabledViews.contains(holder.itemView))

            holder.setItemSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return SubItems.size();
    }

    int lastPosition = -1;

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }

    }

    void StartSelectionMode() {
        SelectedItems = new ArrayList<>();
        SelectedItemEnabledViews = new ArrayList<>();
        SelectionMode = true;
        adapterEvents.intoSelectionMode();
        ElementIsHolded = false;
    }

   /* public void StartSelection()
    {
        StartSelectionMode();
    }
*/
    void RemoveSelectedItems() {

        synchronized (this) {
            final Map<CN_Item, Integer> removedItemsIndexesForUndo = new HashMap<>();
            int selected_item_size = SelectedItems.size();
            for (int i = 0; i < selected_item_size; i++) {
                CN_Item item = SelectedItems.get(i);

                int subitemindex = SubItems.indexOf(item);
                removedItemsIndexesForUndo.put(item, subitemindex);
                SubItems.remove(item);
                item.RemoveInstant();

                adapterEvents.onRemovedItem(subitemindex);

            }
            View rootView = snackBarViewForRemoveInfo;

            //View rootView = snackBarViewForRemoveInfo;
            Snackbar snackbar = Snackbar.make(rootView, String.format(ActivityContext.getString(R.string.items_removed_piece),selected_item_size), Snackbar.LENGTH_LONG)

                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Set<CN_Item> allItms = removedItemsIndexesForUndo.keySet();
                            for (CN_Item item : allItms) {
                                item.CancelInstantRemove();
                                int index = removedItemsIndexesForUndo.get(item);
                                index = index >= SubItems.size() ? SubItems.size() : index;
                                SubItems.add(index, item);
                                notifyItemInserted(index);

                            }
                        }

                    }).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar sb) {
                            super.onShown(sb);

                        }

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                Set<CN_Item> allItms = removedItemsIndexesForUndo.keySet();
                                for (CN_Item item : allItms) {

                                    if (item instanceof CN_Note)
                                        ((CN_Note) item).RemoveWorkFolder();

                                    item.RemoveItself();
                                }
                            }


                        }

                    });
            snackbar.show();

            ExitSelectionMode();
            adapterEvents.onAfterAllSelectedItemsRemoved();
        }

    }

    void ExitSelectionMode() {
        if (SelectionMode != false) {
            for (int i = 0; i < SelectedItemEnabledViews.size(); i++) {
                SelectedItemEnabledViews.get(i).findViewById(R.id.selectedItemChk).setVisibility(View.INVISIBLE);
            }
            SelectedItemEnabledViews.clear();
            SelectedItemEnabledViews = null;

            SelectedItems = null;
            SelectionMode = false;
            ElementIsHolded = false;
            adapterEvents.onExitSelectionMode();
        }

    }


}
