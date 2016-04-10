package com.nuages.note.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;
import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.ToastUtils;

import com.nuages.note.AppManager;
import com.nuages.note.R;
import com.nuages.note.model.User;
import com.nuages.note.util.Util;

/**
 * 修改密码界面
 * 
 * @date 2014-4-24
 * @author Stone
 */
public class UpdatePwdActivity extends Activity implements OnClickListener {

	private Button btnReg;
	private EditText y_password;
	private EditText etPassword;
	private EditText etComfirmPsd;

	private String ypassword = null;
	private String password = null;
	private String comfirmPsd = null;
	User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updatepwd);
		user = BmobUser.getCurrentUser(this, User.class);
		y_password = (EditText) findViewById(R.id.y_password);
		etPassword = (EditText) findViewById(R.id.et_password);
		etComfirmPsd = (EditText) findViewById(R.id.et_comfirm_psd);

		btnReg = (Button) findViewById(R.id.btn_reg_now);
		btnReg.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_reg_now:
			ypassword = y_password.getText().toString();
			password = etPassword.getText().toString();
			comfirmPsd = etComfirmPsd.getText().toString();
			if (!Util.isNetworkConnected(this)) {
				toast("木有网络 ( ⊙ o ⊙ ) ");
			} else if (ypassword.equals("") || password.equals("") || comfirmPsd.equals("")) {
				toast("信息不填完整 ");
			} else if (!comfirmPsd.equals(password)) {
				toast("两次密码输入不一致");
			} else if (!PreferencesUtils.getString(this, "password").equals(ypassword)) {
				toast("原密码输入错误");
			} else {

				user.setPassword(password);
				user.update(UpdatePwdActivity.this, new UpdateListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						ToastUtils.show(UpdatePwdActivity.this, "修改密码成功！");
						Intent mIntent = new Intent(UpdatePwdActivity.this,LoginActivity.class);
						AppManager.getAppManager().finishAllActivity();
						finish();
						startActivity(mIntent);
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub

					}
				});
			}
			break;

		default:
			break;
		}
	}

	public void toast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	};

}
