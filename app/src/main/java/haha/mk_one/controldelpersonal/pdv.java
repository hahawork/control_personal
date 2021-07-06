package haha.mk_one.controldelpersonal;

/**
 * Created by usuario on 29/6/2017.
 */

public class pdv {

    private String DescPdV;
    int idPDV;

    public pdv () {
    }

    public pdv( String descPdV, int idpdv) {

        DescPdV = descPdV;
        idPDV = idpdv;
    }

    public String getDescPdV() {
        return DescPdV;
    }
    public int getIdPDV(){return idPDV;}
}
