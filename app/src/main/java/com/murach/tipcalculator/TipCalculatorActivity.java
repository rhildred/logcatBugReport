package com.murach.tipcalculator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class TipCalculatorActivity extends Activity 
implements OnEditorActionListener, OnClickListener {

    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;   
    private Button   percentUpButton;
    private Button   percentDownButton;
    private Button   bugButton;
    private TextView tipTextView;
    private TextView totalTextView;
    
    // define the SharedPreferences object
    private SharedPreferences savedValues;
    
    // define instance variables that should be saved
    private String billAmountString = "";
    private float tipPercent = .15f;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);
        
        // get references to the widgets
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        percentUpButton = (Button) findViewById(R.id.percentUpButton);
        percentDownButton = (Button) findViewById(R.id.percentDownButton);
        bugButton = (Button) findViewById(R.id.buttonBugReport);

        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);

        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        bugButton.setOnClickListener(this);

        // get SharedPreferences object
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);        
    }
    
    @Override
    public void onPause() {
        // save the instance variables       
        Editor editor = savedValues.edit();        
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();        

        super.onPause();      
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", 0.15f);

        // set the bill amount on its widget
        billAmountEditText.setText(billAmountString);
        
        // calculate and display
        calculateAndDisplay();
    }    
    
    public void calculateAndDisplay() {        

        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount = 0;
       try {
           billAmount = Float.parseFloat(billAmountString);
       }
       catch(Exception e){
           String sError = "format error caught ... Bill Amount was: " + billAmountString;
           Log.d(this.getClass().getName(), sError);
           Toast.makeText(this, sError, Toast.LENGTH_LONG).show();
       }
        
        // calculate tip and total 
        float tipAmount = billAmount * tipPercent;
        float totalAmount = billAmount + tipAmount;
        
        // display the other results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
    		actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }        
        return false;
    }

    private void sendBugReport() {
        try {

            //Send request
            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        //Your code goes here
                        Process process = Runtime.getRuntime().exec("logcat -d");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        StringBuilder log = new StringBuilder();
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line + "\n");
                        }
                        //TextView tv = (TextView) findViewById(R.id.textViewBugReport);
                        //tv.setText(log.toString());
                        URL url;
                        String targetURL = "http://rich-hildred.rhcloud.com/BitBucket";
                        String urlParameters =
                                "title=" + URLEncoder.encode("Android Bug Report", "UTF-8") +
                                        "&user=" + URLEncoder.encode("rhildred", "UTF-8") +
                                        "&component=" + URLEncoder.encode("testingandroiderrors", "UTF-8") +
                                        "&bbAccount=" + URLEncoder.encode("rhildred", "UTF-8") +
                                        "&content=" + URLEncoder.encode(log.toString(), "UTF-8");
                        //Create connection
                        url = new URL(targetURL);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type",
                                "application/x-www-form-urlencoded");

                        connection.setRequestProperty("Content-Length", "" +
                                Integer.toString(urlParameters.getBytes().length));
                        connection.setRequestProperty("Content-Language", "en-US");

                        connection.setUseCaches(false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);

                        DataOutputStream wr = new DataOutputStream(
                                connection.getOutputStream());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        //Get Response
                        InputStream is = connection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String sLine;
                        StringBuffer response = new StringBuffer();
                        while ((sLine = rd.readLine()) != null) {
                            response.append(sLine);
                            response.append('\r');
                        }
                        rd.close();
                        //tv.setText(response.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            });
            thread.start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.percentDownButton:
            tipPercent = tipPercent - .01f;
            calculateAndDisplay();
            break;
        case R.id.percentUpButton:
            tipPercent = tipPercent + .01f;
            calculateAndDisplay();
            break;
        case R.id.buttonBugReport:
            sendBugReport();
            Toast.makeText(this, "Thank-you for submitting a bug report", Toast.LENGTH_LONG).show();
            break;
        }
    }
}