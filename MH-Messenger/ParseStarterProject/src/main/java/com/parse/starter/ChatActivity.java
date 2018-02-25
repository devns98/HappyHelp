package com.parse.starter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ChatActivity extends AppCompatActivity {

    String activeUser = "";

    ArrayList<String> messages = new ArrayList<>();

    ArrayAdapter arrayAdapter;

    public void sendChat(View view) {  //over here we are trying to send the message to the backend server

        final EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

        ParseObject message = new ParseObject("Message");

        final String messageContent = chatEditText.getText().toString();

        message.put("sender", ParseUser.getCurrentUser().getUsername()); //here we are giving the sender user name
        message.put("recipient", activeUser); //and the recipient data
        message.put("message", messageContent); //and the message we sent

        chatEditText.setText("");

        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {

                    messages.add(messageContent);

                    arrayAdapter.notifyDataSetChanged(); //this updates the message as soon as the data set is changed

                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        activeUser = intent.getStringExtra("username");

        setTitle("Chat with " + activeUser);

        ListView chatListView = (ListView) findViewById(R.id.chatListView); //finds the list view

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);

        chatListView.setAdapter(arrayAdapter);

        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message"); //we are quering the message class which we created above

        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername()); //sends the message from the sender1 to sender2
        query1.whereEqualTo("recipient", activeUser);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message"); //sends the message from the sender2 to sender1

        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender", activeUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>(); //I combine the two queries to form one main query

        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);  //or query to see if both are true

        query.orderByAscending("createdAt");  //we use this to sort which message was sent when. thus it arranges the messages correctly

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {

                    if (objects.size() > 0) {

                        messages.clear();

                        for (ParseObject message : objects) {

                            String messageContent = message.getString("message");

                            if (!message.getString("sender").equals(ParseUser.getCurrentUser().getUsername())) { //checking if the current user is not the user

                                messageContent = "> " + messageContent; //use the append sign to distinguish the users

                            }

                            Log.i("Info", messageContent);

                            messages.add(messageContent);

                        }

                        arrayAdapter.notifyDataSetChanged();

                    }

                }

            }
        });




    }
}
