package haha.mk_one.controldelpersonal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MostrarRespuestas extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    ArrayList<datos> Array_Datos_Persona = new ArrayList<>();
    ArrayList<datos> Array_Datos_DetalleResp = new ArrayList<>();
    HashMap<String, List<String>> listDataChild;
    SQLHELPER sqlhelper;
    SQLiteDatabase db;
    Publicas P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_respuestas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sqlhelper = new SQLHELPER(this);
        db = new SQLHELPER(this).getWritableDatabase();
        P = new Publicas();

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);

        // preparing list data
        prepareListData();

        listAdapter = new CustomExpandableListAdapter(this, listDataHeader, listDataChild);


        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " : "
                        + listDataChild.get(listDataHeader.get(groupPosition)).get(
                        childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                /*Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();*/
            }
        });
        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
               /* Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();*/

            }
        });
    }


    /*
    * Preparing the list data
    */
    private void prepareListData() {
        try {
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            int i = 0;

            Cursor cObtener_PXP = db.rawQuery("select distinct tp.IdPersona, tp.Nombre, tp.Apellidos from " + sqlhelper.TBLPREG_X_PERSONA + " as pp inner join " + sqlhelper.TBLPERSONAL + " as tp on pp.IdPersona = tp.IdPersona " +
                    "order by tp.Nombre", null);
            int ver = cObtener_PXP.getCount();
            String pp;
            ArrayList<String> name[] = new ArrayList[cObtener_PXP.getCount()];
            Cursor cObtenerDetalles_Resp;
            if (cObtener_PXP.moveToFirst())
                do {
                    listDataHeader.add(cObtener_PXP.getString(cObtener_PXP.getColumnIndex("Nombre")) + " " + cObtener_PXP.getString(cObtener_PXP.getColumnIndex("Apellidos")));
                    cObtenerDetalles_Resp = db.rawQuery("select * from " + sqlhelper.TBLPREG_X_PERSONA + " as pp inner join " + sqlhelper.TBLCATPREGUNTAS + " as p on pp.IdPregunta = p.IdPregunta  where pp.IdPersona = "
                            + cObtener_PXP.getString(cObtener_PXP.getColumnIndex("IdPersona")) + " order by IdPregunta", null);
                    ver = cObtenerDetalles_Resp.getCount();
                    name[i]= new ArrayList<>();
                    if (cObtenerDetalles_Resp.moveToFirst())
                        do {
                            pp=cObtenerDetalles_Resp.getString(cObtenerDetalles_Resp.getColumnIndex("Pregunta")) + ": " + cObtenerDetalles_Resp.getString(cObtenerDetalles_Resp.getColumnIndex("Repuestas"));
                            name[i].add(pp);

                        } while (cObtenerDetalles_Resp.moveToNext());
                    listDataChild.put(listDataHeader.get(i), name[i]);
                    i++;
                } while (cObtener_PXP.moveToNext());


        } catch (Exception e) {
            P.AlertDialog(this,"Oops!","Ha ocurrido un Error: "+e.getMessage(),R.drawable.ic_error,1);
        }
        // Adding child data
        /*listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");
        listDataHeader.add("Coming Soon..");*/

        // Adding child data
        /*List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);*/


    }
    // Listview on child click listener

    public class datos {
        int IdPersona;
        String Nombre;
        String Apellido;

        public datos(int idPersona, String nombre, String apellido) {
            IdPersona = idPersona;
            Nombre = nombre;
            Apellido = apellido;
        }

        public int getIdPersona() {
            return IdPersona;
        }

        public String getNombre() {
            return Nombre;
        }

        public String getApellido() {
            return Apellido;
        }

        int IdPregunta;
        String Pregunta;
        String Respuesta;

        public datos(int idPersona, int idPregunta, String pregunta, String respuesta) {
            IdPersona = idPersona;
            IdPregunta = idPregunta;
            Pregunta = pregunta;
            Respuesta = respuesta;
        }

        public int getIdPregunta() {
            return IdPregunta;
        }

        public String getPregunta() {
            return Pregunta;
        }

        public String getRespuesta() {
            return Respuesta;
        }
    }

}
