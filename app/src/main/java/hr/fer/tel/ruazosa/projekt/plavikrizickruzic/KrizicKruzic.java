package hr.fer.tel.ruazosa.projekt.plavikrizickruzic;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class KrizicKruzic extends Activity {

    private static final char KRIZIC = 'x';
    private static final char KRUZIC = 'o';
    private char[][] board;
    private char currentPlayerMark;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        board = new char[3][3];
        currentPlayerMark = 'x';
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        /* Za Domagoja i Mateja: OVDJE IMAS INFORMACIJU KOJI
        JE STUPAC I KOJI RED ODABRAN: row i col, te osim toga
        u "wehaveawinner" će se nalaziti ako je došlo do tog da
        je netko pobijedio ili isFull ako je nerješeno.
        Za Domagoja: potrebno je svaki taj potez na neki način trajno spremiti,
        te oznaku je li korisnik pobijedio, izgubio itd i s kim.
        Za Mateja: potrebno je svaki taj potez poslati drugom korisniku preko wifi-a ili
        bluetootha i korisnik taj podatak mora primiti.*/

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
            if (checkRowCol(board[i][0], board[i][1], board[i][2]) == true) {
                wehaveawinner = true;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (checkRowCol(board[0][i], board[1][i], board[2][i]) == true) {
                wehaveawinner = true;
            }
        }

        if ((checkRowCol(board[0][0], board[1][1], board[2][2]) == true) || (checkRowCol(board[0][2], board[1][1], board[2][0]) == true)) {
            wehaveawinner = true;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    isFull = false;
                }
            }
        }

    }

    private boolean checkRowCol(char c1, char c2, char c3) {
        return ((c1 != '-') && (c1 == c2) && (c2 == c3));
    }
}