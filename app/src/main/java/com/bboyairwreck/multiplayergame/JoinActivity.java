package com.bboyairwreck.multiplayergame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {
    public static final String TAG = JoinActivity.class.getSimpleName();
    Firebase gameFirebase;
    private String username;
    private String gameID;
    private Firebase selfPlayerFirebase;
    private Map<String, String> playersMap;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        QwicklyApplication app = QwicklyApplication.getInstance();

        this.username = app.getUsername();
        playersMap = new HashMap<>();

        Intent launchedMe = getIntent();
        String playerType = launchedMe.getStringExtra(Constants.PLAYER_TYPE);

        this.gameID = "FOOD";   /// TODO Change to be dynamic
        app.setGameFirebase(gameID);
        this.gameFirebase = app.getGameFirebase();

        ListView lvPlayers = (ListView) findViewById(R.id.lvPlayers);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());
        lvPlayers.setAdapter(adapter);

        if (playerType.equals(Constants.PLAYER_TYPE_JOINER)) {
            // Player is a joiner
            showJoinerLayout();
        } else {
            // Player is a creator
            showCreatorLayout();
            loadCurrentPlayers();
            setUpStartButton();
        }
    }

    private void loadCurrentPlayers() {
        // Grab current players in game (should be only yourself)
        gameFirebase.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check for players and find who is me vs others
                for (DataSnapshot playerData : dataSnapshot.getChildren()) {
                    String playerID = playerData.getKey();
                    String playerUsername = playerData.getValue().toString();
                    if (playerID.equals(getSelfPlayerID())) {
                        Log.i(TAG, "Player is me");
                    } else {
                        Log.i(TAG, "Player " + playerID + " - " + playerUsername);
                    }
                    addPlayerToList(playerData);
                }

                // Listen for new players
                checkForNewPlayers();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void checkForNewPlayers() {
        gameFirebase.child("players").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot playerData, String s) {
                // Check for new players
                if (isNewPlayer(playerData)) {
                    Log.i(TAG, "Player has been added - " + playerData.getValue());
                    addPlayerToList(playerData);
                } else {
                    Log.i(TAG, "Not a new player");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot playerData, String s) {
                String playerID = playerData.getKey();
                String playerUsername = playerData.getValue().toString();

                // Todo update username in list view if they changed it
            }

            @Override
            public void onChildRemoved(DataSnapshot playerData) {
                removePlayerToList(playerData);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addPlayerToList(DataSnapshot playerData) {
        String playerID = playerData.getKey();
        String playerUsername = playerData.getValue().toString();

        playersMap.put(playerID, playerUsername);

        if (playerID.equals(getSelfPlayerID())) {
            playerUsername += " (me)";
        }


        adapter.add(playerUsername);
        adapter.notifyDataSetChanged();
    }

    private void removePlayerToList(DataSnapshot playerData) {
        String playerID = playerData.getKey();
        String playerUsername = playerData.getValue().toString();

        playersMap.remove(playerID);
        adapter.remove(playerUsername);
        adapter.notifyDataSetChanged();
    }

    private boolean isNewPlayer(DataSnapshot playerData) {
        return playersMap.get(playerData.getKey()) == null;
    }

    private String getSelfPlayerID() {
        return selfPlayerFirebase.getKey();
    }

    private void showJoinerLayout() {
        // Joiner
        Log.i(TAG, "Player is a joiner");
        TextView tvGameCodeLabel = (TextView) findViewById(R.id.tvGameCodeLabel);
        final TextView tvGameCode = (TextView) findViewById(R.id.tvGameCode);
        Button btnStartGame = (Button) findViewById(R.id.btnStartGame);
        final Button btnJoinGame = (Button) findViewById(R.id.btnJoinGame);
        final EditText etGameCode = (EditText) findViewById(R.id.etGameCode);
        final TextView tvWaiting = (TextView) findViewById(R.id.tvWaiting);

        tvGameCode.setVisibility(View.GONE);
        tvGameCodeLabel.setVisibility(View.GONE);
        tvWaiting.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.GONE);
        btnJoinGame.setVisibility(View.VISIBLE);
        etGameCode.setVisibility(View.VISIBLE);

        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO check if game exists
                final String gameCode = etGameCode.getText().toString();

                QwicklyApplication.getInstance().getQFirebase().child("games").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(gameCode)) {
                            Log.i(TAG, "Game exists w/ ID : " + gameCode);

                            tvGameCode.setText(gameID);
                            tvGameCode.setVisibility(View.VISIBLE);
                            btnJoinGame.setVisibility(View.GONE);
                            etGameCode.setVisibility(View.GONE);
                            selfPlayerFirebase = gameFirebase.child("players").push();
                            selfPlayerFirebase.setValue(username);

                            tvWaiting.setVisibility(View.VISIBLE);

                            loadCurrentPlayers();
                        } else {
                            Log.i(TAG, "Game DOES NOT exist w/ ID : " + gameCode);
                            Toast.makeText(JoinActivity.this, "Sorry, that game doesn't exist :'(", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {}
                });



            }
        });
    }

    private void showCreatorLayout() {
        Log.i(TAG, "Player is a creator");
        TextView tvGameCode = (TextView) findViewById(R.id.tvGameCode);
        tvGameCode.setText(gameID);
        selfPlayerFirebase = gameFirebase.child("players").push();
        selfPlayerFirebase.setValue(username);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gameFirebase != null && selfPlayerFirebase != null) {
            gameFirebase.child("players").child(getSelfPlayerID()).removeValue();
        }
    }

    private void setUpStartButton() {
        Button btnStartGame = (Button) findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameFirebase.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Check if there are more than 1 players
                        if (dataSnapshot.getChildrenCount() >= 2) {
                            // TODO Start Game
                        } else {
                            Toast.makeText(JoinActivity.this, "Need at least 2 to play", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });
    }
}
