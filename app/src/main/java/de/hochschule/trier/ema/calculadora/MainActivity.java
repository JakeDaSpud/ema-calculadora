package de.hochschule.trier.ema.calculadora;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.*;

public class MainActivity extends AppCompatActivity {

    private TextView screen;
    private Button
            btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
            btnAdd, btnSubtract, btnMultiply, btnDivide,
            btnEquals, btnClear, btnSignFlip, btnPeriod;

    private int[] groupedIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonPeriod};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);

        // -----

        screen = findViewById(R.id.calculatorScreen);

        // Set Buttons
        btnEquals = findViewById(R.id.buttonEquals);
        btnClear = findViewById(R.id.buttonClear);
        btnSignFlip = findViewById(R.id.buttonFlipSign);

        // Set Event Listeners
        for (int id : groupedIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = ((Button) v).getText().toString();
                    onCharacterPressed(text);
                }
            });
        }

        btnEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqualsPressed();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearPressed();
            }
        });

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClearHeld();
                return false;
            }
        });

        btnSignFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignFlipPressed();
            }
        });

        // -----

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void onEqualsPressed() {
        //term corresponds to the text entered in the display so far; e.g. "5+6".
        String term = screen.getText().toString();

        String result = "uh oh";

        //Here we now calculate the result
        try {
            if (term.length() < 3) {
                term = "0" + term;
            }

            Expression calc = new ExpressionBuilder(term)
                .variables("pi", "e")
                .build();

            result = Double.toString(calc.evaluate());
        } catch (Exception e) {
            result = "Error";
            Log.e(getClass().getName(), "catch block", e);
        }

        screen.setText(result);
    }

    protected void onClearPressed() {
        int len = screen.getText().length();

        if (len < 2) {
            onClearHeld();
        } else {
            screen.setText(screen.getText().subSequence(0, len-1));
        }
    }

    protected void onClearHeld() {
        screen.setText("0");
    }

    protected void onCharacterPressed(String numberOrOperator) {
        if (screen.getText().equals("0")) {
            screen.setText(numberOrOperator);
        } else {
            screen.setText(screen.getText() + numberOrOperator);
        }
    }

    protected void onSignFlipPressed() {
        // Add a '(' to the start, and a ")*-1" to the end lol
        screen.setText("(" + screen.getText() + ")*-1");
    }
}