package com.jeevesandroid.mainscreens.questions;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class AudioQuestion extends Question implements MediaPlayer.OnPreparedListener {

    public AudioQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }


    /** Called when MediaPlayer is ready */
    public void onPrepared(final MediaPlayer player) {
        final Button btnStart = qView.findViewById(R.id.audioBtnStart);
        final Button btnPause = qView.findViewById(R.id.audioBtnPause);
        PhotoView audioview = qView.findViewById(R.id.audioPhotoview);
        audioview.setImageResource(R.drawable.audio);
        btnStart.setEnabled(true);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                player.start();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                player.pause();
            }
        });
    }
    public AudioQuestion getInstance(){
        return this;
    }

    @Override
    public void handle(int position) {
        Map<String, Object> myparams = questions.get(position).getparams();

        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        final String audioName = (String)options.get("audio");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage
            .getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + audioName);
        final File localFile;
        final File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(!externalDir.exists())
            externalDir.mkdirs();
        final View.OnClickListener pauseListenr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,
                    "Please listen to the whole clip!",Toast.LENGTH_SHORT).show();
            }
        };
        final Button btnStart = qView.findViewById(R.id.audioBtnStart);
        final Button btnPause = qView.findViewById(R.id.audioBtnPause);

        final Button btnNext = context.getBtnNext();
        final Button btnBack = context.getBtnBack();

        btnNext.setOnClickListener(pauseListenr);
        btnBack.setOnClickListener(pauseListenr);
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.nextQ();
                    }
                });
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.backQ();
                    }
                });
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(false);
                        btnPause.setEnabled(true);
                        btnNext.setOnClickListener(pauseListenr);
                        btnBack.setOnClickListener(pauseListenr);
                    }
                });
                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnStart.setEnabled(true);
                        btnPause.setEnabled(false);
                    }
                });
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    File f = new File(externalDir,audioName);
                    final Uri myUri = Uri.fromFile(new File(f.getAbsolutePath()));
                    mediaPlayer.setDataSource(context.getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(getInstance());
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread

                }
                catch(IOException e){
                    Log.d("FAIL","Failed to do this thing");
                    e.printStackTrace();
                }
            }
        });
        localFile = new File(externalDir,audioName);
        final Uri myUri = Uri.fromFile(new File(localFile.getAbsolutePath()));
        if(localFile.exists()){
            try {
                mediaPlayer.setDataSource(context.getApplicationContext(), myUri);
                mediaPlayer.setOnPreparedListener(getInstance());
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                Log.d("FAIL","Failed to exist");
                e.printStackTrace();
            }
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    mediaPlayer.setDataSource(context.getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(getInstance());
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread
                    Log.d("FAIL","Failed to set data source");
                } catch (IOException e) {
                    e.printStackTrace();
                }                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FAIL","Failed to get local file");
                exception.printStackTrace();
            }
        });

        PhotoView audio = qView.findViewById(R.id.audioPhotoview);
        audio.setImageResource(R.drawable.finger);
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_audio;
    }
}
