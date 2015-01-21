package hr.fer.tel.ruazosa.projekt.plavikrizickruzic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class KrizicKruzic extends Activity implements AdapterView.OnItemClickListener{

    //Krizic_Kruzic
    Button play, history, exit,notifikacija;

    //Play
    private static final char KRIZIC = 'x';
    private static final char KRUZIC = 'o';
    private char[][] board;
    private char currentPlayerMark;

    //Saving data
    ArrayList<String> myStringArray1,myStringArray2,myStringArray3;
    ArrayAdapter<String> adapter;
    File file;

    //BLUETOTH
    private static final int SUCCESS_CONNECT = 0;
    private static final int MESSAGE_READ = 1;
    private static final int PRIMIO = 2;
    ArrayAdapter<String> listAdapter;
    ListView listview;
    Button connectnew;
    TextView textView;
    int i;

    BluetoothAdapter bAdapter;

    Set<BluetoothDevice> devicesArray;

    ArrayList<String> pariedDevices;
    ArrayList<BluetoothDevice> devices;

    IntentFilter filter;

    BroadcastReceiver receiver;
    android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case SUCCESS_CONNECT:
                    final ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                    connectedThread.start();
                    Toast.makeText(getApplicationContext(), "CONNECTED, najvjerojatnije notifikacija", Toast.LENGTH_SHORT).show();
                    Log.e("", "USAO SAM JEJEJE");
                    String s = "Successfully connected \n";
                    connectedThread.write(s.getBytes());



                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    listAdapter.add("Povezano");
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
                case PRIMIO:
                    Toast.makeText(getApplicationContext(),"MoLIM TE BUDI GOTOV", Toast.LENGTH_SHORT).show();
                    new ConnectedThread((BluetoothSocket) msg.obj).start();
            }
        }
    };


    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //BLUETOTHEND

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krizic_kruzic);
        init();
        makeBoard();

        initBT();


        if (bAdapter == null){
            Toast.makeText(getApplicationContext(), "IDIOT", Toast.LENGTH_LONG).show();
            finish();
        }
        startDiscovery();
        getPariedDevices();
        //printDevices();
        AcceptThread serverDevice = new AcceptThread();
        serverDevice.start();
    }

    private void init() {
        //SAVING DATA ELEMENTS
        file = new File(getFilesDir().toString()+"/history");
        myStringArray1 = new ArrayList<String>();
        myStringArray2 = new ArrayList<String>();
        myStringArray3 = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, myStringArray2);

        play = (Button) findViewById(R.id.btnPlay);
        history = (Button) findViewById(R.id.btnHistory);
        exit = (Button) findViewById(R.id.btnExit);
        notifikacija = (Button) findViewById(R.id.button);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_play);
            }
        });

        notifikacija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ajmoSlati();
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_history);
                ListView lsvHistory= (ListView) findViewById(R.id.lsvHistory);
                lsvHistory.setAdapter(adapter);
                getAllGames(file);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    private void makeBoard() {
        board = new char[3][3];
        currentPlayerMark = 'x';
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    public void kliknuto (View v) {
        int row = 0, col = 0;
        boolean wehaveawinner;
        boolean isFull = true;

        ImageView iv = (ImageView) v;
        LinearLayout parent = (LinearLayout) v.getParent();
        LinearLayout parentParent = (LinearLayout) parent.getParent();

        if(parent.getChildAt(0) == v) row = 0;
        if(parent.getChildAt(1) == v) row = 1;
        if(parent.getChildAt(2) == v) row = 2;

        if(parentParent.getChildAt(0) == parent) col = 0;
        if(parentParent.getChildAt(1) == parent) col = 1;
        if(parentParent.getChildAt(2) == parent) col = 2;

        /*IVAN KOMENTAR!!!*/
        // Za Domagoja i Mateja: OVDJE IMAS INFORMACIJU KOJI
        //JE STUPAC I KOJI RED ODABRAN: row i col, te osim toga
        //u "wehaveawinner" će se nalaziti ako je došlo do tog da
        //je netko pobijedio ili isFull ako je nerješeno.
        //Za Domagoja: potrebno je svaki taj potez na neki način trajno spremiti,
        //te oznaku je li korisnik pobijedio, izgubio itd i s kim.
        //Za Mateja: potrebno je svaki taj potez poslati drugom korisniku preko wifi-a ili
        //bluetootha i korisnik taj podatak mora primiti.

        /*DOMAGOJ KOMENTAR!!!*/
        // Za Ivana i Mateja: malo sam uredio kod i stavio u metode da ne izgleda natrpano sad kad sam nadodao i svoje. Ima dosta privremenih detalja kao button goBack na svakom layoutu da bi se app lakse testirala!
        //Za Mateja: trebala bi mi nekakva identifikacijska oznaka igraca da se i o tome vodi racuna <<"Ako se slučajno sretne suparnik s kojim prošla igra nije dovršena, treba
        //omogućiti nastavak igre">>.

        if(iv.getDrawable() == null) {
            if (board[row][col] == '-') {
                board[row][col] = currentPlayerMark;
            }
            if (currentPlayerMark == KRIZIC) {
            iv.setImageResource(R.drawable.krizic);
            currentPlayerMark = KRUZIC;
            } else {
                iv.setImageResource(R.drawable.kruzic);
                currentPlayerMark = KRIZIC;
            }
        }
        wehaveawinner = false;
        for (int i = 0; i < 3; i++) {
            if (checkRowCol(board[i][0], board[i][1], board[i][2])) {
                wehaveawinner = true;

            }
        }
        for (int i = 0; i < 3; i++) {
            if (checkRowCol(board[0][i], board[1][i], board[2][i])) {
                wehaveawinner = true;

            }
        }

        if ((checkRowCol(board[0][0], board[1][1], board[2][2])) || (checkRowCol(board[0][2], board[1][1], board[2][0]))) {
            wehaveawinner = true;

        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    isFull = false;
                }
            }
        }
        disable(wehaveawinner, isFull);

        addParametersToList(wehaveawinner, isFull, currentPlayerMark, row, col);
    }

    private boolean checkRowCol(char c1, char c2, char c3) {
        return ((c1 != '-') && (c1 == c2) && (c2 == c3));
    }

    private void disable(boolean wehaveawinner, boolean isFull) {
        if(wehaveawinner || isFull)
        {
            ImageView v1 = (ImageView) findViewById(R.id.imageView);
            ImageView v2 = (ImageView) findViewById(R.id.imageView2);
            ImageView v3 = (ImageView) findViewById(R.id.imageView3);
            ImageView v4 = (ImageView) findViewById(R.id.imageView4);
            ImageView v5 = (ImageView) findViewById(R.id.imageView5);
            ImageView v6 = (ImageView) findViewById(R.id.imageView6);
            ImageView v7 = (ImageView) findViewById(R.id.imageView7);
            ImageView v8 = (ImageView) findViewById(R.id.imageView8);
            ImageView v9 = (ImageView) findViewById(R.id.imageView9);
            v1.setEnabled(false);
            v2.setEnabled(false);
            v3.setEnabled(false);
            v4.setEnabled(false);
            v5.setEnabled(false);
            v6.setEnabled(false);
            v7.setEnabled(false);
            v8.setEnabled(false);
            v9.setEnabled(false);
        }
    }

    public void goBack(View v) {
        setContentView(R.layout.activity_krizic_kruzic);
        init();
        makeBoard();
    }

    //SAVING DATA FUNCTION
    private void addParametersToList(boolean wehaveawinner, boolean isFull, char currentPlayerMark, int row, int col) {
        if(wehaveawinner) {
            myStringArray1.add(String.valueOf(currentPlayerMark)+":"+(row*3+col+1));
            myStringArray1.add("STATUS:"+currentPlayerMark);
            SaveGame(file, myStringArray1);
        }
        else {
            if(isFull) {
                myStringArray1.add(String.valueOf(currentPlayerMark) + ":" + (row * 3 + col + 1));
                myStringArray1.add("STATUS:" + "N");
                SaveGame(file, myStringArray1);
            }
            else
                myStringArray1.add(String.valueOf(currentPlayerMark)+":"+(row*3+col+1));
        }
    }

    //SAVING DATA FUNCTION
    public void SaveGame(File file,ArrayList<String> list) {
        FileOutputStream outputStream;
        String str;
        if(!(file.exists())) {
            str="START "+1+":\n";
            try {
                if(file.createNewFile())
                    Log.w("FILE:","there is no history so new file was created!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                for (String string : list) {
                    str += string + "\n";
                }
                str+="END\n";
                outputStream.write(str.getBytes());
                outputStream.flush();
                outputStream.close();
                myStringArray1.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            int i=0;
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("START"))
                        i++;
                }
                br.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            str="START "+(i+1)+":\n";
            try {
                outputStream = openFileOutput(file.getName(), Context.MODE_APPEND);
                for (String string : list) {
                    str += string + "\n";
                }
                str+="END\n";
                outputStream.write(str.getBytes());
                outputStream.flush();
                outputStream.close();
                myStringArray1.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //SAVING DATA FUNCTION
    public void getAllGames(File file) {
        myStringArray2.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String sve="";
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains("END")) {
                    sve+=line.trim();
                    myStringArray2.add(sve);
                    sve = "";
                }
                else
                    sve+=line+" -> ";
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    //SAVING DATA FUNCTION
    public void deleteFile(View v) {
        if(file.exists())
            if(file.delete()) {
                Log.w("FILE:", "file was deleted!");
                myStringArray2.clear();
                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, myStringArray2);
                ListView lsvHistory= (ListView) findViewById(R.id.lsvHistory);
                lsvHistory.setAdapter(adapter);
            }
            else
                Log.w("FILE:","file was not deleted! (something went wrong)");
        else
            Log.w("FILE:","file does not exist!");
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.krizic_kruzic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    //Notofikacija
    /*Dio u komentarima bi trebao aktivirati novi activity Result.class
    * gdje bi se odvijala igra, no kako se Igra odvija u novom layoutu u istom activityu
    * ne znam kako točno se tako prebaciti, probat ću kasnije ako stignem, no slobno netko
     * od vas neka to napravi*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void ajmoSlati(){
        int ID= 001;

        //Intent otvori = new Intent(this, Result.class);
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        //stackBuilder.addParentStack(Result.class);
        //stackBuilder.addNextIntent(otvori);
        //PendingIntent otvorisad = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder notifikacija = new Notification.Builder(this);
        notifikacija.setAutoCancel(true);
        notifikacija.setSmallIcon(R.drawable.krizic);
        notifikacija.setContentTitle("Veza Nađena");
        notifikacija.setContentText("Imate novog suparnika");

        //notifikacija.setContentIntent(otvorisad);


        Notification jesmo = notifikacija.build();
        NotificationManager mangaer = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mangaer.notify(ID,jesmo );
    }


    //OVO SVE BI KAO TREBALO ZA BLUETOTH

    private void startDiscovery() {
        bAdapter.cancelDiscovery();
        bAdapter.startDiscovery();

    }

    private void TrunonBT() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPariedDevices() {
        devicesArray = bAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                devices.add(device);
                pariedDevices.add(device.getName());
                listAdapter.add(device.getName()+"( "+"Paired"+" )"+ "\n" + device.getAddress());
            }
        }
    }


    private void initBT(){
        connectnew = (Button)findViewById(R.id.button);
        listview = (ListView)findViewById(R.id.listView);
        listview.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        listview.setAdapter(listAdapter);
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        pariedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        TrunonBT();

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        receiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s= "";

                    for (int a=0; a<pariedDevices.size(); a++ ){
                        if(device.getName().equals(pariedDevices.get(a))){
                            //aa

                            s ="Paired";
                            break;
                        }
                    }

                    listAdapter.add(device.getName()+"( "+s+" )"+ "\n" + device.getAddress());
                }

                else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    listAdapter.add(pariedDevices.get(0));

                }

                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    //HAS CODE IN IT


                }

                else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if (bAdapter.getState() == bAdapter.STATE_OFF){
                        TrunonBT();

                    }
                }

            }
        };

        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_CANCELED){
            Toast.makeText(getApplicationContext(),"AGAIN IDIOT", Toast.LENGTH_SHORT);
            finish();
        }
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (bAdapter.isDiscovering()){
            bAdapter.cancelDiscovery();
        }

        if(listAdapter.getItem(position).contains("Paired")){

            BluetoothDevice slectedDevice = devices.get(position);
            ConnectThread connect = new ConnectThread(slectedDevice);
            connect.start();


        }
        else{
            Toast.makeText(getApplicationContext(),"device is NOT paried", Toast.LENGTH_SHORT).show();
        }

    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bAdapter.listenUsingRfcommWithServiceRecord("Vezaje", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    mHandler.obtainMessage(PRIMIO, socket).sendToTarget();
                    try {
                        mmServerSocket.close();
                    }catch (IOException e){

                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }


        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    buffer = new byte[1];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer, 0, 1);

                    final int input = buffer[0];

                    Log.d("PINGPONG", "Received message" + input);

                    Thread.sleep(1000);

                    final byte outputBuffer[] = new byte[1];
                    final int out = input + 1;
                    outputBuffer[0] = (byte) out;

                    write(outputBuffer);

                    // Send the obtained bytes to the UI activity
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                Log.d("PINGPONG", "Writing " + bytes);
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    //OVDJE JE KRAJ


}