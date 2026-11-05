package com.example.nutriscan.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    private static final int SIZE = 256;

    // Foto escolhida -> quadrado SIZE x SIZE -> JPEG -> texto Base64
    public static String uriToBase64(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        // 1) Só as dimensões
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int smallest = Math.min(bounds.outWidth, bounds.outHeight);
        if (smallest <= 0) {
            throw new IOException("Imagem inválida");
        }

        // 2) Decodificar já reduzida
        int sample = 1;
        while (smallest / (sample * 2) >= SIZE) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap decoded;
        try (InputStream in = resolver.openInputStream(uri)) {
            decoded = BitmapFactory.decodeStream(in, null, options);
        }
        if (decoded == null) {
            throw new IOException("Imagem inválida");
        }

        // 3) Rotação guardada na foto (EXIF)
        int degrees = 0;
        try (InputStream in = resolver.openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                degrees = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                degrees = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                degrees = 270;
            }
        } catch (Exception ignored) {
            // sem EXIF: assume que a foto está direita
        }

        // 4) Quadrado ao centro, rodado e redimensionado
        int side = Math.min(decoded.getWidth(), decoded.getHeight());
        int x = (decoded.getWidth() - side) / 2;
        int y = (decoded.getHeight() - side) / 2;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        float scale = (float) SIZE / side;
        matrix.postScale(scale, scale);
        Bitmap square = Bitmap.createBitmap(decoded, x, y, side, side, matrix, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        square.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap base64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}