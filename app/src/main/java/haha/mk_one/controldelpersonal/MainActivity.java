package haha.mk_one.controldelpersonal;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import haha.mk_one.controldelpersonal.Fragment.GridFragment;

/**
 * Main activity that holds our fragments
 *
 * @author bherbst
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {

    SQLiteDatabase db;
    Publicas P;
    SQLHELPER sqlhelper;
    SharedPreferences setting;
    ProgressDialog pDialog;
    ClassAlterDB alterDB;
    EditText terminoBusqueda;
    String criterioBusqueda = "";
    ArrayList<pdv> arrPdV = new ArrayList<>();
    Spinner spn_pdv;
    private GoogleApiClient mGoogleApiClient;
    Activity mActivity;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
    private LocationRequest mLocationRequest;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static Location mLastLocationMap;
    boolean isGPS = false;
    private static int UPDATE_INTERVAL = 1000; // 1 sec
    private static int FATEST_INTERVAL = 500; // medio sec
    private static int DISPLACEMENT = 0; // 0 meters
    PendingResult<LocationSettingsResult> result;
    private static final String TAG = MainActivity.class.getSimpleName();
    List<Hist_Asistencia_deldia> arrHistorial = new ArrayList<>();
    TextView tvRep_Hist, tvHora_S_E, tvLugar;
    static int idAsist = 0;
    boolean hayRegistros = true;
    FloatingActionButton fab;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        P = new Publicas(this);
        sqlhelper.getDatabaseStructure(db);
        terminoBusqueda = (EditText) findViewById(R.id.et_busqueda);
        terminoBusqueda.setEnabled(true);
        //    getArrayHist_Asistencia_deldia();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 3);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 2);
        askForPermission(Manifest.permission.READ_PHONE_STATE, 4);

        getPdV();
        // paRA EL mensajito de activar el gps desde la panalla principal
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new Publicas().locationPublica != null) {
                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    integrator.setPrompt("Enfoque el codigo de barras del Carné.");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(true);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.setOrientationLocked(false);
                    integrator.initiateScan();
                } else {
                    new Publicas().Toast(MainActivity.this, "No se ha encontrado Ubicacion, espere e intente denuevo", R.drawable.ic_warning);
                    // new Publicas().AlertDialog(getContext(),"Oops!","No se ha obtenido las coordenadas GPS, espere un momento y vuelva a intentar",R.drawable.ic_warning,4);
                }
            }
        });

        terminoBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    criterioBusqueda = s.toString();
                    buscar();
                } else {
                    criterioBusqueda = "";
                    buscar();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new GridFragment(), "uniqueTag")
                    .commit();
        }
        ModificarBD();
    }


  /*  private void getArrayHist_Asistencia_deldia() {

        try {
            final int position=0;
            idAsist = arrHistorial.get(position).getIdAsistencia();

            if (idAsist > 0) {
                String consulta = "select DISTINCT IdAsistencia" + sqlhelper.TablaPersonal + ".IdPersona" + "FechaEntr" + "FechaSal from" + sqlhelper.TablaAsistencia +
                        "where FechaEntr in(select max(FechaEntr)) and FechaSal in(select max(FechaSal))";
                Cursor cHistorial = db.rawQuery(consulta, null);
                if (cHistorial.moveToFirst()) {
                    do {
                        View view=getLayoutInflater().inflate(R.layout.activity_historial_dia,null);

                        tvRep_Hist = (TextView) findViewById(R.id.tvRep_Hist);
                        TextView tvHora_S_E = (TextView) findViewById(R.id.tvHora_S_E);
                        TextView tvLugar = (TextView) findViewById(R.id.tvLugar);
                        arrHistorial.add(new Hist_Asistencia_deldia(cHistorial.getInt(cHistorial.getColumnIndex("IdAsistencia")),
                                cHistorial.getString(cHistorial.getColumnIndex("IdPersona")),
                                cHistorial.getString(cHistorial.getColumnIndex("FechaEntr")),
                                cHistorial.getString(cHistorial.getColumnIndex("FechaSal"))
                        ));
                    } while (cHistorial.moveToNext());
                }
            } else {
                //arrHistorial.add((Hist_Asistencia_deldia, "No hay mas Asistencias, ¡Oops ☺!", R.drawable.ic_info);
            }
            if (hayRegistros) {
                String detalle = "\t\tHora Entrada:\n";
                detalle += arrHistorial.get(position).getFechaEntr() + "\n\t\t Hora Salida: \n" + arrHistorial.get(position).getFechaSal();

                tvRep_Hist.setText(detalle);
                tvHora_S_E.setText(arrHistorial.get(position).getIdPersona() + " " + arrHistorial.get(position).getIdAsistencia());
                tvLugar.setText(setting.getString("pdv", ""));
            } else {
                tvRep_Hist.setText(arrHistorial.get(position).getIdPersona() + " " + arrHistorial.get(position).getIdAsistencia());
                tvHora_S_E.setText("☺!");
                tvLugar.setText("");
            }
        } catch (Exception e)

        {
            e.printStackTrace();

        }
    }*/

    public void Historial(View v) {
        Intent activity_historial_dia = new Intent(MainActivity.this, Hist_Asistencia_deldia.class);
        startActivity(activity_historial_dia);
    }

    public void buscar() {

        limpiar_contenedrIMG();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, new GridFragment(), "uniqueTag")
                .commit();
    }

    public void limpiar_contenedrIMG() {
        Fragment fragment = new GridFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    public String getDataFragment() {
        return criterioBusqueda;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            int id = item.getItemId();

            if (id == R.id.action_actualizar) {
                new DescargarPersonas().execute();
            }
            if (id == R.id.action_enviar) {
                startActivity(new Intent(MainActivity.this, EnviosPendientes.class));
            }
            if (id == R.id.action_actualizar_app) {
                startActivity((new Intent(MainActivity.this, Actualizacion.class)));// mando a llamar la clase que usare a actualizar la aplicacion
                return true;
            }
            if (id == R.id.action_reporte) {
                startActivity((new Intent(MainActivity.this, Hist_Asistencia_deldia.class)));
                return true;
            }
            if (id == R.id.action_Enviar_PxP) {
                startActivity((new Intent(MainActivity.this, Enviar_RespuestaXPersona.class)));
                return true;
            }
            if (id == R.id.actionver_respuestas) {
                Inset_pass();
            }
        } catch (Exception e) {
            new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        new Publicas().locationPublica = location;
        // Assign the new location
        mLastLocationMap = location;
        // Displaying the new location on UI
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Conexión Fallida: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public class DescargarPersonas extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Descargando los datos. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DescargarPersonas.this.cancel(true);
                    P.Toast(MainActivity.this, "Se ha cancelado la petición.", R.drawable.ic_info);
                }
            });
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("opcion", "getAllPA"));
                params.add(new BasicNameValuePair("listaParametros", setting.getString("pdv", "")));// setting.getString("stLocal", "")));
                params.add(new BasicNameValuePair("format", "json"));

                json = jsonParser.makeHttpRequest(P.HOST + "ws/asist_personal/consultasDB.php",
                        "POST", params);

                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {

                        db.delete(sqlhelper.TBLPERSONAL, null, null);

                        jsonArray = json.getJSONArray("Datos");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject c = jsonArray.getJSONObject(i);

                                publishProgress("" + i, "" + jsonArray.length(), c.getString("Nombre"));

                                ContentValues values = new ContentValues();

                                values.put("IdPersona", c.getInt("IdPersona"));
                                values.put("Nombre", c.getString("Nombre"));
                                values.put("Apellidos", c.getString("Apellido"));
                                values.put("NoCedula", c.getString("NoCedula"));
                                values.put("Foto", c.getString("URLFoto"));
                                values.put("codigo", c.getString("Codigo"));

                                db.insert(sqlhelper.TBLPERSONAL, null, values); // se inserta en la base de datos local
                                DescargarFotoUsuario(c.getString("URLFoto"));

                            } catch (Exception e) {
                                P.AlertDialog(MainActivity.this, "Error", e.getMessage() + ", " + e.getLocalizedMessage(), R.drawable.ic_error, 1);
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(values[0] + " de " + values[1] + " - " + values[2]);
        }

        @Override
        protected void onPostExecute(JSONObject resultado) {
            super.onPostExecute(resultado);
            try {
                if (resultado == null) {
                    P.AlertDialog(MainActivity.this, "Error al enviar", "Sin repuesta del servidor, o revisa la conexión de datos.", R.drawable.ic_error, 1);
                }
                int success = resultado.getInt("success");
                if (success == 1) {
                    P.AlertDialog(MainActivity.this, "Guardado", "Se ha descargado la lista con éxito.", R.drawable.ic_success, 2);
                    recreate();
                }
                if (success == 0) {
                    P.AlertDialog(MainActivity.this, "Error", resultado.getString("message"), R.drawable.ic_error, 1);
                }
            } catch (Exception e) {
                new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
                e.printStackTrace();
            }

            pDialog.dismiss();
        }
    }

    public void DescargarFotoUsuario(String URL) {
        try {

            File direct = new File(Environment.getExternalStorageDirectory() + "/grupovalor/fotos");

            if (!direct.exists()) {
                direct.mkdirs();
            }

            File imgFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/" + URL);
            if (imgFile.exists()) {
            } else {

                DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                Uri downloadUri = Uri.parse("http://grupovalor.com.ni/ws/asist_personal/" + URL);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false).setTitle("Descargando a " + URL)
                        .setDescription("Foto del Personal")
                        .setDestinationInExternalPublicDir("/grupovalor/", URL);
                mgr.enqueue(request);
            }
        } catch (Exception e) {
            new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    private void ModificarBD() {
        try {

            alterDB = new ClassAlterDB(MainActivity.this);
            if (!alterDB.ExisteColumna(sqlhelper.TBLASISTENCIA, "EstadoSalida")) {
                alterDB.AgregarColumna(sqlhelper.TBLASISTENCIA, "EstadoSalida", "integer");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TBLASISTENCIA, "CantIntentos")) {
                alterDB.AgregarColumna(sqlhelper.TBLASISTENCIA, "CantIntentos", "integer");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TBLASISTENCIA, "CanIntSal")) {
                alterDB.AgregarColumna(sqlhelper.TBLASISTENCIA, "CanIntSal", "integer");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TBLPREG_X_PERSONA, "Estado_envio")) {
                alterDB.AgregarColumna(sqlhelper.TBLPREG_X_PERSONA, "Estado_envio", "integer DEFAULT 0");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TBLPERSONAL, "codigo")) {
                alterDB.AgregarColumna(sqlhelper.TBLPERSONAL, "codigo", "text");
            }
            alterDB.AgregarTabla(sqlhelper.Tabla_de_Turnos);

            if (!alterDB.ExisteColumna(sqlhelper.TBLASISTENCIA, "Turno")) {
                alterDB.AgregarColumna(sqlhelper.TBLASISTENCIA, "Turno", "text");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TABLA_P_D_V, "idPdv")) {
                alterDB.AgregarColumna(sqlhelper.TABLA_P_D_V, "idPdv", "integer");
            }
            if (!alterDB.ExisteColumna(sqlhelper.TABLA_P_D_V, "locacioGPS")) {
                alterDB.AgregarColumna(sqlhelper.TABLA_P_D_V, "locacioGPS", "text");
            }


        } catch (Exception e) {
            new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void IntroducirPdv() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dime_de_donde_eres);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        spn_pdv = (Spinner) dialog.findViewById(R.id.spn_pdv);
        Button btn_aceptar_PDV = (Button) dialog.findViewById(R.id.btn_AceptarPDV);

        ArrayAdapter<String> adptrpdvv = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, new Publicas().getListaPdV(arrPdV));
        spn_pdv.setAdapter(adptrpdvv);

        spn_pdv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(MainActivity.this, "el valor de setting es: " + setting.getString("pdv", ""), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        btn_aceptar_PDV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (setting.getString("pdv", "").length() > 0) {
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("pdv", String.valueOf(arrPdV.get(spn_pdv.getSelectedItemPosition()).getIdPDV()));
                editor.commit();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Descargue el registro de personal", Toast.LENGTH_LONG).show();
                // }

            }
        });

        dialog.show();


    }

    private void getPdV() {
        try {

            if (setting.getString("pdv", "").length() == 0) {
                arrPdV.clear();
                Cursor Cpdv = db.rawQuery("select * from " + sqlhelper.TABLA_P_D_V, null);
                if (Cpdv.moveToFirst()) {
                    do {
                        arrPdV.add(new pdv(
                                Cpdv.getString(Cpdv.getColumnIndex("descPdv")),
                                Cpdv.getInt(Cpdv.getColumnIndex("IDpv")))
                        );
                    } while (Cpdv.moveToNext());


                    IntroducirPdv();

                } else {
                    if (new Publicas().TieneConexion(this)) {
                        new DescargarPDV().execute();
                    } else {
                        new Publicas().AlertDialog(this, "Error de Conexion", "Ocurrio un error del servidor o NO ESTAS CONECTADO A INTERNET", R.drawable.ic_warning, 1);
                    }

                }
            }

        } catch (Exception e) {
            new Publicas().AlertDialog(MainActivity.this, "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public class DescargarPDV extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Descargando datos iniciales. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DescargarPDV.this.cancel(true);
                    new Publicas().Toast(MainActivity.this, "Se ha cancelado la petición.", R.drawable.ic_info);
                }
            });
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("opcion", "getAllPV"));
                params.add(new BasicNameValuePair("listaParametros", ""));// setting.getString("stLocal", "")));
                params.add(new BasicNameValuePair("format", "json"));

                json = jsonParser.makeHttpRequest(P.HOST + "ws/asist_personal/consultasDB.php",
                        "POST", params);

                if (json != null) {
                    int success = json.getInt("success");
                    if (success == 1) {

                        db.delete(sqlhelper.TABLA_P_D_V, null, null);

                        jsonArray = json.getJSONArray("Datos");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject c = jsonArray.getJSONObject(i);

                                publishProgress("" + i, "" + jsonArray.length(), c.getString("NombrePdV"));

                                ContentValues values = new ContentValues();
                                values.put("descPdv", c.getString("NombrePdV"));
                                values.put("IDpv", c.getString("id_Local"));
                                values.put("locacioGPS", c.getString("LocationGPS"));
                                db.insert(sqlhelper.TABLA_P_D_V, null, values); // se inserta en la base de datos local

                            } catch (Exception e) {
                                new Publicas().AlertDialog(MainActivity.this, "Error", e.getMessage() + ", " + e.getLocalizedMessage(), R.drawable.ic_error, 1);
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(values[0] + " de " + values[1] + " - " + values[2]);
        }

        @Override
        protected void onPostExecute(JSONObject resultado) {
            super.onPostExecute(resultado);
            try {
                if (resultado == null) {
                    new Publicas().AlertDialog(MainActivity.this, "Error", "Sin repuesta del servidor, o revisa la conexión de datos.", R.drawable.ic_error, 1);
                }
                int success = resultado.getInt("success");
                if (success == 1) {
                    //new Publicas().AlertDialog(MainActivity.this, "Guardado", "Se ha descargado la lista con éxito.", R.drawable.ic_success, 2);
                    // new Publicas().reiniciarActivity(MainActivity.this);
                }
                if (success == 0) {
                    new Publicas().AlertDialog(MainActivity.this, "Error", resultado.getString("message"), R.drawable.ic_error, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pDialog.dismiss();
            }
            getPdV();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //region Metodos gps
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.w("Estoy en OnStar ", "" + mGoogleApiClient);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Estoy en ", "OnResumen");

        checkPlayServices();

        try {
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
                displayLocation();

            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            stopLocationUpdates();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
                Log.w("Estoy en ", "OnStop");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                P.Toast(mActivity, "Dispositivo no soporta.", R.drawable.ic_error);
                finish();
            }
            return false;
        }
        return true;

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected void startLocationUpdates() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert();
            }
            e.printStackTrace();
        }
    }

    public Location displayLocation() {

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_ASK_PERMISSIONS);

                        P.Toast(this, "Solicitando Permisos", R.drawable.ic_info);

                    } else if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED) {

                        P.Toast(this, "Los permisos de GPS han sido concedidos.", R.drawable.ic_success);
                        displayLocation();

                    }
                }
            }

            mLastLocationMap = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            if (mLastLocationMap != null) {
                /*terminoBusqueda.setText( mLastLocationMap.getLatitude() + "," + mLastLocationMap.getLongitude());*/
                isGPS = true;
                startLocationUpdates();
                return mLastLocationMap;
            } else {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mLastLocationMap;
    }

    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public void showSettingsAlert() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL); /// tenia 30 * 1000 como parametro
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);   // tenia 5 * 1000 como parametro
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, 7);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    //endregion Metodos GPS

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    private void Inset_pass() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.intro_contrasena);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        final EditText et_password = (EditText) dialog.findViewById(R.id.et_passwors);
        Button btn_Aceptar = (Button) dialog.findViewById(R.id.btn_aceptarIC);
        Button btn_cancelar = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_Aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_password.getText().toString().equalsIgnoreCase("1234")) {
                    et_password.setBackgroundColor(Color.TRANSPARENT);
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, MostrarRespuestas.class));
                } else {
                    et_password.setText("");
                    et_password.setBackgroundColor(Color.RED);
                }
            }
        });
        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}

