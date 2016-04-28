package br.edu.loginandroid;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
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

    // TRANSIÇÃO ENTRE TELAS
    private EditText editTextLogin;
    private EditText editTextPassword;
    private Switch switchFirebase;
    private TextView textViewStatus;
    private HashMap<String, String> loginSenha = new HashMap<String, String>();
    private String login;
    private String password;

    // FIREBASE
    public static final String FIREBASE_REPO = "loginandroidfirebase";
    public static final String FIREBASE_URL = "https://" + FIREBASE_REPO + ".firebaseio.com";
    private Firebase firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TRANSIÇÃO ENTRE TELAS
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginSenha.put("edu@email.com", "edu");
        editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        switchFirebase = (Switch) findViewById(R.id.switchFirebase);

        // FIREBASE
        if (savedInstanceState == null){
            Firebase.setAndroidContext(this);
        }
        // Create a connection to your Firebase database
        firebase = new Firebase(FIREBASE_URL);

        // LOGIN COM FIREBASE
        AuthData authData = firebase.getAuth();
        if (authData != null) {
            Log.d("authData: ", "usuário autenticado");
        } else {
            Log.d("authData: ", "usuário NÃO autenticado");
        }
        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Log.d("onAuthStateChanged: ", "usuário logado");
                } else {
                    Log.d("onAuthStateChanged: ", "usuário não logado");
                }
            }
        });
    }

    // TRANSIÇÃO ENTRE TELAS
    public void signin (View v){
        login = editTextLogin.getText().toString();
        password = editTextPassword.getText().toString();

        if(!switchFirebase.isChecked()){
            if(loginSenha.containsKey(login) && loginSenha.get(login).equals(password)){
                // TRANSIÇÃO ENTRE TELAS
                transitaParaTelaFirebaseShared();
            }
            else{
                editTextPassword.setError(getString(R.string.invalid));
            }
        }
        // LOGIN COM FIREBASE
        else{
            loginComFirebase();
        }
    }

    // TRANSIÇÃO ENTRE TELAS
    private void transitaParaTelaFirebaseShared() {
        setContentView(R.layout.status_firebase_shared);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);

        // FIREBASE
        // Listen for realtime changes
        firebase.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                if(snap.getValue() != null){
                    textViewStatus.setText(snap.getValue().toString());
                }
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
    }

    // FIREBASE
    public void felizClick (View v){
        firebase.child("status").setValue("Feliz");
    }
    public void tristeClick (View v){
        firebase.child("status").setValue("Triste");
    }

    // LOGIN COM FIREBASE
    private void loginComFirebase() {
        // CRIA USUÁRIO NO FIREBASE COM DADOS PRIVADOS A PARTIR DE EMAIL E SENHA
        firebase.createUser(login, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                editTextLogin.setError("Usuário criado com sucesso! Por favor, tente novamente.");
                Log.d("onSucess: ", "Successfully created user account with uid: " + result.get("uid"));
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                Log.d("onError: ", "Autenticação falhou, " + firebaseError.getMessage());
            }
        });

        // REALIZA LOGIN COM EMAIL E SENHA NO FIREBASE
        // Create a handler to handle the result of the authentication
        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                firebase.child("users").child(authData.getUid()).setValue(map);
                // TRANSIÇÃO ENTRE TELAS
                transitaParaTelaFirebaseShared();
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d("authData: ", "Autenticação falhou, " + firebaseError.getMessage());
                editTextPassword.setError(firebaseError.getMessage());
            }
        };
        firebase.authWithPassword(login, password, authResultHandler);
     }
}
