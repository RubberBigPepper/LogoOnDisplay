package com.albakm.logoondisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ImageHelper {

    public static final String LOGO_FILENAME="logo.png";

    public static Bitmap getImageFromAsset(Context cContext)
    {
        try
        {
            InputStream cStream=cContext.getAssets().open("crosshair.png");
            Bitmap cBmp=BitmapFactory.decodeStream(cStream);
            cStream.close();
            return cBmp;
        }
        catch (OutOfMemoryError ex)
        {
            ex.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public static void RemoveInternalFile(Context cContext)
    {
        try {
            InputStream cStream=cContext.getAssets().open("crosshair.png");
            FileOutputStream fos = cContext.openFileOutput(LOGO_FILENAME,Context.MODE_PRIVATE);
            byte[] byArBuffer=new byte[65536];
            while(true)
            {
                int nRead=cStream.read(byArBuffer);
                if(nRead<=0)
                    break;
                fos.write(byArBuffer,0,nRead);
            }
            cStream.close();
            fos.close();
        }
        catch (Exception ex){}
    }

    public static void CopyFileToInternalFileDir(Context cContext, Uri uriImage)
    {//копируем файл с Uri во внутреннее хранилище
        try
        {
            InputStream is = cContext.getContentResolver().openInputStream(uriImage);
            FileOutputStream fos = cContext.openFileOutput(LOGO_FILENAME,Context.MODE_PRIVATE);
            byte[] byArBuffer=new byte[65536];
            while(true)
            {
                int nRead=is.read(byArBuffer);
                if(nRead<=0)
                    break;
                fos.write(byArBuffer,0,nRead);
            }
            is.close();
            fos.close();
        }
        catch (Exception ex){}
    }

    public static Bitmap getImage(Context cContext)
    {//Файл из внутреннего хранилища
        Bitmap cBmp=null;
        try
        {
            InputStream cStream=cContext.openFileInput(LOGO_FILENAME);
            cBmp=BitmapFactory.decodeStream(cStream);
            cStream.close();
            return cBmp;
        }
        catch (OutOfMemoryError ex)
        {
            ex.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if(cBmp!=null)
            return cBmp;//если не прочитан, тогда по умолчанию из ассета
        return getImageFromAsset(cContext);
    }
}
