package hr.fer.tel.ruazosa.projekt.plavikrizickruzic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class KrizicKruzic extends Activity {

    //Krizic_Kruzic
    Button play, history, exit;

    //Play
    private static final char KRIZIC = 'x';
    private static final char KRUZIC = 'o';
    private char[][] board;
    private char currentPlayerMark;

    //Saving data
    ArrayList<String> myStringArray1,myStringArray2,myStringArray3;
    ArrayAdapter<String> adapter;
    File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krizic_kruzic);
        init();
        makeBoard();
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

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_play);
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
}