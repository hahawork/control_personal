package haha.mk_one.controldelpersonal.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import haha.mk_one.controldelpersonal.R;
import haha.mk_one.controldelpersonal.SQLHELPER;

/**
 * Adapts Views containing persons to RecyclerView cells
 *
 * @author bherbst
 */
public class PersonGridAdapter extends RecyclerView.Adapter<PersonViewHolder> {
    private final int mSize;
    private final Cursor mCursor;
    private final PersonClickListener mListener;
    SQLHELPER sqlhelper;
    SQLiteDatabase db;
    int posicion = 0;

    /**
     * Constructor
     *
     * @param size     The number of persons to show
     * @param listener A listener for person click events
     */
    public PersonGridAdapter(Cursor cursor, int size, PersonClickListener listener) {
        mSize = size;
        mListener = listener;
        mCursor = cursor;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell;
        cell = inflater.inflate(R.layout.grid_item, container, false);
        return new PersonViewHolder(cell);
    }

    @Override
    public void onBindViewHolder(final PersonViewHolder viewHolder, final int position) {

        try {

            if (mCursor.moveToFirst()) {
                mCursor.moveToPosition(posicion);
                String NombreCompleto = mCursor.getString(mCursor.getColumnIndex("Nombre")) + " " + mCursor.getString(mCursor.getColumnIndex("Apellidos"));
                String LabelNombreC = CrearLabel(NombreCompleto);
                viewHolder.Nombre.setText(NombreCompleto);
                viewHolder.Label.setText(LabelNombreC);

            } else {
                viewHolder.Nombre.setText("No hay Registros");
            }
            posicion++;
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewHolder.Nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPersonClicked(viewHolder, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    private String CrearLabel(String Cadena) {
        String Resultado = "";
        String[] temp;
        int n = 2;
        temp = Cadena.split(" ");
        for (int i = 0; i < temp.length; i++) {
            if (i == temp.length - 1 || i == temp.length - n || i == 0) {
                if (temp[i].equals("")) {
                    n+=1;
                    i= 1;
                } else {
                    Resultado += temp[i].substring(0, 1) + ".";
                }
            }

        }
        return Resultado;
    }

}
