package com.esenlermotionstar.lightnote;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ActionMode actionMode;
    Toolbar toolbar;
    float scale;
    LNTItemAdapter adapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);



        // final float scale = getResources().getDisplayMetrics().density;

        scale = getResources().getDisplayMetrics().density;

        this.setRequestedOrientation(this.getResources().getConfiguration().orientation);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), fab);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.add_fab_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_add_category) {
                            LNtNoteEditor.AddNewCategory();
                            //loadItem(LNtNoteEditor.rootCategory);
                        } else if (item.getItemId() == R.id.menu_add_note) {
                            LNtNoteEditor.AddNewNote();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        recyclerView = (RecyclerView) (findViewById(R.id.RVItems));

        initializeNoteEditorAndAdapter();




        int scrObj = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);


        if (scrObj == Configuration.SCREENLAYOUT_SIZE_XLARGE
                ||
                scrObj == Configuration.SCREENLAYOUT_SIZE_LARGE
        ) {
            //Toast.makeText(this, "Small sized screen", Toast.LENGTH_LONG).show();

            BigiestView(recyclerView);

        } else if (scrObj == Configuration.SCREENLAYOUT_SIZE_NORMAL || scrObj == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            GoToSmallScreenMode(recyclerView);
        } else {
            AutoView(recyclerView);
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        //Toast.makeText(this, getApplicationContext().getFilesDir().toString(), Toast.LENGTH_SHORT).show();

    }


    void loadItem(CN_Item itx) {

        if (itx instanceof CN_Category) {

            LNtNoteEditor.intoTheFolder(itx);
            ((LNTItemAdapter) recyclerView.getAdapter()).SubItems = ((CN_Category) itx).SubItems;

            getSupportActionBar().setTitle(createCategoryText(LNtNoteEditor.currentCategory.title));
            /*((TextView) (findViewById(R.id.currentCategoryTitletxt))).
                    setText(createCategoryText(LNtNoteEditor.currentCategory.title));*/
            (recyclerView.getAdapter()).notifyDataSetChanged();

        } else {
            openNoteEditor(itx);
        }
    }

    private void openNoteEditor(CN_Item itx) {
        Intent myIntent = new Intent(getApplicationContext(), textEditorActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String willBeSent = ((CN_Note) itx).getReferanceFolder();
        myIntent.putExtra("path", willBeSent);
        myIntent.putExtra("title", itx.title);
        getApplicationContext().startActivity(myIntent);
    }

    void initializeNoteEditorAndAdapter() {
        LNtNoteEditor.Load(this);

        adapt = new LNTItemAdapter(((CN_Category) (LNtNoteEditor.rootCategory)).SubItems, this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CN_Item itx = (CN_Item) (view.getTag(R.string.category_note_item_property));
                loadItem(itx);
            }
        });
        adapt.snackBarViewForRemoveInfo = recyclerView;
        adapt.adapterEvents = new AdapterEvents() {
            @Override
            void intoSelectionMode() {
                LNTItemsActionModeCallback callback = new LNTItemsActionModeCallback(adapt);
                actionMode = MainActivity.this.startSupportActionMode(callback);
                //actionMode.setTitle("Select Mode");

                MainActivity.this.getSupportActionBar().hide();
            }

            @Override
            void onRemovedItem(int index) {

                recyclerView.getAdapter().notifyItemRemoved(index);

            }

            @Override
            void onExitSelectionMode() {
                if (actionMode != null)
                    actionMode.finish();
                MainActivity.this.getSupportActionBar().show();


            }

            @Override
            boolean ExitSelectionModeRequest() {

                return true;
            }

            @Override
            public void onAfterAllSelectedItemsRemoved() {
                UpdateIndexesOnXML();
            }
        };
        recyclerView.setAdapter(adapt);
        LNtNoteEditor.editorEvents = new LNtNoteEditor.NoteEditorEvents() {

            @Override
            void added(int index) {
                LNTItemAdapter adapter = (LNTItemAdapter) (recyclerView.getAdapter());
                adapter.notifyItemInserted(index);
                recyclerView.scrollToPosition(ScrollView.FOCUS_DOWN);
                loadItem(adapter.SubItems.get(index));
            }
        };
        loadItem(LNtNoteEditor.rootCategory);
        ItemTouchHelper ith = new ItemTouchHelper(getTouchHelperCallback());
        ith.attachToRecyclerView(recyclerView);
    }



    ItemTouchHelper.Callback getTouchHelperCallback() {
        ItemTouchHelper.Callback simpTchHelp = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            Drawable defaultBck;

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    final ColorDrawable background_delete = new ColorDrawable(Color.rgb(244,67,54));
                    //final ColorDrawable background_select = new ColorDrawable(Color.rgb(232,147,44));
                    View itemView = viewHolder.itemView;

                    Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_remove);
                    //Drawable selectIc = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_check_box_white_24dp);
                    //selectIc.setTint(Color.parseColor("#FFFFFF"));

                    if (dX > 0) {
                        //SOLDAN SAĞA - silme
                        background_delete.setBounds(itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getLeft() + (int) dX, viewHolder.itemView.getBottom());

                        int icLeft = (int)(itemView.getLeft() + 16  * scale),
                                icRight = (int)(icLeft + 32 * scale),
                                icTop = (int)(itemView.getTop() + itemView.getHeight() / 2 - 16  * scale),
                                icBot = (int)(icTop + 32 * scale);
                        icon.setBounds(icLeft, icTop, icRight, icBot);

                    } else {
                        //SAĞDAN SOLA - seçme
                        background_delete.setBounds(itemView.getRight() + (int) dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());

                        int icLeft = (int)(itemView.getLeft() + itemView.getWidth() - 16 * scale),
                                icRight = (int)(icLeft - 32 * scale),
                                icTop = (int)(itemView.getTop() + itemView.getHeight() / 2 - 16 * scale),
                                icBot = (int)(icTop + 32 * scale);
                        icon.setBounds(icLeft, icTop, icRight, icBot);


                    }
                    background_delete.draw(c);
                    icon.draw(c);
                    // compute top and left margin to the view bounds



                }


            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
               // Log.i(null, "onSwiped: Direction => " + String.valueOf(direction));
                removeItem(viewHolder.getAdapterPosition());
            }
        };
        return simpTchHelp;
    }

    private void removeItem(int viewHolder) {
        LNTItemAdapter itemadapterforlnt = ((LNTItemAdapter) recyclerView.getAdapter());
        itemadapterforlnt.removeSingleItem(viewHolder);

    }

    //ViewHolder indisindeki elementi target indisine taşıyor.
    private void moveItem(int viewHolder, int target) {
        LNTItemAdapter itemadapterforlnt = ((LNTItemAdapter) recyclerView.getAdapter());

        CN_Item item = itemadapterforlnt.SubItems.get(viewHolder);
        int oldIndex = itemadapterforlnt.SubItems.indexOf(item);
        itemadapterforlnt.SubItems.remove(item);
        itemadapterforlnt.SubItems.add(target, item);
        item.setFlatIndex(target);
        itemadapterforlnt.notifyItemMoved(oldIndex, target);
        if (viewHolder > target)
            UpdateIndexesOnXML(target,viewHolder + 1);
        else
            UpdateIndexesOnXML(viewHolder, target + 1);
    }

    void GoToSmallScreenMode(RecyclerView rec) {
        Anoncment("Phone/Small Screen");

        rec.setLayoutManager(new LinearLayoutManager(this));

    }

    private void Anoncment(String s) {
        System.out.println("ANONCMENT: " + s);
    }

    void AutoView(RecyclerView rec) {
        Anoncment("Unknown Screen");

        Point size = new Point(0, 0);

        getWindowManager().getDefaultDisplay().getSize(size);
        int colLenght = (int) (size.x / (320 * scale));
        //int newItemWidth = (size.x / colLenght);
        //colLenght = (int) (size.x / newItemWidth);

        //Toast.makeText(this, String.valueOf(size.x), Toast.LENGTH_SHORT).show();
        if (colLenght > 1) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(colLenght + 1, 1);
            rec.setLayoutManager(staggeredGridLayoutManager);
        } else
            GoToSmallScreenMode(rec);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LNtNoteEditor.Close();

    }


    @Override
    protected void onPause() {
        super.onPause();
        LNtNoteEditor.editor.save();


    }

    void BigiestView(RecyclerView rec) {
        Anoncment("Large Screen");
        Point size = new Point(0, 0);
        getWindowManager().getDefaultDisplay().getSize(size);
        final float scale = getResources().getDisplayMetrics().density;

        int colLenght = (int) (size.x / (320 * scale));


        //colLenght -= (colLenght / 1.5);
        /*Toast.makeText(this, String.valueOf(size.x), Toast.LENGTH_SHORT).show();*/
        if (colLenght > 1) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(colLenght, 1);
            rec.setLayoutManager(staggeredGridLayoutManager);
        } else
            GoToSmallScreenMode(rec);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    String createCategoryText(String st) {
        if (CN_Category.xmlMainCategoryTitle.equalsIgnoreCase(st)) {
            return getString(R.string.start_value);
        } else
            return st;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent itd = new Intent(this, about.class);
            itd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(itd);
            return true;
        } else if (id == R.id.goToUpCategory) {
            LNtNoteEditor.goUpFolder();
            loadItem(LNtNoteEditor.currentCategory);
          getSupportActionBar().setTitle(createCategoryText(LNtNoteEditor.currentCategory.title));

            return true;
        }
        else if (id == R.id.multiple_selection) {
            adapt.StartSelectionMode();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void UpdateIndexesOnXML()
    {
        int size = adapt.SubItems.size();
        UpdateIndexesOnXML(0,size);
    }

    void UpdateIndexesOnXML(int start_index, int end_index)
    {
        ArrayList<CN_Item> items = adapt.SubItems;
        if (items.size() < end_index)
            end_index = items.size();
        for (int i = start_index; i < end_index;i++)
        {
            CN_Item itmx = items.get(i);
            int flatIndex = itmx.getFlatIndex();
            if (flatIndex != -1)
            {
                itmx.setFlatIndex(i);
            }
        }
    }
}

class LNTItemsActionModeCallback implements ActionMode.Callback {
    LNTItemAdapter Adapter;


    public LNTItemsActionModeCallback(LNTItemAdapter adapter_) {
        Adapter = adapter_;
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.removeSelected) {
            Adapter.RemoveSelectedItems();
            return true;
        }
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode = null;
        Adapter.ExitSelectionMode();
    }
}


