package com.enset.tp4_persistance_de_donnes;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.enset.tp4_persistance_de_donnes.databinding.FragmentSecondBinding;
import com.enset.tp4_persistance_de_donnes.metier.Etudiant;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    final static String FILE_NAME = "etudiants.dat";
    Etudiant etudiant = new Etudiant();

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // g??rer le clic s??lectionner l'image de la galerie
        binding.imageEditEtudiant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //v??rifier l'autorisation d'ex??cution
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        //autorisation non accord??e, demandez-la.
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        // afficher la fen??tre popup pour l'autorisation d'ex??cution
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        // autorisation d??j?? accord??e
                        pickImageFromGallery();
                    }
                }
                else {
                    //system os is less then marshmallow
                    pickImageFromGallery();
                }
            }
        });

        // Button pour returner vers home
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        // R??cuperer la liste des ??tudiants existe dans le fichier <=> D??serialisation
        List<Etudiant> list = new ArrayList<>();
        try {
            FileInputStream fis = getActivity().openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (List<Etudiant>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mettre la liste des etudiant r??cuperer du fichier dans la liste "finalList"
        List<Etudiant> finalList = list;

        binding.saveEtudiat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                etudiant.setCine(binding.editTextCine.getText().toString());
                etudiant.setNom(binding.editTextNom.getText().toString());
                etudiant.setPrenom(binding.editTextPrenom.getText().toString());
                etudiant.setDateNaissance(binding.editTextNeLe.getText().toString());
                etudiant.setNiveau(binding.editTextNiveau.getText().toString());
                 try {
                    FileOutputStream fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                    //s??rialiser la liste des Etudiant dans le fichier.
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    finalList.add(etudiant);
                    oos.writeObject(finalList);
                    oos.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);

                /*
                SharedPreferences sh = getActivity().getSharedPreferences("tpsharedPreference", Context.MODE_APPEND);
                SharedPreferences.Editor editor = sh.edit();
                editor.putString("cine", binding.editTextCine.getText().toString());
                editor.commit();
                 */

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void pickImageFromGallery() {
        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    // g??rer le r??sultat de l'autorisation d'ex??cution
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length >0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //l'autorisation a ??t?? accord??e
                    pickImageFromGallery();
                }
                else {
                    //l'autorisation a ??t?? refus??e
                    Toast.makeText(getContext(), "Permission refus??e...!!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //traite le r??sultat de l'image s??lectionn??e
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            // d??finit l'image sur la vue d'image
            binding.imageEditEtudiant.setImageURI(data.getData());
            etudiant.setPhoto(binding.imageEditEtudiant.getTransitionName());
            etudiant.setPhoto("file:///"+getPathFromURI(data.getData()));
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}