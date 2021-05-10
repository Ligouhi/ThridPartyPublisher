package com.youmuyun.thridpartpublisherapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;



import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ThPartSettingActivity extends AppCompatActivity {

    @BindView(R.id.tp_title)
    EditText tp_title;
    @BindView(R.id.tp_url)
    EditText tp_url;
    @BindView(R.id.tp_push)
    Button tp_push;
    @BindView(R.id.tp_spinner)
    Spinner tp_spinner;
    @BindView(R.id.viewGroup)
    ButtonViewGroup<Button> mGroup;
    @BindView(R.id.viewGroup2)
    ButtonViewGroup<Button> mGroup2;

    int rpos = 0;
    int bitrt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_th_part_setting);
        ButterKnife.bind(this);
        getdata();
        init();
    }

    private void getdata() {
       Intent info =  getIntent();
       String url = info.getStringExtra("url");
       tp_url.setText(url);
    }

    private void init() {
        String[] TextureList = {"竖屏","横屏"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TextureList);
        tp_spinner.setAdapter(adapter);
        tp_spinner.setSelection(0);
        final int Pos = tp_spinner.getSelectedItemPosition();


        final ArrayList<String> viewtexts = new ArrayList<>();
        viewtexts.add("360P");
        viewtexts.add("720P");
        viewtexts.add("1080P");
        mGroup.addItemViews(viewtexts, ButtonViewGroup.TEV_MODE);
        mGroup.chooseItemStyle(rpos);
        mGroup.setGroupClickListener(new ButtonViewGroup.OnGroupItemClickListener() {
            @Override
            public void onGroupItemClick(int item) {
                rpos = item;
                // Toast.makeText(SettingActivity.this, viewtexts.get(item), Toast.LENGTH_SHORT).show();
            }
        });
        //码率选项
        final ArrayList<String> viewtexts2 = new ArrayList<>();
        viewtexts2.add("500kbps");
        viewtexts2.add("1200 kbps");
        mGroup2.addItemViews(viewtexts2, ButtonViewGroup.TEV_MODE);
        mGroup2.chooseItemStyle(bitrt);
        mGroup2.setGroupClickListener(new ButtonViewGroup.OnGroupItemClickListener() {
            @Override
            public void onGroupItemClick(int item) {
                bitrt = item;
                // Toast.makeText(SettingActivity.this, viewtexts.get(item), Toast.LENGTH_SHORT).show();
            }
        });

        tp_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThPartSettingActivity.this,MainActivity.class);
                intent.putExtra("URL",tp_url.getText().toString());
                intent.putExtra("OrienSpinner", Pos);
                intent.putExtra("title",tp_title.getText().toString());
                intent.putExtra("resolusion",rpos);
                intent.putExtra("bitsrate",bitrt);
                startActivity(intent);
            }
        });
    }
}
