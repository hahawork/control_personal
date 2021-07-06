package haha.mk_one.controldelpersonal.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import haha.mk_one.controldelpersonal.MainActivity;
import haha.mk_one.controldelpersonal.Publicas;
import haha.mk_one.controldelpersonal.R;
import haha.mk_one.controldelpersonal.SQLHELPER;

/**
 * Displays a grid of pictures
 *
 * @author bherbst
 */
public class GridFragment extends Fragment implements PersonClickListener {

    SQLHELPER sqlhelper;
    List<Integer> idPersona = new ArrayList<>();
    String consulta = "";
    PersonViewHolder Holder;
    SQLiteDatabase db;
    Boolean escaneadoInicial = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        consulta = activity.getDataFragment();

        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        FloatingActionButton fb = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fb.show();
        EditText busqueda = (EditText) getActivity().findViewById(R.id.et_busqueda);
        busqueda.setEnabled(true);
        sqlhelper = new SQLHELPER(getContext());
        db = new SQLHELPER(this.getContext()).getWritableDatabase();
        Cursor cPersonas = db.rawQuery("SELECT * FROM " + new SQLHELPER(getContext()).TBLPERSONAL + " where Nombre like '%" + consulta + "%' or " +
                "Apellidos like '%" + consulta + "%' order by Nombre", null);
        if (cPersonas.moveToFirst()) {
            do {
                idPersona.add(cPersonas.getInt(cPersonas.getColumnIndex("IdPersona")));
            } while (cPersonas.moveToNext());
        }
        recyclerView.setAdapter(new PersonGridAdapter(cPersonas, cPersonas.getCount(), this));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    }


    @Override
    public void onPersonClicked(PersonViewHolder holder, int position) {
        try {
            if (new Publicas().locationPublica != null) {

                Holder = holder;
                escaneadoInicial = false;
                int personNumber = idPersona.get(position);
                llamar_fragment(personNumber);

            } else {
                new Publicas().Toast(getActivity(), "No se ha encontrado Ubicacion, espere e intente denuevo", R.drawable.ic_warning);
                // new Publicas().AlertDialog(getContext(),"Oops!","No se ha obtenido las coordenadas GPS, espere un momento y vuelva a intentar",R.drawable.ic_warning,4);
            }

        } catch (Exception e) {
            new Publicas().AlertDialog(getActivity(), "Oops!, Ha ocurrido un error:", "" + e.getMessage(), R.drawable.ic_error, 1);
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0x0000c0de:
                try {

                    int id = 0;
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (result != null) {
                        if (result.getContents() == null) {
                            new Publicas().AlertDialog(getContext(), "Oops!", "Has cancelado el escaner de la cédula ", R.drawable.ic_error, 1);
                        } else {
                            String ResultadoEscan = result.getContents();

                            Cursor buscarID = db.rawQuery("select IdPersona from " + sqlhelper.TBLPERSONAL + " where codigo = '" + ResultadoEscan + "' or NoCedula = '" + ResultadoEscan + "'", null);
                            if (buscarID.moveToFirst()) {
                                do {
                                    escaneadoInicial = true;
                                    id = buscarID.getInt(buscarID.getColumnIndex("IdPersona"));
                                } while (buscarID.moveToNext());
                                llamar_fragment(id);
                            } else {
                                new Publicas().Toast(getActivity(), "No es un codigo valido", R.drawable.ic_error);
                                escaneadoInicial = false;
                            }
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void llamar_fragment(int idPsicion) {
        try {

            DetailsFragment personDetails = DetailsFragment.newInstance(idPsicion, escaneadoInicial);

            // Note that we need the API version check here because the actual transition classes (e.g. Fade)
            // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
            // ARE available in the support library (though they don't do anything on API < 21)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                personDetails.setSharedElementEnterTransition(new DetailsTransition());
                personDetails.setEnterTransition(new Fade());
                setExitTransition(new Fade());
                personDetails.setSharedElementReturnTransition(new DetailsTransition());
            }

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    //.addSharedElement(Holder.foto, "personImage")
                    .replace(R.id.container, personDetails)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
