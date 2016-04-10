package com.nuages.note.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.trinea.android.common.util.ToastUtils;

import com.nuages.note.R;
import com.nuages.note.model.Notes;
import com.nuages.note.model.User;
import com.nuages.note.takepic.Bimp;
import com.nuages.note.takepic.FileUtils;
import com.nuages.note.takepic.PhotoActivity;
import com.nuages.note.takepic.TestPicActivity;
import com.nuages.note.util.AudioRecorder;

public class AddNotesActivity extends Activity implements OnClickListener {
	private GridView noScrollgridview;
	private GridAdapter adapter;

	String[] arr;

	ActionBar actionbar;
	EditText title, content;

	LinearLayout loading_book;
	LinearLayout voice_layout;

	ImageView wc, back, voice, take_voice;
	// Button commitButton;
	String dateTime;
	User user;
	boolean isUploadPic = false;

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

	boolean isVoice = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnotes);
		user = BmobUser.getCurrentUser(AddNotesActivity.this, User.class);
		Init();
		initListener();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		hideInputType();
	}
//	private void hideInputType() {
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//
//			public void run() {
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
//			}
//		}, 100);
//	}
	public void Init() {
		title = (EditText) findViewById(R.id.title);
		content = (EditText) findViewById(R.id.edit_content);

		voice_layout = (LinearLayout) findViewById(R.id.voice_layout);
		loading_book = (LinearLayout) findViewById(R.id.loading_book);

		take_voice = (ImageView) findViewById(R.id.take_voice);
		voice = (ImageView) findViewById(R.id.voice);
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		wc = (ImageView) findViewById(R.id.wc);
		wc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String commitContent = content.getText().toString().trim();
				if (TextUtils.isEmpty(commitContent)) {
					ToastUtils.show(AddNotesActivity.this, "内容不能为空");
					return;
				}
				loading_book.setVisibility(View.VISIBLE);

				List<String> list = new ArrayList<String>();
				for (int i = 0; i < Bimp.drr.size(); i++) {
					String Str = Bimp.drr.get(i).substring(Bimp.drr.get(i).lastIndexOf("/") + 1,
							Bimp.drr.get(i).lastIndexOf("."));
					list.add(FileUtils.SDPATH + Str + ".JPEG");
				}
				arr = (String[]) list.toArray(new String[Bimp.drr.size()]);

				if (list.size() == 0 && !isVoice) {
					publishWithoutFigure(commitContent, null, null);// 文字
				} else if (list.size() == 0 && isVoice) {
					publishVoice(commitContent);// 声音
				} else if (list.size() > 0 && !isVoice) {
					publish(commitContent);// 图片
				} else {
					publishall(commitContent);// 图片/声音
				}
			}
		});
		// -----------------------------------------------
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new GridAdapter(AddNotesActivity.this);

		 adapter.update();
		noScrollgridview.setAdapter(adapter);
		noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (arg2 == Bimp.bmp.size()) {
					new PopupWindows(AddNotesActivity.this, noScrollgridview);
				} else {
					Intent intent = new Intent(AddNotesActivity.this, PhotoActivity.class);
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});
		// activity_selectimg_send = (TextView)
		// findViewById(R.id.activity_selectimg_send);
		// activity_selectimg_send.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// List<String> list = new ArrayList<String>();
		// for (int i = 0; i < Bimp.drr.size(); i++) {
		// String Str =
		// Bimp.drr.get(i).substring(Bimp.drr.get(i).lastIndexOf("/") + 1,
		// Bimp.drr.get(i).lastIndexOf("."));
		// list.add(FileUtils.SDPATH + Str + ".JPEG");
		// }
		// // 高清的压缩图片全部就在 list 路径里面了
		// // 高清的压缩过的 bmp 对象 都在 Bimp.bmp里面
		// // 完成上传服务器后 .........
		// FileUtils.deleteDir();
		// }
		// });
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		initListener();
	}

	protected void initListener() {

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
											ToastUtils.show(AddNotesActivity.this, "正在播放声音~");
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
						}
					}

					break;
				}
				return true;
			}
		});
	}

	// -----------------------------------------------------------

	/*
	 * 发表带图片
	 */
	private void publish(final String commitContent) {
		Bmob.uploadBatch(AddNotesActivity.this, arr, new cn.bmob.v3.listener.UploadBatchListener() {

			@Override
			public void onSuccess(List<BmobFile> arg0, List<String> arg1) {
				// TODO Auto-generated method stub
				if (arg0.size() == arr.length) {
					publishWithoutFigure(commitContent, arg0, null);
				}
			}

			@Override
			public void onProgress(int arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 发表带声音
	 */
	private void publishVoice(final String commitContent) {
		String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/my/voice.amr";
		final BmobFile figureFile = new BmobFile(new File(url));
		figureFile.upload(AddNotesActivity.this, new UploadFileListener() {

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
				ToastUtils.show(AddNotesActivity.this, "上传文件失败。" + arg1);
			}
		});

	}

	// private void uploadPic() {
	// // TODO Auto-generated method stub
	//
	// }

	/*
	 * 发表带图片和声音
	 */
	private void publishall(final String commitContent) {
		Bmob.uploadBatch(AddNotesActivity.this, arr, new cn.bmob.v3.listener.UploadBatchListener() {

			@Override
			public void onSuccess(final List<BmobFile> arg0, List<String> arg1) {
				// TODO Auto-generated method stub
				String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/my/voice.amr";
				final BmobFile figureFile = new BmobFile(new File(url));
				figureFile.upload(AddNotesActivity.this, new UploadFileListener() {

					@Override
					public void onSuccess() {
						// LogUtils.i(TAG, "上传文件成功。" + figureFile.getFileUrl());
						publishWithoutFigure(commitContent, arg0, figureFile);
					}

					@Override
					public void onProgress(Integer arg0) {

						// TODO Auto-generated method stub

					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						ToastUtils.show(AddNotesActivity.this, "上传文件失败。" + arg1);
					}
				});

			}

			@Override
			public void onProgress(int arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}
		});

	}

	/*
	 * 发表
	 */
	private void publishWithoutFigure(final String commitContent, final List<BmobFile> picFiles,
			final BmobFile voiceFile) {
		Notes notes = new Notes();
		if ("".equals(title.getText().toString())) {
			notes.setTitle("无标题");
		} else {
			notes.setTitle(title.getText().toString());
		}
		notes.setContent(commitContent);
		// notes.setPic(figureFile);
		notes.setFilenames(picFiles);
		notes.setVoice(voiceFile);
		notes.setUser(user);
		notes.save(AddNotesActivity.this, new SaveListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ToastUtils.show(AddNotesActivity.this, "发表成功！");
				// LogUtils.i(TAG,"创建成功。");
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub

			}
		});

	}

	// -----------------------------------------------------------

	// 录音时显示Dialog
	void showVoiceDialog() {
		dialog = new Dialog(AddNotesActivity.this, R.style.DialogStyle);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.setContentView(R.layout.my_dialog);
		dialog_img = (ImageView) dialog.findViewById(R.id.dialog_img);
		dialog.show();
	}

	// 录音时间太短时Toast显示
	void showWarnToast() {
		Toast toast = new Toast(AddNotesActivity.this);
		LinearLayout linearLayout = new LinearLayout(AddNotesActivity.this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(20, 20, 20, 20);

		// 定义一个ImageView
		ImageView imageView = new ImageView(AddNotesActivity.this);
		imageView.setImageResource(R.drawable.voice_to_short); // 图标

		TextView mTv = new TextView(AddNotesActivity.this);
		mTv.setText("时间太短   录音失败");
		mTv.setTextSize(14);
		mTv.setTextColor(Color.WHITE);// 字体颜色
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

	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater; // 视图容器
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public GridAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void update() {
			loading();
		}

		public int getCount() {
			return (Bimp.bmp.size() + 1);
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
			final int coord = position;
			ViewHolder holder = null;
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_published_grida, parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == Bimp.bmp.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.bmp.get(position));
			}

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading() {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Bimp.max == Bimp.drr.size()) {
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							try {
								if (Bimp.drr.size()==0) {
									break;
								}
								String path = Bimp.drr.get(Bimp.max);
								Bitmap bm = Bimp.revitionImageSize(path);
								Bimp.bmp.add(bm);
								String newStr = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
								FileUtils.saveBitmap(bm, "" + newStr);
								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
								break;
							}
						}
					}
				}
			}).start();
		}
	}

	public String getString(String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}

	protected void onRestart() {
		adapter.update();
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Bimp.drr.clear();
		Bimp.bmp.clear();
		Bimp.max = 0 ;
		super.onDestroy();
	}

	// @Override
	// protected void on() {
	// // TODO Auto-generated method stub
	// System.out.println("ssssssssssssssonStop");
	// Bimp.drr.clear();
	// FileUtils.deleteDir();
	// super.onStop();
	// }

	public class PopupWindows extends PopupWindow {

		public PopupWindows(Context mContext, View parent) {

			View view = View.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(AddNotesActivity.this, TestPicActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}

	private static final int TAKE_PICTURE = 0x000000;
	private String path = "";

	/**
	 * 拍照
	 */
	public void photo() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// File file = new File(Environment.getExternalStorageDirectory() +
		// "/myimage/", String.valueOf(System
		// .currentTimeMillis()) + ".jpg");
		File file = new File(Environment.getExternalStorageDirectory(), String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		path = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			if (Bimp.drr.size() < 9 && resultCode == -1) {
				Bimp.drr.add(path);
			}
			break;
		}
	}

}
