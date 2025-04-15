package com.example.recuproverificatpsit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.recuproverificatpsit.R;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Dichiarazione degli elementi UI
    private EditText editTextObiettivo;
    private Button buttonConfermaObiettivo;
    private Button buttonCorsa, buttonCiclismo, buttonPalestra, buttonRiposo;
    private EditText editTextDurata, editTextCalorie;
    private TextView textViewRiepilogo;

    // Variabili per i dati
    private int obiettivoCalorico = 0;
    private final HashMap<String, Integer[]> attivita = new HashMap<>();
    private int totaleCalorieBruciate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Collegamento agli elementi UI
        editTextObiettivo = findViewById(R.id.editTextObiettivo);
        buttonConfermaObiettivo = findViewById(R.id.buttonConfermaObiettivo);
        buttonCorsa = findViewById(R.id.buttonCorsa);
        buttonCiclismo = findViewById(R.id.buttonCiclismo);
        buttonPalestra = findViewById(R.id.buttonPalestra);
        buttonRiposo = findViewById(R.id.buttonRiposo);
        editTextDurata = findViewById(R.id.editTextDurata);
        editTextCalorie = findViewById(R.id.editTextCalorie);
        textViewRiepilogo = findViewById(R.id.textViewRiepilogo);

        // Inizializza la mappa delle attività
        attivita.put("Corsa", new Integer[]{0, 0});
        attivita.put("Ciclismo", new Integer[]{0, 0});
        attivita.put("Palestra", new Integer[]{0, 0});
        attivita.put("Riposo", new Integer[]{0, 0});

        // Disabilita i pulsanti delle attività e nascondi i campi durata/calorie inizialmente
        setAttivitaButtonsEnabled(false);
        editTextDurata.setVisibility(View.GONE);
        editTextCalorie.setVisibility(View.GONE);

        // Listener per confermare l'obiettivo sia col pulsante che col tasto "Enter"
        buttonConfermaObiettivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confermaObiettivo();
            }
        });
        editTextObiettivo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    buttonConfermaObiettivo.performClick();
                    return true;
                }
                return false;
            }
        });

        // Aggiunge listener per l'EditText delle calorie
        // Se l'utente preme "Enter", il campo perde il focus per attivare il listener già impostato.
        editTextCalorie.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    editTextCalorie.clearFocus();
                    return true;
                }
                return false;
            }
        });

        // Listener generico per tutti i pulsanti delle attività
        View.OnClickListener attivitaListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rendi visibili i campi durata e calorie
                editTextDurata.setVisibility(View.VISIBLE);
                editTextCalorie.setVisibility(View.VISIBLE);

                // Ottieni il nome dell'attività dal pulsante cliccato
                Button clickedButton = (Button) v;
                final String nomeAttivita = clickedButton.getText().toString();

                // Imposta il listener per il campo calorie
                editTextCalorie.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            String durataStr = editTextDurata.getText().toString();
                            String calorieStr = editTextCalorie.getText().toString();

                            if (!TextUtils.isEmpty(durataStr) && !TextUtils.isEmpty(calorieStr)) {
                                try {
                                    int durata = Integer.parseInt(durataStr);
                                    int calorie = Integer.parseInt(calorieStr);

                                    // Verifica vincoli
                                    if (calorie > 300) {
                                        mostraErrore("Le calorie per attività non possono superare 300 kcal.");
                                        return;
                                    }
                                    if (durata > 120) {
                                        mostraErrore("La durata dell'attività non può superare i 120 minuti.");
                                        return;
                                    }

                                    int nuoveCalorieBruciate = totaleCalorieBruciate + calorie;
                                    if (nuoveCalorieBruciate < 0) {
                                        mostraErrore("Le calorie bruciate nette non possono essere negative.");
                                        return;
                                    }

                                    if (obiettivoCalorico > 0 && (double) calorie / obiettivoCalorico > 0.7) {
                                        mostraErrore("Nessuna categoria può superare il 70% dell'obiettivo.");
                                        return;
                                    }

                                    // Aggiorna i totali per l'attività
                                    Integer[] datiAttivita = attivita.get(nomeAttivita);
                                    if (datiAttivita != null) {
                                        datiAttivita[0] += durata;
                                        datiAttivita[1] += calorie;
                                        totaleCalorieBruciate += calorie;
                                        attivita.put(nomeAttivita, datiAttivita);
                                        aggiornaRiepilogo();
                                        // Resetta e nascondi i campi di input
                                        editTextDurata.getText().clear();
                                        editTextCalorie.getText().clear();
                                        editTextDurata.setVisibility(View.GONE);
                                        editTextCalorie.setVisibility(View.GONE);
                                    }

                                } catch (NumberFormatException e) {
                                    Toast.makeText(MainActivity.this, "Inserisci numeri validi per durata e calorie.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
            }
        };

        // Assegna il listener a ciascun pulsante attività
        buttonCorsa.setOnClickListener(attivitaListener);
        buttonCiclismo.setOnClickListener(attivitaListener);
        buttonPalestra.setOnClickListener(attivitaListener);
        buttonRiposo.setOnClickListener(attivitaListener);
    }

    // Metodo per confermare l'obiettivo calorico
    private void confermaObiettivo() {
        String obiettivoStr = editTextObiettivo.getText().toString();
        if (!TextUtils.isEmpty(obiettivoStr)) {
            try {
                obiettivoCalorico = Integer.parseInt(obiettivoStr);
                editTextObiettivo.setEnabled(false);
                buttonConfermaObiettivo.setEnabled(false);
                setAttivitaButtonsEnabled(true);
                aggiornaRiepilogo();
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Inserisci un numero valido per l'obiettivo.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Inserisci il tuo obiettivo calorico giornaliero.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo per aggiornare la TextView di riepilogo
    private void aggiornaRiepilogo() {
        StringBuilder riepilogo = new StringBuilder("Obiettivo calorico giornaliero: ").append(obiettivoCalorico).append(" kcal\n\n");
        for (java.util.Map.Entry<String, Integer[]> entry : attivita.entrySet()) {
            String nome = entry.getKey();
            Integer[] dati = entry.getValue();
            int minuti = dati[0];
            int calorie = dati[1];
            riepilogo.append(nome).append(": ").append(minuti).append(" min, ").append(calorie).append(" kcal\n");
        }
        riepilogo.append("\nTotale calorie bruciate nette: ").append(totaleCalorieBruciate).append(" kcal\n\n");
        if (totaleCalorieBruciate > 0 && obiettivoCalorico > 0) {
            riepilogo.append("Percentuali:\n");
            for (java.util.Map.Entry<String, Integer[]> entry : attivita.entrySet()) {
                String nome = entry.getKey();
                Integer[] dati = entry.getValue();
                int calorie = dati[1];
                double percentuale = (double) calorie / totaleCalorieBruciate * 100;
                riepilogo.append(nome).append(": ").append(String.format(Locale.getDefault(), "%.2f", percentuale)).append("%\n");
            }
            double percentualeRaggiunta = (double) totaleCalorieBruciate / obiettivoCalorico * 100;
            riepilogo.append("\nHai raggiunto il ").append(String.format(Locale.getDefault(), "%.2f", percentualeRaggiunta)).append("% dell'obiettivo.");
        } else if (obiettivoCalorico > 0) {
            riepilogo.append("\nHai raggiunto lo 0% dell'obiettivo.");
        } else {
            riepilogo.append("\nDefinisci prima l'obiettivo calorico.");
        }
        textViewRiepilogo.setText(riepilogo.toString());
    }

    // Metodo per mostrare un errore e disabilitare i pulsanti delle attività
    private void mostraErrore(String messaggio) {
        Toast.makeText(this, messaggio, Toast.LENGTH_LONG).show();
        //setAttivitaButtonsEnabled(false);
    }


    // Metodo per abilitare/disabilitare i pulsanti delle attività
    private void setAttivitaButtonsEnabled(boolean enabled) {
        buttonCorsa.setEnabled(enabled);
        buttonCiclismo.setEnabled(enabled);
        buttonPalestra.setEnabled(enabled);
        buttonRiposo.setEnabled(enabled);
    }
}
