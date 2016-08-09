package edu.utsa.cs.kbaldor.examplesecuremessagingapp.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.R;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

/**
 * Created by kbaldor on 8/4/16.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    // This is named 'reference' as a reminder that the
    // data is allocated and managed elsewhere
    List<Message> dataSetReference;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public MessageView messageView;
        public ViewHolder(MessageView v) {
            super(v);
            messageView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageAdapter(List<Message> dataset) {
        dataSetReference = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder((MessageView)v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyMessageClicked(((MessageView)v).getMessage());
            }
        });
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.messageView.setMessage(dataSetReference.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSetReference.size();
    }

    public interface MessageClickedListener {
        void messageClicked(Message message);
    }
    ArrayList<MessageClickedListener> myListeners = new ArrayList<>();
    public void registerMessageClickedListener(MessageClickedListener listener){
        myListeners.add(listener);
    }
    public void unregisterMessageClickedListener(MessageClickedListener listener){
        myListeners.remove(listener);
    }
    void notifyMessageClicked(Message message){
        for(MessageClickedListener listener: myListeners){
            listener.messageClicked(message);
        }
    }

}

