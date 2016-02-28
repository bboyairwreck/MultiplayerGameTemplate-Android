package com.bboyairwreck.multiplayergame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        Button btnCreate = (Button) findViewById(R.id.btnCreate);
        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set username as player 1 for sender
                ((MultiplayerApplication) getApplication()).setUsername("player1");

                Intent collabJoinIntent = new Intent(ModeActivity.this, JoinActivity.class);
                collabJoinIntent.putExtra(Constants.PLAYER_TYPE, Constants.PLAYER_TYPE_CREATOR);
                startActivity(collabJoinIntent);
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set username as player 2 for receiver
                ((MultiplayerApplication) getApplication()).setUsername("player2");

                Intent collabJoinIntent = new Intent(ModeActivity.this, JoinActivity.class);
                collabJoinIntent.putExtra(Constants.PLAYER_TYPE, Constants.PLAYER_TYPE_JOINER);
                startActivity(collabJoinIntent);
            }
        });
    }
}
