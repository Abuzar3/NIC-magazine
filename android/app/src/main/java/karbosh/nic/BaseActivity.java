package karbosh.nic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;



import karbosh.nic.UpdateIndeterminateProgressBarEvent;

import karbosh.nic.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class BaseActivity extends FragmentActivity implements IBaseContext {
	private static final String TAG = "BaseActivity";

	public static final String TEST_INIT_COMPLETE = "TEST_INIT_COMPLETE";
	protected static final int CONNECTION_ALERT = 1;
	protected static final int SERVER_ALERT = 2;
	protected static final int DOWNLOAD_ALERT = 3;
	protected static final int IO_EXCEPTION = 4;

	private SharedPreferences sharedPreferences;

    /**
	 * A {@link PurchaseObserver} is used to get callbacks when Android Market
	 * sends messages to this application so that we can update the UI.
	 */

	/**
	 * Replaces the language and/or country of the device into the given string.
	 * The pattern "%lang%" will be replaced by the device's language code and
	 * the pattern "%region%" will be replaced with the device's country code.
	 * 
	 * @param str
	 *            the string to replace the language/country within
	 * @return a string containing the local language and region codes
	 */
	protected String replaceLanguageAndRegion(String str) {
		// Substitute language and or region if present in string
		if (str.contains("%lang%") || str.contains("%region%")) {
			Locale locale = Locale.getDefault();
			str = str.replace("%lang%", locale.getLanguage().toLowerCase(Locale.getDefault()));
			str = str.replace("%region%", locale.getCountry().toLowerCase(Locale.getDefault()));
		}
		return str;
	}
	
	protected int getUpdatePeriod() {
		return 1800000;
	}

	@Override
	public boolean isOnline() {
		return true;
	}

	@Override
	public SharedPreferences getPreferences() {
		if (null == sharedPreferences) {
			sharedPreferences = getSharedPreferences(LIBRELIO_SHARED_PREFERENCES, MODE_PRIVATE);
		}
		return sharedPreferences;
	}

	/**
	 * Creates storage directories if necessary
	 */

	/**
	 * Copy files from android assets directory
	 * 
	 * @param src
	 *            the source target
	 * @param dst
	 *            the destination target
	 */
	protected int copyFromAssets(String src, String dst){
		int count = -1;
		Log.d(TAG, "copyFromAssets " + src + " => " + dst);
		try {
			InputStream input = getAssets().open(src);
			OutputStream output = new FileOutputStream(dst);
			byte data[] = new byte[1024];

			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
		} catch (IOException e) {
			Log.e(TAG, "copyFromAssets failed", e);
		}
		return count;
	}

	protected void enableRotation(boolean isEnable) {
		android.provider.Settings.System.putInt(
				getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, isEnable ? 1 : 0);
	}
	

	
	@Override
	protected void onStart(){
		super.onStart();

    }
	
	@Override
	protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(UpdateIndeterminateProgressBarEvent event) {
        setProgressBarIndeterminateVisibility(event.isShowProgress());
    }

}
