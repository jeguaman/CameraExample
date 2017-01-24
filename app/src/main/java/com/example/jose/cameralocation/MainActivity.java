package com.example.jose.cameralocation;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jose.cameralocation.Util.Constants;
import com.example.jose.cameralocation.Util.ImageUtil;
import com.example.jose.cameralocation.Util.PermissionUtil;
import com.soundcloud.android.crop.Crop;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private String mCurrentPhotoPath;
    private Uri imgOriginUri;
    //private Uri imgDestinationUri;
    private double latitud = 0.0;
    private double longitud = 0.0;
    //private Bitmap imageB = null;
    private EditText txtUno;
    private EditText txtDos;
    private String mCurrentPostalPhotoPath;
    private ImageButton mImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtUno = (EditText) findViewById(R.id.txtUno);
        txtDos = (EditText) findViewById(R.id.txtDos);
        imageView = (ImageView) findViewById(R.id.img_pick);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        mImageButton = (ImageButton) findViewById(R.id.save_button);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.id.img_pick);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                saveImageCacaotera(bitmap);
            }
        });
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {// Error occurred while creating the File
                Toast.makeText(MainActivity.this, "Ha ocurrido un error al crear el archivo para la fotografía.", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {// Continue only if the File was successfully created
                imgOriginUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgOriginUri);
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (getLatLng(mCurrentPhotoPath)) {
                        Bitmap yourSelectedImage = null;
                        try {
                            yourSelectedImage = decodeUri(imgOriginUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        yourSelectedImage = ImageUtil.getResizedBitmap(yourSelectedImage, 500, 500);
                        loadImageLocal(yourSelectedImage);
                    } else {
                        Toast.makeText(MainActivity.this, "No se ha encontrado la información geográfica asociada a la imágen.", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
        /*
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        */
    }

    /*
    trae las métricas de la imágen para escalar en múltiplos de 2
    traer de otra dimensión

     */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode imageView size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 500;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }


    private void saveImageCacaotera(Bitmap image) {
        try {
            File pictureFile = createPostalImageFile();
            if (pictureFile == null) {
                Log.d("dasdas",
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            //imgDestinationUri = Uri.fromFile(pictureFile);
            galleryAddPic(mCurrentPostalPhotoPath);
            Toast.makeText(MainActivity.this, "La imágen se guardó en la carpeta " + Constants.PICTURES_DIRECTORY, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            Log.d("Camera:", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Camera:", "Error accessing file: " + e.getMessage());
        }
    }


    private void loadImageLocal(Bitmap image) {
        String msgUno = txtUno.getText().toString();
        String msgDos = txtDos.getText().toString();
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap imageAux = ImageUtil.combineImages(image, b);
        if (!msgUno.isEmpty()) {
            ImageUtil.drawTextOnBitmap(imageAux, msgUno.toLowerCase(), 360f, 386f, Typeface.DEFAULT_BOLD, 28, Color.GREEN, Paint.Align.CENTER);
        }
        if (!msgDos.isEmpty()) {
            ImageUtil.drawTextOnBitmap(imageAux, msgDos.toLowerCase(), 360f, 420f, Typeface.DEFAULT_BOLD, 28, Color.GREEN, Paint.Align.CENTER);
        }
        ImageUtil.drawTextOnBitmap(imageAux, String.valueOf(latitud), 330f, 60f, Typeface.DEFAULT_BOLD, 16, Color.GREEN, Paint.Align.LEFT);
        ImageUtil.drawTextOnBitmap(imageAux, String.valueOf(longitud), 350f, 40f, Typeface.DEFAULT_BOLD, 16, Color.GREEN, Paint.Align.LEFT);
        imageView.setImageBitmap(imageAux);
    }


    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private File createPostalImageFile() throws IOException {// Create an imageView file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Postal_" + timeStamp;
        if (Environment.getExternalStorageDirectory().canWrite()) {
            File aux = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.PICTURES_DIRECTORY);
            if (!aux.exists()) {
                aux.mkdirs();
            }
            File storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.PICTURES_DIRECTORY);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            mCurrentPostalPhotoPath = image.getAbsolutePath();
            return image;
        } else {
            return null;
        }
    }

    private File createImageFile() throws IOException {
        // Create an imageView file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = Constants.NAME_IMAGE + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private boolean getLatLng(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            float latlng[] = new float[2];
            if (exifInterface.getLatLong(latlng)) {
                //Toast.makeText(MainActivity.this, "Latitud " + latlng[0] + ", Longitud " + latlng[1], Toast.LENGTH_LONG).show();
                latitud = latlng[0];
                longitud = latlng[1];
                return true;
            }
        } catch (IOException e) {
            Log.e("MAin Activity", "" + e.toString());
        }
        return false;
    }


}

