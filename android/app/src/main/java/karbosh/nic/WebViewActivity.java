package karbosh.nic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


import karbosh.nic.R;

public class WebViewActivity extends ActionBarActivity {
	
	public static void startWithUrl(Context context, String url) {
		if (url.contains("youtube") || url.contains("dailymotion") || url.contains("vimeo")) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			context.startActivity(intent);
			return;
		}
		Intent webAdvertisingActivityIntent = new Intent(context, WebViewActivity.class);
		webAdvertisingActivityIntent.putExtra(WebViewActivity.PARAM_LINK, url);
		context.startActivity(webAdvertisingActivityIntent);
	}

	public static final String PARAM_LINK = "PARAM_LINK";
	private String advertisingLink;

	private WebView webView;
	private Button doneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		


		//getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_webview);

		//overridePendingTransition(R.anim.flip_right_in, R.anim.flip_left_out);

		advertisingLink = getIntent().getStringExtra(PARAM_LINK);

		webView = (WebView) findViewById(R.id.activity_web_advertising_browser_view);
		doneButton = (Button) findViewById(R.id.activity_web_advertising_button_done);

		prepareBarButtons();
		loadWebContent();
	}

	private void prepareBarButtons() {

		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void loadWebContent() {
		if (advertisingLink != null) {
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new WebViewClient() {

				@Override
				public void onPageFinished(WebView view, String url) {
					setProgressBarIndeterminateVisibility(false);
					super.onPageFinished(view, url);
				}
			});
			webView.loadUrl(advertisingLink);
			setProgressBarIndeterminateVisibility(true);
		}
	}
	



}
