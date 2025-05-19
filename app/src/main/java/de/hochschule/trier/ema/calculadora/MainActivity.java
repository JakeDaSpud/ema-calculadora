package de.hochschule.trier.ema.calculadora;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.*;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MainActivity extends AppCompatActivity implements SensorEventListener, PopupMenu.OnMenuItemClickListener {

    private TextView screen;
    private Button
            btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
            btnAdd, btnSubtract, btnMultiply, btnDivide,
            btnEquals, btnClear, btnSignFlip, btnResize, btnPeriod;

    private int[] groupedIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonPeriod};

    // Accelerometer stuff
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    final private float SHAKE_THRESHOLD = 0.01f;

    private static final String DISPLAY_TEXT = "display_text";
    private static final String BUTTON_FONTSIZE = "button_size";
    int currentButtonSize = 12;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Obviously save the screen text
        outState.putString(DISPLAY_TEXT, screen.getText().toString());
        // Save the button font size
        outState.putInt(BUTTON_FONTSIZE, (int) currentButtonSize);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String restoredText = savedInstanceState.getString(DISPLAY_TEXT);
        int restoredSize = savedInstanceState.getInt(BUTTON_FONTSIZE);

        if (restoredText != null) {
            screen.setText(restoredText);
        }

        currentButtonSize = restoredSize;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);

        // -----

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(
                this,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_UI  // <-- Lower sensitivity (slower updates)
        );

        screen = findViewById(R.id.calculatorScreen);

        // Set Buttons
        btnEquals = findViewById(R.id.buttonEquals);
        btnClear = findViewById(R.id.buttonClear);
        btnSignFlip = findViewById(R.id.buttonFlipSign);
        btnResize = findViewById(R.id.buttonResize);

        setTextButtonSize(currentButtonSize);

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

    public void showResizePopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.size_menu, popup.getMenu());

        popup.show();
    }

    // Setting actual text size
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.size_small) {
            setTextButtonSize(12);
            return true;
        }

        else if (item.getItemId() == R.id.size_middle) {
            setTextButtonSize(24);
            return true;
        }

        else if (item.getItemId() == R.id.size_large) {
            setTextButtonSize(32);
            return true;
        }

        return false;
    }

    protected void setTextButtonSize(int newTextSize) {
        for (int id : groupedIds) {
            Button button = findViewById(id);
            button.setTextSize(TypedValue.COMPLEX_UNIT_PT, newTextSize);
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            float acceleration = (float) Math.sqrt(x*x + y*y + z*z);

            if (acceleration > SHAKE_THRESHOLD) {
                onEqualsPressed();
            }
        }
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