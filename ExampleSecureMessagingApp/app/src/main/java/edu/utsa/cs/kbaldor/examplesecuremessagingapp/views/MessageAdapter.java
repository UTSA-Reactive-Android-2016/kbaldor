package edu.utsa.cs.kbaldor.examplesecuremessagingapp.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.R;
import edu.utsa.cs.kbaldor.examplesecuremessagingapp.models.Message;

/**
 * Created by kbaldor on 8/4/16.
 */
public class MessageAdapter extends ArrayAdapter<Message> {
    final LayoutInflater layoutInflater;

    public MessageAdapter(Context context, LayoutInflater layoutInflater){
        super(context,R.layout.message_view);
        this.layoutInflater = layoutInflater;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MessageView view = (MessageView) layoutInflater.inflate(R.layout.message_view, parent, false);

        view.setMessage(getItem(position));
        return view;
    }
}