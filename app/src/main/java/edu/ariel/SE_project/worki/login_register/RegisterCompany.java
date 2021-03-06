package edu.ariel.SE_project.worki.login_register;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.ariel.SE_project.worki.R;
import edu.ariel.SE_project.worki.assistance_classes.GlobalMetaData;
import edu.ariel.SE_project.worki.assistance_classes.Transitions;
import edu.ariel.SE_project.worki.data.Company;
import edu.ariel.SE_project.worki.data.CurrentUser;
import edu.ariel.SE_project.worki.data.User;

/**
 * Register a company.
 */
public class RegisterCompany extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText name;

    private EditText address;
    private EditText phone;

    private Button enterButton;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_company);


        // Initialize variables
        name = findViewById(R.id.name);

        address = findViewById(R.id.companyPostalAddress);
        phone = findViewById(R.id.companyPhoneNumber);

        enterButton = findViewById(R.id.companyRegisterButton);

        loadingProgressBar = findViewById(R.id.comapnyRegisterProgressBar);
        loadingProgressBar.setVisibility(View.GONE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user == null)
        {
            // TODO Error!
            return;
        }

        // Called if the user presses enter from password field.
        phone.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    enter();
                }
                return false;
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enter();
            }
        });
    }

    /**
     * Info was entered. Now we register the company
     */
    private void enter()
    {
        if (validateForm())
        {
            loadingProgressBar.setVisibility(View.VISIBLE);

            CurrentUser.getInstance().addOnUserNotNullListener(new Consumer<User>()
            {
                @Override
                public void accept(User user)
                {
                    writeToDatabase(user);
                }
            });
        }
    }

    private void writeToDatabase(User user)
    {
        Log.d("Register Company", "Trying write to database...");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(GlobalMetaData.companiesPath + '/' + user.companyId);

        new Company(user.companyId, name.getText().toString(), address.getText().toString()).writeToDatabase(myRef);

        Log.d("Register Company", "Finished");
        updateUI();
    }


    /**
     * Called after registering the company.
     */
    private void updateUI()
    {
        Transitions.toLoggedInActivity(this);
    }

    /**
     * Make sure the info was filled before entering it.
     *
     * @return true if the info is valid
     */
    private boolean validateForm()
    {
        boolean valid = true;

        String nm = name.getText().toString();
        if (TextUtils.isEmpty(nm))
        {
            name.setError("Required.");
            valid = false;
        } else
        {
            name.setError(null);
        }

        String addr = address.getText().toString();
        if (TextUtils.isEmpty(addr))
        {
            address.setError("Required.");
            valid = false;
        } else
        {
            address.setError(null);
        }

        String ph = phone.getText().toString();
        if (TextUtils.isEmpty(ph))
        {
            phone.setError("Required.");
            valid = false;
        } else
        {
            phone.setError(null);
        }

        return valid;
    }
}
