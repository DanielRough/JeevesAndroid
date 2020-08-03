package com.jeeves.mainscreens.questions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeeves.R;
import com.jeeves.firebase.FirebaseQuestion;
import com.jeeves.mainscreens.SurveyActivity;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ImageQuestion extends Question{
    private PhotoView photoView;
    public ImageQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }


    @Override
    public void handle(int position) {
        photoView = qView.findViewById(R.id.photo_view2);
        photoView.setVisibility(View.VISIBLE);
        Map<String, Object> myparams = questions.get(position).getparams();

        if(myparams == null)return;
        Map<String, Object> options = (Map<String, Object>) myparams.get("options");
        String imageName = (String)options.get("image");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage.getReference().child(imageName);
        //StorageReference gsReference = storage
        //    .getReferenceFromUrl("gs://jeeves-27914.appspot.com/" + imageName);
        final File localFile;
        File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(!externalDir.exists())
            externalDir.mkdirs();

        localFile = new File(externalDir,imageName);
        if(localFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            photoView.setImageBitmap(myBitmap);
            return;
        }
        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                photoView.setImageBitmap(myBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_image;
    }
}
