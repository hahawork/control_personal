package haha.mk_one.controldelpersonal;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static haha.mk_one.controldelpersonal.R.layout.historial_asistencia_dia;

/**
 * Created by usuario on 8/7/2017.
 */

public class Hist_Asistencia_deldia extends AppCompatActivity {

    SQLiteDatabase db;
    SQLHELPER sqlhelper;
    // String idAsistencia, int idPersona,String fechaEntr,String fechaSal
    private ArrayList<Lista_Hist_Asistencia> arrHistorial = new ArrayList<>();
    private ListView Lista_Historial;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(historial_asistencia_dia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Lista_Historial = (ListView) findViewById(R.id.lvhisttorialAsisisDia);
        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        MostrarReporteHistorial();


    }

    public void MostrarReporteHistorial() {

        try {
            //*SQLHELPER db=new SQLHELPER(activity_historial_dia.this,"TablaAsistencia",null,1);
            //sqlhelper = new SQLHELPER(this)db = new SQLHELPER(this).getWritableDatabase();
            // SQLHELPER db=new SQLHELPER(activity_historial_dia.this,"TablaAsistencia",null,1);
            // SQLiteDatabase bh = new SQLiteDatabase(activity_historial_dia.this, "sqlhelper.TBLASISTENCIA", null, 1);
            //   if (db != null) {
            String fecha_hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date()); //aqui se guarda la fecha del dia actual en formato año-mes-dia
            String consulta = "select IdAsistencia," + sqlhelper.TBLPERSONAL + ".IdPersona," + sqlhelper.TBLPERSONAL + ".Nombre, FechaEntr, FechaSal, CantIntentos, CanIntSal from " + sqlhelper.TBLASISTENCIA + " INNER JOIN " +
                    sqlhelper.TBLPERSONAL + " on " + sqlhelper.TBLASISTENCIA + ".IdPersona = " + sqlhelper.TBLPERSONAL + ".IdPersona where FechaEntr like '%" + fecha_hoy + "%'";
            Cursor cHistorial = db.rawQuery(consulta, null);

            if (cHistorial.moveToFirst()) {
                do {
                    // View view = getLayoutInflater().inflate(R.layout.activity_historial_dia, null);
                    // String idAsistencia, int idPersona,String fechaEntr,String fechaSal

                    arrHistorial.add(new Lista_Hist_Asistencia(cHistorial.getString(cHistorial.getColumnIndex("IdAsistencia")),
                            cHistorial.getInt(cHistorial.getColumnIndex("IdPersona")),
                            cHistorial.getString(cHistorial.getColumnIndex("Nombre")),
                            cHistorial.getString(cHistorial.getColumnIndex("FechaEntr")),
                            cHistorial.getString(cHistorial.getColumnIndex("FechaSal")),
                            cHistorial.getInt(cHistorial.getColumnIndex("CantIntentos")),
                            cHistorial.getInt(cHistorial.getColumnIndex("CanIntSal"))));


                } while (cHistorial.moveToNext());

            } else {
               // arrHistorial.add(new Lista_Hist_Asistencia("", 0, "", "", "", 0,0));


            }

            //  }
            List<String> arrayList = new ArrayList<>();

            for (int i = 0; i < arrHistorial.size(); i++) {
                arrayList.add(arrHistorial.get(i).getIdPersona() + " " + arrHistorial.get(i).getIdAsistencia());
            }

            String[] arrAsistencia = new String[arrayList.size()];
            arrayList.toArray(arrAsistencia);
            //lvPendientes.setAdapter(new Custom_List_Pendientes(this, simpleArray));
            Lista_Historial.setAdapter(new Custom_List_Historial(this, arrAsistencia));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class Custom_List_Historial extends ArrayAdapter<String> {

        public Custom_List_Historial(Activity context, String[] arrAsistencia) {
            super(context, R.layout.activity_historial_dia, arrAsistencia);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = Hist_Asistencia_deldia.this.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.activity_historial_dia, null, true);
            String PrecioCompetCadena = "";
            try {

                TextView tvCanInt = (TextView) rowView.findViewById(R.id.tvCanIntentos);
                TextView tvRep_Hist = (TextView) rowView.findViewById(R.id.tvRep_Hist);
                TextView tvHora_S_E = (TextView) rowView.findViewById(R.id.tvHora_S_E);
                TextView tvLugar = (TextView) rowView.findViewById(R.id.tvLugar);
                TextView tvCanIntSalida=(TextView) rowView.findViewById(R.id.tvCanIntSalida);
                ImageView ImgEstado = (ImageView)rowView.findViewById(R.id.img_estado);

                /*se llenan los label que van a presentar los precios pendientes
                a enviar a los usuarios*/

                //if (hayRegistros) {
                String detalle = "\t\tHora Entrada:\n\t\t\t\t";
                detalle += arrHistorial.get(position).getFechaEntr() + "\n\t\t Hora Salida: \n\t\t\t\t" + arrHistorial.get(position).getFechaSal();
                if (arrHistorial.get(position).getFechaSal().length()>10){
                    ImgEstado.setImageResource(R.drawable.ic_ya_marco);
                }else {
                    ImgEstado.setImageResource(R.drawable.ic_no_ha_marcado);
                }

                tvRep_Hist.setText(arrHistorial.get(position).getNombre());

                tvLugar.setText(" Referencia: " + arrHistorial.get(position).getIdAsistencia());
                tvHora_S_E.setText(detalle);
                tvCanInt.setText("\t\t\t"+arrHistorial.get(position).getCantIntentos() + " Intento(s) Entrada"); //se muestra la cantidad de intentos q se hicieron a la hora de marcar entrada

                tvCanIntSalida.setText("\t\t\t"+arrHistorial.get(position).getCantIntsal() + " Intento(s) Salida "); //se muestra la cantidad de intentos q se hicieron a la hora de marcar entrada

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }
    }

}
      /*  @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = Hist_Asistencia_deldia.this.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.activity_historial_dia, null, true);
            List<Hist_Asistencia_deldia> arrHist = new ArrayList<>();
            boolean hayRegistros = true;
            String PrecioCompetCadena = "";
            try {
                TextView tvRep_Hist = (TextView) rowView.findViewById(R.id.tvRep_Hist);
                TextView tvHora_S_E = (TextView) rowView.findViewById(R.id.tvHora_S_E);
                TextView tvLugar = (TextView) rowView.findViewById(R.id.tvLugar);

                /*se llenan los label que van a presentar los precios pendientes
                a enviar a los usuarios*/

              /*  if (hayRegistros) {
                    String detalle = "\t\tHora Entrada:\n";
                    detalle += arrHist.get(position).getFechaEntr() + "\n\t\t Hora Salida: \n" + arrHist.get(position).getFechaSal();

                    tvRep_Hist.setText(detalle);
                    tvHora_S_E.setText(arrHist.get(position).getIdPersona() + " " + arrHist.get(position).getIdAsistencia());

                    tvLugar.setText(setting.getString("pdv", ""));
                } else {
                    tvRep_Hist.setText(arrHist.get(position).getIdPersona());
                    //*tvPresent.setText("☺!");
                    // tvpdp.setText("");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }*/
//}



