package com.nuages.note.ui;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.bmob.v3.datatype.BmobFile;

import com.nuages.note.R;

public class HudongImageDialog extends Activity {
	// private ImageView image;
	ImageView image;
	int temp = 0;
	int startX, startY, endX, endY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_image);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);// 需要添加的语句
		image = (ImageView) findViewById(R.id.image);
		BmobFile bmobFile = (BmobFile) getIntent().getSerializableExtra("url");
		bmobFile.loadImage(this, image);
		image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
}