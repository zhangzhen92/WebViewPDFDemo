package com.example.zz.webpdfdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity {

    private UserWebView webView;
    private int webViewHeight;                                   //webview整体的高度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(judgeAndroidVersion()){                                  //如果要是5.0手机以上，必须要使用该属性,否则快照内容不全
           WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        webView = ((UserWebView) findViewById(R.id.webview_id));                  //webview一定要重写，里面有两个获取高度和宽度的方法
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDefaultTextEncodingName("UTF-8");
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("http://mp.weixin.qq.com/s/ePJ2GnvLLnoBkg5ajbbe6A");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(judgeAndroidVersion()){                                         //可以通过获取缩放，然后设置值从而控制webview快照的高度
                    float scale = webView.getScale()-1;                        
                    webViewHeight = (int) (webView.getPageHeight()*1);
                }else {
                    webViewHeight = webView.getPageHeight();
                }
                final Bitmap bitmap = Bitmap.createBitmap(webView.getPageWidth(), webViewHeight, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                webView.draw(canvas);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveBitmapFile(bitmap,"/webview.jpg");                         //把整体的图片保存到本地下
                        convertPDF(bitmap);
                    }
                }).start();

            }
        },2000);
    }

    public void convertPDF(Bitmap bitmap){
        Document document = new Document(PageSize.A4,  0, 0, 0,0);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
                    + "/webviewpdf.pdf"));//通过书写器（Writer）可以将文档写入磁盘中
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();
        float PDFBitmapRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();//是否需要分页
        if (PDFBitmapRatio <= 1.4) {///不需要分页

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到stream中
            byte[] byteArray = stream.toByteArray();
            try {
                Image image = Image.getInstance(byteArray);//生成image实例
                image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());//适应A4纸
                document.add(image);//将图片放入磁盘
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {//需要分页
            float BitmapHeightPerPage = (float) bitmap.getWidth() * 1.4f;//每一页的高度
            int pages = (int) Math.ceil(bitmap.getHeight() / BitmapHeightPerPage);//向上取整
            System.out.println("pages:" + pages);
            Bitmap sub_bitmap;
            for (int i = 0; i < pages; i++) {
                if (i == pages - 1) {//最后一页需要处理一下
                    /**
                     * can not use default setting, or pdf reader cannot read the exported pdf
                     */
                    sub_bitmap = Bitmap.createBitmap(bitmap, 0, (int) BitmapHeightPerPage * i, bitmap.getWidth(), (int) (bitmap.getHeight() - (BitmapHeightPerPage * (pages - 1))));
                    /*
                    第一个参数是数据源，第二个是x偏移量，第三个是y偏移量，第四个是截取宽度，第五个是截取高度
                     */
                } else {
                    sub_bitmap = Bitmap.createBitmap(bitmap, 0, (int) BitmapHeightPerPage * i, bitmap.getWidth(), (int) BitmapHeightPerPage);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                sub_bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到stream中
                byte[] byteArray = stream.toByteArray();
                try {
                    Image image = Image.getInstance(byteArray);
                    image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                    document.add(image);
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
               // sub_bitmap.recycle();//垃圾回收
            }
        }
        document.close();
    }


    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }




    /**
     * 保存图片到指定位置
     * @param bitmap
     */
    public static void saveBitmapFile(Bitmap bitmap,String name) {
        File file = new File(Environment.getExternalStorageDirectory() + name);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断安卓版本
     */
    public Boolean judgeAndroidVersion(){
        //如果要是5.0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return true;
    }
        return false;
}
}
