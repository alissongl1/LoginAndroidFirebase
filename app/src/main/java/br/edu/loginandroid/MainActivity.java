package br.edu.loginandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, String> loginSenha = new HashMap<String, String>();

    public static final String FIREBASE_REPO = "blinding-heat-9190";
    public static final String FIREBASE_URL = "https://" + FIREBASE_REPO + ".firebaseio.com";
    private Firebase firebase;

    private EditText editTextLogin;
    private EditText editTextPassword;
    private Switch switchFirebase;
    private TextView textViewStatus;
    private String login;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null){
            Firebase.setAndroidContext(this);
        }

        loginSenha.put("edu@email.com", "edu");

        editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        switchFirebase = (Switch) findViewById(R.id.switchFirebase);

        // Create a connection to your Firebase database
        firebase = new Firebase(FIREBASE_URL);

        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Log.d("onAuthStateChanged: ", "usuário logado");
                    // user is logged in
                } else {
                    Log.d("onAuthStateChanged: ", "usuário não logado");
                    // user is not logged in
                }
            }
        });

        AuthData authData = firebase.getAuth();
        if (authData != null) {
            // user authenticated
            Log.d("authData: ", "usuário autenticado");
        } else {
            // no user authenticated
            Log.d("authData: ", "usuário NÃO autenticado");
        }
    }

    public void signin (View v){
        login = editTextLogin.getText().toString();
        password = editTextPassword.getText().toString();

        if(!switchFirebase.isChecked()){
            if(loginSenha.containsKey(login) && loginSenha.get(login).equals(password)){
                transitaParaTelaFirebaseShared();
            }
            else{
                editTextPassword.setError(getString(R.string.invalid));
            }
        }else{
            loginComFirebase();
        }
    }

    /**
     *
     */
    private void transitaParaTelaFirebaseShared() {
        setContentView(R.layout.activity_firebase_shared);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);

        // Listen for realtime changes
        firebase.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                textViewStatus.setText(snap.getValue().toString());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });

    }

    public void felizClick (View v){
        firebase.child("status").setValue("Feliz");
    }

    public void tristeClick (View v){
        firebase.child("status").setValue("Triste");
    }

    private void loginComFirebase() {

        firebase.createUser(login, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d("onSucess: ", "Successfully created user account with uid: " + result.get("uid"));
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                // there was an error
                Log.d("onError: ", "Autenticação falhou, " + firebaseError.getMessage());
            }
        });


        // Create a handler to handle the result of the authentication
        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                if(authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                }
                firebase.child("users").child(authData.getUid()).setValue(map);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                Log.d("authData: ", "Autenticação falhou, " + firebaseError.getMessage());
            }
        };

        // Or with an email/password combination
        firebase.authWithPassword(login, password, authResultHandler);

        transitaParaTelaFirebaseShared();

     }

}
