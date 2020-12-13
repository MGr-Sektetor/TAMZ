package com.example.snake_tamz;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.snake_tamz.Database;

import java.io.IOException;
import java.util.Random;

import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")



public class GameActivity extends Activity implements LocationListener {

    private static Database myDb;

    Canvas canvas;
    SnakeView snakeView;
    Bitmap headBitmap;
    Bitmap bodyBitmap;
    Bitmap tailBitmap;
    Bitmap appleBitmap;
    private Bitmap[] bmp;
    private volatile boolean soundOn = false;

    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    int directionOfTravel=0;

    int screenWidth;
    int screenHeight;
    int topGap;

    long lastFrameTime;
    int fps;
    int score;

    int [] snakeX;
    int [] snakeY;
    int snakeLength;
    int appleX;
    int appleY;

    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    private LocationManager locationManager;
    private double latitude  = 0;
    private double longitude = 0;
    private String provider;

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private void updateLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);

            if(provider != null) {
                locationManager.requestSingleUpdate(provider, this, null);
                Location location = locationManager.getLastKnownLocation(provider);

                if(location != null) {
                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);

                    sharedPreferences.edit().putString(MainActivity.LOCATION, String.valueOf(location.getLatitude())).apply();
                    sharedPreferences.edit().putString(MainActivity.LOCATION, String.valueOf(location.getLongitude())).apply();
                    sharedPreferences.edit().putString(MainActivity.LOCATION, location.getProvider()).apply();

                }
            }

        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ImageButton.setImageResource(R.drawable.unmute);
        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
        myDb = new Database(this);

    }
    class SnakeView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();


            snakeX = new int[200];
            snakeY = new int[200];


            getSnake();

            getApple();
        }

        public void getSnake(){
            snakeLength = 3;

            snakeX[0] = numBlocksWide/2;
            snakeY[0] = numBlocksHigh /2;


            snakeX[1] = snakeX[0]-1;
            snakeY[1] = snakeY[0];


            snakeX[1] = snakeX[1]-1;
            snakeY[1] = snakeY[0];
        }

        public void getApple(){
            Random random = new Random();
            appleX = random.nextInt(numBlocksWide-1)+1;
            appleY = random.nextInt(numBlocksHigh-1)+1;
        }

        @Override
        public void run() {
            while (playingSnake) {
                // extractDb();
                loadHighscoreFromPreferences();
                updateGame();
                drawGame();
                controlFPS();

            }

        }

        public void updateGame() {

            if(snakeX[0] == appleX && snakeY[0] == appleY){

                snakeLength++;

                getApple();

                score = score + snakeLength+10;
                if (soundOn) soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            for(int i=snakeLength; i >0 ; i--){
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];
            }

            switch (directionOfTravel){
                case 0://up
                    snakeY[0]  --;
                    break;

                case 1://right
                    snakeX[0] ++;
                    break;

                case 2://down
                    snakeY[0] ++;
                    break;

                case 3://left
                    snakeX[0] --;
                    break;
            }
            boolean dead = false;

            if(snakeX[0] == -1)dead=true;
            if(snakeX[0] >= numBlocksWide)dead=true;
            if(snakeY[0] == -1)dead=true;
            if(snakeY[0] == numBlocksHigh)dead=true;

            for (int i = snakeLength-1; i > 0; i--) {
                if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                    dead = true;
                }
            }

            if(dead){

                if (soundOn) soundPool.play(sample4, 1, 1, 0, 0, 1);
                checkHighscorePreferences();
                score = 0;
                getSnake();

            }

        }

        public void drawGame() {

            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap/2);
                canvas.drawText("Score:" + score + "  HighestScore:" + loadHighscoreFromPreferences(), 10, topGap-6, paint);

                //  ImageButton muteButton = getResources(R.drawable.unmute);

                canvas.drawBitmap(bmp[soundOn ? 1 : 2], null,
                        new Rect(22 * blockSize + blockSize/2, 10 * blockSize, (22 * blockSize) + blockSize/2 + blockSize, (0 * blockSize) + blockSize-5), null);

                paint.setStrokeWidth(3);
                canvas.drawLine(1,topGap,screenWidth-1,topGap,paint);
                canvas.drawLine(screenWidth-1,topGap,screenWidth-1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(screenWidth-1,topGap+(numBlocksHigh*blockSize),1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(1,topGap, 1,topGap+(numBlocksHigh*blockSize), paint);
                canvas.drawBitmap(headBitmap, snakeX[0]*blockSize, (snakeY[0]*blockSize)+topGap, paint);


                for(int i = 1; i < snakeLength-1;i++){
                    canvas.drawBitmap(bodyBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }
                canvas.drawBitmap(tailBitmap, snakeX[snakeLength-1]*blockSize, (snakeY[snakeLength-1]*blockSize)+topGap, paint);
                canvas.drawBitmap(appleBitmap, appleX*blockSize, (appleY*blockSize)+topGap, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 100 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0) {

                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    Log.e("error", "failed to load sound files");
                }

            }

            lastFrameTime = System.currentTimeMillis();
        }


        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
            }

        }

        public void resume() {
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getX() >= screenWidth / 2) {
                        //turn right
                        directionOfTravel ++;
                        if(directionOfTravel == 4) {//no such direction
                            //loop back to 0(up)
                            directionOfTravel = 0;
                        }
                    } else {
                        //turn left
                        directionOfTravel--;
                        if(directionOfTravel == -1) {//no such direction
                            //loop back to 0(up)
                            directionOfTravel = 3;
                        }
                    }
            }
            return true;
        }


    }

    public void checkHighscorePreferences(){
        if (score > loadHighscoreFromPreferences()) {

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else{
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(MainActivity.HIGHSCORE, score);
                editor.apply();
                Log.d(TAG, "checkHighscorePreferences: new: " + score);
                UpdateData(String.valueOf(score), soundOn ? "0" : "1");
                updateLocation();
            }
        }
    }

    public int loadHighscoreFromPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getInt(MainActivity.HIGHSCORE, 0);
    }


    @Override
    protected void onStop() {
        super.onStop();

        while (true) {
            snakeView.pause();
            break;
        }

        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            snakeView.pause();



            Intent i = new Intent(this, MenuActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    public void loadSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            //Create objects of the 2 required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            //create our three fx in memory ready for use
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);


            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);


        } catch (IOException e) {
            //Print an error message to the console
            Log.e("error", "failed to load sound files");

        }
    }

    public void configureDisplay(){
        //find out the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        topGap = screenHeight/14;

        //Determine the size of each block/place on the game board
        blockSize = screenWidth/40;

        //Determine how many game blocks will fit into the height and width
        //Leave one block for the score at the top
        numBlocksWide = 40;
        numBlocksHigh = ((screenHeight - topGap ))/blockSize;

        //Load and scale bitmaps
        headBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);

        bmp = new Bitmap[3];

        bmp[1] = BitmapFactory.decodeResource(getResources(),R.drawable.muted);
        bmp[2] = BitmapFactory.decodeResource(getResources(),R.drawable.unmuted);

        //scale the bitmaps to match the block size
        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize, false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);


    }



    private void extractDb(){
        Cursor entries = getEntries();
        entries.moveToNext();
        entries.getString(0);
        score = Integer.parseInt(entries.getString(1));
        soundOn = Integer.parseInt(entries.getString(2)) == 1 ? true : false;
    }

    public static Cursor getEntries(){
        Cursor res = myDb.getData();
        if(res.getCount()==0){
            addData();
            getEntries();

        }
        return res;
    }

    public void viewAll() {
        Cursor res = myDb.getData();
        if(res.getCount() == 0) {
            addData();
            return;
        }
    }

    public static void UpdateData(String highestscore, String sound){
        boolean isUpdate = myDb.updateData("1",highestscore,sound);
    }

    public static void addData(){
        myDb.insertData("0","1");
    }

    public void deleteData(){Integer deleteRows = myDb.deleteData("1");}


}
