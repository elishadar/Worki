package edu.ariel.SE_project.worki.worker_to_company_registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.ariel.SE_project.worki.R;
import edu.ariel.SE_project.worki.assistance_classes.GlobalMetaData;
import edu.ariel.SE_project.worki.data.CurrentUser;
import edu.ariel.SE_project.worki.data.InviteMessage;
import edu.ariel.SE_project.worki.data.User;
// this appear on manager page
public class RegisterWorkerToCompanyActivity extends AppCompatActivity
{
    private TextView WorkerMail;
    private Button AddButton;
    private Button ClearButton;
    private ListView repliesListview;

    private ArrayList<String> myReplies;
    private ArrayAdapter arrAdap;
    private DatabaseReference databaseUsersRef;
    private DatabaseReference databaseMessagesRef;

    private boolean flag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_worker_to_company);

        WorkerMail = findViewById(R.id.addWorkerToCompanyTextView);
        AddButton = findViewById(R.id.addWorkerToCompanyButton);

        ClearButton = findViewById(R.id.clearRecievedRepliesFromWorkersButton);
        repliesListview = findViewById(R.id.managerRepliesFromWorkersListView);

//        myReplies = new ArrayList<>();

        CurrentUser.getInstance().addOnUserNotNullListener(new Consumer<User>()
        {
            @Override
            public void accept(User user)
            {
                searchForNewReplies();
//                updateRepliesListView();

            }
        });


        // shows replies recieved from workers
        repliesListview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!MessagesHandler.workerReplies.containsKey(CurrentUser.getInstance().getUserData().email) ||
                        MessagesHandler.workerReplies.get(CurrentUser.getInstance().getUserData().email).isEmpty())
                {
                    ClearButton.setEnabled(false);
                }
                else
                {
                    ClearButton.setEnabled(true);
                }

            }
        });

        AddButton.setOnClickListener(new View.OnClickListener()
                                     {
                                         @Override
                                         public void onClick(View v)
                                         {
                                             if (mailIsValid(WorkerMail.getText().toString()))
                                             {
                                                 sendInvitationToWorker(WorkerMail.getText().toString());
                                             }
                                             else
                                             {
                                                 showErrorMessage();
                                             }
                                         }
                                     }
        );

        //done
        ClearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MessagesHandler.deleteMessage(true, CurrentUser.getInstance().getUserData().email);
            }
        });
    }

    // done
    private void updateRepliesListView()
    {
        if (MessagesHandler.workerReplies.containsKey(CurrentUser.getInstance().getUserData().email))
        {
            myReplies = new ArrayList<>();
            myReplies = MessagesHandler.convertToStrings(MessagesHandler.workerReplies.get(CurrentUser.getInstance().getUserData().email));

            arrAdap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myReplies);
            repliesListview.setAdapter(arrAdap);
        }
    }

    private void searchForNewReplies()
    {
        // todo implement send AnswerToManager, need some kind of static object to handle messages
        if (MessagesHandler.workerReplies.containsKey(CurrentUser.getInstance().getUserData().email))
        {
            updateRepliesListView();
        }

    }

    // searches for this mail address on users db
    private boolean mailIsValid(final String text)
    {
        Query query = FirebaseDatabase.getInstance().getReference(GlobalMetaData.usersPath).orderByChild("email").equalTo(text);
        query.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.child(text).exists())
                    {
                        flag = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                flag = false;
            }

        });

        if (text == null || text.isEmpty() || flag)
        {
            return false;
        }
        return true;
    }

    // search for the user's email in the database, if so - saves the message on the firebase.
    private void sendInvitationToWorker(final String mailAddress)
    {
//        Toast.makeText(this, "entered sendinvitationtoworker func", Toast.LENGTH_LONG).show();
        databaseMessagesRef = FirebaseDatabase.getInstance().getReference(GlobalMetaData.messagesPath);
        databaseUsersRef = FirebaseDatabase.getInstance().getReference(GlobalMetaData.usersPath);

        Query query = FirebaseDatabase.getInstance().getReference(GlobalMetaData.usersPath).orderByChild("email").equalTo(mailAddress);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
//                if (dataSnapshot.getChildrenCount() > 0)
//                {
//                    String id = databaseRef.push().getKey();
                    InviteMessage InviteNewWorker = new InviteMessage();
                    InviteNewWorker.readFromDatabase(dataSnapshot);
                    InviteNewWorker.writeToDatabase(databaseMessagesRef.push());
                    MessagesHandler.sendMessage(true, InviteNewWorker);
                    showSentMessage();
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }


    // done
    private void showErrorMessage()
    {
        Toast.makeText(this, "Please Enter a Valid Mail Address.", Toast.LENGTH_SHORT).show();
    }

    //done
    private void showSentMessage()
    {
        Toast.makeText(this, "Invitation Was Sent Successfully!", Toast.LENGTH_SHORT).show();
    }
}
