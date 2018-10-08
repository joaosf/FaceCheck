package com.check.face.facecheck;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.check.face.facecheck.Models.Face;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.MEDIA_CONTENT_CONTROL;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.lang.System.out;

@EActivity(R.layout.activity_home)
public class HomeActivity extends AppCompatActivity {

    Bitmap fotoOriginal;
    List<Bitmap> listFoto;
    List<Face> listFaces;
    List<String> listNomesFaces;
    @ViewById
    ImageView img;
    @ViewById
    ListView listView;

    @AfterViews
    protected void init() {
        requestPermissionMediaContentControl(READ_EXTERNAL_STORAGE);
    }
    private boolean requestPermissionMediaContentControl(final String permissao) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(permissao) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(permissao)) {
            Snackbar.make(listView, "TESTE", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{permissao}, 0);
                        }
                    });
        } else {
            requestPermissions(new String[]{permissao}, 0);
        }
        return false;
    }



    @Click
    void btnComecar() {
        if (img.getVisibility() == View.VISIBLE) {
            img.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            fotoOriginal = BitmapFactory.decodeFile(picturePath);
            firebaseFaceCheck();

        }
    }

    private void firebaseFaceCheck() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(fotoOriginal);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(getFirebaseVisionFaceDetectorOptions());
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        int i = 1;
                                        listFoto = new ArrayList<>();
                                        listFaces = new ArrayList<>();
                                        listNomesFaces = new ArrayList<>();
                                        List<String> descricao = new ArrayList<>();
                                        if (faces.isEmpty()) {
                                            showMessageDialog("Ops","Não conseguimos processar sua foto, tente outra.");
                                        }
                                        for (FirebaseVisionFace face : faces) {
                                            FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);

                                            Face faceModel = new Face();
                                            faceModel.setCodigo(i);
                                            faceModel.setSmilingProbability(face.getSmilingProbability()*100);
                                            faceModel.setRightEyeOpenProbability(face.getRightEyeOpenProbability()*100);
                                            faceModel.setLeftEyeOpenProbability(face.getLeftEyeOpenProbability()*100);
                                            faceModel.setNoseX(nose.getPosition().getX());
                                            faceModel.setNoseY(nose.getPosition().getY());
                                            descricao.add(faceModel.toString()+"\r\n");

                                            Rect rect = face.getBoundingBox();
                                            int x = rect.left;
                                            int y = rect.top < 0 ? rect.top*-1 : rect.top;
                                            int widht = rect.right-rect.left;
                                            int height = rect.bottom-rect.top;

                                            listFoto.add(Bitmap.createBitmap(fotoOriginal,x,y,widht,height));
                                            listFaces.add(faceModel);
                                            listNomesFaces.add("Rosto "+faceModel.getCodigo()+": Está "+faceModel.getSmilingProbability()+"% feliz!");
                                            i++;
                                        }
                                        popularListView();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
    }

    public void showMessageDialog(String Titulo, String Mensagem) {
        new AlertDialog.Builder(this)
                .setTitle(Titulo)
                .setMessage(Mensagem)
                .setCancelable(false)
                .setPositiveButton("Entendi",null)
                .show();
    }


    private void popularListView() {
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listNomesFaces);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                img.setImageBitmap(listFoto.get(position));
                img.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (img.getVisibility() == View.VISIBLE) {
            img.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public FirebaseVisionFaceDetectorOptions getFirebaseVisionFaceDetectorOptions() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();
        return options;
    }
}
