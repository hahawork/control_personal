package haha.mk_one.controldelpersonal.Fragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import haha.mk_one.controldelpersonal.R;

/**
 * ViewHolder for person cells in our grid
 *
 * @author haha
 */
public class PersonViewHolder extends RecyclerView.ViewHolder {
    TextView Nombre,Label;

    public PersonViewHolder(View itemView) {
        super(itemView);
        Nombre=(TextView)itemView.findViewById(R.id.tvNombre_MA);
        Label=(TextView)itemView.findViewById(R.id.tvLabelNombre);
    }
}
