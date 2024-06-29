package com.example.serverapplication;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.io.OutputStream;
import java.net.Socket;

public class ServerActivity extends AppCompatActivity {

    private static final String TAG = "ServerActivity";
    private TextView numberTextView;
    private Button respondButton;
    private int receivedNumber;
    private static Socket clientSocket;
    private static OutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        numberTextView = findViewById(R.id.numberTextView);
        respondButton = findViewById(R.id.respond_btn);

        receivedNumber = getIntent().getIntExtra("number", 0);
        numberTextView.setText(String.valueOf(receivedNumber));

        respondButton.setOnClickListener(v -> new Thread(this::respondToClient).start());
    }

    public static void setSocket(Socket socket, OutputStream outputStream) {
        clientSocket = socket;
        out = outputStream;
    }

    private void respondToClient() {
        int responseNumber = receivedNumber + 1;
        try {
            if (clientSocket != null && clientSocket.isConnected() && out != null) {
                out.write((responseNumber + "\n").getBytes());
                out.flush();
                clientSocket.close();
                Log.d(TAG, "Sent response: " + responseNumber);
            } else {
                Log.e(TAG, "Client socket is not connected or output stream is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error sending response", e);
        }
    }
}
