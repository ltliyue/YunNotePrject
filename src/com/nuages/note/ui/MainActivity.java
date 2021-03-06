package com.nuages.note.ui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.trinea.android.common.util.ToastUtils;

import com.nuages.note.AppManager;
import com.nuages.note.R;
import com.nuages.note.model.Notes;
import com.nuages.note.model.User;
import com.nuages.note.util.BaseTools;

public class MainActivity extends Activity {
	EditText search_edittext;
	TextView yh;
	ImageView wc;
	List<Notes> notes;
	ListView list;
	User user;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AppManager.getAppManager().addActivity(this);
		user = BmobUser.getCurrentUser(this, User.class);
		initView();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		search_edittext.setText("");
//		hideInputType();
		queryNotes();
	}

//	private void hideInputType() {
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//
//			public void run() {
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(search_edittext.getWindowToken(), 0);
//			}
//		}, 100);
//	}

	private void initView() {
		search_edittext = (EditText) findViewById(R.id.search_edittext);

		yh = (TextView) findViewById(R.id.yh);
		list = (ListView) findViewById(R.id.list);
		wc = (ImageView) findViewById(R.id.wc);
		search_edittext.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ("".equals(v.getText().toString())) {
					queryNotes();
				} else {
					queryNotes(v.getText().toString());
				}
				return true;
			}
		});
		yh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(MainActivity.this, UpdatePwdActivity.class);
				startActivity(mIntent);
			}
		});
		wc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(MainActivity.this, AddNotesActivity.class);
				startActivity(mIntent);
			}
		});
	}

	private void initListener() {
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(MainActivity.this, NotesInfoActivity.class);
				mIntent.putExtra("objid", notes.get(arg2).getObjectId());
				startActivity(mIntent);
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setIcon(R.drawable.launch_icon);
				builder.setTitle("是否删除该笔记？");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Notes note = new Notes();
						note.setObjectId(notes.get(arg2).getObjectId());
						note.delete(MainActivity.this, new DeleteListener() {

							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								ToastUtils.show(MainActivity.this, "删除成功！");
								onResume();
							}

							@Override
							public void onFailure(int arg0, String arg1) {
								// TODO Auto-generated method stub
								ToastUtils.show(MainActivity.this, "删除失败");
							}
						});
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// 这里添加点击确定后的逻辑
						dialog.dismiss();
					}
				});
				builder.create().show();
				return false;
			}
		});
	}

	private void queryNotes(String gjz) {
		BmobQuery<Notes> bmobQuery = new BmobQuery<Notes>();
		bmobQuery.order("-updatedAt");
		bmobQuery.setLimit(200);
		bmobQuery.addWhereEqualTo("user", user);
		bmobQuery.addWhereContains("content", gjz);
		bmobQuery.findObjects(this, new FindListener<Notes>() {

			@Override
			public void onSuccess(List<Notes> arg0) {
				// TODO Auto-generated method stub
				if (arg0 != null && arg0.size() > 0) {
					notes = arg0;
					NotesAdapter notesAdapter = new NotesAdapter(arg0);
					list.setAdapter(notesAdapter);
					initListener();
				} else {
					list.setAdapter(null);
					ToastUtils.show(MainActivity.this, "未找到相应的笔记");
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				list.setAdapter(null);
				ToastUtils.show(MainActivity.this, "未找到相应的笔记");
			}

		});
	}

	private void queryNotes() {
		// TODO Auto-generated method stub
		BmobQuery<Notes> bmobQuery = new BmobQuery<Notes>();
		bmobQuery.order("-updatedAt");
		bmobQuery.setLimit(200);
		bmobQuery.addWhereEqualTo("user", user);
		bmobQuery.findObjects(this, new FindListener<Notes>() {

			@Override
			public void onSuccess(List<Notes> arg0) {
				// TODO Auto-generated method stub
				if (arg0 != null && arg0.size() > 0) {
					notes = arg0;
					NotesAdapter notesAdapter = new NotesAdapter(arg0);
					list.setAdapter(notesAdapter);
					initListener();
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}

		});
	}

	class NotesAdapter extends BaseAdapter {
		List<Notes> notes;
		LayoutInflater layoutInflater;

		public NotesAdapter(List<Notes> notes) {
			// TODO Auto-generated constructor stub
			layoutInflater = getLayoutInflater();
			this.notes = notes;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return notes != null ? notes.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return notes.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			convertView = layoutInflater.inflate(R.layout.ai_item, null);

			((TextView) convertView.findViewById(R.id.title)).setText(notes.get(arg0).getTitle());
			((TextView) convertView.findViewById(R.id.content)).setText(notes.get(arg0).getContent());
			((TextView) convertView.findViewById(R.id.time)).setText(notes.get(arg0).getUpdatedAt());

			return convertView;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			BaseTools.ExitApp(MainActivity.this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
