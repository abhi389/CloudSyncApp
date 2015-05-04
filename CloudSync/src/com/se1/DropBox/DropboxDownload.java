package com.se1.DropBox;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragment;
import com.commonsware.cwac.merge.MergeAdapter;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.se1.dao.DatabaseOperation;
import com.se1.main.R;
import com.se1.navdrawer.SuperAwesomeCardFragment;
import com.se1.oneDrive.LiveSdkSampleApplication;
import com.se1.oneDrive.SignInActivity;
import com.se1.oneDrive.skydrive.SkyDriveAlbum;
import com.se1.oneDrive.skydrive.SkyDriveAudio;
import com.se1.oneDrive.skydrive.SkyDriveFile;
import com.se1.oneDrive.skydrive.SkyDriveFolder;
import com.se1.oneDrive.skydrive.SkyDriveObject;
import com.se1.oneDrive.skydrive.SkyDrivePhoto;
import com.se1.oneDrive.skydrive.SkyDriveVideo;
import com.se1.oneDrive.util.FilePicker;
import com.se1.oneDrive.util.JsonKeys;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class DropboxDownload extends SherlockFragment implements OnItemClickListener {
    private class NewFolderDialog extends Dialog {
        public NewFolderDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.create_folder);
            setTitle("New Folder");

            final EditText name = (EditText) findViewById(R.id.nameEditText);
            final EditText description = (EditText) findViewById(R.id.descriptionEditText);

            findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> folder = new HashMap<String, String>();
                    folder.put(JsonKeys.NAME, name.getText().toString());
                    folder.put(JsonKeys.DESCRIPTION, description.getText().toString());

                    //final ProgressDialog progressDialog =
                      ///      .showProgressDialog("", "Saving. Please wait...", true);
                    //progressDialog.show();

                    mClient.postAsync(mCurrentFolderId,
                            new JSONObject(folder),
                            new LiveOperationListener() {
                                @Override
                                public void onError(LiveOperationException exception, LiveOperation operation) {
                                    //progressDialog.dismiss();
                                    showToast(exception.getMessage());
                                }

                                @Override
                                public void onComplete(LiveOperation operation) {
                                    //progressDialog.dismiss();

                                    JSONObject result = operation.getResult();
                                    if (result.has(JsonKeys.ERROR)) {
                                        JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                                        String message = error.optString(JsonKeys.MESSAGE);
                                        String code = error.optString(JsonKeys.CODE);
                                        showToast(code + ":" + message);
                                    } else {
                                        dismiss();
                                        loadFolder(mCurrentFolderId);
                                    }
                                }
                            });
                }
            });

            findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }
    private DropboxAPI<AndroidAuthSession> mApi;
    private String DIR = "/";
    private ArrayList<Entry> files;
    private ArrayList<String> dir;
    private boolean isItemClicked = false;
    private boolean isInsideDropBoxDirectory = false;
    // , onResume = false;
    private ListView lvDropboxDownloadFilesList;
    private MergeAdapter adapter=null;
    private Context mContext;
    private ImageView mView;
    private Drawable mDrawable;
    private FileOutputStream mFos;
    private Long mFileLen;
    // private Button btnDropboxDownloadDone;
    private ProgressDialog pd;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                mergingAdapter("DropBox",true);
                //lvDropboxDownloadFilesList.setAdapter(new DownloadFileAdapter(
                  //      getActivity(), files));
                pd.dismiss();
            } else if (msg.what == 1) {
                Toast.makeText(getActivity(),
                        "File save at " + msg.obj.toString(), Toast.LENGTH_LONG)
                        .show();
            }
            else if (msg.what == 2) {
                mergingAdapter("DropBox", false);
                pd.dismiss();
            }
        };
    };
    public  void setDataFromLogin( DropboxAPI<AndroidAuthSession> api) {
        mApi = api;

    }
    /**/

    public static final String EXTRA_PATH = "path";

    private static final int DIALOG_DOWNLOAD_ID = 0;
    private static final String HOME_FOLDER = "me/skydrive";

    private LiveConnectClient mClient;
    private SkyDriveListAdapter mPhotoAdapter;
    private String mCurrentFolderId;
    private Stack<String> mPrevFolderIds;

    private class SkyDriveListAdapter extends BaseAdapter {
        private  LayoutInflater mInflater;
        private  ArrayList<SkyDriveObject> mSkyDriveObjs;
        private View mView;

        public SkyDriveListAdapter(Context context) {
            //super(context);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSkyDriveObjs = new ArrayList<SkyDriveObject>();
        }

        /**
         * @return The underlying array of the class. If changes are made to this object and you
         * want them to be seen, call {@link #notifyDataSetChanged()}.
         */
        public ArrayList<SkyDriveObject> getSkyDriveObjs() {
            return mSkyDriveObjs;
        }

        @Override
        public int getCount() {
            return mSkyDriveObjs.size();
        }

        @Override
        public SkyDriveObject getItem(int position) {
            return mSkyDriveObjs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // Note: This implementation of the ListAdapter.getView(...) forces a download of thumb-nails when retrieving
        // views, this is not a good solution in regards to CPU time and network band-width.
        @Override
        public View getView(int position, View convertView,  final ViewGroup parent) {
            SkyDriveObject skyDriveObj = getItem(position);
            mView = convertView != null ? convertView : null;


            skyDriveObj.accept(new SkyDriveObject.Visitor() {
                @Override
                public void visit(SkyDriveVideo video) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.video_icon);
                    setName(video);
                    setDescription(video);
                }

                @Override
                public void visit(SkyDriveFile file) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.file_icon);
                    setName(file);
                    setDescription(file);
                }

                @Override
                public void visit(SkyDriveFolder folder) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder_icon);
                    setName(folder);
                    setDescription(folder);
                }

                @Override
                public void visit(SkyDrivePhoto photo) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.image_icon);
                    setName(photo);
                    setDescription(photo);

                    // Try to find a smaller/thumbnail and use that source
                    String thumbnailSource = null;
                    String smallSource = null;
                    for (SkyDrivePhoto.Image image : photo.getImages()) {
                        if (image.getType().equals("small")) {
                            smallSource = image.getSource();
                        } else if (image.getType().equals("thumbnail")) {
                            thumbnailSource = image.getSource();
                        }
                    }

                    String source = thumbnailSource != null ? thumbnailSource :
                            smallSource != null ? smallSource : null;

                    // if we do not have a thumbnail or small image, just leave.
                    if (source == null) {
                        return;
                    }

                    // Since we are doing async calls and mView is constantly changing,
                    // we need to hold on to this reference.
                    final View v = mView;
                    new AsyncTask<String, Long, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... params) {
                            try {
                                // Download the thumb nail image
                                LiveDownloadOperation operation = mClient.download(params[0]);

                                // Make sure we don't burn up memory for all of
                                // these thumb nails that are transient
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPurgeable = true;
                                return BitmapFactory.decodeStream(operation.getStream(), (Rect)null, options);
                            } catch (Exception e) {
                                //showToast(e.getMessage());
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            ImageView imgView = (ImageView)v.findViewById(R.id.skyDriveItemIcon);
                            //imgView.setImageBitmap(result);
                        }
                    }.execute(source);
                }

                @Override
                public void visit(SkyDriveAlbum album) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.folder_icon);
                    setName(album);
                    setDescription(album);
                }

                @Override
                public void visit(SkyDriveAudio audio) {
                    if (mView == null) {
                        mView = inflateNewSkyDriveListItem();
                    }

                    setIcon(R.drawable.audio_icon);
                    setName(audio);
                    setDescription(audio);
                }

                private void setName(SkyDriveObject skyDriveObj) {
                    TextView tv = (TextView) mView.findViewById(R.id.nameTextView);
                    tv.setText(skyDriveObj.getName());
                }

                private void setDescription(SkyDriveObject skyDriveObj) {
                    String description = skyDriveObj.getDescription();
                    if (description == null) {
                        description = "No description.";
                    }

                    TextView tv = (TextView) mView.findViewById(R.id.descriptionTextView);
                    tv.setText(description);
                }

                private View inflateNewSkyDriveListItem() {
                    return mInflater.inflate(R.layout.skydrive_list_item, parent, false);
                }

                private void setIcon(int iconResId) {
                    ImageView img = (ImageView) mView.findViewById(R.id.skyDriveItemIcon);
                    img.setImageResource(iconResId);
                }
            });


            return mView;
        }
    }

    private void loadFolder(final String folderId) {
        assert folderId != null;
        mCurrentFolderId = folderId;

        final ProgressDialog progressDialog =
              ProgressDialog.show(getActivity(), "", "Loading. Please wait...", true);

        mClient.getAsync(folderId + "/files", new LiveOperationListener() {
            @Override
            public void onComplete(LiveOperation operation) {
                progressDialog.dismiss();

                JSONObject result = operation.getResult();
                if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    showToast(code + ": " + message);
                    return;
                }
                Log.d("inside loadFolder","inside loadFolder");
                ArrayList<SkyDriveObject> skyDriveObjs = mPhotoAdapter.getSkyDriveObjs();
                skyDriveObjs.clear();

                JSONArray data = result.optJSONArray(JsonKeys.DATA);
                for (int i = 0; i < data.length(); i++) {
                    SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));
                    if (skyDriveObj != null) {
                        skyDriveObjs.add(skyDriveObj);
                    }
                }
                boolean isDropboxAdded= new DatabaseOperation(getActivity()).isdropBoxAdded();
                if(!(folderId.equalsIgnoreCase(HOME_FOLDER))) {
                    mergingAdapter("OneDrive", true);
                }
                else if(isDropboxAdded) {
                    mergingAdapter("DropBox", true);
                }
                mPhotoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(LiveOperationException exception, LiveOperation operation) {
                progressDialog.dismiss();

                showToast(exception.getMessage());
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        if(mClient !=null ) {
            loadFolder(HOME_FOLDER);
        }
    }
    /**/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("in dropbox Download","inside");
        View v =inflater.inflate(R.layout.dropboxdownload,container,false);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        mPrevFolderIds = new Stack<String>();
        boolean isDropboxAdded= new DatabaseOperation(getActivity()).isdropBoxAdded();
        mContext = getActivity().getApplicationContext();
        //mView = (ImageView)v.findViewById(R.id.image_view);
        lvDropboxDownloadFilesList = (ListView) v.findViewById(R.id.lvDropboxDownloadFilesList);
        if(isDropboxAdded) {
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // Log.i(tag, "keyCode: " + keyCode);
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        Log.d("back", "onKey Back listener is working!!!");
                        if (isItemClicked && isInsideDropBoxDirectory) {

                            if (DIR.length() == 0) {
                                // logOut();
                                getActivity().setResult(getActivity().RESULT_OK);
                                getActivity().onBackPressed();
                            } else {
                                DIR = DIR.substring(0, DIR.lastIndexOf('/'));
                                setLoggedIn(true);

                            }
                        } else {
                            if (mPrevFolderIds.isEmpty()) {
                                getActivity().setResult(getActivity().RESULT_OK);
                                getActivity().onBackPressed();
                                return false;
                            }

                            loadFolder(mPrevFolderIds.pop());


                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            AndroidAuthSession session = buildSession();
            mApi = new DropboxAPI<AndroidAuthSession>(session);
            checkAppKeySetup();

            if (!Constants.mLoggedIn) {
                mApi.getSession().startAuthentication(getActivity());

            }
        }
        lvDropboxDownloadFilesList.setOnItemClickListener(this);
        mPhotoAdapter = new SkyDriveListAdapter(getActivity());
        mergingAdapter("OneDrive",true);
        mPhotoAdapter.notifyDataSetChanged();
        //setListAdapter(mPhotoAdapter);

        LiveSdkSampleApplication app = (LiveSdkSampleApplication) getActivity().getApplication();
        mClient = app.getConnectClient();
        return v;
    }
    public void mergingAdapter(String cloudAccountName,boolean isDropBoxRootDirectory)
    {
        adapter=new MergeAdapter();

        if(cloudAccountName.equalsIgnoreCase("DropBox"))
            adapter.addAdapter(new DownloadFileAdapter(getActivity(), files));
        if(isDropBoxRootDirectory) {
            adapter.addAdapter(mPhotoAdapter);
        }
        lvDropboxDownloadFilesList.setAdapter(adapter);

    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


        MergeAdapter mergeAdapter=(MergeAdapter)arg0.getAdapter();
        //Log.d("arg0","arg0"+mergeAdapter.getAdapter(arg2).getClass().getName());

        if(mergeAdapter.getAdapter(arg2).getClass().getName().equalsIgnoreCase("com.se1.DropBox.DownloadFileAdapter")) {
            Entry fileSelected = files.get(arg2);
            if (fileSelected.isDir) {
                isItemClicked = true;
                DIR = dir.get(arg2);
                setLoggedIn(true);
            } else {

                downloadDropboxFile(fileSelected);
                // getIntent().getStringExtra("fileParentPath"));
            }
        }
        else
        {
                    SkyDriveObject skyDriveObj = (SkyDriveObject) arg0.getItemAtPosition(arg2);

                    skyDriveObj.accept(new SkyDriveObject.Visitor() {
                        @Override
                        public void visit(SkyDriveAlbum album) {
                            mPrevFolderIds.push(mCurrentFolderId);
                            loadFolder(album.getId());
                        }

                        @Override
                        public void visit(SkyDrivePhoto photo) {
                            ViewPhotoDialog dialog =
                                    new ViewPhotoDialog(getActivity(), photo);
                            dialog.setOwnerActivity(getActivity());
                            dialog.show();
                        }

                        @Override
                        public void visit(SkyDriveFolder folder) {
                            mPrevFolderIds.push(mCurrentFolderId);
                            loadFolder(folder.getId());
                        }

                        @Override
                        public void visit(SkyDriveFile file) {
                            Bundle b = new Bundle();
                            b.putString(JsonKeys.ID, file.getId());
                            b.putString(JsonKeys.NAME, file.getName());
                            String fileName=file.getName();

                            char[] fileNameCharArray=fileName.toCharArray();
                            char[] fileTypeReverse=getFileType(fileNameCharArray).toCharArray();
                            downloadOnedriveFile(DIALOG_DOWNLOAD_ID,b,getFileType(fileTypeReverse));
                            //getActivity().showDialog(DIALOG_DOWNLOAD_ID, b);
                        }

                        @Override
                        public void visit(SkyDriveVideo video) {
                            PlayVideoDialog dialog = new PlayVideoDialog(getActivity(), video);
                            dialog.setOwnerActivity(getActivity());
                            dialog.show();
                        }

                        @Override
                        public void visit(SkyDriveAudio audio) {
                            PlayAudioDialog audioDialog =
                                    new PlayAudioDialog(getActivity(), audio);
                            audioDialog.show();
                        }
                    });


        }
    }
    public String getFileType(char[] fileNameChar)
    {
        String docType="";
        for(int charArrayIndex=fileNameChar.length-1;charArrayIndex>=0;charArrayIndex--)
        {
            if(fileNameChar[charArrayIndex]=='.')
                break;
            docType=docType+fileNameChar[charArrayIndex];
        }
        return docType;
    }


    private class ViewPhotoDialog extends Dialog {
        private final SkyDrivePhoto mPhoto;

        public ViewPhotoDialog(Context context, SkyDrivePhoto photo) {
            super(context);
            assert photo != null;
            mPhoto = photo;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(mPhoto.getName());
            final ImageView imgView = new ImageView(getContext());
            addContentView(imgView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            mClient.downloadAsync(mPhoto.getSource(), new LiveDownloadOperationListener() {
                @Override
                public void onDownloadProgress(int totalBytes,
                                               int bytesRemaining,
                                               LiveDownloadOperation operation) {
                }

                @Override
                public void onDownloadFailed(LiveOperationException exception,
                                             LiveDownloadOperation operation) {
                    showToast(exception.getMessage());
                }

                @Override
                public void onDownloadCompleted(LiveDownloadOperation operation) {
                    new AsyncTask<LiveDownloadOperation, Long, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(LiveDownloadOperation... params) {
                            return extractScaledBitmap(mPhoto, params[0].getStream());
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            imgView.setImageBitmap(result);
                        }
                    }.execute(operation);
                }
            });
        }
    }
    private Bitmap extractScaledBitmap(SkyDrivePhoto photo, InputStream imageStream) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int IMAGE_MAX_SIZE = Math.max(display.getWidth(), display.getHeight());

        int scale = 1;
        if (photo.getHeight() > IMAGE_MAX_SIZE  || photo.getWidth() > IMAGE_MAX_SIZE) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(photo.getHeight(), photo.getWidth())) / Math.log(0.5)));
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = scale;
        return BitmapFactory.decodeStream(imageStream, (Rect)null, options);
    };
    private class PlayAudioDialog extends Dialog {
        private final SkyDriveAudio mAudio;
        private MediaPlayer mPlayer;
        private TextView mPlayerStatus;

        public PlayAudioDialog(Context context, SkyDriveAudio audio) {
            super(context);
            assert audio != null;
            mAudio = audio;
            mPlayer = new MediaPlayer();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(mAudio.getName());

            mPlayerStatus = new TextView(getContext());
            mPlayerStatus.setText("Buffering...");
            addContentView(mPlayerStatus,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayerStatus.setText("Playing...");
                    mPlayer.start();
                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayerStatus.setText("Finished playing.");
                }
            });

            try {
                mPlayer.setDataSource(mAudio.getSource());
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.prepareAsync();
            } catch (IllegalArgumentException e) {
                showToast(e.getMessage());
                return;
            } catch (IllegalStateException e) {
                showToast(e.getMessage());
                return;
            } catch (IOException e) {
                showToast(e.getMessage());
                return;
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    private class PlayVideoDialog extends Dialog {
        private final SkyDriveVideo mVideo;
        private VideoView mVideoHolder;

        public PlayVideoDialog(Context context, SkyDriveVideo video) {
            super(context);
            assert video != null;
            mVideo = video;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(mVideo.getName());

            mVideoHolder = new VideoView(getContext());
            mVideoHolder.setMediaController(new MediaController(getContext()));
            mVideoHolder.setVideoURI(Uri.parse(mVideo.getSource()));
            addContentView(mVideoHolder,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @Override
        protected void onStart() {
            super.onStart();
            mVideoHolder.start();
        }
    }
    private void checkAppKeySetup() {
        if (Constants.DROPBOX_APP_KEY.startsWith("CHANGE")
                || Constants.DROPBOX_APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            getActivity().finish();
            return;
        }
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + Constants.DROPBOX_APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getActivity().getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's "
                    + "manifest is not set up correctly. You should have a "
                    + "com.dropbox.client2.android.AuthActivity with the "
                    + "scheme: " + scheme);
            getActivity().finish();
        }
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast error = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
                error.show();
            }
        });
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(Constants.DROPBOX_APP_KEY,
                Constants.DROPBOX_APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0],
                    stored[1]);
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE,
                    accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE);
        }

        return session;
    }

    public void setLoggedIn(final boolean loggedIn) {
        pd = ProgressDialog.show(getActivity(), null,
                "Retrieving data...");
        new Thread(new Runnable() {

            @Override
            public void run() {
                Constants.mLoggedIn = loggedIn;
                if (loggedIn) {
                    int i = 0;
                    com.dropbox.client2.DropboxAPI.Entry dirent;
                    try {
                        dirent = mApi.metadata(DIR, 1000, null, true, null);
                        files = new ArrayList<com.dropbox.client2.DropboxAPI.Entry>();
                        dir = new ArrayList<String>();
                        for (com.dropbox.client2.DropboxAPI.Entry ent : dirent.contents) {
                            files.add(ent);
                            dir.add(new String(files.get(i++).path));
                        }
                        i = 0;
                        if(dirent.path.equalsIgnoreCase("/")) {
                            isInsideDropBoxDirectory=false;
                            mHandler.sendEmptyMessage(0);
                        }
                        else {
                            isInsideDropBoxDirectory=true;
                            mHandler.sendEmptyMessage(2);
                        }
                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    @Override
    public void onResume() {

        super.onResume();
        boolean isDropboxAdded= new DatabaseOperation(getActivity()).isdropBoxAdded();
        if(isDropboxAdded) {
            AndroidAuthSession session = mApi.getSession();

            if (session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();

                    TokenPair tokens = session.getAccessTokenPair();
                    storeKeys(tokens.key, tokens.secret);
                    setLoggedIn(true);
                } catch (IllegalStateException e) {
                    showToast("Couldn't authenticate with Dropbox:"
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    private void storeKeys(String key, String secret) {
        SharedPreferences prefs =getActivity().getSharedPreferences(
                Constants.ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.putString(Constants.ACCESS_KEY_NAME, key);
        edit.putString(Constants.ACCESS_SECRET_NAME, secret);
        edit.commit();
    }


    private String[] getKeys() {
        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(Constants.ACCESS_KEY_NAME, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    private boolean downloadDropboxFile(Entry fileSelected) {// , String
        // localFilePath)
        // {
        File dir = new File(Utils.getPath());
        if (!dir.exists())
            dir.mkdirs();
        Log.d("type",fileSelected.mimeType);

            try {
                File localFile = new File(dir + "/" + fileSelected.fileName());

                if (!localFile.exists()) {
                    localFile.createNewFile();
                    copy(fileSelected, localFile,dir,fileSelected.mimeType);
                } else {
                    showFileExitsDialog(fileSelected, localFile,dir,fileSelected.mimeType);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        return true;
    }



    private void openFile(File dir,final String fileName,String type,boolean isCalledFromDropbox) {
        File file = new File(dir+"");
        if(isCalledFromDropbox) {
            file = new File(dir + "/" + fileName);
        }

        Log.d("dir","dir"+dir);
        Uri localpath = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(localpath,type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            mContext.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Log.d("Error","No Application Available to View this file");
        }
    }
	/*copy file from dropbox to local directory*/

    private void copy(final Entry fileSelected, final File localFile, final File dir,String type) {
        final ProgressDialog pd = ProgressDialog.show(getActivity(),
                "Downloading...", "Please wait...");
        Log.d("fileSelected.path","fileSelected.path"+fileSelected.path);
        new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedInputStream br = null;
                BufferedOutputStream bw = null;
                DropboxInputStream fd;
                try {
                    fd = mApi.getFileStream(fileSelected.path,null);
                           // localFile.getPath());
                    br = new BufferedInputStream(fd);
                    bw = new BufferedOutputStream(new FileOutputStream(
                            localFile));

                    byte[] buffer = new byte[4096];
                    int read;
                    while (true) {
                        read = br.read(buffer);
                        if (read <= 0) {
                            break;
                        }
                        bw.write(buffer, 0, read);
                    }
                    pd.dismiss();
                    Message message = new Message();
                    message.obj = localFile.getAbsolutePath();
                    message.what = 1;
                    mHandler.sendMessage(message);
                    openFile(dir,fileSelected.fileName(),fileSelected.mimeType,true);

                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bw != null) {
                        try {
                            bw.close();
                            if (br != null) {
                                br.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();

    }

    private void showFileExitsDialog(final Entry fileSelected,
                                     final File localFile,final File dir, final String type) {
        AlertDialog.Builder alertBuilder = new Builder(getActivity());
        alertBuilder.setMessage(Constants.OVERRIDEMSG);
        alertBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        copy(fileSelected, localFile,dir,type);
                    }
                });
        alertBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openFile(dir,fileSelected.fileName(),fileSelected.mimeType,true);
                    }
                });

        alertBuilder.create().show();

    }

    public void downloadOnedriveFile(final int id, final Bundle bundle,final String fileType) {
        Dialog dialog = null;
        switch (id) {
            case DIALOG_DOWNLOAD_ID: {
                AlertDialog.Builder builder = new Builder(getActivity());
                builder.setMessage("This file will be downloaded to the sdcard.");
                builder.setPositiveButton("OK", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog =
                                        new ProgressDialog(getActivity());

                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressDialog.setMessage("Downloading...");
                                progressDialog.setCancelable(true);
                                progressDialog.show();

                                String fileId = bundle.getString(JsonKeys.ID);
                                final String name = bundle.getString(JsonKeys.NAME);

                                final File file = new File(Utils.getPath(), name);
                                final LiveDownloadOperation operation =
                                        mClient.downloadAsync(fileId + "/content",
                                                file,
                                                new LiveDownloadOperationListener() {
                                                    @Override
                                                    public void onDownloadProgress(int totalBytes,
                                                                                   int bytesRemaining,
                                                                                   LiveDownloadOperation operation) {
                                                        int percentCompleted =
                                                                computePrecentCompleted(totalBytes, bytesRemaining);

                                                        progressDialog.setProgress(percentCompleted);
                                                    }

                                                    @Override
                                                    public void onDownloadFailed(LiveOperationException exception,
                                                                                 LiveDownloadOperation operation) {
                                                        progressDialog.dismiss();
                                                        showToast(exception.getMessage());
                                                    }

                                                    @Override
                                                    public void onDownloadCompleted(LiveDownloadOperation operation) {
                                                        progressDialog.dismiss();
                                                        Log.d("complete download","fileType"+fileType);
                                                        showToast("File downloaded.");
                                                        if(fileType.equalsIgnoreCase("docx"))
                                                            openFile(file,name,"application/msword",false);
                                                        else if(fileType.equalsIgnoreCase("pptx"))
                                                            openFile(file,name,"application/vnd.ms-powerpoint",false);
                                                        else if(fileType.equalsIgnoreCase("txt"))
                                                            openFile(file,name,"text/plain",false);
                                                        else if(fileType.equalsIgnoreCase("pdf"))
                                                            openFile(file,name,"application/pdf",false);

                                                    }
                                                });

                                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        operation.cancel();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();
                break;
            }
        }

        if (dialog != null) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getActivity().removeDialog(id);
                }
            });
        }


    }
    private int computePrecentCompleted(int totalBytes, int bytesRemaining) {
        return (int) (((float)(totalBytes - bytesRemaining)) / totalBytes * 100);
    }



}
