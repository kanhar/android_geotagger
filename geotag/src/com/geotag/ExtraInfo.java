package com.geotag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ExtraInfo extends Activity implements View.OnClickListener{
	// public class Class1{
// 	}
	public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
	ExtraInfoData data = new ExtraInfoData("ExtraInfo");

	//@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(data.TAG,"onCreate");
		setContentView(R.layout.extra_info);
		Button btnSave=(Button)findViewById(R.id.btn_save);
		Button btnCancel=(Button)findViewById(R.id.btn_cancel);
		data.mTxtDescription = (EditText)findViewById(R.id.txt_extra_info);
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}
	
	//@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_save:
			Intent intent=new Intent();
			intent.putExtra(KEY_DESCRIPTION, data.mTxtDescription.getText().toString());
			Log.d(TakeSnap.TAG,data.mTxtDescription.getText().toString());
			setResult(RESULT_OK, intent);
			finish();
			break;

		case R.id.btn_cancel:
		default:
			setResult(RESULT_CANCELED);
			finish();
			break;
	}
	
	}


}
