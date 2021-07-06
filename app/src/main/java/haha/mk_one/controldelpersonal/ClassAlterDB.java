package haha.mk_one.controldelpersonal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by User on 31/5/2017.
 */

public class ClassAlterDB {

    static SQLiteDatabase db;

    public ClassAlterDB(Context ctx) {
        db = new SQLHELPER(ctx).getWritableDatabase();
    }

    public boolean ExisteColumna(String Tabla, String Columna) {

        boolean Existe = false;
        try {

            Cursor cVerificaColumna = db.rawQuery("select * from " + Tabla + " where 0", null);

            String[] ColumnasExis = cVerificaColumna.getColumnNames();
            if (ColumnasExis.length > 0) {
                for (int i = 0; i < ColumnasExis.length; i++) {
                    if (ColumnasExis[i].equalsIgnoreCase(Columna)) {
                        Existe = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return Existe;
    }

    public void AgregarColumna(String Tabla, String newColumna, String TipoDato) {
        try {

            db.execSQL("ALTER TABLE " + Tabla + " ADD COLUMN " + newColumna + " " + TipoDato );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AgregarTabla(String Tabla){
        try{
            db.execSQL(Tabla);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
