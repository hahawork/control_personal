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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Enviar_RespuestaXPersona extends AppCompatActivity {

    ListView lvPendientes;
    SQLiteDatabase db;
    static ArrayList<ListaPersonalConRespuestas> arrPendientes = new ArrayList<>();
    SQLHELPER sqlhelper;
    SharedPreferences setting;
    static int idPersonaSeleccionada = 0;
    ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar__respuesta_xpersona);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvPendientes = (ListView) findViewById(R.id.lvEnviosP_respxpersona);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        getArrayPendientes();

        lvPendientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try {
                    idPersonaSeleccionada = arrPendientes.get(position).getIDpersonal();
                        new C_Registrar_Resp_ENSERVIDOR().execute(
                                CadenaRespuesta(arrPendientes.get(position).getIDpersonal())
                        );

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void getArrayPendientes() {

        try {

            Cursor cPendient = db.rawQuery("select * from " + sqlhelper.TBLPERSONAL, null);
            arrPendientes.clear();

            if (cPendient.moveToFirst()) {
                do {
                    if (verificar_Todas_respuestas(cPendient.getInt(cPendient.getColumnIndex("IdPersona"))))
                        arrPendientes.add(new ListaPersonalConRespuestas(
                                cPendient.getInt(cPendient.getColumnIndex("IdPersona")),
                                cPendient.getString(cPendient.getColumnIndex("Nombre")),
                                cPendient.getString(cPendient.getColumnIndex("Apellidos"))
                        ));
                } while (cPendient.moveToNext());
            } else {
                arrPendientes.add(new ListaPersonalConRespuestas(0, "", ""));
            }

            List<String> arrayList = new ArrayList<>();

            for (int i = 0; i < arrPendientes.size(); i++) {
                arrayList.add(arrPendientes.get(i).getNombres() + " " + arrPendientes.get(i).getApellidos());
            }

            String[] simpleArray = new String[arrayList.size()];
            arrayList.toArray(simpleArray);

            lvPendientes.setAdapter(new Custom_List_Pendientes(this, simpleArray));
        } catch (Exception e) {
            e.printStackTrace();
            new Publicas().AlertDialog(Enviar_RespuestaXPersona.this, "Error!", e.getMessage(), R.drawable.ic_error, 1);

        }
    }

    public boolean verificar_Todas_respuestas(int idPersona) {
        boolean completado = false;
        Cursor cContarCantRespuestas = db.rawQuery("select * from " + sqlhelper.TBLPREG_X_PERSONA + " where IdPersona = " + idPersona + " and Estado_envio = 0", null);
        if (cContarCantRespuestas.getCount() == 10)
            completado = true;
        return completado;
    }

    private class ListaPersonalConRespuestas {
        int IDpersonal;
        String Nombres;
        String Apellidos;

        public ListaPersonalConRespuestas(int IDpersonal, String nombres, String apellidos) {
            this.IDpersonal = IDpersonal;
            Nombres = nombres;
            Apellidos = apellidos;
        }

        public int getIDpersonal() {
            return IDpersonal;
        }

        public String getNombres() {
            return Nombres;
        }

        public String getApellidos() {
            return Apellidos;
        }
    }
    // Cursor cPendient = db.rawQuery("select PP.* from " + sqlhelper.TBLPREG_X_PERSONA + " as PP inner join " + sqlhelper.TBLCATPREGUNTAS + " as catP on PP.IdPregunta = catP.IdPregunta inner join " + sqlhelper.TBLPERSONAL + " as P on PP.IdPersona = P.IdPersona", null);

    public class Custom_List_Pendientes extends ArrayAdapter<String> {

        public Custom_List_Pendientes(Activity context, String[] a) {
            super(context, R.layout.activity_ependientes_pregxresp, a);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = Enviar_RespuestaXPersona.this.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.activity_ependientes_pregxresp, null, true);
            String PrecioCompetCadena = "";
            try {
                TextView tvNombre = (TextView) rowView.findViewById(R.id.tv_Nombre);
                tvNombre.setText(arrPendientes.get(position).getNombres() + " " + arrPendientes.get(position).getApellidos());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }
    }

    class C_Registrar_Resp_ENSERVIDOR extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Enviar_RespuestaXPersona.this);
            pDialog.setMessage("Enviando los datos. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    C_Registrar_Resp_ENSERVIDOR.this.cancel(true);
                    new Publicas().Toast(Enviar_RespuestaXPersona.this, "Se ha cancelado la petición.", R.drawable.ic_info);
                }
            });
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("cadenarespuestas", args[0]));

            json = jsonParser.makeHttpRequest(new Publicas().HOST + "ws/asist_personal/ws_guardar_Respuestas.php",
                    "POST", params);

            if (json != null) {
                try {
                    int success = json.getInt("successInsert");
                    if (success == 1) {
                            ContentValues values = new ContentValues();
                            values.put("Estado_envio", 1);
                            db.update(sqlhelper.TBLPREG_X_PERSONA, values, "IdPersona = " + idPersonaSeleccionada, null);

                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                if (file_url == null) {
                    pDialog.dismiss();
                    new Publicas().AlertDialog(Enviar_RespuestaXPersona.this, "Error al enviar", "Sin repuesta del servidor, o revisa la conexión de datos.", R.drawable.ic_error, 1);

                } else {
                    int success = file_url.getInt("successInsert");
                    if (success == 1) {
                        new Publicas().AlertDialog(Enviar_RespuestaXPersona.this, "Enviado!.", file_url.getString("messageInsert"), R.drawable.ic_success, 2);
                        getArrayPendientes();
                    } else {
                        new Publicas().AlertDialog(Enviar_RespuestaXPersona.this, "Error!.", file_url.getString("messageInsert"), R.drawable.ic_error, 1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    public String CadenaRespuesta( int idPersona) {
        String Cadena_Resp = "";

        Cursor CadenaResp = db.rawQuery("select * from "+sqlhelper.TBLPREG_X_PERSONA+" where IdPersona = "+idPersona+" order by IdPregunta", null);

        if (CadenaResp.moveToFirst()) {
            Cadena_Resp += "INSERT INTO TBLResp_x_Persona (IdR_x_P, IdPersona, IdPregunta, Respuesta, Local) VALUES ";
            do {
                Cadena_Resp += "(null, ";
                Cadena_Resp += "'" + CadenaResp.getString(CadenaResp.getColumnIndex("IdPersona")) + "', ";
                Cadena_Resp += "'" + CadenaResp.getString(CadenaResp.getColumnIndex("IdPregunta")) + "', ";
                Cadena_Resp += "'" + CadenaResp.getString(CadenaResp.getColumnIndex("Repuestas")) + "', ";
                Cadena_Resp += "'" + setting.getString("pdv","") + "') ";

                if (CadenaResp.isLast()) {
                    Cadena_Resp += ";";
                } else {
                    Cadena_Resp += ",";
                }
            } while (CadenaResp.moveToNext());

        }

        return Cadena_Resp;
    }
}
