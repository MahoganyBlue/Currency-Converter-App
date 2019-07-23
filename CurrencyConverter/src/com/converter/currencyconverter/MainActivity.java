package com.converter.currencyconverter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.lang.reflect.Field;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    File file;
    TextView myTextView;
    RelativeLayout myBackgroundLayout;
    int color_scheme = 0;
    int text_size = 0;

    TextView txtResult;
    EditText etValue;
    
    SharedPreferences sharedpreferences;
    JSONObject jsonOBJ = null;
    NumberPicker fromCurrency;
    NumberPicker toCurrency;

    double eurValue = 1.0, gbpValue = 1.0, cadValue = 1.0, AudValue = 1.0, chfValue = 1.0
            , plnValue = 1.0, jpyValue = 1.0;
    double[] values = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    double myInput = 0.0;

    Button updateButton, convertButton;
    ProgressDialog pd;
    String currencyString = null;

    double setEditText = 0.0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.setTheme(R.style.DarkAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myBackgroundLayout = (RelativeLayout) findViewById(R.id.portraitView); // layout background
        etValue = (EditText) findViewById(R.id.currencyValue);
        sharedpreferences = getSharedPreferences("MyPref",
                Context.MODE_PRIVATE);
        
        if (!(savedInstanceState == null)) // the APP just started running
        {
        	//fromCurrency.setValue(savedInstanceState.getInt("FromValue"));//not working
        	//toCurrency.setValue(savedInstanceState.getInt("ToValue"));
        	setEditText = savedInstanceState.getDouble("E_Text");
            values = savedInstanceState.getDoubleArray("Value");
        }
        
        loadCurrencyData();
        
        //setup country Spinner wheel
        String[] stringValues = { "USD", "EUR", "GBP", "CAD", "AUD", "CHF", "PLN", "JPY" };
        fromCurrency = (NumberPicker) findViewById(R.id.countryPicker);
        fromCurrency.setMinValue(0);
        fromCurrency.setMaxValue(stringValues.length - 1);
        fromCurrency.setDisplayedValues(stringValues);
        fromCurrency.setWrapSelectorWheel(true);
        //not working
        /*fromCurrency.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
        	@Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        		SharedPreferences.Editor editor = sharedpreferences.edit();
        		editor.putString("pickerVal", "" + newVal);// Storing string
        		editor.commit();
        	}
        });*/
        
        //setup country2 Spinner wheel
        toCurrency = (NumberPicker) findViewById(R.id.countryPicker2);
        toCurrency.setMinValue(0);
        toCurrency.setMaxValue(stringValues.length - 1);
        toCurrency.setDisplayedValues(stringValues);
        toCurrency.setWrapSelectorWheel(true);
      //not working
        /*toCurrency.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
        	@Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        		SharedPreferences.Editor editor = sharedpreferences.edit();
        		editor.putString("picker2Val", "" + newVal);// Storing string
        		editor.commit();
        	}
        });*/
        


        // --------------------API fetch-----------------------
        final String url = "http://www.apilayer.net/api/live" +
                "?access_key=c60a9856bda3085304d64b933d91c100" +
                "&currencies=EUR,GBP,CAD,AUD,CHF,PLN,JPY";
        
        //set convert button
        convertButton = (Button) findViewById(R.id.convertbtn);
        convertButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    myInput = Double.parseDouble(etValue.getText().toString());
                } catch (Exception ignored){}
                if (!(myInput == 0.0)){
                    double to = values[toCurrency.getValue()];
                    double from = values[fromCurrency.getValue()];
                    double result = myInput * (from/to);
                    etValue.setText(Double.toString((Math.floor(result * 100)/ 100)));
                }
            }
        });
        
        //set update button
        updateButton = (Button) findViewById(R.id.updatebtn);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JsonTask().execute(url);
            }
        });
    } // onCreate end

    @SuppressLint("StaticFieldLeak")
    class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... arg) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

                try {
                    URL url = new URL(arg[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuilder buffer = new StringBuilder();
                    String line;

                    while (!((line = reader.readLine()) == null)) {
                        buffer.append(line).append("\n");
                        Log.d("Response: ", "> " + line);
                    }
                    currencyString = buffer.toString();
                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
        }
        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            if (pd.isShowing()) {
                pd.dismiss();
            }

            try {
                jsonOBJ = new JSONObject(currencyString);
                // get time stamp and display date
                //int timestamp = jsonOBJ.getInt("timestamp");
                //date = getDate(timestamp);
                // txtDate.setText(date);

                // String quotes = jsonOBJ.getString("quotes");
                String EUR = jsonOBJ.getJSONObject("quotes")
                        .getString("USDEUR");
                // txtEUR.setText(EUR);
                eurValue = Double.parseDouble(EUR);

                String GBP = jsonOBJ.getJSONObject("quotes")
                        .getString("USDGBP");
                // txtGBP.setText(GBP);
                gbpValue = Double.parseDouble(GBP);

                String CAD = jsonOBJ.getJSONObject("quotes")
                        .getString("USDCAD");
                // txtCAD.setText(CAD);
                cadValue = Double.parseDouble(CAD);

                String AUD = jsonOBJ.getJSONObject("quotes")
                        .getString("USDAUD");
                // txtCAD.setText(CAD);
                AudValue = Double.parseDouble(AUD);

                String CHF = jsonOBJ.getJSONObject("quotes")
                        .getString("USDCHF");
                chfValue = Double.parseDouble(AUD);

                String PLN = jsonOBJ.getJSONObject("quotes")
                        .getString("USDPLN");
                plnValue = Double.parseDouble(AUD);

                String JPY = jsonOBJ.getJSONObject("quotes")
                        .getString("USDJPY");
                jpyValue = Double.parseDouble(JPY);

                values[0] = 1.0;//USD
                values[1] = Double.parseDouble(EUR);
                values[2] = Double.parseDouble(GBP);
                values[3] = Double.parseDouble(CAD);
                values[4] = Double.parseDouble(AUD);
                values[5] = Double.parseDouble(CHF);
                values[6] = Double.parseDouble(PLN);
                values[7] = Double.parseDouble(JPY);

                //"USD", "EUR", "GBP", "CAD", "AUD", "CHF", "PLN", "JPY"
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //save currency data in shared preferences
    public void saveCurrencyData(String[] data){
    	SharedPreferences.Editor editor = sharedpreferences.edit();

    	editor.putString("eurValue", data[1]);// Storing string
    	editor.putString("gbpValue", data[2]);
    	editor.putString("cadValue", data[3]);
    	editor.putString("AudValue", data[4]);
    	editor.putString("chfValue", data[5]);
    	editor.putString("plnValue", data[6]);
    	editor.putString("jpyValue", data[7]);
    	editor.commit(); // commit changes
    	
    }

    //Load currency data from shared preferences called "MyPref"
    public void loadCurrencyData(){
    	SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);// 0 - for private mode
    	try{
    	values[1] = Double.parseDouble(pref.getString("eurValue", null));// getting String
    	values[2] = Double.parseDouble(pref.getString("gbpValue", null));
    	values[3] = Double.parseDouble(pref.getString("cadValue", null));
    	values[4] = Double.parseDouble(pref.getString("AudValue", null));
    	values[5] = Double.parseDouble(pref.getString("chfValue", null));
    	values[6] = Double.parseDouble(pref.getString("plnValue", null));
    	values[7] = Double.parseDouble(pref.getString("jpyValue", null));
    	}catch(Exception x){
    		//error message
    	}
    }

    @SuppressWarnings("unused")
	private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    
    // Handle action bar item clicks here. The action bar will
    // call four (4) color scheme in the changeScheme method
    // to handle the background and text color. Call three (3)
    // text sizes from the changeTextSize method.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        switch (id) {
            case R.id.SaveUpdate:
                String[] data = {"1.0", "1.0", "1.0", "1.0", "1.0", "1.0", "1.0", "1.0"};
                for (int i = 0 ; i <= values.length-1; i++){
                    data[i] = "" + values[i];
                }
                saveCurrencyData(data);
                return true;
            case R.id.Dark_Mode:
                changeTheme(0);
                return true;
            case R.id.Normal_Mode:
                changeTheme(1);
                return true;
            case R.id.text_sml:
                changeTextSize(0);
                return true;
            case R.id.text_med:
                changeTextSize(1);
                return true;
            case R.id.text_lrg:
                changeTextSize(2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // save values of screen for rotation
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putDoubleArray("Value", values);
        outState.putDouble("E_Text", Double.parseDouble(etValue.getText().toString()));
        //outState.putInt("FromValue", fromCurrency.getValue());
        //outState.putInt("ToValue", toCurrency.getValue());
    } // end method onSaveInstanceState

    public void changeTheme(int theme){

        switch (theme){
            case 0:
                //setPickers
                setPickerTextColor(toCurrency, getResources().getColor(R.color.OffWhite));
                setPickerTextColor(fromCurrency, getResources().getColor(R.color.OffWhite));
                fromCurrency.setBackgroundColor(getResources().getColor(R.color.Blue));

                toCurrency.setBackgroundColor(getResources().getColor(R.color.Blue));
                //setBackground
                myBackgroundLayout.setBackgroundColor(getResources().getColor(
                		R.color.Black));
                //setEditText
                etValue.setTextColor(getResources().getColor(R.color.OffWhite));
                //setButtons
                convertButton.setTextColor(getResources().getColor(R.color.OffWhite));
                convertButton.setBackgroundColor(getResources().getColor(R.color.Blue));
                updateButton.setTextColor(getResources().getColor(R.color.OffWhite));
                updateButton.setBackgroundColor(getResources().getColor(R.color.Blue));
                //setTheme(R.style.AppTheme);
                //setContentView(R.layout.activity_main);
                break;
            case 1:
                //setPickers
                setPickerTextColor(toCurrency, getResources().getColor(R.color.White));
                setPickerTextColor(fromCurrency, getResources().getColor(R.color.White));
                fromCurrency.setBackgroundColor(getResources().getColor(R.color.Green));
                toCurrency.setBackgroundColor(getResources().getColor(R.color.Green));
                //setBackground
                myBackgroundLayout.setBackgroundColor(getResources().getColor(
                		R.color.White));
                //setEditText
                etValue.setTextColor(getResources().getColor(R.color.Black));
                //setButtons
                convertButton.setTextColor(getResources().getColor(R.color.White));
                convertButton.setBackgroundColor(getResources().getColor(R.color.Green));
                updateButton.setTextColor(getResources().getColor(R.color.White));
                updateButton.setBackgroundColor(getResources().getColor(R.color.Green));

                break;
        }
    }

    public static void setPickerTextColor(NumberPicker numberPicker, int color)
    {

        try{
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
        }
        catch(NoSuchFieldException e){
            Log.w("setPickerTextColor", e);
        }
        catch(IllegalAccessException e){
            Log.w("setPickerTextColor", e);
        }
        catch(IllegalArgumentException e){
            Log.w("setPickerTextColor", e);
        }

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText)
                ((EditText)child).setTextColor(color);
        }
        numberPicker.invalidate();
    }

    public void changeTextSize(int size) {
        myTextView = (TextView) findViewById(R.id.currencyValue);
        updateButton = (Button) findViewById(R.id.updatebtn);
        convertButton = (Button) findViewById(R.id.convertbtn);
        fromCurrency = (NumberPicker) findViewById(R.id.countryPicker);
        toCurrency = (NumberPicker) findViewById(R.id.countryPicker2);
        switch (size) {
            case 0:
                convertButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_sml));
                updateButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_sml));
                myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_sml));
                fromCurrency.setScrollBarSize(R.dimen.text_size_sml);
                toCurrency.setScrollBarSize(R.dimen.text_size_sml);
                break;
            case 1:
                convertButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_med));
                updateButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_med));
                myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_med));
                break;
            case 2:
                convertButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_lge));
                updateButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_lge));
                myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
                        .getDimension(R.dimen.text_size_lge));
                toCurrency.setScrollBarSize(R.dimen.text_size_lge);
                break;
        }
    } // end of changeTextSize method


}
