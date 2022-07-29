package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.SignInButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddImageActivity extends AppCompatActivity {
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    void navigateToSaveImgToDBActivity(String img_url, int id){
        finish();
        Intent intent = new Intent(AddImageActivity.this,SaveImgToDB.class);
        intent.putExtra("email", email);
        intent.putExtra("img_url", img_url);
        intent.putExtra("product_id", id);
        startActivity(intent);
    }

    final int SELECT_MULTIPLE_IMAGES = 0;
    String image_path;
    ArrayList<String> selectedImagesPaths; // Paths of the image(s) selected by the user.
    boolean imagesSelected = false; // Whether the user selected at least an image or not.
    Button add_image;

    public static String FALLBACK_COPY_FOLDER = "upload_part";

    private static String TAG = "FileUtils";

    private static Uri contentUri = null;
    static  int product_id;
    static String email;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        //requestPermissions(new String[]{Manifest.permission.INTERNET}, 2);
        ActivityCompat.requestPermissions(AddImageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        Bundle bundle = getIntent().getExtras();
        email = bundle.getString("email");
        product_id = bundle.getInt("product_id");

        setContentView(R.layout.activity_add_image);
        context =  getApplicationContext();
        ActivityResultLauncher<Intent> launchSomeActivity
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        String currentImagePath;
                        selectedImagesPaths = new ArrayList<>();
                        TextView numSelectedImages = findViewById(R.id.numSelectedImages);
                        if (result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            currentImagePath = getPath(uri);
                            Log.d("ImageDetails", "Single Image URI : " + uri);
                            Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
                            selectedImagesPaths.add(currentImagePath);
                            image_path = currentImagePath;
                            imagesSelected = true;
                            numSelectedImages.setText("Image selected successfully");
                        } else {
                            // When multiple images are selected.
                            if (result.getData().getClipData() != null) {
                                ClipData clipData = result.getData().getClipData();
                                for (int i = 0; i < clipData.getItemCount(); i++) {

                                    ClipData.Item item = clipData.getItemAt(i);
                                    Uri uri = item.getUri();

                                    currentImagePath = getPath(uri);
                                    selectedImagesPaths.add(currentImagePath);
                                    Log.d("ImageDetails", "Image URI " + i + " = " + uri);
                                    Log.d("ImageDetails", "Image Path " + i + " = " + currentImagePath);
                                    imagesSelected = true;
                                    numSelectedImages.setText("Image selected successfully");
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(getApplicationContext(), selectedImagesPaths.size() + " Image(s) Selected.", Toast.LENGTH_LONG).show();

                });
        add_image = (Button) findViewById(R.id.add_image);
        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                launchSomeActivity.launch(intent);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    public void navigateToHomepage(View v){
        finish();
        Intent intent = new Intent(AddImageActivity.this,HomePage.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    public void connectServer(View v) {
        if (imagesSelected == false) { // This means no image is selected and thus nothing to upload.
            Toast.makeText(getApplicationContext(), "No Image Selected to Upload. Select Image and Try Again.", Toast.LENGTH_LONG).show();
            return;
        }

        String postUrl = "http://10.100.102.195:3000/add_image";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (int i = 0; i < selectedImagesPaths.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                // Read BitMap by file path.
                Bitmap bitmap = BitmapFactory.decodeFile(image_path, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "Please Make Sure the Selected File is an Image.", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image" + i, "Android_Flask_" + i + ".jpg", RequestBody.create(byteArray));
        }
        RequestBody postBodyImage = multipartBodyBuilder.build();

//        RequestBody postBodyImage = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                .build();
        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to upload, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(getApplicationContext(), "uploaded successfully", Toast.LENGTH_SHORT).show();
                            String responseData =  response.body().string();
                            JSONObject Jobject = null;
                            try {
                                Jobject = new JSONObject(responseData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                String img_url = Jobject.get("img_url").toString();
                                navigateToSaveImgToDBActivity(img_url, product_id);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void selectImage(View v) {
        ActivityResultLauncher<Intent> launchSomeActivity
                = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        String currentImagePath;
                        selectedImagesPaths = new ArrayList<>();
                        TextView numSelectedImages = findViewById(R.id.numSelectedImages);
                        if (result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            currentImagePath = getPath(uri);
                            Log.d("ImageDetails", "Single Image URI : " + uri);
                            Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
                            selectedImagesPaths.add(currentImagePath);
                            imagesSelected = true;
                            numSelectedImages.setText("Image selected successfully");
                        } else {
                            // When multiple images are selected.
                            // Thanks tp Laith Mihyar for this Stackoverflow answer : https://stackoverflow.com/a/34047251/5426539
                            if (result.getData().getClipData() != null) {
                                ClipData clipData = result.getData().getClipData();
                                for (int i = 0; i < clipData.getItemCount(); i++) {

                                    ClipData.Item item = clipData.getItemAt(i);
                                    Uri uri = item.getUri();

                                    currentImagePath = getPath(uri);
                                    selectedImagesPaths.add(currentImagePath);
                                    Log.d("ImageDetails", "Image URI " + i + " = " + uri);
                                    Log.d("ImageDetails", "Image Path " + i + " = " + currentImagePath);
                                    imagesSelected = true;
                                    numSelectedImages.setText("Image selected successfully");
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(getApplicationContext(), selectedImagesPaths.size() + " Image(s) Selected.", Toast.LENGTH_LONG).show();

    });
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(intent);
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE_IMAGES);
    }

    public String getPath(final Uri uri) {
        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        // DocumentProvider

        if (isKitKat) {
            // ExternalStorageProvider

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);

                if (fullPath == null || !fileExists(fullPath)) {
                    Log.d(TAG, "Copy files as a fallback");
                    fullPath = copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER);
                }

                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }


            // DownloadsProvider

            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[] {
                                MediaStore.MediaColumns.DISPLAY_NAME
                        }, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);

                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[] {
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };

                        for (String contentUriPrefix: contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));

                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }
                    }
                } else {
                    final String id = DocumentsContract.getDocumentId(uri);

                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    if (contentUri != null)
                        return getDataColumn(context, contentUri, null, null);
                }
            }


            // MediaProvider
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Log.d(TAG, "MEDIA DOCUMENT TYPE: " + type);

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
//                } else if ("document".equals(type)) {
//                    contentUri = MediaStore.Files.getContentUri(MediaStore.getVolumeName(uri));
//                }

                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };


                return getDataColumn(context, contentUri, selection, selectionArgs);
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri);
            }

            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri);
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // return getFilePathFromURI(context,uri);
                    return copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER);
                    // return getRealPathFromURI(context,uri);
                } else {
                    return getDataColumn(context, uri, null, null);
                }

            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } else {
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri);
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {
                        MediaStore.Images.Media.DATA
                };
                Cursor cursor = null;

                try {
                    cursor = context.getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER);
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = File.separator + pathData[1];
        String fullPath = "";


        Log.d(TAG, "MEDIA EXTSD TYPE: " + type);
        Log.d(TAG, "Relative path: " + relativePath);
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        if ("home".equalsIgnoreCase(type)) {
            fullPath = "/storage/emulated/0/Documents" + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return null;
    }

    private String getDriveFilePath(Uri uri) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e(TAG, "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e(TAG, "Path " + file.getPath());
            Log.e(TAG, "Size " + file.length());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return file.getPath();
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private String copyFileToInternalStorage(Uri uri, String newDirName) {
        Uri returnUri = uri;

        Cursor returnCursor = context.getContentResolver().query(returnUri, new String[] {
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
        }, null, null, null);


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if (!newDirName.equals("")) {
            String random_collision_avoidance = UUID.randomUUID().toString();

            File dir = new File(context.getFilesDir() + File.separator + newDirName + File.separator + random_collision_avoidance);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            output = new File(context.getFilesDir() + File.separator + newDirName + File.separator + random_collision_avoidance + File.separator + name);
        } else {
            output = new File(context.getFilesDir() + File.separator + name);
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];

            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return output.getPath();
    }

    private String getFilePathForWhatsApp(Uri uri) {
        return copyFileToInternalStorage(uri, "whatsapp");
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            if(uri.getAuthority()!=null) {

                cursor = context.getContentResolver().query(uri, projection,
                        selection, selectionArgs, null);
            }
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public boolean isWhatsAppFile(Uri uri) {
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }

    private boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }
}

