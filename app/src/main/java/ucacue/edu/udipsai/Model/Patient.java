package ucacue.edu.udipsai.Model;

public class Patient {
    private long id;
    private String nombre;
    private String apellido;
    private String genero;
    private int edad;
    private String direccion;
    private String telefono;
    private long fechaRegistro;

    // Constructor vacío requerido por Firestore
    public Patient() {}

    // Constructor con parámetros
    public Patient(String nombre, String apellido, String genero, int edad, String direccion, String telefono) {
        long timestamp = System.currentTimeMillis(); // Generar ID basado en timestamp

        this.id = timestamp;
        this.nombre = nombre;
        this.apellido = apellido;
        this.genero = genero;
        this.edad = edad;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechaRegistro = timestamp;
    }

    // Getters
    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getGenero() { return genero; }
    public int getEdad() { return edad; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public long getFechaRegistro() { return fechaRegistro; }

    // Setters (Firestore los necesita para deserialización)
    public void setId(long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setEdad(int edad) { this.edad = edad; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
