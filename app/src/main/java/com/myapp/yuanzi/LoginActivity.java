package com.myapp.yuanzi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.myapp.yuanzi.ConstString.ConstStrings;
import com.myapp.yuanzi.util.HttpUtil;
import com.myapp.yuanzi.util.LogUtil;
import com.myapp.yuanzi.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText userToken;
    private Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userToken=findViewById(R.id.token);
        loginBtn=findViewById(R.id.login);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(this).edit();
        LogUtil.d(ConstStrings.TAG,sharedPreferences.getString("lastToken","null"));
        if (sharedPreferences.getString("lastToken",null)!=null){
            userToken.setText(sharedPreferences.getString("lastToken",null));
        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Token=userToken.getText().toString();
                if (!("".equals(Token))){
                    String url= ConstStrings.HTTP_ADDRESS+"/orgs";
                    ConstStrings.TOKEN=Token;
                    LogUtil.d(ConstStrings.TAG,ConstStrings.TOKEN);
                    HttpUtil.sendOkHttpRequest(url, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            //toast
                            showToast("糟糕网络怎么没有连接");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseText=response.body().string();
                            LogUtil.d(ConstStrings.TAG,responseText);
                            if (Utility.handleLoginResponse(responseText)){
                                //可以做跳转
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_LONG).show();
                                    }
                                });
                                editor.putString("lastToken",Token);
                                editor.apply();
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this,"TOKEN错了呢，怎么这么粗心",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    showToast("请输入你的TOKEN，然后再登录呀");
                }


            }
        });
    }
    private void showToast(String msg){
        Toast.makeText(LoginActivity.this,msg,Toast.LENGTH_LONG).show();
    }
}
