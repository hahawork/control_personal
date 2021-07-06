package haha.mk_one.controldelpersonal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by User on 30/5/2017.
 */

public class SQLHELPER extends SQLiteOpenHelper {
    Context ctx;
    SQLiteDatabase db;
    public final String TBLPERSONAL = "Personal", TBLCATPREGUNTAS = "CatPreguntas", TBLPREG_X_PERSONA = "PreguntaPersona", TBLASISTENCIA = "Asistencia", TABLAOPCIONESRESP = "opcionesDrespuesta",
    TABLA_P_D_V = "PDV", TABLA_TURNO = "Turnos";

    String TablaPersonal = "CREATE TABLE IF NOT EXISTS " + TBLPERSONAL + " (IdPersona INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, Nombre NVARCHAR(50), Apellidos NVARCHAR(50), NoCedula char(14), Foto Text, Estado integer,Long text,Lat text, codigo text)";
    String TablaCatPreguntas = "CREATE TABLE IF NOT EXISTS " + TBLCATPREGUNTAS + " (IdPregunta INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, Pregunta TEXT)";
    String TablaPreg_Persona = "CREATE TABLE IF NOT EXISTS " + TBLPREG_X_PERSONA + " (IdPP INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, IdPersona INTEGER NOT NULL, IdPregunta INTEGER NOT NULL, Repuestas TEXT, Estado_envio integer DEFAULT 0)";
    String TablaAsistencia = "CREATE TABLE IF NOT EXISTS " + TBLASISTENCIA + " (IdAsistencia INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, IdPersona INTEGER, IdPregunta INTEGER, FechaEntr TIMESTAMP, FechaSal TIMESTAMP, Foto BLOB, EstadoSalida integer," +
            " EstadoEnvio INTEGER, IdEnviado INTEGER, Comentario_E text, Comentario_S text, CantIntentos integer, CanIntSal integer, Turno text)";
    String TablaOpcionesDRespuesta = "CREATE TABLE IF NOT EXISTS "+TABLAOPCIONESRESP+"(idopcresp INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idPreg integer, opcrespuesta text)";
    String Tabla_de_pdv = "CREATE TABLE IF NOT EXISTS "+TABLA_P_D_V+" (Idpdv INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, descPdv text, IDpv int, locacioGPS text)";
    String Tabla_de_Turnos = "CREATE TABLE IF NOT EXISTS "+TABLA_TURNO+" (idTurno INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DescTurno text)";

    public SQLHELPER(Context context) {
        super(context, "ASISTENCIA.db", null, 1);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TablaPersonal);
        db.execSQL(TablaPreg_Persona);
        db.execSQL(TablaCatPreguntas);
        db.execSQL(TablaAsistencia);
        db.execSQL(TablaOpcionesDRespuesta);
        db.execSQL(Tabla_de_pdv);
        db.execSQL("INSERT INTO "+ TBLCATPREGUNTAS+ " VALUES " +
                "(null, 'Nombre de una mascota.')," +
                "(null, 'Nombre de un/a novio/a.')," +
                "(null, 'Nombre de un primo/a.')," +
                "(null, 'Marca de zapatos favorita.')," +
                "(null, 'Primer apellido de la mamá.')," +
                "(null, 'Color favorito.')," +
                "(null, 'Número de celular propio.')," +
                "(null, 'Mes de nacimiento.')," +
                "(null, 'Año de nacimiento.')," +
                "(null, 'Primer nombre de un profesor/a')");
        db.execSQL(Tabla_de_Turnos);

        //db.execSQL("INSERT INTO "+ TABLA_P_D_V+ " VALUES " + "(null, 'Momotombo'),(null, 'otro ejemplo')");

        db.execSQL("INSERT INTO "+ TABLAOPCIONESRESP+ " VALUES " +
                "(null,1,'Flipi'),"+
                "(null,1,'Fibi'),"+
                "(null,1,'Peludo'),"+
                "(null,1,'Gordo'),"+
                "(null,1,'Firulai'),"+
                "(null,2,'Adriana'),"+
                "(null,2,'Adrian'),"+
                "(null,2,'Santiago'),"+
                "(null,2,'Guillermo'),"+
                "(null,2,'Giovana'),"+
                "(null,2,'Luisa'),"+
                "(null,2,'Juana'),"+
                "(null,2,'Thalia'),"+
                "(null,2,'Esteban'),"+
                "(null,2,'Ricardo'),"+
                "(null,4,'Fara'),"+
                "(null,4,'Airstep'),"+
                "(null,4,'Fila'),"+
                "(null,4,'Puma'),"+
                "(null,4,'Reebok'),"+
                "(null,5,'Mendoza'),"+
                "(null,5,'Herrera'),"+
                "(null,5,'Reyes'),"+
                "(null,5,'Urbina'),"+
                "(null,5,'Davila'),"+
                "(null,6,'Turkesa'),"+
                "(null,6,'Plateado'),"+
                "(null,6,'Cian'),"+
                "(null,6,'Jade'),"+
                "(null,6,'Indigo'),"+
                "(null,7,'78965426'),"+
                "(null,7,'54862401'),"+
                "(null,7,'82364021'),"+
                "(null,7,'77795563'),"+
                "(null,7,'87156304'),"+
                "(null,8,'Junio'),"+
                "(null,8,'Enero'),"+
                "(null,8,'Mayo'),"+
                "(null,8,'Noviembre'),"+
                "(null,8,'Febrero'),"+
                "(null,9,'1972'),"+
                "(null,9,'1900'),"+
                "(null,9,'1977'),"+
                "(null,9,'1991'),"+
                "(null,9,'1980'),"+
                "(null,2,'Carlos'),"+
                "(null,2,'Elena'),"+
                "(null,2,'Gerardo'),"+
                "(null,2,'Ariel'),"+
                "(null,2,'Vicente')");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void getDatabaseStructure(SQLiteDatabase db) {

        Cursor c = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name !='android_metadata'", null);
        ArrayList<String[]> result = new ArrayList<String[]>();
        int i = 0;
        result.add(c.getColumnNames());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String[] temp = new String[c.getColumnCount()];
            for (i = 0; i < temp.length; i++) {
                temp[i] = c.getString(i);
                System.out.println("TABLE - " + temp[i]);


                Cursor c1 = db.rawQuery("SELECT * FROM " + temp[i], null);
                c1.moveToFirst();
                String[] COLUMNS = c1.getColumnNames();
                for (int j = 0; j < COLUMNS.length; j++) {
                    c1.move(j);
                    System.out.println("    COLUMN - " + COLUMNS[j]);
                }
            }
            result.add(temp);
        }
    }
}
/* SQLiteDatabase db = openOrCreateDatabase("test.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        String qry = "CREATE TABLE IF NOT EXISTS choices ( category_no INTEGER NOT NULL, subcategory_no INTEGER NOT NULL, quiz_no INTEGER NOT NULL, choice_no INTEGER NOT NULL, answer TEXT NOT NULL, content_id INTEGER NOT NULL, PRIMARY KEY ( category_no, quiz_no, choice_no ) )";
        db.execSQL(qry);

        ContentValues cv = new ContentValues();
        cv.put("category_no",2);
        cv.put("subcategory_no",5);
        cv.put("quiz_no",2);
        cv.put("choice_no",2);
        cv.put("answer",3);
        cv.put("content_id",4);

        long i = db.insert("choices", null, cv);
        Log.d("Values of I = ", "******************* " + i + " ***************");
        db.close();*/