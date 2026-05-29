import java.time.LocalDateTime;
import java.util.Scanner;

// --- 1. ENTIDADES ---
enum Estado {
    DISPONIBLE, PRESTADO
}

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
    public String toString() {
        return "ISBN: " + isbn + " | Título: " + titulo + " [" + estado + "]";
    }
}

class Usuario {
    int id;
    String nombre;

    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}

// --- 2. LA UNIDAD BÁSICA: EL NODO ---
class Nodo {
    Object dato;
    Nodo ramaIzquierda;
    Nodo ramaDerecha;

    public Nodo(Object dato) {
        this.dato = dato;
        this.ramaIzquierda = null;
        this.ramaDerecha = null;
    }
}

// --- 3. CLASE PRINCIPAL ---
public class SistemaBiblioteca {
    // Cabezas de las estructuras
    private static Nodo listaLibros = null; // LISTA ENLAZADA (Catálogo)
    private static Nodo listaUsuarios = null; // LISTA ENLAZADA (Usuarios)
    private static Nodo cimaPila = null; // PILA (Historial - LIFO)
    private static Nodo frenteCola = null; // COLA (Espera - FIFO)
    private static Nodo finCola = null;

    private static EstructurasNoLineales grafoRelaciones = new EstructurasNoLineales();

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
            System.out.println("6. Calificar Libro Leído (Agregar Arista al Grafo)");
            System.out.println("7. Ver Grafo de Interacciones (Lista Adyacencia)");
            System.out.println("8. Sistema para Recomendación");
            System.out.println("9. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            // limpiar menu
            sc.nextLine();
            switch (opcion) {
                case 1:
                    agregarLibro(sc);
                    break;
                case 2:
                    agregarUsuario(sc);
                    break;
                case 3:
                    gestionarPrestamo(sc);
                    break;
                case 4:
                    mostrarUltimaAccion();
                    break;
                case 5:
                    mostrarTodo();
                    break;
                case 6:
                    registrarLecturaGrafo(sc);
                    break;
                case 7:
                    mostrarGrafoBiblioteca();
                    break;
                case 8:
                    ejecutarRecomendacion(sc);
                    break;
            }
        } while (opcion != 9);
    }

    // --- LÓGICA DE PILA (HISTORIAL) ---
    // Justificación: LIFO (Last In, First Out) para obtener el evento más reciente.
    public static void registrarEnPila(String evento) {
        // Creamos el nodo con el texto del evento
        Nodo nuevoEvento = new Nodo(evento + " a las " + LocalDateTime.now());

        // Como la Pila es lineal, usaremos siempre 'ramaDerecha' para conectar hacia
        // abajo
        // Es como si la rama derecha fuera el "siguiente" de antes
        nuevoEvento.ramaDerecha = cimaPila;

        // La nueva cima es el evento que acaba de entrar
        cimaPila = nuevoEvento;
    }

    public static void mostrarUltimaAccion() {
        if (cimaPila == null)
            System.out.println("No hay historial.");
        else
            System.out.println("Última acción: " + cimaPila.dato);
    }

    // --- LÓGICA DE ÁRBOL BST (LIBROS) ---
    public static void agregarLibro(Scanner sc) {
        try {
            int isbn;
            // Validación: No códigos negativos
            while (true) {
                System.out.print("Ingrese ISBN (Número positivo): ");
                isbn = sc.nextInt();
                if (isbn >= 0) {
                    break; // Sale del bucle si el número es válido
                }
                System.out.println("¡Error! El ISBN debe ser un número positivo.");
            }

            sc.nextLine(); // Limpiar el salto de línea del buffer
            System.out.print("Ingrese Título: ");
            String titulo = sc.nextLine();

            Libro nuevoLibro = new Libro(isbn, titulo);

            // Inserción real en la estructura No Lineal (Árbol BST)
            listaLibros = insertarEnArbol(listaLibros, nuevoLibro);

            // Registrar la acción en la Pila
            registrarEnPila("Libro registrado: " + titulo);

            // ¡MENSAJE DE ÉXITO AQUÍ! (Ya pasó todas las etapas con éxito)
            System.out.println("\n[ÉXITO]: ¡Libro registrado correctamente en el Árbol BST!");

        } catch (java.util.InputMismatchException e) {
            System.out.println("\n[ERROR]: El ISBN debe ser un número entero válido. Registro cancelado.");
            sc.nextLine(); // CRUCIAL: Limpia el texto erróneo del buffer para evitar bucles en el menú
        }
    }

    // Función para encontrar el lugar correcto en el árbol
    private static Nodo insertarEnArbol(Nodo raizActual, Libro libroNuevo) {
        if (raizActual == null) {
            return new Nodo(libroNuevo);
        }

        Libro libroExistente = (Libro) raizActual.dato;

        if (libroNuevo.isbn < libroExistente.isbn) {
            // Si el nuevo es menor, va a la izquierda
            raizActual.ramaIzquierda = insertarEnArbol(raizActual.ramaIzquierda, libroNuevo);
        } else if (libroNuevo.isbn > libroExistente.isbn) {
            // Si el nuevo es mayor, va a la derecha
            raizActual.ramaDerecha = insertarEnArbol(raizActual.ramaDerecha, libroNuevo);
        }

        return raizActual;
    }

    private static Usuario buscarUsuarioEnArbol(Nodo raiz, int idBuscado) {
        if (raiz == null)
            return null; // No lo encontramos

        Usuario usuarioAqui = (Usuario) raiz.dato;

        if (idBuscado == usuarioAqui.id) {
            return usuarioAqui; // ¡Lo encontramos!
        }

        // Si el ID es menor, buscamos por la izquierda; si es mayor, por la derecha
        return idBuscado < usuarioAqui.id
                ? buscarUsuarioEnArbol(raiz.ramaIzquierda, idBuscado)
                : buscarUsuarioEnArbol(raiz.ramaDerecha, idBuscado);
    }

    // Función para buscar un libro por su ISBN dentro del árbol
    private static Libro buscarLibroEnArbol(Nodo raiz, int isbnBuscado) {
        // 1. Si llegamos a una rama vacía, el libro no existe
        if (raiz == null) {
            return null;
        }

        // 2. Convertimos el dato del nodo a tipo Libro para comparar
        Libro libroAqui = (Libro) raiz.dato;

        // 3. Si es el que buscamos, lo devolvemos
        if (isbnBuscado == libroAqui.isbn) {
            return libroAqui;
        }

        // 4. Si el ISBN buscado es menor, buscamos por la izquierda
        if (isbnBuscado < libroAqui.isbn) {
            return buscarLibroEnArbol(raiz.ramaIzquierda, isbnBuscado);
        }

        // 5. Si es mayor, buscamos por la derecha
        return buscarLibroEnArbol(raiz.ramaDerecha, isbnBuscado);
    }

    // --- LÓGICA DE COLA (PRÉSTAMOS / ESPERA) ---
    // Justificación: FIFO (First In, First Out) para respetar turnos de reserva.
    public static void gestionarPrestamo(Scanner sc) {
        System.out.print("ID Usuario (int): ");
        int idU = sc.nextInt();

        // 1. Validar Usuario
        Usuario usuarioEncontrado = buscarUsuarioEnArbol(listaUsuarios, idU);
        if (usuarioEncontrado == null) {
            System.out.println("El usuario con ID " + idU + " no existe.");
            System.out.println("Redirigiendo al módulo de registro de usuarios...");
            agregarUsuario(sc);
            // Reintentamos buscarlo tras el registro
            usuarioEncontrado = buscarUsuarioEnArbol(listaUsuarios, idU);
            if (usuarioEncontrado == null)
                return;
        }

        // 2. Validar Libro
        System.out.print("ISBN Libro (int): ");
        int isbn = sc.nextInt();
        Libro libroEncontrado = buscarLibroEnArbol(listaLibros, isbn);

        if (libroEncontrado == null) {
            System.out.println("Error: El libro con ISBN " + isbn + " no existe en el catálogo.");
            return;
        }

        // 3. Encolar solicitud si ambos existen
        Nodo nuevoTurno = new Nodo("Usuario: " + usuarioEncontrado.nombre + " solicita: " + libroEncontrado.titulo);

        if (frenteCola == null) {
            frenteCola = nuevoTurno;
            finCola = nuevoTurno;
        } else {
            finCola.ramaDerecha = nuevoTurno;
            finCola = nuevoTurno;
        }

        registrarEnPila("Solicitud de préstamo: " + usuarioEncontrado.nombre + " solicita " + libroEncontrado.titulo);
        System.out.println("Turno de reserva asignado para: " + libroEncontrado.titulo);
    }

    public static void agregarUsuario(Scanner sc) {
        int id;

        // 1. Validación Humana (Saber actitudinal: Precisión y Ética)
        while (true) {
            System.out.print("ID del Usuario (solo números positivos): ");
            id = sc.nextInt();
            if (id >= 0) {
                break; // El ID es correcto, salimos del bucle
            } else {
                System.out.println("Error: El código de usuario no puede ser menor a cero. Intente de nuevo.");
            }
        }

        sc.nextLine(); // Limpiar el salto de línea del Scanner
        System.out.print("Nombre completo del usuario: ");
        String nombre = sc.nextLine();

        Usuario nuevoUsuario = new Usuario(id, nombre);

        // 2. Insertar en el Árbol (Saber procedimental: Eficiencia)
        // En lugar de: listaUsuarios = nuevo;
        // Usamos la lógica de ramas:
        listaUsuarios = insertarUsuarioEnArbol(listaUsuarios, nuevoUsuario);

        registrarEnPila("Usuario registrado: " + nombre);
        System.out.println("¡Usuario guardado con éxito en el sistema!");
    }

    private static Nodo insertarUsuarioEnArbol(Nodo raizActual, Usuario usuarioNuevo) {
        // Si llegamos a un lugar vacío, aquí es donde plantamos el nuevo nodo
        if (raizActual == null) {
            return new Nodo(usuarioNuevo);
        }

        // Comparamos el ID nuevo con el ID del usuario que ya está en esta posición
        Usuario usuarioExistente = (Usuario) raizActual.dato;

        if (usuarioNuevo.id < usuarioExistente.id) {
            // Si el ID es menor, buscamos sitio por la rama izquierda
            raizActual.ramaIzquierda = insertarUsuarioEnArbol(raizActual.ramaIzquierda, usuarioNuevo);
        } else if (usuarioNuevo.id > usuarioExistente.id) {
            // Si el ID es mayor, buscamos sitio por la rama derecha
            raizActual.ramaDerecha = insertarUsuarioEnArbol(raizActual.ramaDerecha, usuarioNuevo);
        } else {
            // Si el ID es igual, podrías mostrar un mensaje de que el usuario ya existe
            System.out.println("Aviso: El ID " + usuarioNuevo.id + " ya está registrado.");
        }

        return raizActual;
    }

    // Función para mostrar cualquier árbol
    private static void recorrerYMostrar(Nodo raiz) {
        if (raiz != null) {
            recorrerYMostrar(raiz.ramaIzquierda);
            if (raiz.dato instanceof Libro)
                System.out.println(raiz.dato);
            else if (raiz.dato instanceof Usuario) {
                Usuario u = (Usuario) raiz.dato;
                System.out.println("ID: " + u.id + " | Nombre: " + u.nombre);
            }
            recorrerYMostrar(raiz.ramaDerecha);
        }
    }

    public static void mostrarTodo() {
        System.out.println("\n--- CATÁLOGO DE LIBROS (Árbol BST) ---");
        if (listaLibros == null)
            System.out.println("Catálogo vacío.");
        else
            recorrerYMostrar(listaLibros);

        System.out.println("\n--- LISTA DE USUARIOS (Árbol BST) ---");
        if (listaUsuarios == null)
            System.out.println("No hay usuarios registrados.");
        else
            recorrerYMostrar(listaUsuarios);
    }

    public static void registrarLecturaGrafo(Scanner sc) {
        System.out.print("ID del Usuario (int): ");
        int idU = sc.nextInt();
        System.out.print("ISBN del Libro (int): ");
        int isbn = sc.nextInt();
        System.out.print("Calificación del libro (1 al 5): ");
        int rating = sc.nextInt();

        // Validamos de forma cruzada que existan en tus listas enlazadas antes de
        // unirlos en el grafo
        boolean usuarioExiste = false;
        Nodo tU = listaUsuarios;
        while (tU != null) {
            if (((Usuario) tU.dato).id == idU) {
                usuarioExiste = true;
                break;
            }
            tU = tU.ramaDerecha;
        }

        boolean libroExiste = false;
        Nodo tL = listaLibros;
        while (tL != null) {
            if (((Libro) tL.dato).isbn == isbn) {
                libroExiste = true;
                break;
            }
            tL = tL.ramaDerecha;
        }

        if (!usuarioExiste || !libroExiste) {
            System.out.println("Error: El usuario o el libro no existen en el catálogo/registro de listas.");
            return;
        }

        // Llamamos al método del otro archivo
        grafoRelaciones.agregarInteraccion(idU, isbn, rating);
        registrarEnPila("Interacción registrada en grafo: Usuario " + idU + " -> ISBN " + isbn);
        System.out.println("¡Interacción añadida con éxito al Grafo!");
    }

    public static void mostrarGrafoBiblioteca() {
        // Llamamos al otro archivo para pintar la lista de adyacencia
        grafoRelaciones.mostrarGrafo();
    }

    public static void ejecutarRecomendacion(Scanner sc) {
        System.out.print("Ingrese el ID del Usuario: ");
        int idU = sc.nextInt();

        // El grafo analiza vecinos y relaciones complejas en el otro archivo
        int isbnRecomendado = grafoRelaciones.recomendarLibro(idU);

        if (isbnRecomendado == -1) {
            System.out.println("No hay recomendaciones disponibles para este usuario por el momento.");
        } else {
            String titulo = "Desconocido";
            Nodo tL = listaLibros;
            while (tL != null) {
                if (((Libro) tL.dato).isbn == isbnRecomendado) {
                    titulo = ((Libro) tL.dato).titulo;
                    break;
                }
                tL = tL.ramaDerecha;
            }
            System.out.println("\n[RECOMENDACIÓN DEL GRAFO]: Basado en usuarios con gustos similares...");
            System.out.println("Sugerencia de lectura: \"" + titulo + "\" (ISBN: " + isbnRecomendado + ")");
        }
    }

}