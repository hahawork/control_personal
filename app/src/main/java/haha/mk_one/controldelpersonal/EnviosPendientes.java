package haha.mk_one.controldelpersonal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EnviosPendientes extends AppCompatActivity {

    ListView lvPendientes;
    SQLiteDatabase db;
    static ArrayList<ListaAsitencia_EnvioPendiente> arrPendientes = new ArrayList<>();
    SQLHELPER sqlhelper;
    SharedPreferences setting;
    boolean hayRegistros = true;
    static int idAsistSelecc = 0;
    ProgressDialog pDialog;
    String NombrePDVlocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envios_pendientes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvPendientes = (ListView) findViewById(R.id.lvEnviosPendientes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        setting = PreferenceManager.getDefaultSharedPreferences(this);

        Cursor cObtenerNombreLocalpdv = db.rawQuery("select * from "+sqlhelper.TABLA_P_D_V+" where IDpv = "+setting.getString("pdv",""),null);
        cObtenerNombreLocalpdv.moveToFirst();
        NombrePDVlocal = cObtenerNombreLocalpdv.getString(cObtenerNombreLocalpdv.getColumnIndex("descPdv"));



        getArrayPendientes();
        lvPendientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try {

                    idAsistSelecc = arrPendientes.get(position).getIdAsistencia();

                    if (idAsistSelecc > 0) {
                        Cursor cEnviar = db.rawQuery("select * from " + sqlhelper.TBLASISTENCIA + " where IdAsistencia = " + idAsistSelecc, null);
                        if (cEnviar.getCount() > 0)
                            new Enviar().execute(
                                    arrPendientes.get(position).getId_Persona()+"",
                                    arrPendientes.get(position).getFechaEntrada(),
                                    arrPendientes.get(position).getFechaSalida(),
                                    arrPendientes.get(position).getComentarioEntrada(),
                                    arrPendientes.get(position).getComentarioSalida(),
                                    arrPendientes.get(position).getIntentoEntrada()+"",
                                    arrPendientes.get(position).getIntentoSalida()+"",
                                    setting.getString("pdv","")
                            );
                    } else {
                        new Publicas().Toast(EnviosPendientes.this, "No tienes envios de Asistencias pendientes, ¡Queee bieeen ☺!", R.drawable.ic_info);
                    }

                } catch (
                        Exception e)

                {
                    e.printStackTrace();
                }
            }

        });
    }


    private void getArrayPendientes() {

        try {

            Cursor cPendient = db.rawQuery("select asist.Comentario_E,asist.Comentario_S,asist.IdAsistencia,asist.IdPersona,person.Nombre,person.Apellidos, asist.FechaEntr,asist.FechaSal, asist.CantIntentos, asist.CanIntSal, asist.Turno from " + sqlhelper.TBLASISTENCIA + " as asist inner join "
                    + sqlhelper.TBLPERSONAL + " as person on asist.IdPersona = person.IdPersona where asist.EstadoSalida = 1 and asist.EstadoEnvio = 0", null);

            arrPendientes.clear();

            if (cPendient.moveToFirst()) {
                do {
//String marca, String fecha, String pdV, String cadenaPrecioCompetencia, String cadenaMIprecio, int IDpdv, int idMarca, int numDGuardado
                    arrPendientes.add(new ListaAsitencia_EnvioPendiente(
                            cPendient.getString(cPendient.getColumnIndex("FechaEntr")),
                            cPendient.getString(cPendient.getColumnIndex("FechaSal")),
                            cPendient.getInt(cPendient.getColumnIndex("IdAsistencia")),
                            cPendient.getString(cPendient.getColumnIndex("Comentario_E")),
                            cPendient.getString(cPendient.getColumnIndex("Comentario_S")),
                            cPendient.getInt(cPendient.getColumnIndex("IdPersona")),
                            cPendient.getString(cPendient.getColumnIndex("Nombre")),
                            cPendient.getString(cPendient.getColumnIndex("Apellidos")),
                            cPendient.getInt(cPendient.getColumnIndex("CantIntentos")),
                            cPendient.getInt(cPendient.getColumnIndex("CanIntSal"))


                    ));
                } while (cPendient.moveToNext());
            } else {
                arrPendientes.add(new ListaAsitencia_EnvioPendiente("", "", 0, "","", 0, "EXCELENTE, NO HAY PENDIENTES", "",0,0));
                hayRegistros = false;
            }

            List<String> arrayList = new ArrayList<>();

            for (int i = 0; i < arrPendientes.size(); i++) {
                arrayList.add(arrPendientes.get(i).getNombre() + " " + arrPendientes.get(i).getApellidos());
            }

            String[] simpleArray = new String[arrayList.size()];
            arrayList.toArray(simpleArray);

            lvPendientes.setAdapter(new Custom_List_Pendientes(this, simpleArray));
        } catch (Exception e) {
            e.printStackTrace();
            new Publicas().AlertDialog(EnviosPendientes.this, "Error!", e.getMessage(), R.drawable.ic_error, 1);

        }
    }

    private class ListaAsitencia_EnvioPendiente {

        private String FechaEntrada;
        private String FechaSalida;
        private int IdAsistencia;
        private String ComentarioSalida;
        private String ComentarioEntrada;
        private String Nombre;
        private String Apellidos;
        private int Id_Persona;
        private int IntentoEntrada;
        private int IntentoSalida;

        public ListaAsitencia_EnvioPendiente(String fechaEntrada, String fechaSalida, int idasistencia, String comentarioentrada,String comentariosalida, int id_Persona, String nombre, String apellidos, int intentoentrada, int intentosalida) {
            FechaEntrada = fechaEntrada;
            FechaSalida = fechaSalida;
            IdAsistencia = idasistencia;
            ComentarioEntrada = comentarioentrada;
            ComentarioSalida = comentariosalida;
            Id_Persona = id_Persona;
            Nombre = nombre;
            Apellidos = apellidos;
            IntentoEntrada = intentoentrada;
            IntentoSalida = intentosalida;
        }

        public String getFechaEntrada() {
            return FechaEntrada;
        }

        public String getFechaSalida() {
            return FechaSalida;
        }

        public int getIdAsistencia() {
            return IdAsistencia;
        }

        public String getComentarioEntrada() {
            return ComentarioEntrada;
        }

        public String getComentarioSalida(){ return ComentarioSalida;}

        public int getId_Persona() {
            return Id_Persona;
        }

        public String getNombre() {
            return Nombre;
        }

        public String getApellidos() {
            return Apellidos;
        }

        public int getIntentoEntrada() {return IntentoEntrada;}

        public int getIntentoSalida() {return IntentoSalida;}

    }

    public class Custom_List_Pendientes extends ArrayAdapter<String> {

        public Custom_List_Pendientes(Activity context, String[] a) {
            super(context, R.layout.activity_ependientes, a);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = EnviosPendientes.this.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.activity_ependientes, null, true);
            String PrecioCompetCadena = "";
            try {
                TextView tvPresent = (TextView) rowView.findViewById(R.id.tvAsistencia_EPP);
                TextView tvmarca = (TextView) rowView.findViewById(R.id.tvNomb_EPP);
                TextView tvpdp = (TextView) rowView.findViewById(R.id.tvHoraMarcada_EPP);

                /*se llenan los label que van a presentar los precios pendientes
                a enviar a los usuarios*/

                if (hayRegistros) {
                    String detalle = "\t\tFecha: ";
                    detalle += arrPendientes.get(position).getFechaEntrada().substring(0,10);

                    tvPresent.setText(detalle);
                    tvmarca.setText(arrPendientes.get(position).getNombre() + " " + arrPendientes.get(position).getApellidos());
                    tvpdp.setText(NombrePDVlocal);
                } else {
                    tvmarca.setText(arrPendientes.get(position).getNombre());
                    tvPresent.setText("☺!");
                    tvpdp.setText("");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }
    }

    class Enviar extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EnviosPendientes.this);
            pDialog.setMessage("Guardando los datos. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Enviar.this.cancel(true);
                    new Publicas().Toast(EnviosPendientes.this, "Se ha cancelado la petición.", R.drawable.ic_info);
                }
            });
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idpersonal", args[0]));
            params.add(new BasicNameValuePair("fyhentrada", args[1]));
            params.add(new BasicNameValuePair("fyhsalida", args[2]));
            params.add(new BasicNameValuePair("comententrada",args[3]));
            params.add(new BasicNameValuePair("comentsalida",args[4]));
            params.add(new BasicNameValuePair("intentoentrada",args[5]));
            params.add(new BasicNameValuePair("intentosalida",args[6]));
            params.add(new BasicNameValuePair("id_local",args[7]));
            Log.w("Enviando fv enc", URLEncodedUtils.format(params, "utf-8"));

            json = jsonParser.makeHttpRequest(new Publicas().HOST + "ws/asist_personal/ws_guardar_Asistencia_del_Personal.php",
                    "POST", params);

            if (json != null) {
                try {
                    int success = json.getInt("successInsert");
                    if (success == 1) {

                        int successMaxID = json.getInt("successMaxID");
                        int idInsertado = json.getInt("idInsertado");
                        if (successMaxID == 1) {

                            ContentValues values = new ContentValues();
                            values.put("EstadoEnvio", 1);
                            values.put("IdEnviado", idInsertado);
                            db.update(sqlhelper.TBLASISTENCIA, values, "IdAsistencia = " + idAsistSelecc, null);
                        }
                    }

                } catch (Exception e) {

                }
            }
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                Log.w("Enviando fv enc", "onpostexecute");

                if (file_url == null) {
                    pDialog.dismiss();
                    new Publicas().AlertDialog(EnviosPendientes.this, "Error al enviar", "Sin repuesta del servidor, o revisa la conexión de datos.", R.drawable.ic_error, 1);

                } else {
                    int success = file_url.getInt("successInsert");
                    if (success == 1) {
                        new Publicas().AlertDialog(EnviosPendientes.this, "Enviado!.", file_url.getString("messageInsert"), R.drawable.ic_success, 2);
                        getArrayPendientes();
                    } else {
                        new Publicas().AlertDialog(EnviosPendientes.this, "Error!.", file_url.getString("messageInsert"), R.drawable.ic_error, 1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }
}
