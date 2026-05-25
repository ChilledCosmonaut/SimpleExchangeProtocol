package com.example.simpleexchangeprotocol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.simpleexchangeprotocol.data.Contract;
import com.example.simpleexchangeprotocol.functionality.PictureHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;

    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private Bitmap header, footer;
    private final int STORAGE_PERMISSION_CODE = 1;
    private EditText contractNumber, partnerFirst, partnerSecond;
    private PdfDocument myPdfDocument;

    private Contract contract = new Contract();

    private File ImageBuffer;

    private ActivityResultLauncher<Uri> takePictureIntent;

    /**
     * Creates a file with a STATIC name.
     * We do NOT use timestamps here so the filename remains constant.
     */
    private File createStaticImageFile() {
        File storageDir = getExternalFilesDir("Pictures");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, "latest_capture.jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        contractNumber = findViewById(R.id.ContractNumberInput);
        partnerFirst = findViewById(R.id.ContractPartnerFirstName);
        partnerSecond = findViewById(R.id.ContractPartnerName);

        if (savedInstanceState != null) {

            Contract restoredContract = (Contract) savedInstanceState.get("Saved_Contract");

            if (restoredContract != null){
                contract = restoredContract;
            }
        }

        if (contract.Number != null)
            contractNumber.setText(contract.Number);
        if (contract.Partner != null && contract.Partner.firstName != null)
            partnerFirst.setText(contract.Partner.firstName);
        if (contract.Partner != null && contract.Partner.lastName != null)
            partnerSecond.setText(contract.Partner.lastName);

        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns grid
        photoAdapter = new PhotoAdapter(contract.Images, this::deletePhoto);
        photoRecyclerView.setAdapter(photoAdapter);

        // 2. Initialize the single capture file
        try {
            ImageBuffer = createStaticImageFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Register the Camera Launcher (Once)
        takePictureIntent = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        String newPath = PictureHandler.copyTempToPermanent(this, ImageBuffer);
                        if (newPath != null) {
                            contract.Images.add(newPath);
                            photoAdapter.notifyItemInserted(contract.Images.size() - 1);
                        }
                    }
                }
        );

        // Move to PDF Generator
        try {
            InputStream headerInput = getAssets().open("biefkopfcutout.png");
            Bitmap headerCache = BitmapFactory.decodeStream(headerInput);
            //header = enlargeBitmap(headerCache, 2040);
            //upperAnchor = header.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the list of file paths as an ArrayList of Strings
        outState.putParcelable("Saved_Contract", contract);
    }

    // Handles cases in which the storage permission is denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {

            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "App requires access to data to store PDFs!", Toast.LENGTH_LONG).show();

            }
        }
    }

    public void takePicture(View view) {

        Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", ImageBuffer);
        takePictureIntent.launch(photoURI);
    }

    private void deletePhoto(String path) {
        File f = new File(path);
        if (f.exists()) f.delete();
        int deletedPath = contract.Images.indexOf(path);
        contract.Images.remove(path);
        photoAdapter.notifyItemRemoved(deletedPath);
    }

    public void generateContract(){

    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
        private final List<String> paths;
        private final OnDeleteListener deleteListener;

        interface OnDeleteListener { void onDelete(String path); }

        PhotoAdapter(List<String> paths, OnDeleteListener listener) {
            this.paths = paths;
            this.deleteListener = listener;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            String path = paths.get(position);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            }

            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(path));
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        static class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            Button btnDelete;
            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.photoImageView);
                btnDelete = itemView.findViewById(R.id.btnDeletePhoto);
            }
        }
    }
}