package com.nuages.note.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;

/**
 * 
 * <图片压缩> <功能详细描述>
 * 
 * @author cyf
 * @version [版本号, 2013-9-17]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class BitmapUtil {
	/**
	 * 图片大于300k则压缩。
	 */
	private static final int imageMaxSize = 300 * 1024;

	private static final String PACKAGE_NAME = "com.meyao.note";

	/**
	 * 如果没有sd卡，图片保存在此路径
	 */
	private static final String SYSTEM_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/YunNote/";

	/**
	 * 
	 * <图片按比例大小压缩方法（根据路径获取图片并压缩）> <功能详细描述>
	 * 
	 * @param srcPath
	 * @param isNeedTime
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static String getImageByPath(String srcPath) {
		Bitmap bitmap = null;
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		newOpts.inJustDecodeBounds = false;
		int width = newOpts.outWidth;
		int height = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float minHeight = 800f;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (width > height && width > minHeight) {
			// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / minHeight);
		} else if (width < height && height > minHeight) {
			// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / minHeight);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 
	 * <质量压缩方法,并且添加时间水印> <功能详细描述>
	 * 
	 * @param image
	 * @param date
	 * @param isNeedTime
	 *            是否需要添加水印
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	private static String compressImage(Bitmap image) {
		String path = "";
		File file;
		if (image != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Bitmap bitMap = image;
				bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
				int options = 100;
				while (baos.toByteArray().length > imageMaxSize) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
					baos.reset();// 重置baos即清空baos
					bitMap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
					options -= 5;// 每次都减少5
				}
				bitMap.recycle();
				String photoName = System.currentTimeMillis() + ".jpg";
				if (existSDCard()) {
					path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()
							+ "/YunNote/" + photoName;
					file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
							.getPath() + "/YunNote/", photoName);
				} else {
					path = SYSTEM_PATH + photoName;
					file = new File(SYSTEM_PATH, photoName);
				}
				FileOutputStream stream = new FileOutputStream(file);
				if (baos != null) {
					stream.write(baos.toByteArray());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return path;
		} else {
			return "";
		}
	}

	/**
	 * 
	 * <生成缩略图> <功能详细描述>
	 * 
	 * @param nowPhoto
	 * @param zoomToPitmap
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static Bitmap zoomPhoto(Bitmap nowPhoto, Bitmap zoomToPitmap) {
		Bitmap newbmp = ThumbnailUtils.extractThumbnail(nowPhoto, zoomToPitmap.getWidth(), zoomToPitmap.getHeight());
		nowPhoto.recycle();
		return newbmp;
	}

	/**
	 * 
	 * <判断SD卡是否存在> <功能详细描述>
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	private static boolean existSDCard() {
		File file;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YunNote");
			if (!file.exists()) {
				file.mkdirs();
			}
			return true;
		} else {
			file = new File(SYSTEM_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
			return false;
		}
	}

}