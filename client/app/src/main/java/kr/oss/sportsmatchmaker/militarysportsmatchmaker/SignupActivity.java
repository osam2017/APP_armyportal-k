package kr.oss.sportsmatchmaker.militarysportsmatchmaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SignupActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "kr.oss.sportsmatchmaker.signup.id";
    public static final String EXTRA_PW = "kr.oss.sportsmatchmaker.signup.pw";

    public final int GET_PICTURE_URI = 1;

    public ArrayList<String> ranks;
    public ArrayList<String> sexes;
    public ArrayList<String> units;
    // widgets
    private EditText idView;
    private Button idCheckButton;
    private EditText pwView;
    private EditText pwView2;
    private EditText nameView;
    private EditText favView;
    private EditText descView;
    private Spinner rankView;
    private Spinner sexView;
    private Spinner unitView;
    private Button signupButton;
    private ImageButton profPic;
    private Uri profPicUri;

    // id uniqueness check flag
    private Boolean idFlag;

    // Proxy
    public Proxy proxy;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        proxy = new Proxy(getApplicationContext());

        initializeSpinner();
        idFlag = true;
        verifyStoragePermissions(this);
        // connect widgets
        idView = (EditText) findViewById(R.id.signup_id);
        pwView = (EditText) findViewById(R.id.signup_pw);
        pwView2 = (EditText) findViewById(R.id.signup_pw2);
        nameView = (EditText) findViewById(R.id.signup_name);
        favView = (EditText) findViewById(R.id.signup_favorite);
        descView = (EditText) findViewById(R.id.signup_desc);
        idCheckButton = (Button) findViewById(R.id.signup_idcheck);
        profPic = (ImageButton) findViewById(R.id.signup_profPic);

        // set spinner to rank
        rankView = (Spinner) findViewById(R.id.signup_rank);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ranks);
        rankView.setAdapter(adapter);
        rankView.setSelection(0);

        // set spinner to sex
        sexView = (Spinner) findViewById(R.id.signup_sex);
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexes);
        sexView.setAdapter(adapterSex);
        sexView.setSelection(0);

        unitView = (Spinner) findViewById(R.id.signup_unit);
        ArrayAdapter<String> adapterUnit = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, units);
        unitView.setAdapter(adapterUnit);
        unitView.setSelection(0);


        // check if id already exists in DB.
        idCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idView.getText().toString();
                proxy.idCheck(id, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            idFlag = ! response.getBoolean("result");
                            if (! idFlag){
                                Toast.makeText(getApplicationContext(), "사용 가능한 군번입니다.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "이미 사용한 군번입니다.", Toast.LENGTH_SHORT).show();
                            }
                            idFlag = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "idCheckFail");
                    }
                });
                Toast.makeText(getApplicationContext(), "사용 가능한 군번입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // profpic button adds profile picture!
        profPicUri = Uri.EMPTY;
        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_PICTURE_URI);
            }
        });

        signupButton = (Button) findViewById(R.id.signup_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = idView.getText().toString();
                final String pw = pwView.getText().toString();
                String pw2 = pwView2.getText().toString();
                String name = nameView.getText().toString();
                String fav = favView.getText().toString();
                String desc = descView.getText().toString();
                String unit = unitView.getSelectedItem().toString();
                String rank = rankView.getSelectedItem().toString();
                String sex = sexView.getSelectedItem().toString();
                int sexid = sexes.size() - sexes.indexOf(sex) - 2;
                // 제대로 다 입력했는지 확인.
                if (id.equals("")){
                    idView.setError("군번을 입력해주십시오.");
                    idView.requestFocus();
                    return;
                }
                if (idFlag){
                    idView.setError("중복검사를 해주십시오.");
                    idView.requestFocus();
                    return;
                }
                if (pw.equals("")){
                    pwView.setError("비밀번호를 입력해주십시오.");
                    pwView.requestFocus();
                    return;
                }
                if (!pw.equals(pw2)){
                    pwView2.setError("비밀번호가 일치하지 않습니다.");
                    pwView2.requestFocus();
                    return;
                }
                if (pw.length() < 6){
                    pwView.setError("비밀번호가 너무 짧습니다.");
                    pwView.requestFocus();
                    return;
                }
                if (name.equals("")){
                    nameView.setError("이름을 입력해주십시오.");
                    nameView.requestFocus();
                    return;
                }
                if (unit.equals("소속")  || unit.equals("----")){
                    TextView errorUnit = (TextView) unitView.getSelectedView();
                    errorUnit.setError("소속부대를 입력해주십시오.");
                    errorUnit.requestFocus();
                    return;
                }
                if (rank.equals("----") || rank.equals("계급") || rank.equals("")){
                    TextView errorRank = (TextView) rankView.getSelectedView();
                    errorRank.setError("계급을 선택해주십시오.");
                    errorRank.requestFocus();
                    return;
                }
                int rankid = RankHelper.rankToInt(rank);
                if (sexid >= sexes.size() - 2){
                    TextView errorSex = (TextView) sexView.getSelectedView();
                    errorSex.setError("성별을 선택해주십시오.");
                    errorSex.requestFocus();
                    return;
                }
                if (fav.equals("")){
                    favView.setError("좋아하는 운동을 입력해주십시오.");
                    favView.requestFocus();
                    return;
                }
                if (desc.equals("")){
                    descView.setError("자기소개를 입력해주십시오.");
                    descView.requestFocus();
                    return;
                }
                //TODO: does getPath work?
                proxy.signup(id, pw, name, rankid, unit, sexid, fav, desc, getPath(profPicUri), new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            boolean result = response.getBoolean("result");
                            // success in signing up -> go back to signin screen.
                            if (result){
                                // 로그인창으로 돌아가기
                                Intent data = new Intent();
                                data.putExtra(EXTRA_ID, id);
                                data.putExtra(EXTRA_PW, pw);
                                setResult(RESULT_OK, data);
                                finish();
                            }
                            else {
                                String error = response.getString("reason");
                                if (error.equals("MissingValuesException")) {
                                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                                else if (error.equals("AlreadyExistingException")){
                                    idView.setError("이미 존재하는 군번입니다.");
                                    idView.requestFocus();
                                    idFlag = true;
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }
                                Log.e("TAG", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    //initialize spinners
    private void initializeSpinner(){
        // spinner for rank
        ranks = new ArrayList<String>();
        ranks.add("계급");
        ranks.add("----");
        // add all ranks to arraylist
        for(int i = 0; i < RankHelper.numRanks(); i++){
            ranks.add(RankHelper.ranks[RankHelper.numRanks() - 1 - i]);
        }
        // spinner for sex
        sexes = new ArrayList<String>();
        sexes.add("성별");
        sexes.add("----");
        sexes.add("여성");
        sexes.add("남성");

        // spinner for unit
        units = new ArrayList<String>();
        units.add("소속");
        units.add("----");
        units.add("1대대");
        units.add("2대대");
        units.add("3대대");
        units.add("4대대");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        if (requestCode == GET_PICTURE_URI) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    profPicUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profPicUri);
                    RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    rbd.setCornerRadius(bitmap.getHeight()/8.0f);
                    profPic.setImageDrawable(rbd);
                } catch (Exception e) {
                    Log.e("test", e.getMessage());
                }
            }
        }
    }

    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
}
