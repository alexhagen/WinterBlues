package io.github.alexhagen.winterblues;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.rubensousa.previewseekbar.PreviewSeekBar;
import com.github.rubensousa.previewseekbar.PreviewSeekBarLayout;
import com.jetradarmobile.snowfall.SnowfallView;
import com.mancj.slideup.SlideUp;
import com.tapadoo.alerter.Alerter;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import at.markushi.ui.CircleButton;

public class daily_screen extends AppCompatActivity {


    String filepath;
    Queue<Integer> neededdays;
    String[] daystrings;
    String[] paragraphs;
    String[] media_paths;
    String[] longdates;
    CircleButton[] daybuttons;
    ProgressBar pbar;
    SlideUp daySlideView;
    ImageView media_bkg;
    ThinDownloadManager downloadManager;
    Queue<Integer> downloads;
    TextView media_text, media_date;
    CircleButton play, pause;
    LinearLayout playpause;
    MediaPlayer mixtapemedia = new MediaPlayer();
    Context mContext;
    VideoView video;
    MediaController videocontroller;
    Alerter downloadalerter;
    SnowfallView snowfall;
    int toastduration;

    private void readpathfile(File fin, CircleButton cb, int i) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fin));

        media_paths[i] = br.readLine();
        paragraphs[i] = br.readLine();
        File media_file = new File(filepath + media_paths[i]);
        if (!media_file.exists()) {
            neededdays.add(i);
        } else {
            String fname = media_paths[i];
            final Uri destinationUri = Uri.parse(filepath + daystrings[i] + '.' + fname);
            setbuttonup(cb, media_paths[i], destinationUri, i);
        }

        br.close();
        return;
    }

    private void downloadfrompathfile(File fin, int i) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fin));

        media_paths[i] = br.readLine();
        paragraphs[i] = br.readLine();
        Log.d("media_path", media_paths[i]);
        Log.d("paragraph", paragraphs[i]);

        br.close();
        return;
    }


    private void download(final String fname, final CircleButton cb, final int i) {
        Uri downloadUri = Uri.parse("http://128.46.92.221/" + fname);
        Log.d("URL", "http://128.46.92.221/" + fname);
        final Uri destinationUri = Uri.parse(filepath + fname);
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadListener(new DownloadStatusListener() {
                    @Override
                    public void onDownloadComplete(int id) {
                        File file = new File(filepath + fname);
                        try {
                            downloadfrompathfile(file, i);
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                        downloadmedia(media_paths[i], cb, i);
                    }

                    @Override
                    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                        Log.d("DOWNLOAD ERROR", errorMessage);
                    }

                    @Override
                    public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {
                        //pbar.setMax((int) totalBytes);
                        //pbar.setProgress((int) downlaodedBytes);
                        downloadalerter.setText(String.format("%d%%", progress));
                    }
                });
        downloadManager.add(downloadRequest);
    }

    private void setbuttonup(CircleButton cb, final String fname, final Uri destinationUri, final int i) {
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fname.equals("mp3")) {
                    mixtapemedia = MediaPlayer.create(mContext, destinationUri);
                    mixtapemedia.start();
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mixtapemedia.start();
                        }
                    });
                    pause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mixtapemedia.pause();
                        }
                    });
                    media_bkg.setImageResource(R.drawable.bkg3);
                    //Picasso.with(mContext).load(R.drawable.bkg2).into(media_bkg);
                    playpause.setVisibility(View.VISIBLE);
                } else if (fname.equals("jpg")) {
                    media_bkg.setImageURI(destinationUri);
                    //Picasso.with(mContext).load(destinationUri).into(media_bkg);
                } else if (fname.equals("png")) {
                    media_bkg.setImageURI(destinationUri);
                    //Picasso.with(mContext).load(destinationUri).into(media_bkg);
                } else if (fname.equals("mp4")){
                    media_bkg.setVisibility(View.GONE);
                    video.setVisibility(View.VISIBLE);
                    // video.setZOrderMediaOverlay(true);
                    video.setVideoURI(destinationUri);
                    // video.setZOrderOnTop(true);
                    // video.requestFocus();
                    video.start();
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            video.start();
                        }
                    });
                    pause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            video.pause();
                        }
                    });
                    playpause.setVisibility(View.VISIBLE);


                }
                media_date.setText(longdates[i]);
                media_text.setText(paragraphs[i]);
                daySlideView.show();
            }
        });
    }

    private void downloadmedia(final String fname, final CircleButton cb, final int i) {
        Uri downloadUri = Uri.parse("http://128.46.92.221/" + daystrings[i] + '.' + fname);
        final Uri destinationUri = Uri.parse(filepath + daystrings[i] + '.' + fname);
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadListener(new DownloadStatusListener() {
                    @Override
                    public void onDownloadComplete(int id) {
                        setbuttonup(cb, fname, destinationUri, i);
                        downloads.poll();
                        downloadalerter.setDuration(2);
                        if (downloads.peek() == null) {
                            //pbar.setVisibility(View.INVISIBLE);
                            Toast toast = Toast.makeText(mContext, "All Downloads Finished!", toastduration);
                            toast.show();
                        }
                    }

                    @Override
                    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                        Log.d("DOWNLOAD ERROR", errorMessage);
                    }

                    @Override
                    public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {
                        //pbar.setMax((int) totalBytes);
                        //pbar.setProgress((int) downlaodedBytes);
                        downloadalerter.setText(String.format("%d%%", progress));
                    }
                });
        int status = downloadManager.add(downloadRequest);
        downloads.add(status);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_screen);
        mContext = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View slideView = findViewById(R.id.infoview);
        View dayView = findViewById(R.id.mediaview);

        final SlideUp infoSlideView = new SlideUp.Builder<>(slideView)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.TOP)
                .build();

        daySlideView = new SlideUp.Builder<>(dayView)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.BOTTOM)
                .withListeners(new SlideUp.Listener(){
                    @Override
                    public void onSlide(float percent) {
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE){
                            playpause.setVisibility(View.GONE);
                            mixtapemedia.stop();
                            video.stopPlayback();
                            video.setVisibility(View.GONE);
                            media_bkg.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .build();

        media_bkg = (ImageView) findViewById(R.id.media);
        media_text = (TextView) findViewById(R.id.mediatext);
        media_date = (TextView) findViewById(R.id.dateforslide);

        CircleButton info_button = (CircleButton) findViewById(R.id.info_button);
        info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoSlideView.show();
            }
        });


        // get the day

        Calendar c = Calendar.getInstance();
        SimpleDateFormat mnth = new SimpleDateFormat("MMMM", Locale.US);
        TextView month = (TextView) findViewById(R.id.month);
        month.setText(mnth.format(c.getTime()));
        SimpleDateFormat dy = new SimpleDateFormat("d");
        TextView date = (TextView) findViewById(R.id.date);
        date.setText(dy.format(c.getTime()));
        if (month.getText() == "March") {

        }
        daystrings = new String[7];
        media_paths = new String[7];
        paragraphs = new String[7];
        daybuttons = new CircleButton[7];
        longdates = new String[7];
        daybuttons[0] = (CircleButton) findViewById(R.id.button0);
        daybuttons[1] = (CircleButton) findViewById(R.id.button1);
        daybuttons[2] = (CircleButton) findViewById(R.id.button2);
        daybuttons[3] = (CircleButton) findViewById(R.id.button3);
        daybuttons[4] = (CircleButton) findViewById(R.id.button4);
        daybuttons[5] = (CircleButton) findViewById(R.id.button5);
        daybuttons[6] = (CircleButton) findViewById(R.id.button6);
        play = (CircleButton) findViewById(R.id.playbutton);
        pause = (CircleButton) findViewById(R.id.pausebutton);
        playpause = (LinearLayout) findViewById(R.id.playpause);
        neededdays = new LinkedList<Integer>();
        pbar = (ProgressBar) findViewById(R.id.downloadprogress);
        downloadalerter = Alerter.create(this).setTitle("Downloading").setText("0%").setBackgroundColor(R.color.colorPrimaryDark);
        video = (VideoView) findViewById(R.id.videoView);
        video.setFocusable(true);
        toastduration = Toast.LENGTH_SHORT;


        SimpleDateFormat df = new SimpleDateFormat("MM_dd_yy");
        SimpleDateFormat ld = new SimpleDateFormat("MMMM d", Locale.US);
        for(int i=0; i<7; i++){
            daystrings[i] = df.format(c.getTime());
            longdates[i] = ld.format(c.getTime());
            Log.d("DATE IS", daystrings[i]);
            c.add(Calendar.DATE, -1);
        }

        // then, check in external memory if the files are there
        filepath = this.getExternalCacheDir().toString() + "/";
        for(int i=0; i<7; i++){
            File file = new File(filepath + daystrings[i]);
            if(!file.exists()){
                neededdays.add(i);
            } else {
                // inside each file is a path and a string
                try {
                    readpathfile(file, daybuttons[i], i);
                } catch(IOException e){
                    e.printStackTrace();
                    neededdays.add(i);
                }
            }
        }

        // then, if they arent there, download them to external memory
        Integer daytodownload;
        downloads = new LinkedList<Integer>();
        downloadManager = new ThinDownloadManager();
        downloadalerter.show();
        while((daytodownload = neededdays.poll()) != null){
            String fname = daystrings[daytodownload];
            CircleButton cb = daybuttons[daytodownload];
            Log.d("TAG", fname);
            download(fname, cb, daytodownload);
        }


        //pbar.setVisibility(View.INVISIBLE);

    }
}
