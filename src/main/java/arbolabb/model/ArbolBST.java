package arbolabb.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

public class ArbolBST<T extends Comparable<T>> {

    private NodoArbol<T> raiz;

    public ArbolBST() {
        this.raiz = null;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public void agregar(T dato) {
        raiz = agregarRec(raiz, dato);
    }
    private NodoArbol<T> agregarRec(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return new NodoArbol<>(dato);
        int cmp = dato.compareTo(nodo.getDato());
        if (cmp < 0)
            nodo.setIzquierdo(agregarRec(nodo.getIzquierdo(), dato));
        else if (cmp > 0)
            nodo.setDerecho(agregarRec(nodo.getDerecho(), dato));
        // si cmp == 0 no se insertan duplicados
        return nodo;
    }

    public List<T> inorden() {
        List<T> lista = new ArrayList<>();
        inordenRec(raiz, lista);
        return lista;
    }
    private void inordenRec(NodoArbol<T> nodo, List<T> lista) {
        if (nodo == null) return;
        inordenRec(nodo.getIzquierdo(), lista);
        lista.add(nodo.getDato());
        inordenRec(nodo.getDerecho(), lista);
    }

    public List<T> preorden() {
        List<T> lista = new ArrayList<>();
        preordenRec(raiz, lista);
        return lista;
    }
    private void preordenRec(NodoArbol<T> nodo, List<T> lista) {
        if (nodo == null) return;
        lista.add(nodo.getDato());
        preordenRec(nodo.getIzquierdo(), lista);
        preordenRec(nodo.getDerecho(), lista);
    }

    public List<T> postorden() {
        List<T> lista = new ArrayList<>();
        postordenRec(raiz, lista);
        return lista;
    }
    private void postordenRec(NodoArbol<T> nodo, List<T> lista) {
        if (nodo == null) return;
        postordenRec(nodo.getIzquierdo(), lista);
        postordenRec(nodo.getDerecho(), lista);
        lista.add(nodo.getDato());
    }


    public boolean existeDato(T dato) {
        return existeRec(raiz, dato);
    }
    private boolean existeRec(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return false;
        int cmp = dato.compareTo(nodo.getDato());
        if (cmp == 0) return true;
        return cmp < 0
            ? existeRec(nodo.getIzquierdo(), dato)
            : existeRec(nodo.getDerecho(), dato);
    }

    public int obtenerPeso() {
        return pesoRec(raiz);
    }
    private int pesoRec(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + pesoRec(nodo.getIzquierdo()) + pesoRec(nodo.getDerecho());
    }

    public int obtenerAltura() {
        return alturaRec(raiz);
    }
    private int alturaRec(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + Math.max(alturaRec(nodo.getIzquierdo()), alturaRec(nodo.getDerecho()));
    }

    public int obtenerNivel(T dato) {
        return nivelRec(raiz, dato, 1);
    }
    private int nivelRec(NodoArbol<T> nodo, T dato, int nivel) {
        if (nodo == null) return -1;   // no encontrado
        int cmp = dato.compareTo(nodo.getDato());
        if (cmp == 0) return nivel;
        return cmp < 0
            ? nivelRec(nodo.getIzquierdo(), dato, nivel + 1)
            : nivelRec(nodo.getDerecho(), dato, nivel + 1);
    }

    public int contarHojas() {
        return hojasRec(raiz);
    }
    private int hojasRec(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) return 1;
        return hojasRec(nodo.getIzquierdo()) + hojasRec(nodo.getDerecho());
    }

    public T obtenerMenor() {
        if (estaVacio()) return null;
        return menorRec(raiz);
    }
    private T menorRec(NodoArbol<T> nodo) {
        return nodo.getIzquierdo() == null
            ? nodo.getDato()
            : menorRec(nodo.getIzquierdo());
    }

    public T obtenerNodoMayor() {
        if (estaVacio()) return null;
        return mayorRec(raiz);
    }
    private T mayorRec(NodoArbol<T> nodo) {
        return nodo.getDerecho() == null
            ? nodo.getDato()
            : mayorRec(nodo.getDerecho());
    }

    public T obtenerNodoMenor() {
        return obtenerMenor();
    }

    public List<List<T>> imprimirAmplitud() {
        List<List<T>> niveles = new ArrayList<>();
        if (estaVacio()) return niveles;

        Queue<NodoArbol<T>> cola = new LinkedList<>();
        cola.add(raiz);

        while (!cola.isEmpty()) {
            int tam = cola.size();
            List<T> nivel = new ArrayList<>();
            for (int i = 0; i < tam; i++) {
                NodoArbol<T> actual = cola.poll();
                nivel.add(actual.getDato());
                if (actual.getIzquierdo() != null) cola.add(actual.getIzquierdo());
                if (actual.getDerecho() != null) cola.add(actual.getDerecho());
            }
            niveles.add(nivel);
        }
        return niveles;
    }

    public void eliminar(T dato) {
        raiz = eliminarRec(raiz, dato);
    }
    private NodoArbol<T> eliminarRec(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return null;
        int cmp = dato.compareTo(nodo.getDato());
        if (cmp < 0) {
            nodo.setIzquierdo(eliminarRec(nodo.getIzquierdo(), dato));
        } else if (cmp > 0) {
            nodo.setDerecho(eliminarRec(nodo.getDerecho(), dato));
        } else {
            // Nodo encontrado — 3 casos
            if (nodo.getIzquierdo() == null) return nodo.getDerecho();
            if (nodo.getDerecho() == null)   return nodo.getIzquierdo();
            // Tiene dos hijos: reemplazar con el menor del subárbol derecho
            T sucesor = menorRec(nodo.getDerecho());
            nodo.setDato(sucesor);
            nodo.setDerecho(eliminarRec(nodo.getDerecho(), sucesor));
        }
        return nodo;
    }

    public void borrarArbol() {
        raiz = null;
    }

    public NodoArbol<T> getRaiz() {
        return raiz;
    }
}