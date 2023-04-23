package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  {

    private TextView editText;
    private Button[] buttons; //creating an array for all the buttons

    private int[] buttonIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.plus, R.id.subtract, R.id.multiply, R.id.btn_divide, R.id.btn_clear, R.id.btn_equal}; //creating a array for all the buttonIDs

    private StringBuilder inputStringBuilder = new StringBuilder(); //declaring an stringbuilder object to manipulate the text from the button text
    private double num1 = Double.NaN;
    private double num2 = Double.NaN;
    private char currentOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialising the components of the UI
        editText = findViewById(R.id.result); //we print the result in the textView
        buttons = new Button[buttonIds.length];

        for (int i = 0; i < buttonIds.length; i++) {
            buttons[i] = findViewById(buttonIds[i]); //giving the button ids
            buttons[i].setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            String buttonText = button.getText().toString();

            switch (buttonText) {
                case "C":
                    inputStringBuilder.setLength(0);
                    editText.setText("");//if user selects clear, we set the edittext to empty string and break out of the loop
                    break;
                case "=":
                    if (inputStringBuilder.length() > 0 && !Double.isNaN(num1)) {
                        num2 = Double.parseDouble(inputStringBuilder.toString());
                        double result = performCalculation();
                        editText.setText(String.valueOf(result));
                        inputStringBuilder.setLength(0);
                        num1 = Double.NaN;
                    }
                    break;
                case "+":
                case "-":
                case "*":
                case "/":
                    if (inputStringBuilder.length() > 0) {
                        num1 = Double.parseDouble(inputStringBuilder.toString());
                        inputStringBuilder.setLength(0);
                        currentOperator = buttonText.charAt(0);
                    }
                    break;
                default:
                    inputStringBuilder.append(buttonText);
                    editText.setText(inputStringBuilder.toString());
                    break;
            }
        }
    };

    private double performCalculation() {
        double result = Double.NaN;

        switch (currentOperator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            case '/':
                result = num1 / num2;
                break;
        }

        return result;
    }
}