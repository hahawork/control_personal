package haha.mk_one.controldelpersonal;

/**
 * Created by User on 6/6/2017.
 */

public class Persona {

    private int IdPersona;
    private String Nombre;
    private String Apellidos;
    private String NoCedula;
    private String Foto;
    private int IdPregAleat;
    private String PreguntaAleat;
    private String Respuesta;

    public Persona(int idPersona, String nombre, String apellidos, String noCedula, String foto, int idPreg, String preguntaAleat, String respuesta) {
        IdPersona = idPersona;
        Nombre = nombre;
        Apellidos = apellidos;
        NoCedula = noCedula;
        Foto = foto;
        IdPregAleat = idPreg;
        PreguntaAleat = preguntaAleat;
        Respuesta = respuesta;
    }

    public void setIdPregAleat(int idPregAleat) {IdPregAleat = idPregAleat;}

    public void setPreguntaAleat(String preguntaAleat) {PreguntaAleat = preguntaAleat;}

    public void setRespuesta(String respuesta) {Respuesta = respuesta;}

    public int getIdPersona() {
        return IdPersona;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getApellidos() {
        return Apellidos;
    }

    public String getNoCedula() {
        return NoCedula;
    }

    public String getFoto() {
        return Foto;
    }

    public int getIdPregAleat() {
        return IdPregAleat;
    }

    public String getPreguntaAleat() {
        return PreguntaAleat;
    }

    public String getRespuesta() {
        return Respuesta;
    }
}

