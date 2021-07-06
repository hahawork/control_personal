package haha.mk_one.controldelpersonal;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditarPersona extends ActionBarActivity {

    Toolbar mToolbarBottom;
    SQLiteDatabase db;
    Publicas P;
    SQLHELPER sqlhelper;
    SharedPreferences setting;
    int IdPersona = 0, CantidadRespondidad = 0;

    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditarPersona.this, MainActivity.class));
        EditarPersona.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //startActivity(new Intent(EditarPersona.this,MainActivity.class));
        EditarPersona.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);

        IniciarComponentes();
        CustomAndroidGridViewAdapter();
        ToolBarBotton();
    }

    private void IniciarComponentes() {

        db = new SQLHELPER(this).getWritableDatabase();
        sqlhelper = new SQLHELPER(this);
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        P = new Publicas(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            IdPersona = extras.getInt("IdPersona");
            CantidadRespondidad = extras.getInt("CantidadPreg");
            if (IdPersona < 1) {
                this.finish();
            }
            // and get whatever type user account id is
        } else {
            this.finish();
        }

    }

    private void ToolBarBotton() {
        mToolbarBottom = (Toolbar) findViewById(R.id.inc_tb_bottom);
        mToolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.action_cancelar:
                        startActivity(new Intent(EditarPersona.this, MainActivity.class));
                        EditarPersona.this.finish();
                        break;
                }
                return true;
            }
        });
        mToolbarBottom.inflateMenu(R.menu.menu_bottom_edit_person);
    }

    public void CustomAndroidGridViewAdapter() {
        try {
            final LinearLayout layout = (LinearLayout) findViewById(R.id.llPreguntas_EP);

            layout.removeAllViews();

            final Cursor cPreguntas = db.rawQuery("select * from " + sqlhelper.TBLCATPREGUNTAS, null);

            if (cPreguntas.moveToFirst()) {
                do {
                    View view = getLayoutInflater().inflate(R.layout.custom_grid_preguntas, null);
                    final TextView tvpreg = (TextView) view.findViewById(R.id.tvGridPreguntas_CGP);

                    tvpreg.setText(cPreguntas.getInt(cPreguntas.getColumnIndex("IdPregunta")) + " .:. " + cPreguntas.getString(cPreguntas.getColumnIndex("Pregunta")));

                    final int finalJ = cPreguntas.getInt(cPreguntas.getColumnIndex("IdPregunta"));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor cVerifPreg_yaRespondida = db.rawQuery("select * from " + sqlhelper.TBLPREG_X_PERSONA + " where IdPersona = " + IdPersona + " and IdPregunta = " + finalJ, null);

                            if (cVerifPreg_yaRespondida.getCount() > 0) {
                                cVerifPreg_yaRespondida.moveToFirst();
                                new Publicas().Toast(EditarPersona.this, "Usted ya ha respondido esta pregunta " + Obtener_Pregunta(cVerifPreg_yaRespondida.getInt(cVerifPreg_yaRespondida.getColumnIndex("IdPregunta"))), R.drawable.ic_warning);
                            } else {
                                DialogIngresarRepuesta(finalJ, tvpreg.getText().toString());
                                //Toast.makeText(EditarPersona.this, "" + finalJ, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    layout.addView(view);

                } while (cPreguntas.moveToNext());
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public String Obtener_Pregunta(int idPregunta) {
        String Pregunta = "";
        Cursor Obtener_pregunta = db.rawQuery("select * from " + sqlhelper.TBLCATPREGUNTAS + " where IdPregunta = " + idPregunta, null);
        if (Obtener_pregunta.moveToFirst())
            Pregunta = Obtener_pregunta.getString(Obtener_pregunta.getColumnIndex("Pregunta"));
        return Pregunta;
    }

    private void DialogIngresarRepuesta(final int idPreg, String Pregunta) {
        final Dialog dialog = new Dialog(EditarPersona.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ingresar_repuesta);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        final EditText etcodigo = (EditText) dialog.findViewById(R.id.etRepuesta_DIR);
        TextView tvTitulo = (TextView) dialog.findViewById(R.id.tvTitulo_DIR);
        TextView tvCantPreg = (TextView) dialog.findViewById(R.id.tvCantPreg_DIR);

        tvCantPreg.setText("Tienes " + String.format(getResources().getString(R.string.CANTIDAD_PREGUNTAS), CantidadRespondidad));
        tvTitulo.setText(String.format(getResources().getString(R.string.TEXT_PREGUNT_SELECCION), Pregunta));
        if(idPreg == 7 || idPreg == 9)
            etcodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        else
            etcodigo.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);


        // set the custom dialog components - text, image and button
        ((Button) dialog.findViewById(R.id.btnCancelar_DIR)).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ((Button) dialog.findViewById(R.id.btnGuardar_DIR)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (TextUtils.isEmpty(etcodigo.getText())) {
                        Toast.makeText(EditarPersona.this, "Ingrese una repuesta", Toast.LENGTH_LONG).show();
                    } else {
                        String obtener_R_sinacento = remover_acentos(etcodigo.getText().toString());
                        ContentValues values = new ContentValues();
                        values.put("IdPP", (byte[]) null);
                        values.put("IdPersona", IdPersona);
                        values.put("IdPregunta", idPreg);
                        values.put("Repuestas", obtener_R_sinacento);

                        Long Success = db.insert(sqlhelper.TBLPREG_X_PERSONA, null, values);
                        if (Success > -1) {
                            // Toast.makeText(EditarPersona.this, "ok", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            getCantPreguntas();
                            if (CantidadRespondidad == 10) {
                                startActivity(new Intent(EditarPersona.this, MainActivity.class).putExtra("idpersona", IdPersona));
                                EditarPersona.this.finish();
                            }
                        }

                    }
                } catch (Exception e) {
                    Mensaje(e.getMessage(), e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }

    private void getCantPreguntas() {
        try {

            Cursor cCantidad = db.rawQuery("select * from " + sqlhelper.TBLPREG_X_PERSONA + " where IdPersona = " + IdPersona, null);
            CantidadRespondidad = cCantidad.getCount();
        } catch (Exception e) {
            Mensaje(e.getMessage(), e.getLocalizedMessage());
        }
    }

    private void Mensaje(String mensaje, String localizacion) {
        new AlertDialog.Builder(this).setTitle("Alerta!").setMessage(mensaje + " en " + localizacion).show();
    }
    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String remover_acentos(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i=0; i<original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }//remove1
}