package haha.mk_one.controldelpersonal;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 1/6/2017.
 */

public class Publicas {

    private static Context context;
    final public String HOST = "http://www.grupovalor.com.ni/";
    ProgressDialog pDialog;
    public static Location locationPublica = null;

    public Context getContext() {
        return this.context;
    }

    public Publicas() {

    }

    public Publicas(Context ctx) {
        context = ctx;
    }

    public boolean TieneConexion(Context context) {
        boolean bConectado = false;
        try {
            ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] redes = connec.getAllNetworkInfo();
            if (redes != null) {
                for (int i = 0; i < 2; i++) {
                    if (redes[i].getState() == NetworkInfo.State.CONNECTED
                            && redes[i].isConnected()) {

                        bConectado = true;

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bConectado;
    }

    public void Toast(Activity context, String Texto, int image) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_layout_toast, (ViewGroup) context.findViewById(R.id.toast_layout_root)); //"inflamos" nuestro layout
        TextView text = (TextView) layout.findViewById(R.id.text_toast);
        ImageView imgToast = (ImageView) layout.findViewById(R.id.imgToast);
        text.setText(Texto); //texto a mostrar y asignado al textView de nuestro layout
        imgToast.setImageResource(image);
        Toast toast = new Toast(context); //Instanciamos un objeto Toast
        toast.setGravity(Gravity.CENTER, 0, 0); //lo situamos centrado arriba en la pantalla
        toast.setDuration(Toast.LENGTH_SHORT); //duracion del toast
        toast.setView(layout); //asignamos nuestro layout personalizado al objeto Toast
        toast.show(); //mostramos el Toast en pantalla
    }

    public void AlertDialog(Context context, String title, String mensaje, int icono, int TipoMensaje) {
        // custom dialog
        final int ERROR = 1, SUCCESS = 2, INFO = 3, WARNING = 4;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogue);

        RelativeLayout rlMain = (RelativeLayout) dialog.findViewById(R.id.RL_MainDG);
        if (TipoMensaje == ERROR) {
            rlMain.setBackgroundResource(R.color.rojopastel);
        } else if (TipoMensaje == SUCCESS) {
            rlMain.setBackgroundResource(R.color.verdepastel);
        } else if (TipoMensaje == INFO) {
            rlMain.setBackgroundResource(R.color.azulpastel);
        } else if (TipoMensaje == WARNING) {
            rlMain.setBackgroundResource(R.color.amarillopastel);
        }
        // set the custom dialog components - text, image and button
        TextView titulo = (TextView) dialog.findViewById(R.id.tvTitleDG);
        titulo.setText(title);
        TextView text = (TextView) dialog.findViewById(R.id.tvMessageDG);
        text.setText(mensaje);
        ImageView image = (ImageView) dialog.findViewById(R.id.imgIconDG);
        image.setImageResource(icono);

       /* Button dialogButtonCerrar = (Button) dialog.findViewById(R.id.btn_cerrarDG);
        dialogButtonCerrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });*/
       dialog.show();
        Button dialogButton = (Button) dialog.findViewById(R.id.btnCloseDG);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // reinicia una Activity
    public void reiniciarActivity(Activity actividad) {
        Intent intent = new Intent();
        intent.setClass(actividad, actividad.getClass());
        //llamamos a la actividad
        actividad.startActivity(intent);
        //finalizamos la actividad actual
        actividad.finish();
    }

    public String[] getListaPdV(ArrayList<pdv> pdVs) {
        List<String> arrayList = new ArrayList<>();

        for (int i = 0; i < pdVs.size(); i++) {
            arrayList.add(pdVs.get(i).getDescPdV());
        }

        String[] simpleArray = new String[arrayList.size()];
        arrayList.toArray(simpleArray);

        return simpleArray;
    }

    public float distance(double lat_a, double lng_a, float lat_b, float lng_b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }
}
