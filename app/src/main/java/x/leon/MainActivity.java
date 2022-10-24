package x.leon;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//import leon.x.xdarknet2.R;

public class MainActivity extends AppCompatActivity {

    protected Button button;
    private static String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ File.separator + "XDarknet" + File.separator;
    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    Bitmap srcBitmap;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        DarknetDao.getInstance(this);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });
        srcBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.dog);
        imageView = (ImageView)findViewById(R.id.imageView);
        // copy dog to /data
        UtilFile.CopyResFileToSdcard(this, R.drawable.dog, PATH+"temp.jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            int bytes = bitmap.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
            bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区
            byte[] rgba = buffer.array();
            c.close();

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ File.separator + "XDarknet" + File.separator+"temp.jpg");
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.write(rgba);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("Path:",imagePath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                long time = System.currentTimeMillis();
                Result[] results = DarknetDao.getInstance(MainActivity.this).inference(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "XDarknet" + File.separator + "temp.jpg");
                Log.e("TIME:", "dealing " + (System.currentTimeMillis() - time));
                for (Result r : results) {
                    Log.e("Rst:", r.getLabel() + " " + r.getConfidence() + " " + r.getLeft() + " " + r.getTop() + " " + r.getWidth()+ " " + r.getHeight());
                }
                }
            }).run();
        }
    }

    public void detectClick(View view)
    {

        switch (view.getId()) {
            case R.id.detbtn:
                // Do something
                Result[] results = DarknetDao.getInstance(MainActivity.this).inference(PATH + "temp.jpg");
                Bitmap bitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
                int h = srcBitmap.getHeight();
                int w = srcBitmap.getWidth();
                int xBounding = ((View) imageView.getParent()).getWidth();//EXPECTED WIDTH
                int yBounding = ((View) imageView.getParent()).getHeight();//EXPECTED HEIGHT

                float xScale = ((float) w)/xBounding;
                float yScale = ((float)h) /yBounding;
                Canvas cnvs=new Canvas(bitmap);
                Paint paint=new Paint();
                paint.setStrokeWidth( 8F);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.RED);
                cnvs.drawBitmap(bitmap, 0, 0, null);

                Paint textPaint = new Paint();
                // textPaint.setColor(Color.WHITE);
                // textPaint.setStyle(Paint.Style.FILL);
                // cnvs.drawPaint(textPaint);
                textPaint.setColor(Color.GREEN);
                textPaint.setTextSize(100);

                for(int i=0; i<results.length; i++) {
                    cnvs.drawRect(results[i].getLeft()*w, results[i].getTop()*h, (results[i].getLeft()+results[i].getWidth())*w, (results[i].getTop()+results[i].getHeight())*h, paint);
                    cnvs.drawText(results[i].getLabel(), results[i].getLeft()*w, results[i].getTop()*h, textPaint);
                }
                imageView.setImageBitmap(bitmap);
            default: Log.e("detection", "no output");
        }
    }
}
