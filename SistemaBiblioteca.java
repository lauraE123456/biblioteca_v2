import java.time.LocalDateTime;
import java.util.Scanner;

// --- 1. ENTIDADES ---
enum Estado { DISPONIBLE, PRESTADO }

class Libro {
    int isbn;
    String titulo;
    Estado estado;
    
    public Libro(int isbn, String titulo) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.estado = Estado.DISPONIBLE;
    }
    @Override
    public String toString() { return "ISBN: " + isbn + " | Título: " + titulo + " [" + estado + "]"; }
}

class Usuario {
    int id;
    String nombre;
    public Usuario(int id, String nombre) { this.id = id; this.nombre = nombre; }
}

// --- 2. LA UNIDAD BÁSICA: EL NODO ---
class Nodo {
    Object dato; 
    Nodo siguiente;
    public Nodo(Object dato) { this.dato = dato; this.siguiente = null; }
}

// --- 3. CLASE PRINCIPAL ---
public class SistemaBiblioteca {
    // Cabezas de las estructuras
    private static Nodo listaLibros = null;    // LISTA ENLAZADA (Catálogo)
    private static Nodo listaUsuarios = null;  // LISTA ENLAZADA (Usuarios)
    private static Nodo cimaPila = null;       // PILA (Historial - LIFO)
    private static Nodo frenteCola = null;     // COLA (Espera - FIFO)
    private static Nodo finCola = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n--- SISTEMA DE GESTIÓN BIBLIOTECARIA ---");
            System.out.println("1. Registrar Libro (Lista Enlazada)");
            System.out.println("2. Registrar Usuario (Lista Enlazada)");
            System.out.println("3. Prestar Libro (Gestión de Cola)");
            System.out.println("4. Ver Última Acción (Pila)");
            System.out.println("5. Ver Catálogo y Usuarios");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();

            switch (opcion) {
                case 1: agregarLibro(sc); break;
                case 2: agregarUsuario(sc); break;
                case 3: gestionarPrestamo(sc); break;
                case 4: mostrarUltimaAccion(); break;
                case 5: mostrarTodo(); break;
            }
        } while (opcion != 6);
    }

    // --- LÓGICA DE PILA (HISTORIAL) ---
    // Justificación: LIFO (Last In, First Out) para obtener el evento más reciente.
    public static void registrarEnPila(String evento) {
        Nodo nuevo = new Nodo(evento + " a las " + LocalDateTime.now());
        nuevo.siguiente = cimaPila;
        cimaPila = nuevo;
    }

    public static void mostrarUltimaAccion() {
        if (cimaPila == null) System.out.println("No hay historial.");
        else System.out.println("Última acción: " + cimaPila.dato);
    }

    // --- LÓGICA DE LISTA ENLAZADA (LIBROS) ---
    public static void agregarLibro(Scanner sc) {
        System.out.print("Ingrese ISBN (int): "); int isbn = sc.nextInt();
        sc.nextLine(); 
        System.out.print("Ingrese Título: "); String titulo = sc.nextLine();

        Libro nuevoLibro = new Libro(isbn, titulo);
        Nodo nuevoNodo = new Nodo(nuevoLibro);

        // Inserción al final para mantener orden cronológico de ingreso
        if (listaLibros == null) {
            listaLibros = nuevoNodo;
        } else {
            Nodo temp = listaLibros;
            while (temp.siguiente != null) temp = temp.siguiente;
            temp.siguiente = nuevoNodo;
        }
        registrarEnPila("Libro registrado: " + titulo);
    }

    // --- LÓGICA DE COLA (PRÉSTAMOS / ESPERA) ---
    // Justificación: FIFO (First In, First Out) para respetar turnos de reserva.
    public static void gestionarPrestamo(Scanner sc) {
        System.out.print("ID Usuario (int): "); int idU = sc.nextInt();
        System.out.print("ISBN Libro (int): "); int isbn = sc.nextInt();

        // Encolar solicitud
        Nodo nuevoTurno = new Nodo("Usuario " + idU + " solicita ISBN " + isbn);
        if (frenteCola == null) {
            frenteCola = nuevoTurno;
            finCola = nuevoTurno;
        } else {
            finCola.siguiente = nuevoTurno;
            finCola = nuevoTurno;
        }
        registrarEnPila("Solicitud de préstamo encolada para Usuario: " + idU);
        System.out.println("Turno de reserva asignado.");
    }

    public static void agregarUsuario(Scanner sc) {
        System.out.print("ID Usuario (int): "); int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Nombre: "); String nombre = sc.nextLine();
        
        Nodo nuevo = new Nodo(new Usuario(id, nombre));
        nuevo.siguiente = listaUsuarios;
        listaUsuarios = nuevo;
        registrarEnPila("Usuario registrado: " + nombre);
    }

    public static void mostrarTodo() {
        System.out.println("\n--- CATÁLOGO ---");
        Nodo tL = listaLibros;
        while(tL != null) { System.out.println(tL.dato); tL = tL.siguiente; }
        
        System.out.println("\n--- USUARIOS ---");
        Nodo tU = listaUsuarios;
        while(tU != null) { 
            Usuario u = (Usuario)tU.dato;
            System.out.println("ID: " + u.id + " | " + u.nombre); 
            tU = tU.siguiente; 
        }
    }
}