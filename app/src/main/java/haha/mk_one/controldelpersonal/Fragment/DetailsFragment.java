package haha.mk_one.controldelpersonal.Fragment;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import haha.mk_one.controldelpersonal.EditarPersona;
import haha.mk_one.controldelpersonal.MainActivity;
import haha.mk_one.controldelpersonal.Persona;
import haha.mk_one.controldelpersonal.Publicas;
import haha.mk_one.controldelpersonal.R;
import haha.mk_one.controldelpersonal.SQLHELPER;

import static android.graphics.Color.RED;

/**
 * Display details for a given person
 *
 * @author bherbst
 */
public class DetailsFragment extends Fragment {
    private static final String ARG_PERSON_NUMBER = "argPersonNumber", ARG_ESCANEO_INICIO = "escaneodeinicio";
    SQLiteDatabase db;
    SQLHELPER sqlhelper;
    EditText etNoCedul, etRepSecreta, etComentario;
    Button btnMarcar;
    TextInputLayout tilResSecreta;
    Persona persona;
    int IdPregunta, personNumber, salida = 1, entrada = 0, ContadorPreguntas = 1, ContadorRespuesta = 0;
    String Pregunta, Respuesta;
    boolean yaSalio = false, entrada_por_escaneo;
    TextView tv_resultado_de_GPS, tv_GPS;
    String Comentario = "";
    int CantIntentos = 1;
    ImageButton ibScanCedula;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    RadioButton op1;
    RadioButton op2;
    RadioButton op3;
    RadioButton op4;
    ArrayList<String> arrRespuestas = new ArrayList<>();
    MainActivity location = new MainActivity();

    /**
     * Create a new DetailsFragment
     *
     * @param personnNumber The number (between 1 and 6) of the person to display
     */
    public static DetailsFragment newInstance(@IntRange(from = 1, to = 6) int personnNumber, boolean Entrada_escaneado) {
        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_NUMBER, personnNumber);
        args.putBoolean(ARG_ESCANEO_INICIO,Entrada_escaneado);

        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            sqlhelper = new SQLHELPER(view.getContext());
            db = new SQLHELPER(view.getContext()).getWritableDatabase();
            ImageView image = (ImageView) view.findViewById(R.id.image);
            TextView nombre = (TextView) view.findViewById(R.id.tvNombre_DF);
            etNoCedul = (EditText) view.findViewById(R.id.etCedula_DF);
            etRepSecreta = (EditText) view.findViewById(R.id.etPregunttaAleator_Df);
            tilResSecreta = (TextInputLayout) view.findViewById(R.id.TILPreguntaAleatoria_DF);
            btnMarcar = (Button) view.findViewById(R.id.btnMarcarAsistencia);
            tv_resultado_de_GPS = (TextView) view.findViewById(R.id.tv_Condicion_de_marcado);
            tv_GPS = (TextView) view.findViewById(R.id.tv_gps);
            etComentario = (EditText) view.findViewById(R.id.etComentario);
            ibScanCedula = (ImageButton) view.findViewById(R.id.ibScannCedula_DF);
            EditText terminoBusqueda = (EditText) getActivity().findViewById(R.id.et_busqueda);
            FloatingActionButton fb = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            fb.hide();
            terminoBusqueda.setEnabled(false);

            setting = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            editor = setting.edit();

            Bundle args = getArguments();
            personNumber = args.containsKey(ARG_PERSON_NUMBER) ? args.getInt(ARG_PERSON_NUMBER) : 0;
            entrada_por_escaneo = args.getBoolean(ARG_ESCANEO_INICIO);
            etNoCedul.requestFocus();
            etNoCedul.setEnabled(false);
            etRepSecreta.setEnabled(false);
            etComentario.setEnabled(false);
            btnMarcar.setEnabled(false);
            ibScanCedula.setEnabled(false);


            // obtiene la cantidad de intentos que ha hecho esa persona para marcar entrada
            CantIntentos = setting.getInt("CantIntentos_" + personNumber, 1);


            tv_GPS.setText("Coordenadas GPS: " + location.mLastLocationMap.getLatitude() + " , " + location.mLastLocationMap.getLongitude());
            VerificaCantidadPreguntas();


            Cursor cPersona = db.rawQuery("SELECT * FROM " + sqlhelper.TBLPERSONAL + " WHERE IdPersona = " + personNumber, null);

            if (cPersona.moveToFirst()) {

                persona = new Persona(cPersona.getInt(cPersona.getColumnIndex("IdPersona")),
                        cPersona.getString(cPersona.getColumnIndex("Nombre")),
                        cPersona.getString(cPersona.getColumnIndex("Apellidos")),
                        cPersona.getString(cPersona.getColumnIndex("NoCedula")),
                        cPersona.getString(cPersona.getColumnIndex("Foto")),
                        IdPregunta, Pregunta, Respuesta
                );


                String pathFoto = persona.getFoto();
                File imgFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/grupovalor/" + pathFoto);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    image.setImageBitmap(myBitmap);
                } else {
                    image.setImageResource(R.drawable.ic_error);
                }
                nombre.setText(persona.getNombre() + " " + persona.getApellidos());
                tilResSecreta.setHint(persona.getPreguntaAleat());
            }
        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
        EventosControles();
        VerificcarSino_ha_salido();
        Validar_si_esta_en_PDV();

    }


    public void EventosControles() {
        try {

            ibScanCedula.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    integrator.setPrompt("Enfoque el codigo de barras de la cédula.");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(true);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.setOrientationLocked(false);
                    integrator.initiateScan();
                }
            });
            etNoCedul.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (!TextUtils.isEmpty(etNoCedul.getText())) {

                        etNoCedul.setBackgroundColor(Color.TRANSPARENT);

                        if (s.length() >= 13) {
                            etNoCedul.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                        } else {
                            etNoCedul.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }

                        if (s.length() >= 14) {    // el tamaño del numero de cedula sin la letra
                            if (persona.getNoCedula()/*.replaceAll("\\s+","")*/.equalsIgnoreCase(s.toString())) {

                                etRepSecreta.setEnabled(true);
                                etComentario.requestFocus();
                                etNoCedul.setEnabled(false);
                                etComentario.setEnabled(true);
                                btnMarcar.setEnabled(true);
                                ibScanCedula.setEnabled(false);
                            } else {

                                etNoCedul.setBackgroundColor(RED);

                                /*new AlertDialog.Builder(getContext())
                                        .setMessage(s + " no es igual a " + persona.getNoCedula())
                                        .show();*/

                            }
                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }


        btnMarcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String Respuestaobtenida = etRepSecreta.getText().toString();
                    //obtenemos la cadena de la respuesta sin acentos
                    String Obtener_sinacento = remover_acentos (Respuestaobtenida);
                    //obtener cadenas sin espacios
                    String Resp_Original = persona.getRespuesta().trim();
                    String Resp_Obtenida = Obtener_sinacento.trim();
                    //Toast.makeText(getContext(), "ROb: "+Resp_Obtenida+"   ROr: "+Resp_Original, Toast.LENGTH_SHORT).show();
                    //si la repueta que ingreseo es igual a la repuesta obtenida aleatorio
                    if (Resp_Obtenida.equalsIgnoreCase(Resp_Original)) {
                        // si el campo tiene al menos 1 letra
                        if (etComentario.length() > 0)
                            Comentario = etComentario.getText().toString();

                       Guardar_marcacion();
                    } else {//si la repueta que ingreseo no es igual a la repuesta obtenida aleatorio
                        // si ya tiene dos intentos se le da el tercero oportunidad
                        if (ContadorPreguntas == 2 && ContadorRespuesta == 3) {
                            etRepSecreta.setBackgroundColor(Color.TRANSPARENT);
                            etRepSecreta.getText().clear();
                            Ultimointento_de_ingreso();

                        } else {
                            CantIntentos++;
                            editor.putInt("CantIntentos_" + personNumber, CantIntentos);
                            editor.commit();

                            ContadorRespuesta++;
                            etRepSecreta.setBackgroundColor(RED);
                            if (ContadorRespuesta == 1 && ContadorPreguntas == 1) {
                                ContadorRespuesta = 3;
                                ContadorPreguntas = 2;
                                getPreguntaAleatoria();
                                persona.setIdPregAleat(IdPregunta);
                                persona.setPreguntaAleat(Pregunta);
                                persona.setRespuesta(Respuesta);
                                new Publicas().Toast(getActivity(), "No has contestado bien, Prueba con la siguiente pregunta", R.drawable.ic_error);
                                tilResSecreta.setHint(persona.getPreguntaAleat());
                                etRepSecreta.setBackgroundColor(Color.TRANSPARENT);
                                etRepSecreta.getText().clear();
                                etRepSecreta.refreshDrawableState();
                            }
                        }

                    }
                } catch (Exception e) {
                    new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
                    e.printStackTrace();
                }
            }
        });
    }

    private void getPreguntaAleatoria() {
        try {

            Cursor cPreguntas = db.rawQuery("select IdPP, " + sqlhelper.TBLPREG_X_PERSONA + ".IdPregunta, Pregunta, Repuestas from " +
                    sqlhelper.TBLPREG_X_PERSONA + " inner join " + sqlhelper.TBLCATPREGUNTAS +
                    " on " + sqlhelper.TBLPREG_X_PERSONA + ".IdPregunta = " + sqlhelper.TBLCATPREGUNTAS + ".IdPregunta where " +
                    sqlhelper.TBLPREG_X_PERSONA + ".IdPersona = " + personNumber, null);

            if (cPreguntas.moveToFirst()) {

                cPreguntas.moveToPosition(new Random().nextInt(10));
                IdPregunta = cPreguntas.getInt(cPreguntas.getColumnIndex("IdPregunta"));
                Pregunta = cPreguntas.getString(cPreguntas.getColumnIndex("Pregunta"));
                Respuesta = cPreguntas.getString(cPreguntas.getColumnIndex("Repuestas"));
            }

        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    private void VerificaCantidadPreguntas() {

        try {

            Cursor cPreguntasSec = db.rawQuery("Select * from " + sqlhelper.TBLPREG_X_PERSONA + " where IdPersona = " + personNumber, null);
            if (cPreguntasSec.getCount() == 10) {
                getPreguntaAleatoria();
            } else {
                startActivity(new Intent(getContext(), EditarPersona.class).putExtra("IdPersona", personNumber).putExtra("CantidadPreg", cPreguntasSec.getCount()));
                getActivity().finish();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(getContext()).setMessage(e.getMessage()).show();
        }
    }

    public void VerificcarSino_ha_salido() {
        try {
            Cursor cVerificarSalida = db.rawQuery("select * from " + sqlhelper.TBLASISTENCIA + " where IdPersona = " + persona.getIdPersona() + " and IdAsistencia in (select MAX(IdAsistencia) from "
                    + sqlhelper.TBLASISTENCIA + " where IdPersona = " + persona.getIdPersona() + ")", null);

            if (cVerificarSalida.moveToFirst()) {
                do {
                    if (cVerificarSalida.getInt(cVerificarSalida.getColumnIndex("EstadoSalida")) == entrada) {
                        btnMarcar.setText(" Marcar Salida");
                        yaSalio = true;
                    } else {
                        btnMarcar.setText("Marcar Entrada");
                    }
                } while (cVerificarSalida.moveToNext());
            }
        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void Guardar_marcacion() {

        try {


            if (yaSalio) {
                etRepSecreta.setBackgroundColor(Color.TRANSPARENT);
                String fechaReg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                ContentValues values = new ContentValues();
                values.put("FechaSal", fechaReg);
                values.put("EstadoSalida", salida);
                values.put("Comentario_S", Comentario);
                values.put("CanIntSal", CantIntentos);
                values.put("Turno",setting.getString("TurnoPersona_"+personNumber,"N/A"));

                Long id = Long.valueOf(db.update(sqlhelper.TBLASISTENCIA, values, "EstadoSalida = " + entrada + " and IdAsistencia in (select MAX(IdAsistencia) from " + sqlhelper.TBLASISTENCIA +
                        " where IdPersona = " + persona.getIdPersona() + ")", null));
                if (id > -1) {
                    new Publicas().Toast(getActivity(), " Marcacion de 'Salida' guardada Satisfactoriamente", R.drawable.ic_save);

                    //se ponen los intentos en 1
                    editor.putInt("CantIntentos_" + personNumber, 1);
                    editor.commit();

                    startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    new Publicas().Toast(getActivity(), "Ocurrio un error al guardar", R.drawable.ic_error);
                }


            } else {
                etRepSecreta.setBackgroundColor(Color.TRANSPARENT);
                String fechaReg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                ContentValues values = new ContentValues();
                values.put("IdPersona", persona.getIdPersona());
                values.put("IdPregunta", persona.getIdPregAleat());
                values.put("FechaEntr", fechaReg);
                values.put("FechaSal", "");
                values.put("Foto", "");
                values.put("EstadoEnvio", 0);
                values.put("IdEnviado", 0);
                values.put("EstadoSalida", entrada);
                values.put("Comentario_E", Comentario);
                values.put("Comentario_S", "");
                values.put("CantIntentos", CantIntentos);
                values.put("Turno",setting.getString("TurnoPersona_"+personNumber,"N/A"));

                Long id = db.insert(sqlhelper.TBLASISTENCIA, null, values);
                if (id > -1) {
                    //se ponen los intentos en 1
                    editor.putInt("CantIntentos_" + personNumber, 1);
                    editor.commit();
                    new Publicas().Toast(getActivity(), " Marcacion de 'entrada' guardada Satisfactoriamente", R.drawable.ic_save);
                    startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    new Publicas().Toast(getActivity(), "Ocurrio un error al guardar", R.drawable.ic_error);
                }
            }

        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void Ultimointento_de_ingreso() {
        try {


            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.ultimo_intento_df);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            op1 = (RadioButton) dialog.findViewById(R.id.rb_Opcion1);
            op2 = (RadioButton) dialog.findViewById(R.id.rb_Opcion2);
            op3 = (RadioButton) dialog.findViewById(R.id.rb_Opcion3);
            op4 = (RadioButton) dialog.findViewById(R.id.rb_Opcion4);

            //CantIntentos = 3;
            CantIntentos++;
            editor.putInt("CantIntentos_" + personNumber, CantIntentos);
            editor.commit();
            OpcDresp();

            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rg_TipoBusqueda);

            final TextView pregunta = (TextView) dialog.findViewById(R.id.tv_Pregunta);
            pregunta.setText(persona.getPreguntaAleat());

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                    if (checkedId == op1.getId()) {
                        if (op1.getText().toString().equalsIgnoreCase(persona.getRespuesta())) {
                            Guardar_marcacion();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            new Publicas().Toast(getActivity(), "Desafortunadamente este era el ultimo intento", R.drawable.ic_error);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }

                    }
                    if (checkedId == op2.getId()) {
                        if (op2.getText().toString().equalsIgnoreCase(persona.getRespuesta())) {
                            Guardar_marcacion();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            new Publicas().Toast(getActivity(), "Desafortunadamente este era el ultimo intento", R.drawable.ic_error);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }

                    }
                    if (checkedId == op3.getId()) {
                        if (op3.getText().toString().equalsIgnoreCase(persona.getRespuesta())) {
                            Guardar_marcacion();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            new Publicas().Toast(getActivity(), "Desafortunadamente este era el ultimo intento", R.drawable.ic_error);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }

                    }
                    if (checkedId == op4.getId()) {
                        if (op4.getText().toString().equalsIgnoreCase(persona.getRespuesta())) {
                            Guardar_marcacion();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        } else {
                            new Publicas().Toast(getActivity(), "Desafortunadamente este era el ultimo intento", R.drawable.ic_error);
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }
                    }
                }
            });


            dialog.show();
        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void OpcDresp() {
        try {


            Cursor copcAlternasdeResp;

            if (persona.getIdPregAleat() == 10 || persona.getIdPregAleat() == 2 || persona.getIdPregAleat() == 3) {
                copcAlternasdeResp = db.rawQuery("select opcrespuesta from " + sqlhelper.TABLAOPCIONESRESP + " where idPreg = 2", null);
            } else {
                copcAlternasdeResp = db.rawQuery("select opcrespuesta from " + sqlhelper.TABLAOPCIONESRESP + " where idPreg = " + persona.getIdPregAleat(), null);
            }

            arrRespuestas.clear();
            int verValordelCount = copcAlternasdeResp.getCount();
            arrRespuestas.add(persona.getRespuesta());
            for (int i = 0; i <= 4; i++) {
                copcAlternasdeResp.moveToPosition(new Random().nextInt(copcAlternasdeResp.getCount()));
                String resp = copcAlternasdeResp.getString(0);
                if (arrRespuestas.contains(resp) || persona.getRespuesta().equalsIgnoreCase(resp)) {
                    i = arrRespuestas.size();
                } else {
                    arrRespuestas.add(resp);
                    i = arrRespuestas.size();
                }
            }
            String ReapAsig;
            int index;

            ReapAsig = arrRespuestas.get(new Random().nextInt(arrRespuestas.size()));
            index = arrRespuestas.indexOf(ReapAsig);
            op1.setText(ReapAsig);
            arrRespuestas.remove(index);

            ReapAsig = arrRespuestas.get(new Random().nextInt(arrRespuestas.size()));
            index = arrRespuestas.indexOf(ReapAsig);
            op2.setText(ReapAsig);
            arrRespuestas.remove(index);

            ReapAsig = arrRespuestas.get(new Random().nextInt(arrRespuestas.size()));
            index = arrRespuestas.indexOf(ReapAsig);
            op3.setText(ReapAsig);
            arrRespuestas.remove(index);

            ReapAsig = arrRespuestas.get(0);
            index = arrRespuestas.indexOf(ReapAsig);
            op4.setText(ReapAsig);
            arrRespuestas.remove(index);
        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void Validar_si_esta_en_PDV() {
        Cursor cObternCooerdenadas = db.rawQuery("select locacioGPS from "+sqlhelper.TABLA_P_D_V+" where IDpv = "+setting.getString("pdv",""),null);
        cObternCooerdenadas.moveToFirst();
        String locacion = cObternCooerdenadas.getString(cObternCooerdenadas.getColumnIndex("locacioGPS"));
        String[] lonYlat = locacion.split(",");
        try {
            if (new Publicas().distance(location.mLastLocationMap.getLatitude(), location.mLastLocationMap.getLongitude(), Float.parseFloat(lonYlat[0]), Float.parseFloat(lonYlat[1])) >= 200) {
                tv_resultado_de_GPS.setText("No estas en almenos un radio de 200 metros con respecto al local al que debes marcar");
                etNoCedul.setEnabled(false);
                ibScanCedula.setEnabled(false);
            } else {
                if (entrada_por_escaneo){
                    etNoCedul.setText(persona.getNoCedula());
                    tv_resultado_de_GPS.setText("Habilitado");
                    etNoCedul.setEnabled(false);
                    ibScanCedula.setEnabled(false);
                }else {
                    tv_resultado_de_GPS.setText("Habilitado");
                    etNoCedul.setEnabled(true);
                    ibScanCedula.setEnabled(true);
                }

            }

        } catch (Exception e) {
            new Publicas().AlertDialog(getContext(), "Oops!, Ha ocurrido un error", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0x0000c0de:
                try {
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (result != null) {
                        if (result.getContents() == null) {
                            new Publicas().AlertDialog(getContext(), "Oops!", "Has cancelado el escaner de la cédula ", R.drawable.ic_error, 1);
                        } else {
                            etNoCedul.setText(result.getContents());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

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