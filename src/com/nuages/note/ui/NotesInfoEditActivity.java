package com.nuages.note.ui;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.trinea.android.common.util.ToastUtils;

import com.nuages.note.R;
import com.nuages.note.model.Notes;
import com.nuages.note.model.User;
import com.nuages.note.ui.NotesInfoActivity.SeekBarChangeEvent;
import com.nuages.note.util.AudioRecorder;
import com.nuages.note.util.Player;

public class NotesInfoEditActivity extends Activity implements OnClickListener {
	private GridView noScrollgridview;
	private static final int REQUEST_CODE_ALBUM = 1;
	private static final int REQUEST_CODE_CAMERA = 2;
	ActionBar actionbar;
	EditText title, content;

	LinearLayout loading_book, pic_content;
	LinearLayout voice_layout;

	ImageView wc, back, voice;
	String dateTime;
	User user;
	BmobFile delBmobFileFile;
	Notes notes;
	TextView title_title;
	Player player;
	SeekBar skbProgress;
	private AudioRecorder mr;
	private static boolean playState = false; // 播放状态
	private MediaPlayer mediaPlayer;
	private Thread recordThread;
	private Dialog dialog;
	private ImageView dialog_img;
	private static int MAX_TIME = 15; // 最长录制时间，单位秒，0为无时间限制
	private static int MIX_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1
	private static int RECORD_NO = 0; // 不在录音
	private static int RECORD_ING = 1; // 正在录音
	private static int RECODE_ED = 2; // 完成录音
	private static int RECODE_STATE = 0; // 录音的状态
	private static float recodeTime = 0.0f; // 录音的时间
	private static double voiceValue = 0.0; // 麦克风获取的音量值

	boolean isDelPic;
	boolean isVoice = false;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				title.setText(notes.getTitle());
				content.setText(notes.getContent());
				try {
					if (notes.getVoice() != null) {

						voice.setVisibility(View.VISIBLE);
						// skbProgress.setVisibility(View.VISIBLE);
						skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
						player = new Player(skbProgress);
						voice.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								System.out.println("http://file.bmob.cn/" + notes.getVoice().getUrl());
								player.playUrl("http://file.bmob.cn/" + notes.getVoice().getUrl());
							}
						});
					}
					if (notes.getFilenames() != null && notes.getFilenames().size() > 0) {
						GridAdapter gridAdapter = new GridAdapter(NotesInfoEditActivity.this, notes.getFilenames());
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
		setContentView(R.layout.addnotes);
		user = BmobUser.getCurrentUser(NotesInfoEditActivity.this, User.class);
		initView();
		initListener();
		queryNotes();
	}

	private void queryNotes() {
		// TODO Auto-generated method stub
		notes = (Notes) getIntent().getSerializableExtra("notes");
		if (notes != null) {
			mHandler.sendEmptyMessage(1);

		}
	}

	private void initView() {
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		title_title = (TextView) findViewById(R.id.title_title);
		title_title.setText("笔记编辑");
		title = (EditText) findViewById(R.id.title);
		content = (EditText) findViewById(R.id.edit_content);
		pic_content = (LinearLayout) findViewById(R.id.pic_content);
		skbProgress = (SeekBar) this.findViewById(R.id.skbProgress);
		// pic_content.setVisibility(View.GONE);
		voice_layout = (LinearLayout) findViewById(R.id.voice_layout);
		loading_book = (LinearLayout) findViewById(R.id.loading_book);

		voice = (ImageView) findViewById(R.id.voice);
		back = (ImageView) findViewById(R.id.back);
		wc = (ImageView) findViewById(R.id.wc);
		// commitButton = (Button)findViewById(R.id.commit_edit);

		wc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final String commitContent = content.getText().toString().trim();
				if (TextUtils.isEmpty(commitContent)) {
					ToastUtils.show(NotesInfoEditActivity.this, "内容不能为空");
					return;
				}
				loading_book.setVisibility(View.VISIBLE);

				// if (targeturl == null && !isVoice) {
				if (!isVoice) {
					publishWithoutFigure(commitContent, null, null);// 文字
				} else {
					publishVoice(commitContent);// 声音
				}
				// } else if (targeturl == null && isVoice) {
				// publishVoice(commitContent);// 声音
				// } else if (targeturl != null && !isVoice) {
				// publish(commitContent);// 图片
				// } else {
				// publishall(commitContent);// 图片/声音
				// }
			}
			// }
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.open_layout:
			Date date1 = new Date(System.currentTimeMillis());
			dateTime = date1.getTime() + "";
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent, REQUEST_CODE_ALBUM);
			break;
		case R.id.take_layout:
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent2.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.png")));
			startActivityForResult(intent2, REQUEST_CODE_CAMERA);
			break;
		default:
			break;
		}
	}
	/*
	 * 发表带声音
	 */
	private void publishVoice(final String commitContent) {
		String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/my/voice.amr";
		final BmobFile figureFile = new BmobFile(new File(url));
		figureFile.upload(NotesInfoEditActivity.this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				// LogUtils.i(TAG, "上传文件成功。" + figureFile.getFileUrl());
				publishWithoutFigure(commitContent, null, figureFile);
			}

			@Override
			public void onProgress(Integer arg0) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ToastUtils.show(NotesInfoEditActivity.this, "上传文件失败。" + arg1);
			}
		});

	}
	/*
	 * 发表
	 */
	private void publishWithoutFigure(final String commitContent, final List<BmobFile> picFiles,
			final BmobFile voiceFile) {
		Notes note = new Notes();
		note.setObjectId(notes.getObjectId());
		if ("".equals(title.getText().toString())) {
			note.setTitle("无标题");
		} else {
			note.setTitle(title.getText().toString());
		}
		note.setContent(commitContent);
		// notes.setPic(figureFile);
		note.setFilenames(picFiles);
		note.setVoice(voiceFile);
		note.setUser(user);
		note.update(NotesInfoEditActivity.this, new UpdateListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ToastUtils.show(NotesInfoEditActivity.this, "编辑成功！");
				finish();
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				ToastUtils.show(NotesInfoEditActivity.this, "编辑失败！"+arg1);
			}
		});
	}

	// -----------------------------------------------------------

	protected void initListener() {
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// 发送声音：
		voice_layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (RECODE_STATE != RECORD_ING) {
						mr = new AudioRecorder("voice");
						RECODE_STATE = RECORD_ING;
						showVoiceDialog();
						try {
							mr.start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mythread();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE = RECODE_ED;
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
						try {
							mr.stop();
							voiceValue = 0.0;
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (recodeTime < MIX_TIME) {
							showWarnToast();
							RECODE_STATE = RECORD_NO;
						} else {
							voice.setVisibility(View.VISIBLE);
							isVoice = true;
							voice.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									int text_id = v.getId();
									// TODO Auto-generated method stub
									if (!playState) {
										mediaPlayer = new MediaPlayer();
										String url = Environment.getExternalStorageDirectory().getAbsolutePath()
												+ "/my/voice.amr";
										try {
											mediaPlayer.setDataSource(url);
											// mediaPlayer.setDataSource(getAmrPath());
											mediaPlayer.prepare();
											mediaPlayer.start();
											ToastUtils.show(NotesInfoEditActivity.this, "正在播放声音~");
											playState = true;
											// 设置播放结束时监听
											mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

												@Override
												public void onCompletion(MediaPlayer mp) {
													if (playState) {
														playState = false;
													}
												}
											});
										} catch (IllegalArgumentException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IllegalStateException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									} else {
										if (mediaPlayer.isPlaying()) {
											mediaPlayer.stop();
											playState = false;
										} else {
											playState = false;
										}
										// player.setText("播放录音");
									}
								}
							});
							// record.setText("录音完成!点击重新录音");
							// luyin_txt.setText("录音时间："+((int)recodeTime));
							// luyin_path.setText("文件路径："+getAmrPath());
						}
					}

					break;
				}
				return true;
			}
		});
	}

	// 录音时显示Dialog
	void showVoiceDialog() {
		dialog = new Dialog(NotesInfoEditActivity.this, R.style.DialogStyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.setContentView(R.layout.my_dialog);
		dialog_img = (ImageView) dialog.findViewById(R.id.dialog_img);
		dialog.show();
	}

	// 录音时间太短时Toast显示
	void showWarnToast() {
		Toast toast = new Toast(NotesInfoEditActivity.this);
		LinearLayout linearLayout = new LinearLayout(NotesInfoEditActivity.this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(20, 20, 20, 20);

		// 定义一个ImageView
		ImageView imageView = new ImageView(NotesInfoEditActivity.this);
		imageView.setImageResource(R.drawable.voice_to_short); // 图标

		TextView mTv = new TextView(NotesInfoEditActivity.this);
		mTv.setText("时间太短   录音失败");
		mTv.setTextSize(14);
		mTv.setTextColor(Color.WHITE);// 字体颜色
		// mTv.setPadding(0, 10, 0, 0);

		// 将ImageView和ToastView合并到Layout中
		linearLayout.addView(imageView);
		linearLayout.addView(mTv);
		linearLayout.setGravity(Gravity.CENTER);// 内容居中
		linearLayout.setBackgroundResource(R.drawable.record_bg);// 设置自定义toast的背景

		toast.setView(linearLayout);
		toast.setGravity(Gravity.CENTER, 0, 0);// 起点位置为中间 100为向下移100dp
		toast.show();
	}

	// 录音计时线程
	void mythread() {
		recordThread = new Thread(ImgThread);
		recordThread.start();
	}

	// 录音Dialog图片随声音大小切换
	void setDialogImage() {
		if (voiceValue < 200.0) {
			dialog_img.setImageResource(R.drawable.record_animate_01);
		} else if (voiceValue > 200.0 && voiceValue < 400) {
			dialog_img.setImageResource(R.drawable.record_animate_02);
		} else if (voiceValue > 400.0 && voiceValue < 800) {
			dialog_img.setImageResource(R.drawable.record_animate_03);
		} else if (voiceValue > 800.0 && voiceValue < 1600) {
			dialog_img.setImageResource(R.drawable.record_animate_04);
		} else if (voiceValue > 1600.0 && voiceValue < 3200) {
			dialog_img.setImageResource(R.drawable.record_animate_05);
		} else if (voiceValue > 3200.0 && voiceValue < 5000) {
			dialog_img.setImageResource(R.drawable.record_animate_06);
		} else if (voiceValue > 5000.0 && voiceValue < 7000) {
			dialog_img.setImageResource(R.drawable.record_animate_07);
		} else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_08);
		} else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_09);
		} else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_10);
		} else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_11);
		} else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_12);
		} else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_13);
		} else if (voiceValue > 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_14);
		}
	}

	// 录音线程
	private Runnable ImgThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (RECODE_STATE == RECORD_ING) {
				if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
					imgHandle.sendEmptyMessage(0);
				} else {
					try {
						Thread.sleep(200);
						recodeTime += 0.2;
						if (RECODE_STATE == RECORD_ING) {
							voiceValue = mr.getAmplitude();
							imgHandle.sendEmptyMessage(1);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		Handler imgHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 0:
					// 录音超过15秒自动停止
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE = RECODE_ED;
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
						try {
							mr.stop();
							voiceValue = 0.0;
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (recodeTime < 1.0) {
							showWarnToast();
							RECODE_STATE = RECORD_NO;
						} else {
							voice.setVisibility(View.VISIBLE);
							isVoice = true;

						}
					}
					break;
				case 1:
					setDialogImage();
					break;
				default:
					break;
				}

			}
		};
	};

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
		public View getView(int position, View convertView, ViewGroup parent) {
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

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

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
}
