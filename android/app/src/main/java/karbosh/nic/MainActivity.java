package karbosh.nic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.provider.OpenableColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;





public class MainActivity extends ActionBarActivity implements karbosh.nic.AsyncTaskCompleteListener<String>,ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    DownloadManager manager;
    SharedPreferences preferenceManager;
    SharedPreferences.Editor prefEdit;
    MySimpleArrayAdapter aradpt;
    ProgressBar prog;
    Intent mainint;
    static String base_url; //The website which handle upload & download PDF file
    static String imsi;     //The device ID it can use if we wont to implements subscription
    static boolean first = true;
    static boolean paused = true;
    SectionsPagerAdapter mSectionsPagerAdapter;
    public boolean site_down;
    Context forClear = this;// use to pass context to class clear history
    ListView item_list;     // A list View to show downloaded PDF
    String site ="http://nic.gov.sd/1255/";
    String site2 = "https://www.facebook.com/nicsudan/";
    Fragment toWV;
    ProgressBar progressBar;
    String rssFeedUrl = "http://www.aljazeera.net/aljazeerarss/3c66e3fb-a5e0-4790-91be-ddb05ec17198/4e9f594a-03af-4696-ab98-880c58cd6718";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    public  Activity pdfactivity = this;

    //@Override
    public void onTaskComplete(String result) {
        if(result != "")
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    /**
     * Class to check connectivity state
     * @return
     */
    public boolean isOnline() {


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Class to delete all downloaded PDF when user select Menu "clear my publication"
     * @param context
     */

    public static void clear_history(Context context) {

        File cache = context.getFilesDir();
        String orgFile = cache.getAbsolutePath();
        File appDir = new File(cache.getParent());
        if (cache.exists()) {
            String[] children = cache.list();
            
            for (String s : children) {
                File f = new File(cache, s);

                    if (deleteDir(f))
                        Toast.makeText(context, "Clear ...("+ s +")", Toast.LENGTH_LONG).show();
                    // Log.i(TAG, String.format("**************** DELETED -> (%s) *******************", f.getAbsolutePath()));
                }

        }
    }
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    public void MySimpleArrayAdapter_clear(){

                prefEdit.clear();
                prefEdit.commit();
                String files = preferenceManager.getString("filelist", "");
                String[] filelist = (files == "") ? new String[] {} : files.split("#");
                aradpt=new MySimpleArrayAdapter(forClear, filelist);
                aradpt.notifyDataSetChanged();
                finish();
                startActivity(mainint);

    }

    /**
     * MainActivity onResume
     * Reread File list
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            String filelist = preferenceManager.getString("filelist", "");
            String[] files = filelist.split("#");
            for (String file : files) {
                File f = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        file);
                if (f.exists())
                    f.delete();
            }
            startActivity(getIntent());
            paused = false;
        }



    }

    /**
     * MainActivity onPause
     */
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;


    }

    /**
     * Instance the RssFragment
     * @param rssFeedUrl
     * @return
     */
    public static Fragment newInstanceRss(String rssFeedUrl) {
        RssFragment f = new RssFragment();
        Bundle a = new Bundle();
        a.putString(RssFragment.RSS_FEED_URL, rssFeedUrl);
        f.setArguments(a);
        return f;
    }

    /**
     * Instance the WEbViewFragment
     * @param url
     * @return
     */
    public static Fragment newInstanceWV(String url) {
        Fragment f = new WebViewFragmentS();
        Bundle a = new Bundle();
        a.putString(WebViewFragmentS.URL, url);
        f.setArguments(a);
        return f;
    }

    /**
     * MainActivity onCreate
     * @param savedInstanceState
     */
    @Override   
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        //Resources res = getResources();
       // Drawable drawable = res.getDrawable(R.drawable.background);

        progressBar = (ProgressBar) findViewById(R.id.downloadProgress);
        //progressBar.setProgressDrawable(drawable);
       //progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        prefEdit = preferenceManager.edit();
        base_url = getString(R.string.base_url);
        //TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //imsi = mTelephonyMgr.getSubscriberId();
        imsi="11223344556677";
        //msisdn = mTelephonyMgr.getLine1Number();
        String files = preferenceManager.getString("filelist", "");
        String[] filelist = (files == "") ? new String[] {} : files.split("#");
        aradpt = new MySimpleArrayAdapter(this, filelist);

        mainint = getIntent();
        
        new Requester(this).execute(getString(R.string.base_url)+"index.php/welcome/registerdemo/"+imsi);

        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {

            case R.id.clear_history:

                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.btn_clear_history);
                adb.setMessage(R.string.confirm_clear );

                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MySimpleArrayAdapter_clear();
                        clear_history(forClear);
                    }
                });
                adb.show();


            return true;
            case R.id.action_settings:
                  Intent intent = new Intent(pdfactivity, FullscreenActivity.class);
                 //intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, pathtofile

                startActivity(intent);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment fragment;

            switch (position){
                case 0:
                    fragment=new ItemListFragment();
                    break;
                case 1:
                    fragment=new toDayNPFragment();
                    break;
                case 2:

                    fragment =  newInstanceRss(rssFeedUrl);
                    break;
                case 3:
                    toWV = newInstanceWV(site2);
                    fragment = toWV;
                    break;
                case 4:
                    //toWV = newInstanceWV(site2);
                    fragment = newInstanceWV(site);
                    break;
                default:
                    fragment = new ItemListFragment();
                    break;

            }
            return fragment;
            
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 4:
                    return getString(R.string.title_section5).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }


    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        
        private final Context context;
        private ArrayList<String> values;

        public MySimpleArrayAdapter(Context context, String[] values) {
            super(context, R.layout.item, values);
            this.context = context;
            this.values = new ArrayList<String>(Arrays.asList(values));
            Collections.sort(this.values);
            Collections.reverse(this.values);
        }
        
        public void addItem(String name) {
            values.add(name);
            Collections.sort(this.values);
            Collections.reverse(this.values);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public String getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item, parent, false);
            TextView textView = (TextView) rowView
                    .findViewById(R.id.item_title);
            TextView infoView = (TextView) rowView.findViewById(R.id.item_info);
            if (preferenceManager.getBoolean(values.get(position), false))
                infoView.setText("Open");
            else
                infoView.setText("Downloading..");
            ImageView imageView = (ImageView) rowView
                    .findViewById(R.id.item_icon);
            textView.setText(values.get(position).substring(0,
                    values.get(position).length() - 4));

            String newspaper = preferenceManager.getString(
                    "n" + values.get(position), "");
            int id = getResources().getIdentifier("d" + newspaper, "drawable",
                    "karbosh.nic");
            imageView.setImageResource(id);
            Resources res = getResources();
            Drawable drawable = res.getDrawable(R.drawable.background);
            prog = (ProgressBar) rowView.findViewById(R.id.downloadProgress);
            //prog.setProgressDrawable(drawable);
            //prog.setProgress(0);

            return rowView;
        }
    }
    public class ItemListFragment extends Fragment {
        public String pathtofile;
        String filelist[];
        ListView lv;
        Context mContext;
        String url = base_url+"index.php/welcome/get_file/";
        /**
        @Override
        public void onResume() {
            super.onResume();

            aradpt = new MySimpleArrayAdapter(forClear, filelist);
            aradpt.notifyDataSetChanged();
             lv.setAdapter(aradpt);
        }
        **/

        public ItemListFragment(){

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.item_list, container,
                    false);

            mContext = rootView.getContext();

            lv = (ListView) rootView.findViewById(R.id.listView);

            lv.setAdapter(aradpt);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    String fName = aradpt.getItem(position);
                    String newspaper = preferenceManager.getString("n" + fName,
                            "");
                    openFile(url + "/" + fName + "/" + imsi, fName, newspaper);
                }
            });
          
           
            item_list=lv;
            return rootView;
        }

        public boolean isDownloadManagerAvailable(Context context) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName("com.android.providers.downloads.ui",
                        "com.android.providers.downloads.ui.DownloadList");
                List<ResolveInfo> list = context.getPackageManager()
                        .queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            } catch (Exception e) {
                return false;
            }
        }

        public File copyFileToPublic(File src) {
            FileInputStream in = null;
            FileOutputStream out = null;
            File dest = null;

            try {
                dest = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        src.getName());
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return dest;
        }

        public File moveFileToPrivate(File src) {
            FileInputStream in = null;
            FileOutputStream out = null;
            File dest = new File(getFilesDir(), src.getName());
            try {
                in = new FileInputStream(src);
                out = mContext.openFileOutput(src.getName(),
                        Context.MODE_WORLD_READABLE);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    src.exists();
                    src.delete();
                    src.exists();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return dest;
        }

        public void openFile(String url, String fileName, String newspaper) {
            File file = new File(getFilesDir(), fileName);
             pathtofile = file.getAbsolutePath();
            if (!file.exists()) {
                Toast.makeText(mContext, "File Not Found.. Downloading",
                        Toast.LENGTH_LONG).show();
                prefEdit.putBoolean(fileName, false);
                downloadFile(url, fileName, newspaper);
            } else {
               // //File pub = copyFileToPublic(file);
              //  Intent intent = new Intent(pdfactivity, myPdfViewerActivity.class);
              // intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, pathtofile
              //  );
              //  startActivity(intent);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
               intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
               startActivity(intent);
            }
        }

        public void downloadFile(String url, String fileName, String newspaper) {
            if (isOnline()) {
                first = true;
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setDescription("Issue Download");
                request.setTitle(fileName);
                // in order for this if to run, you must use the android 3.2 to
                // compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                }
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName);

                // get download service and enqueue file
                long download_id = manager.enqueue(request);

                prefEdit.putString("filename", fileName);
                prefEdit.putString("n" + fileName, newspaper);
                prefEdit.putLong("downloadid", download_id);
                prefEdit.commit();

                BroadcastReceiver onComplete = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        if (first) {
                            DownloadManager.Query qry = new DownloadManager.Query();
                            qry.setFilterById(preferenceManager.getLong(
                                    "downloadid", 0));
                            Cursor cur = manager.query(qry);

                            if (cur.moveToFirst()) {
                                String filename = preferenceManager.getString(
                                        "filename", "");
                                File file = new File(
                                        Environment
                                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                        filename);
                                moveFileToPrivate(file);

                                String files = preferenceManager.getString(
                                        "filelist", "");
                                files = (files.equals("")) ? filename
                                        : filename + "#" + files;
                                prefEdit.putString("filelist", files);
                                prefEdit.putBoolean(filename, true);
                                prefEdit.commit();
                                Toast.makeText(mContext,
                                        "File " + filename + " Downloaded",
                                        Toast.LENGTH_SHORT).show();
                                mViewPager.setCurrentItem(0);
                                aradpt.addItem(filename);
                            }
                            first = false;
                        }
                    }
                };
                registerReceiver(onComplete, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            } else {
                Toast.makeText(mContext, "Please Connect to the Internet",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class WebAppInterface {
        Context mContext;
        private int progressStatus = 0;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        public boolean isDownloadManagerAvailable(Context context) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName("com.android.providers.downloads.ui",
                        "com.android.providers.downloads.ui.DownloadList");
                List<ResolveInfo> list = context.getPackageManager()
                        .queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            } catch (Exception e) {
                return false;
            }
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }

        public File copyFileToPublic(File src) {
            FileInputStream in = null;
            FileOutputStream out = null;
            File dest = null;

            try {
                dest = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        src.getName());
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return dest;
        }

        public File moveFileToPrivate(File src) {
            FileInputStream in = null;
            FileOutputStream out = null;
            File dest = new File(getFilesDir(), src.getName());
            try {
                in = new FileInputStream(src);
                out = mContext.openFileOutput(src.getName(),
                        Context.MODE_WORLD_READABLE);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    src.exists();
                    src.delete();
                    src.exists();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return dest;
        }

        @JavascriptInterface
        public void openFile(String url, String fileName, String newspaper) {
            File file = new File(getFilesDir(), fileName);
            if (!file.exists()) {
                Toast.makeText(mContext, "File Not Found.. Downloading",
                        Toast.LENGTH_LONG).show();
                prefEdit.putBoolean(fileName, false);
                downloadFile(url, fileName, newspaper);
            } else {
                //File pub = copyFileToPublic(file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        }

        @JavascriptInterface
        public void downloadFile(String url, String fileName, String newspaper) {

            if (isOnline()) {
                first = true;
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setDescription("Issue Download");
                request.setTitle(fileName);
                // in order for this if to run, you must use the android 3.2 to
                // compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
                }
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName);

                // get download service and enqueue file
                final long download_id = manager.enqueue(request);

                prefEdit.putString("filename", fileName);
                prefEdit.putString("n" + fileName, newspaper);
                prefEdit.putLong("downloadid", download_id);
                prefEdit.commit();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewPager.setCurrentItem(0);
                        aradpt.addItem(preferenceManager.getString("filename",
                                ""));
                       new DownloadProgress().execute(download_id);

                    }
                });

                BroadcastReceiver onComplete = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        if (first) {
                            DownloadManager.Query qry = new DownloadManager.Query();
                            qry.setFilterById(preferenceManager.getLong(
                                    "downloadid", 0));
                            Cursor cur = manager.query(qry);

                            if (cur.moveToFirst()) {


                                String filename = preferenceManager.getString(
                                        "filename", "");
                                File file = new File(
                                        Environment
                                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                        filename);
                                moveFileToPrivate(file);

                                String files = preferenceManager.getString(
                                        "filelist", "");
                                files = (files.equals("")) ? filename
                                        : filename + "#" + files;
                                prefEdit.putString("filelist", files);
                                prefEdit.putBoolean(filename, true);
                                prefEdit.commit();
                                Toast.makeText(mContext,
                                        "File " + filename + " Downloaded",
                                        Toast.LENGTH_SHORT).show();
                                        cur.close();
                                finish();
                                startActivity(mainint);
                            }
                            first = false;
                        }
                    }
                };
                registerReceiver(onComplete, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            } else {
                Toast.makeText(mContext, "Please Connect to the Internet",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    public class Requester extends AsyncTask<String, Void, String> {
        private AsyncTaskCompleteListener<String> callback;

        public Requester(AsyncTaskCompleteListener<String> callback){
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... args) {
            
            try {
                HttpClient cl = new DefaultHttpClient();
                HttpPost post = new HttpPost(args[0]);
                HttpResponse resp = cl.execute(post);
                StatusLine status = resp.getStatusLine();
                StringBuilder builder = new StringBuilder();
                HttpEntity entity = resp.getEntity();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(entity.getContent()));
                String line;
                //if (status.getStatusCode() != HttpStatus.SC_OK){
                   // site_down=true;
                 //   return "";}
               // else {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                entity.consumeContent();

                return builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            callback.onTaskComplete(result);
        }
    }
    public class DownloadProgress extends AsyncTask<Long, Integer, Void> {

        int i;
        @Override
        protected Void doInBackground(Long... arg0) {
            long download_id = arg0[0];
            boolean downloading = true;
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(download_id);
            Cursor cursor = manager.query(q);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            //long size = cursor.getLong(sizeIndex);
            while (downloading) {

                cursor.moveToFirst();
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                double bytes_downloaded = cursor
                        .getDouble(cursor
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                double bytes_total = cursor
                        .getDouble(cursor
                                .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));


                if (cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;

                }

                i = (int) (bytes_downloaded * 100D / bytes_total);
                publishProgress(i);
                //Log.d(LOG_TAG, "bytes download: " + bytes_downloaded );

            }
            cursor.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Log.d(LOG_TAG, "Progress Update: " + values[0].toString());
            super.onProgressUpdate(values);

            prog.setProgress(values[0]);

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            //Log.i(LOG_TAG, "Post-Execute: " + result);
            super.onPostExecute(result);

          // prog.setVisibility(View.INVISIBLE);
        }
    }

    public  class toDayNPFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater
                    .inflate(R.layout.webview, container, false);
            WebView wv = (WebView) rootView.findViewById(R.id.browser);


            if (isOnline()) {
                if (!site_down) {

                    wv.getSettings().setJavaScriptEnabled(true);
                    wv.addJavascriptInterface(
                            new WebAppInterface(rootView.getContext()), "Android");

                    wv.loadUrl(base_url + "index.php/welcome/showlist/" + imsi);
                } else {
                    String customHtml = "<html><body><h3>The Application Server Is Down for Maintenance It will be up soon</h3></body></html>";
                    wv.loadData(customHtml, "text/html", "UTF-8");
                }
            }
            else {
                String customHtml = "<html><body><h3>Internet service is not available Please activate data service and try again</h3></body></html>";
                wv.loadData(customHtml, "text/html", "UTF-8");
            }
            return rootView;
        }




    }

}
