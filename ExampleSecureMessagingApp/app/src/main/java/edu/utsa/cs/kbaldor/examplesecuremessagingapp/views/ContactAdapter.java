package edu.utsa.cs.kbaldor.examplesecuremessagingapp.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.R;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Contact;

/**
 * Created by kbaldor on 8/4/16.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    // This is named 'reference' as a reminder that the
    // data is allocated and managed elsewhere
    List<Contact> dataSetReference;
    boolean chooser;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public Contact contact;
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContactAdapter(List<Contact> dataset, boolean chooser) {
        dataSetReference = dataset;
        this.chooser = chooser;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        int layout = R.layout.contact_item;
        if(chooser) layout = R.layout.contact_item_choose;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        final ViewHolder vh = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyContactClicked(vh.contact);
            }
        });
        if(!chooser) {
            ((ImageButton) v.findViewById(R.id.contact_item_edit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyEditClicked(vh.contact);
                }
            });
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Contact contact = dataSetReference.get(position);
        holder.contact = contact;
        if(contact.logged_in){
            ((ImageView)holder.view.findViewById(R.id.contact_item_status)).setImageResource(R.drawable.ic_check_box_black_24dp);
        } else {
            ((ImageView)holder.view.findViewById(R.id.contact_item_status)).setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
        }
        ((TextView)holder.view.findViewById(R.id.contact_item_username)).setText(contact.name);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSetReference.size();
    }

    public interface ContactClickedListener {
        void contactClicked(Contact contact);
    }
    ArrayList<ContactClickedListener> myClickListeners = new ArrayList<>();
    public void registerContactClickedListener(ContactClickedListener listener){
        myClickListeners.add(listener);
    }
    public void unregisterContactClickedListener(ContactClickedListener listener){
        myClickListeners.remove(listener);
    }
    void notifyContactClicked(Contact contact){
        for(ContactClickedListener listener: myClickListeners){
            listener.contactClicked(contact);
        }
    }

    public interface EditClickedListener {
        void contactEditClicked(Contact contact);
    }
    ArrayList<EditClickedListener> myEditListeners = new ArrayList<>();
    public void registerEditClickedListener(EditClickedListener listener){
        myEditListeners.add(listener);
    }
    public void unregisterEditClickedListener(EditClickedListener listener){
        myEditListeners.remove(listener);
    }
    void notifyEditClicked(Contact contact){
        for(EditClickedListener listener: myEditListeners){
            listener.contactEditClicked(contact);
        }
    }


}

