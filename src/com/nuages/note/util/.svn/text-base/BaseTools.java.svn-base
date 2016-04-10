package com.nuages.note.util;

import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.entity.FailedReason;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.service.impl.ImageCache;
import cn.trinea.android.common.service.impl.ImageCache.CompressListener;
import cn.trinea.android.common.service.impl.ImageMemoryCache.OnImageCallbackListener;
import cn.trinea.android.common.util.CacheManager;
import cn.trinea.android.common.util.FileUtils;

import com.nuages.note.AppManager;

public class BaseTools {
	public static boolean flag = false;
	/** image cache **/
	public static final ImageCache IMAGE_CACHE = new ImageCache();
	public static final ImageCache IMAGE_CACHE2 = CacheManager.getImageCache();

	static {
		// final BaseTools baseTools = new BaseTools();
		OnImageCallbackListener imageCallBack = new OnImageCallbackListener() {

			@Override
			public void onPreGet(String imageUrl, View view) {
				// Log.e(TAG_CACHE, "pre get image");
			}

			@Override
			public void onGetSuccess(String imageUrl, Bitmap loadedImage, View view, boolean isInCache) {
				System.out.println("-----onGetSuccess");
				if (view != null && loadedImage != null && view instanceof ImageView) {
					ImageView imageView = (ImageView) view;
					imageView.setImageBitmap(loadedImage);
				}
			}

			@Override
			public void onGetFailed(String imageUrl, Bitmap loadedImage, View view, FailedReason failedReason) {
				// ((ImageView) view).setImageResource(R.drawable.cpimage);
				System.out.println("-----onGetFailed");
			}

			@Override
			public void onGetNotInCache(String imageUrl, View view) {
				System.out.println("-----onGetNotInCache" + imageUrl);
				IMAGE_CACHE2.get(imageUrl, (ImageView) view);
			}
		};
		IMAGE_CACHE.setCacheFullRemoveType(new CacheFullRemoveType<Bitmap>() {

			@Override
			public int compare(CacheObject<Bitmap> obj1, CacheObject<Bitmap> obj2) {
				// TODO Auto-generated method stub
				return (obj2.getPriority() > obj1.getPriority()) ? 1 : ((obj2.getPriority() == obj1.getPriority()) ? 0
						: -1);
			}
		});
		IMAGE_CACHE.setCompressListener(new CompressListener() {

			@Override
			public int getCompressSize(String imagePath) {
				long fileSize = FileUtils.getFileSize(imagePath) / 1000;
				/**
				 * if image bigger than 100k, compress to 1/(n + 1) width and
				 * 1/(n + 1) height, n is fileSize / 100k
				 **/
				if (fileSize > 1000) {
					return (int) (fileSize / 1000) + 1;
				}
				return 1;
			}
		});
		IMAGE_CACHE.setOnImageCallbackListener(imageCallBack);
	}

	public final static int getWindowsWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	private static long exitTime = 0;

	public static void ExitApp(Context context) {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			try {
				BaseTools.IMAGE_CACHE.saveDataToDb(context, "image");
				AppManager.getAppManager().finishAllActivity();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 获得屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 获得屏幕宽度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.heightPixels;
	}

	public final static ImageGetter imgGetter = new Html.ImageGetter() {
		@SuppressLint("NewApi")
		public Drawable getDrawable(String source) {
			Drawable drawable = null;
			URL url;
			try {
				url = new URL(source);
				if (android.os.Build.VERSION.SDK_INT > 9) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				drawable = Drawable.createFromStream(url.openStream(), ""); // 获取网路图片
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth() + 10, drawable.getIntrinsicHeight() + 10);
			return drawable;
		}
	};
}
