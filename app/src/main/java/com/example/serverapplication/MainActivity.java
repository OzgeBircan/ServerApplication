package com.example.serverapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ServerApp";
    private TextView textView;
    private ServerSocket serverSocket;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int receivedNumber;
    private boolean showUI;
    private Socket clientSocket;
    private OutputStream out;

    private ServerReceiver serverReceiver;
    private boolean isReceiverRegistered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.displayNumber);
        Button respondButton = findViewById(R.id.respondButton);

        respondButton.setOnClickListener(v -> new Thread(this::respondToClient).start());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("number")) {
            receivedNumber = intent.getIntExtra("number", 0);
            showUI = intent.getBooleanExtra("showUI", true);
            handleReceivedNumber(receivedNumber, showUI);

            Log.d(TAG, "MainActivity started with number: " + receivedNumber + " and showUI: " + showUI);

            if (showUI) {
                textView.setText(String.valueOf(receivedNumber));
            } else {
                // Arka planda cevap ver
                new Thread(() -> {
                    try {
                        Socket socket = new Socket("127.0.0.1", 5000);
                        OutputStream out = socket.getOutputStream();
                        out.write((receivedNumber + 1 + "\n").getBytes());
                        out.flush();
                        socket.close();
                        finish(); // İşlem tamamlandığında aktiviteyi kapat
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        new Thread(new ServerThread()).start();

        serverReceiver = new ServerReceiver();
        IntentFilter filter = new IntentFilter(ServerReceiver.ACTION_START_SERVER);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(serverReceiver, filter, Context.RECEIVER_EXPORTED);
        isReceiverRegistered = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isReceiverRegistered) {
            unregisterReceiver(serverReceiver);
            isReceiverRegistered = false;
        }
    }


    private class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(5000);
                handler.post(() -> textView.setText("Server started on port 5000"));
                Log.d(TAG, "Server started on port 5000");

                while (true) {
                    Log.d(TAG, "Waiting for client connection...");
                    clientSocket = serverSocket.accept();
                    Log.d(TAG, "Client connected");
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error in ServerThread", e);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = clientSocket.getOutputStream();
                String receivedMessage = in.readLine();
                Log.d(TAG, "Received message: " + receivedMessage);
                String[] parts = receivedMessage.split(":");
                receivedNumber = Integer.parseInt(parts[0]);
                showUI = Boolean.parseBoolean(parts[1]);
                handleReceivedNumber(receivedNumber, showUI);

                /*
                handler.post(() -> {
                    if (showUI) {
                        Log.d(TAG, "Showing UI for received number: " + receivedNumber);
                        Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                        intent.putExtra("number", receivedNumber);
                        ServerActivity.setSocket(clientSocket, out); // Set socket and output stream
                        startActivity(intent);
                    } else {
                        new Thread(() -> {
                            int responseNumber = receivedNumber + 1;
                            try {
                                out.write((responseNumber + "\n").getBytes());
                                out.flush();
                                clientSocket.close();
                                Log.d(TAG, "Sent response: " + responseNumber);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error sending response", e);
                            }
                        }).start();
                    }
                });
                */

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error in ClientHandler", e);
            }
        }
    }

    private void handleReceivedNumber(int number, boolean showUI) {
        handler.post(() -> {
            if (showUI) {
                Log.d(TAG, "Showing UI for received number: " + number);
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                intent.putExtra("number", number);
                ServerActivity.setSocket(clientSocket, out); // Set socket and output stream
                startActivity(intent);
            } else {
                new Thread(() -> {
                    int responseNumber = number + 1;
                    try {
                        if (out == null){
                            Log.d(TAG, "handleReceivedNumber: out nulmus");
                        }
                        out.write((responseNumber + "\n").getBytes());
                        out.flush();
                        clientSocket.close();
                        Log.d(TAG, "Sent response: " + responseNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error sending response", e);
                    }
                }).start();
            }
        });
    }

    private void respondToClient() {
        new Thread(() -> {
            int responseNumber = receivedNumber + 1;
            try {
                out.write((responseNumber + "\n").getBytes());
                out.flush();
                clientSocket.close();
                Log.d(TAG, "Sent response: " + responseNumber);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error sending response", e);
            }
        }).start();
    }
}
