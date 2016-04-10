package com.nuages.note.ui;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;

import com.nuages.note.R;
import com.nuages.note.model.Notes;
import com.nuages.note.model.User;
import com.nuages.note.util.Player;

public class NotesInfoActivity extends Activity {
	private static final int REQUEST_CODE_ALBUM = 1;
	private static final int REQUEST_CODE_CAMERA = 2;
	ActionBar actionbar;
	TextView title, content, tj;
	Player player;
	SeekBar skbProgress;
	ImageView back, voice;
	User user;
	String objString;
	Notes notes;
	GridView noScrollgridview;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				title.setText("标题：" + notes.getTitle());
				content.setText("内容：" + notes.getContent());
				try {
					if (notes.getVoice() != null) {

						voice.setVisibility(View.VISIBLE);
						// skbProgress.setVisibility(View.VISIBLE);
						skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
						player = new Player(skbProgress);
					}
					if (notes.getFilenames() != null && notes.getFilenames().size() > 0) {
						GridAdapter gridAdapter = new GridAdapter(NotesInfoActivity.this, notes.getFilenames());
						noScrollgridview.setAdapter(gridAdapter);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes_info);
		user = BmobUser.getCurrentUser(NotesInfoActivity.this, User.class);
		objString = getIntent().getStringExtra("objid");
		initView();
		queryNotes();
	}

	private void queryNotes() {
		// TODO Auto-generated method stub
		BmobQuery<Notes> notBmobQuery = new BmobQuery<Notes>();
		notBmobQuery.addWhereEqualTo("user", user);
		notBmobQuery.addWhereEqualTo("objectId", getIntent().getStringExtra("objid"));
		notBmobQuery.findObjects(NotesInfoActivity.this, new FindListener<Notes>() {

			@Override
			public void onSuccess(List<Notes> arg0) {
				// TODO Auto-generated method stub
				notes = arg0.get(0);
				if (notes != null) {
					mHandler.sendEmptyMessage(1);
					tj.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent mIntent = new Intent(NotesInfoActivity.this, NotesInfoEditActivity.class);
							mIntent.putExtra("notes", notes);
							startActivity(mIntent);
							finish();
						}
					});
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void initView() {
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		title = (TextView) findViewById(R.id.title);
		content = (TextView) findViewById(R.id.edit_content);
		tj = (TextView) findViewById(R.id.tj);

		skbProgress = (SeekBar) this.findViewById(R.id.skbProgress);

		voice = (ImageView) findViewById(R.id.voice);
		back = (ImageView) findViewById(R.id.back);

		// commitButton = (Button)findViewById(R.id.commit_edit);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		voice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				System.out.println("http://file.bmob.cn/" + notes.getVoice().getUrl());
				player.playUrl("http://file.bmob.cn/" + notes.getVoice().getUrl());
			}
		});
	}

	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
		int progress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
			this.progress = progress * player.mediaPlayer.getDuration() / seekBar.getMax();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
			player.mediaPlayer.seekTo(progress);
		}
	}

	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater; // 视图容器
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;
		List<BmobFile> bmobFiles;
		Context context;

		public GridAdapter(Context context, List<BmobFile> bmobFiles) {
			// TODO Auto-generated constructor stub
			this.bmobFiles = bmobFiles;
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public int getCount() {
			return bmobFiles.size();
		}

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_published_grida, parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			bmobFiles.get(position).loadImageThumbnail(context, holder.image, 300, 300);
			holder.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent mIntent = new Intent(context, HudongImageDialog.class);
					mIntent.putExtra("url", bmobFiles.get(position));
					mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(mIntent);
				}
			});

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

	}
}
