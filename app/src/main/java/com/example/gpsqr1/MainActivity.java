package com.example.gpsqr1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

//---------------------------------------------------
//- Currently locking the screen in "Portrait mode" -
//---------------------------------------------------


//-------------------------------------------------------------------------------------------------------------------
// Android application that gets the users location /date/time and displays it as a QR code, updating every second. -
//-------------------------------------------------------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    public double longitude;
    public double latitude;
    public Location loc;
    String QRString;
    Bitmap bitmap;
    public final static int QRCodeWidth = 250;
    public final static int QRCodeHeight = 250;

    String lonDisplay;
    String latDisplay;
    Date dateobj;
    DateFormat df;


    TextView longitudeTextView;
    TextView latitudeTextView;
    TextView time;
    ImageView imageView;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create location manager
        final LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        //create location listener
        final LocationListener locationListener = new MyLocationListener();


        Log.d("testingGPS", "here1");

        //check if you have the necessary permissions to yse GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            Log.d("testingGPS", "here2");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            //set the location to the last know location provided by the gps
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else{
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        }

        //formatting the date
        df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        //assigning all of the gui text/image views
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        latitudeTextView = (TextView) findViewById(R.id.latitudeTextView);
        time = (TextView) findViewById(R.id.time);
        imageView = (ImageView)findViewById(R.id.imageView);

        //set up the timer that will run after 1 second and repeat every second
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //get the latitude/longitude/date/time
                updateGPS(loc, locationManager, locationListener);

                //create the bit map for the QR code that includes
                //latitude/longitude/date/time that was just updated
                try {
                    bitmap = TextToImageEncode();
                    Log.d("testingGPS", "TextToImageEncode updated");
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                //update the gui text/image views inside of the "runOnUiThread"
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("testingGPS", "updateGPS updated");
                        longitudeTextView.setText(lonDisplay);
                        Log.d("testingGPS", "lon ui updated");
                        latitudeTextView.setText(latDisplay);
                        Log.d("testingGPS", "lat ui updated");
                        time.setText(df.format(dateobj));
                        Log.d("testingGPS", "date/time ui updated");
                        imageView.setImageBitmap(bitmap);
                        Log.d("testingGPS", "bitmap ui updated");

                    }
                });

            }
        },1000, 1000);


    }

    //method to get the
    public void updateGPS(Location loc, LocationManager locationManager, LocationListener locationListener){

        //locationManager and locationListener are not used atm because we are checking the location every second, not just when it changes.

        //-------------------------------------------------------------------------------------------------------
        //- NOTE: going to look in to "requestLocationUpdates" to see if that will help with location accuracy. ---> https://developer.android.com/reference/android/location/LocationManager#clearTestProviderLocation(java.lang.String)
        //-------------------------------------------------------------------------------------------------------

        //ATM the location updates itself but I do not know if it is updating as much as I would like it to.

        //getting the longitude and latitude
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();

        //setting the longitude and latitude up as strings to display as a text view
        lonDisplay = "Longitude: " + longitude;
        latDisplay = "Latitude: " + latitude;

        //get the date/time
        dateobj = new Date();

        Log.d("testingGPS", "This is the dateobj " + dateobj.toString());

        //format the data to go in to the QR code
        QRString = latDisplay + "\n" + lonDisplay +"\n" + dateobj.toString() + "\n";

    }

    //generating and returning the bitmaps that are the QR code using XZing
    Bitmap TextToImageEncode() throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    QRString,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRCodeWidth, QRCodeHeight, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 250, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

}
