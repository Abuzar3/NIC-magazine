package karbosh.nic;

import android.os.Bundle;

import karbosh.nic.BaseActivity;
import karbosh.nic.SystemHelper;

public abstract class AbstractLockRotationActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setRequestedOrientation(
				//SystemHelper.getScreenOrientation(this);
	}	
	
}
