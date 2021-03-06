package com.example.jp.chatjson;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Creamos las variables para despues llamarlas
    JSONObject json, clienteRX;

    private WebSocketClient mWebSocketClient;
    public static String nickname;

    //permiso de internet
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectWebSocket();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
                sendMessage();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override


    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override

    //metodo en el cual desde el navigation menu(barra lateral) hacemos que pulsando un boton,nos salga un AlertDialog
    //pidiendo que insertemos el nick que aparecera en el chat web
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        //si el id es igual al usuario mostramos un alert dialog

        if (id == R.id.usuario) {
            AlertDialog.Builder alert= new AlertDialog.Builder(this);
            final EditText user=new EditText(this);
            user.setSingleLine();
            user.setPadding(50,0,50,0);
            alert.setTitle("NickName");
            alert.setMessage("Introducir NickName");
            alert.setView(user);
            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    nickname=user.getText().toString();
                    if(nickname!=null){
                        connectWebSocket();
                    }
                }
            });
            alert.setNegativeButton("Cancelar",null);
            alert.create();
            alert.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
//meotod que se conecta al servidor mediante websockets y donde le pasamos la direccion  de nuestro chat web
    private void connectWebSocket() {

        URI uri;
        try {
            uri = new URI("ws://nodejs-json-pruebas--jpatricio.c9users.io:8081");//url del cht server
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Map<String, String> headers = new HashMap<>();

        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), headers, 0) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"id\":\"" + nickname + "\"}");//se envia el mensaje
            }

            @Override

            //metodo que recibe mensajes ,y ademas que si marcamos la opcion de privado solo lo recibe la persona que le indicamos
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.textmensaje);
                        String nick;
                        String msg;
                        String dest;
                        Boolean prv;
                        try {
                            clienteRX = new JSONObject(message);//creamos un objeto json para recibir id,mensaje,destino y si es privado
                            nick = clienteRX.getString("id");
                            msg = clienteRX.getString("mensaje");
                            dest = clienteRX.getString("destino");
                            prv = clienteRX.getBoolean("Privado");

                            //este if comprueba si el mensaje que esta recibiendo es o no privado
                            if (prv.equals(Boolean.TRUE)) {
                                if (dest.equals(nickname)) {
                                    textView.setText(textView.getText() + "\n" + nick + "\n" + msg);
                                }
                            } else
                                textView.setText("Mensaje Privado");

                        } catch (JSONException e) {

                            textView.setText(textView.getText() + "\n" + message);
                        }
                    }
                });
            }

            @Override
            //mtodo que cierra el websocket
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();

    }


    //metodo para enviar mensajes al chat web desde nuestro dispositivo,si el mensaje no es privado se podra ver en el chat
    // de lo contrario solo se enviara el mensaje a la persona indicada
    public void sendMessage() {
        EditText msg = (EditText) findViewById(R.id.mensaje);
        EditText destin = (EditText) findViewById(R.id.destino);
        CheckBox box = (CheckBox) findViewById(R.id.priv);
        String d, m;
        Boolean bl;
        d = destin.getText().toString();
        m = msg.getText().toString();

        //comprueba si es privado o no
        if (box.isChecked()) {
            bl = Boolean.TRUE;
        } else {
            bl = Boolean.FALSE;
        }
        json = new JSONObject();//creamos un objetos json el cual contendra id,mensaje,destino y si ses privado
        try {
            json.put("id", nickname);
            json.put("mensaje", m);
            json.put("destino", d);
            json.put("Privado", bl);
            msg.setText("");
            destin.setText("");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebSocketClient.send(json.toString());
    }

    //Metodo que contiene las instrucciones de uso
    public void instrucciones() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Conexión");
        build.setMessage(intruc);
        build.setPositiveButton("Aceptar", null);
        build.create();
        build.show();
    }
    public String intruc = "Escribe un nick para poder iniciar el chat.\n" +
            "-En el menu laterl puedes instroducir tu nick.\n" +
            "- Una vez introducido se conectará automaticamente.";
}
