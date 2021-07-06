package haha.mk_one.controldelpersonal;

/**
 * Created by USUARIO on 18/7/2017.
 */

public class Lista_Hist_Asistencia {
    private String IdAsistencia, FechaEntr, FechaSal, Nombre;
    private int IdPersona, CantIntentos,CantIntsal;

    public Lista_Hist_Asistencia(String idAsistencia, int idPersona, String nombre, String fechaEntr, String fechaSal, int cantIntentos, int canIntsal) {
        IdAsistencia = idAsistencia;
        IdPersona = idPersona;
        Nombre = nombre;
        FechaEntr = fechaEntr;
        FechaSal = fechaSal;
        CantIntentos = cantIntentos;
        CantIntsal=canIntsal;


    }

    public String getNombre() {
        return Nombre;
    }

    public String getIdAsistencia() {
        return IdAsistencia;
    }

    public String getFechaEntr() {
        return FechaEntr;
    }

    public String getFechaSal() {
        return FechaSal;
    }

    public int getIdPersona() {
        return IdPersona;
    }

    public int getCantIntentos() {
        return CantIntentos;
    }

    public int getCantIntsal() {
        return CantIntsal;
    }
}
