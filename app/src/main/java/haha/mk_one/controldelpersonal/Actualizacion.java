package haha.mk_one.controldelpersonal;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

//Created by USUARIO on 13/7/2017.

public class Actualizacion extends AppCompatActivity implements View.OnClickListener {
    private final int APLICACION = 1, RESPUESTAS = 2;

    ProgressDialog pDialog;
    Publicas P;
    Context mContext;
    Button btnApp,btn_GetResp;
    SharedPreferences setting;
    SQLiteDatabase db;
    SQLHELPER sqlhelper;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizacion);
        setting = PreferenceManager.getDefaultSharedPreferences(this); // siempre instanciar de primero
        mContext = this;
        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        // toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnApp = (Button) findViewById(R.id.btnApp_A);
        btn_GetResp = (Button)findViewById(R.id.btn_Respuestas);
        btnApp.setOnClickListener(this);
        btn_GetResp.setOnClickListener(this);
        P= new Publicas(this);
        //instanciar clase

        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 3);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String list = bundle.getString("actualizar", "");
          /*  if (list.length() > 0) {
                Toast.makeText(Actualizacion.this, "La lista de " + list + " debe ser Actuaizada.", Toast.LENGTH_LONG).show();
            }*/
            int nuevaVersion = bundle.getInt("descNewVersion");
            if (nuevaVersion == 1) {
                new DescargarArchivo().execute(P.HOST + "ad/fv.apk");
            }
        } else {
            Toast.makeText(Actualizacion.this, "La actualizacion de la aplicacion es obligatoria.", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Metodo para pedir permisos al usuario
     *
     * @param permission  Tipo de Permiso
     * @param requestCode Codigo unico para esperar respuesta del usuario
     */
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " ya esta permtido.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:

                break;
            case 2:
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Ir_a(APLICACION);
                }
                break;
        }
    }
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Actualizacion Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    //EVENTO DE BOTONES
    public void onClick(View v) {
        if (v == btnApp) {
            Ir_a(APLICACION);
        }
        if (v == btn_GetResp){
            Ir_a(RESPUESTAS);
        }
    }

    private void Ir_a(int pos) {
        if (P.TieneConexion(this))
            switch (pos) {
                case APLICACION:
                    new DescargarArchivo().execute(P.HOST + "ad/app-cp.apk");
                    break;
                case RESPUESTAS:
                    Verificar_nohayPendiente();
                    break;
            }
        else
            P.AlertDialog(mContext, "Internet", "Revisa la conexión a internet.", R.drawable.ic_error, 1);

    }

    public class DescargarArchivo extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Actualizacion.this);
            pDialog.setMessage("Descargando la nueva versión, por favor espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    P.Toast(Actualizacion.this, "Se descargará en segundo plano.", R.drawable.ic_info);
                }
            });
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/");
                if (!file.exists()) {
                    file.mkdirs();
                }

                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/app-cp.apk");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    Log.e("Descarga archivo", "" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setMessage("Descargando el " + progress[0] + "% de la nueva versión, por favor espere...");
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();

            Instalar();

        }
    }

    public void Instalar() {

        SharedPreferences.Editor editor = setting.edit();
        editor.putInt("appversion", 0);
        editor.apply();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/grupovalor" + "/inventariopermanente.apk")), "application/vnd.android.package-archive");
        startActivity(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity();
        } else finish();
    }


    public class getRespuestas extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Actualizacion.this);
            pDialog.setMessage("Descargando los datos. \nEsto puede tardar unos segundos, espere...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getRespuestas.this.cancel(true);
                    P.Toast(Actualizacion.this, "Se ha cancelado la petición.", R.drawable.ic_info);
                }
            });
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            // getting JSON Object
            // Note that create product url accepts POST metho
            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("local", setting.getString("pdv","")));
                json = jsonParser.makeHttpRequest(P.HOST + "/ws/asist_personal/get_Respuestas_ot_Version.php",
                        "POST", params);
                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {

                        db.delete(sqlhelper.TBLPREG_X_PERSONA, null, null);

                        jsonArray = json.getJSONArray("Respuestas");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);

                            publishProgress("" + i, "Descargarndo");
                           // Log.w("desc presentacion", c.getInt("idPresentComp") + ", " + c.getString("NombrePresentMarcaComp"));

                            ContentValues values = new ContentValues();

                            values.put("IdPP", c.getInt("idR_X_P"));
                            values.put("IdPersona", c.getInt("IdPersona"));
                            values.put("IdPregunta", c.getInt("IdPregunta"));
                            values.put("Repuestas", c.getString("Respuesta"));
                            values.put("Estado_envio",1);

                            db.insert(sqlhelper.TBLPREG_X_PERSONA, null, values); // se inserta en la base de datos local
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage("Descargando");//values[0] + " de " + values[1] + " - " + values[2]);
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                if (file_url == null) {
                    P.AlertDialog(mContext, "Error al Obtener Datos", "Sin repuesta del servidor, o revisa la conexión de datos.", R.drawable.ic_error,1);
                } else {
                    int success = file_url.getInt("success");
                    if (success == 1) {
                        P.AlertDialog(mContext, "Guardado", "Se ha descargado la lista de Respuestas con éxito.", R.drawable.ic_success,2);
                    } else {
                        P.AlertDialog(mContext, "Error", file_url.getString("message"), R.drawable.ic_error,1);
                    }
                }
            } catch (JSONException e) {
                P.AlertDialog(mContext, "Error", e.getMessage(), R.drawable.ic_error,1);
                e.printStackTrace();
            }
            pDialog.dismiss();

        }
    }

    /**
     * Metodo que verifica que no hallan respuestas a pregunta secreta que aun esten pendientes de envio, evitando que se descarguen solo lo que estan enviando y creando conflictos entre id
     * de la tabla local con el id que proviene del servidor; solo se descargara si todas las respuestas estan enviadas.
     */
    private void Verificar_nohayPendiente(){
        Cursor cVerificar =db.rawQuery("select * from "+sqlhelper.TBLPREG_X_PERSONA+" where Estado_envio = 0",null);
        if (cVerificar.getCount()>0){
            P.AlertDialog(this,"Advertencia","Aun existen respuestas de pregunta secreta pendientes de envio o no se han completado, por favor envie y vuelva a intentarlo",R.drawable.ic_warning,4);
        }else {
            new getRespuestas().execute();
        }

    }

}
