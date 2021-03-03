package com.koreait.boardclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String TAG = this.getClass().getName();

    EditText t_title;
    EditText t_writer;
    EditText t_content;
    ListView listView;

    BoardAdapter boardAdapter;
    HttpManager httpManager;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //리스트와 어댑터 연결
        //디자인이 복잡한(복합뷰) ListView는 BaseAdapter를 재정의해야 한다.
        listView = this.findViewById(R.id.listView);
        //사용하고 싶은 녀석들 xml에서 id만들어서 받아다 주기
        t_title = this.findViewById(R.id.t_title);
        t_writer = this.findViewById(R.id.t_writer);
        t_content = this.findViewById(R.id.t_content);


        boardAdapter = new BoardAdapter(this);
        //ListView와 어탭터와의 연결!
        listView.setAdapter(boardAdapter);
        httpManager = new HttpManager();
        handler = new Handler(Looper.getMainLooper()){
            //handleMessage영역은 UI를 제어할 수 있는 영역

            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                ArrayList<Board> boardList = bundle.getParcelableArrayList("boardList");
                boardAdapter.list = boardList;  //어댑터에 리스트 주입
                boardAdapter.notifyDataSetChanged();
            }
        };
    }

    public void getList(View view){
        //네트워크 통신을 위한 쓰레드 생성 및 실행
        Thread thread = new Thread(){
            @Override
            public void run() {
                  ArrayList<Board> boardList = httpManager.requestByGet("http://192.168.1.2:8888/rest/board");

                  //핸들러에 요청 시점
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("boardList",boardList);
                message.setData(bundle);

                handler.sendMessage(message); //UI에 대신 뭐좀 해달라고 부탁!

            }
        };
        thread.start();
    }

    public void regist(View view){
        Thread thread = new Thread(){
            @Override
            public void run() {
                JSONObject json=null;
                try {
                    //json생성하기
                    json = new JSONObject();   //비어이는 jsonobject를 만들어서
                    json.put("title",t_title.getText().toString());
                    json.put("writer",t_writer.getText().toString());
                    json.put("content",t_content.getText().toString());

                    Log.d(TAG,"json스트링은"+json.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                httpManager.requestByPost("http://192.168.1.2:8888/rest/board",json.toString());

            }
        };
        thread.start();
    }
}