package com.example.simpleexchangeprotocol;

import static com.example.simpleexchangeprotocol.functionality.FileHandler.CREATE_FILE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simpleexchangeprotocol.data.Contract;
import com.example.simpleexchangeprotocol.data.Name;
import com.example.simpleexchangeprotocol.functionality.FileHandler;
import com.example.simpleexchangeprotocol.functionality.PdfGenerator;
import com.example.simpleexchangeprotocol.functionality.PictureHandler;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;

    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private final int STORAGE_PERMISSION_CODE = 1;
    private EditText contractNumber, partnerFirst, partnerSecond;
    private PdfDocument myPdfDocument;
    private Uri filePath;
    private PaintView paintView;

    private Contract contract = new Contract();

    private File ImageBuffer;

    private ActivityResultLauncher<Uri> takePictureIntent;
    private ActivityResultLauncher<String> saveFileIntent;

    /**
     * Creates a file with a STATIC name.
     * We do NOT use timestamps here so the filename remains constant.
     */
    private File createStaticImageFile() {
        File storageDir = getExternalFilesDir("Pictures");
        assert storageDir != null;
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, "latest_capture.jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                PackageManager.PERMISSION_GRANTED);


        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        contractNumber = findViewById(R.id.ContractNumberInput);
        partnerFirst = findViewById(R.id.ContractPartnerFirstName);
        partnerSecond = findViewById(R.id.ContractPartnerName);

        paintView = findViewById(R.id.paintView);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);

        if (savedInstanceState != null) {

            Contract restoredContract = (Contract) savedInstanceState.get("Saved_Contract");

            if (restoredContract != null){
                contract = restoredContract;
            }
        }

        if (contract.Number != null)
            contractNumber.setText(String.valueOf(contract.Number));
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
        try {
            takePictureIntent = PictureHandler.registerPictureIntent(this, result -> {
                        if (result) {
                            String newPath = PictureHandler.copyTempToPermanent(this, ImageBuffer);
                            if (newPath != null) {
                                contract.Images.add(newPath);
                                photoAdapter.notifyItemInserted(contract.Images.size() - 1);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Uri PDFPath = data.getData();
            System.out.println(PDFPath);
            FileHandler.SavePDF(this, PDFPath, myPdfDocument);
        }else {
            super.onActivityResult(requestCode, resultCode, data);
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

    public void ClearDrawingPad(View view) {
        paintView.clear();
    }

    public void generateContract(View view){
        contract.Number = Integer.parseInt(contractNumber.getText().toString());
        contract.Partner = new Name(partnerFirst.getText().toString(), partnerSecond.getText().toString());

        try {
            myPdfDocument = PdfGenerator.createMyPDF(this, contract, paintView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (myPdfDocument == null){
            return;
        }

        File myFile = new File(Environment.getExternalStorageDirectory().getPath());
        try {
            Uri contractPath = Uri.fromFile(myFile);
            FileHandler.createFile(this, contractPath, contract);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not save Pdf because of: " + e.toString(), Toast.LENGTH_SHORT).show();
            myFile.mkdir();
        }
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