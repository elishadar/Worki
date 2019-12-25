package edu.ariel.SE_project.worki;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.ariel.SE_project.worki.R;
import edu.ariel.SE_project.worki.assistance_classes.GlobalMetaData;
import edu.ariel.SE_project.worki.data.ShiftStamp;

public class TimerActivity extends AppCompatActivity
{

    private Button startStop;

    private Button pauseShift;
    private Chronometer timer;

    private long duration;
    private boolean paused = false;
    private boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Initialize variables
        startStop = findViewById(R.id.startStop);

        pauseShift = findViewById(R.id.pauseShift);

        timer = findViewById(R.id.chronometer);

        startStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                started = !started;
                if (started)
                    startTimer();
                else
                    stopTimer();
            }
        });

        pauseShift.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paused = !paused;
                if (paused)
                    pauseTimer();
                else
                    continueTimer();

            }
        });

    }

    /**
     * Start the timer.
     */
    private void startTimer()
    {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        startStop.setText(R.string.start_shift_text);
        writeDB(ShiftStamp.ShiftStampType.Start, System.currentTimeMillis());
    }

    /**
     * Pause the timer.
     */
    private void pauseTimer()
    {
        duration = SystemClock.elapsedRealtime() - timer.getBase();
        timer.stop();

        pauseShift.setText(R.string.pause_shift_text);
        writeDB(ShiftStamp.ShiftStampType.Pause, System.currentTimeMillis());
    }

    /**
     * Continue (unpause) the timer.
     */
    private void continueTimer()
    {
        timer.setBase(SystemClock.elapsedRealtime() - duration);
        timer.start();

        pauseShift.setText(R.string.continue_shift_text);
        writeDB(ShiftStamp.ShiftStampType.Continue, System.currentTimeMillis());
    }

    /**
     * Stop the timer.
     */
    private void stopTimer()
    {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.stop();

        startStop.setText(R.string.stop_shift_text);
        writeDB(ShiftStamp.ShiftStampType.Stop, System.currentTimeMillis());
    }

    private void writeDB(ShiftStamp.ShiftStampType type, long time)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(GlobalMetaData.userDataPath());
        ShiftStamp ss = new ShiftStamp(type, time);
        ss.writeToDatabase(myRef.child("shiftStamps").push());
    }
}
