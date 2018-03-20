package com.example.databaseapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    // データベース名を定数に登録
    private final static String DB_NAME = "testDB.db";
    // テーブル名を登録
    private final static String DB_TABLE = "testTable";
    // データベースのバージョンを登録
    private final static int DB_VERSION = 1;

    private static final String BITMAP = "bitmap";

    // データベース用のオブジェクトを格納するフィールド変数
    private SQLiteDatabase databaseObject;				//①

    //エディットテキスト
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);

        // データベースオブジェクトの作成(データベースのオープン)
        DatabaseHelper dbHelperObject =	new DatabaseHelper(MainActivity.this);
        databaseObject = dbHelperObject.getWritableDatabase();

        // 保存用ボタンの処理
        findViewById(R.id.button1)
            .setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // データベースへの書き込み
                            Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bmp1.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] bytes = byteArrayOutputStream.toByteArray();

                            writeToDB(bytes);
                        } catch (Exception e) {
                            // 書き込み失敗時にメッセージを表示
                            showDialog(
                                    getApplicationContext(),
                                    "ERROR",
                                    "データの書き込みに失敗しました "
                            );
                        }
                    }
            });

        findViewById(R.id.button2)
            .setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // データベースの読み込み
                            Bitmap bitmap = null;
                            byte[] bytes = readToDB();	//④
                            if (bytes != null) {
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ImageView ivCamera = (ImageView) findViewById(R.id.ivCamera);  // （3)
                                ivCamera.setImageBitmap(bitmap);  // （3）

                            }

                        } catch (Exception e) {
                            showDialog(
                                getApplicationContext(),
                                "ERROR",
                                "データの読み込みに失敗しました ");
                        }
                    }
             });
    }

    /**********************************************
     * データベースへの書き込みを行うメソッド
     * @param editedStrings  書き込む文字列
     **********************************************/
    private void writeToDB(byte[] bytes) throws Exception {
        // ContentValues型のインスタンスを生成
        ContentValues contentValObject = new ContentValues();	//⑤
        // カラムの作成
        contentValObject.put("id", "0");			//⑥
        // カラムの追加
        contentValObject.put("info", bytes);	//⑦
//        contentValObject.put("location", bytes);	//⑦


        // レコードを上書き
        int numberOfColumns =				//⑧
        databaseObject.update(
                DB_TABLE,
                contentValObject,
                null,
                null
        );
        // レコードが存在しない場合は新規に作成
        if (numberOfColumns == 0)			//⑨
        databaseObject.insert(
                DB_TABLE,
                null,
                contentValObject
        );
    }

    /**********************************************
     * データベースから読み込みを行うメソッド
     * @return  valueCursor  レコードのデータ
     **********************************************/
    private byte[] readToDB() throws Exception {
        // データベースからテーブルを読み込む
        byte[] bytes = null;
        Cursor cursor = databaseObject.query(	//⑩
                DB_TABLE,
                new String[]{"id", "info"},
                "id='0'",
                null,
                null,
                null,
                null
        );

        // cursor内のレコード数が0の場合は例外処理を行うインスタンスを生成
        if (cursor.getCount() == 0) {	//⑪
            throw new Exception();
        }

        // カーソルの位置を先頭のレコードに移動
        cursor.moveToFirst();		//⑫
        // cursorオブジェクト内のレコードのデータをString型に変換
        bytes = cursor.getBlob(1);		//⑬
        // カーソルを閉じる
        cursor.close();		//⑭
        // レコードのデータを呼び出し元に返す
        return bytes;
    }

    /**********************************************
     * ダイアログを表示するメソッド
     * @param context    アプリケーションのContextオブジェクト
     * @param title      ダイアログのタイトル
     * @param text       ダイアログのメッセージ
     **********************************************/
    private static void showDialog(
            Context context,
            String title,
            String text
    ) {
        AlertDialog.Builder varAlertDialog =
                new AlertDialog.Builder(context);
        varAlertDialog.setTitle(title);
        varAlertDialog.setMessage(text);
        varAlertDialog.setPositiveButton("OK", null);
        varAlertDialog.show();
    }

    // ヘルパークラスの定義
    private static class DatabaseHelper extends SQLiteOpenHelper {	//⑮
        // データベースを作成、または開く、管理するための処理
        public DatabaseHelper(Context context) {		//⑯
            // ヘルパークラスクラスのコンストラクターの呼び出し
            super(
                context,
                DB_NAME,
                null,
                DB_VERSION
            );
        }

        // テーブルを作成するメソッドの定義
        @Override
        public void onCreate(SQLiteDatabase db) {	//⑰
            // テーブルの作成
            db.beginTransaction();
            try {
                StringBuilder sql = new StringBuilder();
//                sql.append("CREATE TABLE IF NOT EXISTS ").append(DB_TABLE).append(" {");
//                sql.append("id").append("TEXT PRIMARY KEY");
//                sql.append("info").append("TEXT");
//                sql.append("location").append("TEXT");
                sql.append("};");
                db.execSQL(sql.toString());

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

        }

        // データベースをアップグレードするメソッドの定義
        @Override
        public void onUpgrade(		//⑲
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
        ) {
            // 古いバージョンのテーブルが存在する場合はこれを削除
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);	//⑳
            // 新規テーブルの作成
            onCreate(db);
        }
    }
}