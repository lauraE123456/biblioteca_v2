// --- CLASES ESPECIALIZADAS PARA EL GRAFO (FASE 3) ---

// Representa una conexión dirigida con un peso (Rating del libro de 1 a 5)
class Arista {
    int destinoIsbn;
    int pesoRating;

    public Arista(int destinoIsbn, int pesoRating) {
        this.destinoIsbn = destinoIsbn;
        this.pesoRating = pesoRating;
    }
}

// Representa un nodo del Grafo que contiene la lista de adyacencia de un
// usuario
class VerticeUsuario {
    int idUsuario;
    Nodo listaAristas; // Reutiliza tu clase Nodo existente para las conexiones
    VerticeUsuario siguienteVertice;

    public VerticeUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        this.listaAristas = null;
        this.siguienteVertice = null;
    }
}

// Esta es la clase principal de este archivo, encargada de la lógica del Grafo
public class EstructurasNoLineales {
    private VerticeUsuario cabezaUsuarios;

    public EstructurasNoLineales() {
        this.cabezaUsuarios = null;
    }

    // Agrega un usuario al grafo si no existe
    public void agregarVerticeUsuario(int idUsuario) {
        if (buscarVertice(idUsuario) == null) {
            VerticeUsuario nuevo = new VerticeUsuario(idUsuario);
            nuevo.siguienteVertice = cabezaUsuarios;
            cabezaUsuarios = nuevo;
        }
    }

    private VerticeUsuario buscarVertice(int idUsuario) {
        VerticeUsuario temp = cabezaUsuarios;
        while (temp != null) {
            if (temp.idUsuario == idUsuario)
                return temp;
            temp = temp.siguienteVertice;
        }
        return null;
    }

    // Registra una arista (Interacción lectura/calificación)
    public void agregarInteraccion(int idUsuario, int isbnLibro, int rating) {
        agregarVerticeUsuario(idUsuario);
        VerticeUsuario vu = buscarVertice(idUsuario);

        Nodo temp = vu.listaAristas;
        while (temp != null) {
            Arista aristaActual = (Arista) temp.dato;
            if (aristaActual.destinoIsbn == isbnLibro) {
                aristaActual.pesoRating = rating;
                return;
            }
            temp = temp.ramaDerecha;
        }

        Arista nuevaArista = new Arista(isbnLibro, rating);
        Nodo nuevoNodo = new Nodo(nuevaArista);
        nuevoNodo.ramaDerecha = vu.listaAristas;
        vu.listaAristas = nuevoNodo;
    }

    // Algoritmo de optimización: Sistema de recomendación por afinidad
    public int recomendarLibro(int idUsuario) {
        VerticeUsuario usuarioObjetivo = buscarVertice(idUsuario);
        if (usuarioObjetivo == null || usuarioObjetivo.listaAristas == null)
            return -1;

        VerticeUsuario otroUsuario = cabezaUsuarios;
        int mejorIsbnRecomendado = -1;
        int maxRatingEncontrado = -1;

        while (otroUsuario != null) {
            if (otroUsuario.idUsuario != idUsuario) {
                boolean compartenGustos = verificarAfinidad(usuarioObjetivo, otroUsuario);

                if (compartenGustos) {
                    Nodo aristaOtro = otroUsuario.listaAristas;
                    while (aristaOtro != null) {
                        Arista aO = (Arista) aristaOtro.dato;
                        if (aO.pesoRating > maxRatingEncontrado && !tieneLibro(usuarioObjetivo, aO.destinoIsbn)) {
                            maxRatingEncontrado = aO.pesoRating;
                            mejorIsbnRecomendado = aO.destinoIsbn;
                        }
                        aristaOtro = aristaOtro.ramaDerecha;
                    }
                }
            }
            otroUsuario = otroUsuario.siguienteVertice;
        }
        return mejorIsbnRecomendado;
    }

    private boolean verificarAfinidad(VerticeUsuario u1, VerticeUsuario u2) {
        Nodo a1 = u1.listaAristas;
        while (a1 != null) {
            Arista arista1 = (Arista) a1.dato;
            Nodo a2 = u2.listaAristas;
            while (a2 != null) {
                Arista arista2 = (Arista) a2.dato;
                if (arista1.destinoIsbn == arista2.destinoIsbn && arista1.pesoRating >= 4 && arista2.pesoRating >= 4) {
                    return true;
                }
                a2 = a2.ramaDerecha;
            }
            a1 = a1.ramaDerecha;
        }
        return false;
    }

    private boolean tieneLibro(VerticeUsuario u, int isbn) {
        Nodo a = u.listaAristas;
        while (a != null) {
            if (((Arista) a.dato).destinoIsbn == isbn)
                return true;
            a = a.ramaDerecha;
        }
        return false;
    }

    public void mostrarGrafo() {
        VerticeUsuario temp = cabezaUsuarios;
        while (temp != null) {
            System.out.print("Usuario ID [" + temp.idUsuario + "] ha leído -> ");
            Nodo aristaTemp = temp.listaAristas;
            if (aristaTemp == null)
                System.out.print("Ningún libro aún.");
            while (aristaTemp != null) {
                Arista a = (Arista) aristaTemp.dato;
                System.out.print("[ISBN: " + a.destinoIsbn + " (★" + a.pesoRating + ")] ");
                aristaTemp = aristaTemp.ramaDerecha;
            }
            System.out.println();
            temp = temp.siguienteVertice;
        }
    }
}