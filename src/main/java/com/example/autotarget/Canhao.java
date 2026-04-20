package com.example.autotarget;

import java.util.List;
import java.util.ArrayList;

public class Canhao extends Thread {

    private double x;
    private double y;
    private double angulo;
    private List<Projetil> projeteis;
    private boolean ativo;
    private Jogo jogo; // Referência para buscar alvos
    private static final double VELOCIDADE_PROJETIL = 15;
    private static final int INTERVALO_DE_DISPARO = 800; // 800ms para balanceamento

    public Canhao(double x, double y, Jogo jogo) {
        this.x = x;
        this.y = y;
        this.jogo = jogo;
        this.angulo = 0;
        this.projeteis = new ArrayList<>();
        this.ativo = true;
    }

    /**
     * Mira automaticamente no alvo mais próximo.
     */
    public synchronized void mirar() {
        List<Alvo> alvos = jogo.getAlvos();
        Alvo alvoMaisProximo = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Alvo a : alvos) {
            if (a.isAtivo()) {
                double dx = a.getX() - this.x;
                double dy = a.getY() - this.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < menorDistancia) {
                    menorDistancia = dist;
                    alvoMaisProximo = a;
                }
            }
        }

        if (alvoMaisProximo != null) {
            double dx = alvoMaisProximo.getX() - this.x;
            double dy = alvoMaisProximo.getY() - this.y;
            this.angulo = Math.atan2(dy, dx);
        }
    }

    /**
     * Dispara um projétil na direção do ângulo atual.
     */
    public synchronized void disparar() throws JogoException {
        try {
            if (!ativo) return;
            
            // Cria e inicia a Thread do Projétil
            Projetil projetil = new Projetil(x, y, angulo, VELOCIDADE_PROJETIL);
            synchronized (projeteis) {
                projeteis.add(projetil);
            }
            projetil.start();
        } catch (Exception e) {
            throw new JogoException("Erro ao disparar: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                mirar();    // Lógica automática de mira
                disparar(); // Disparo automático
                Thread.sleep(INTERVALO_DE_DISPARO);
            } catch (JogoException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void mover(double novaX, double novaY, double novoAngulo) {
        this.x = novaX;
        this.y = novaY;
        this.angulo = novoAngulo;
    }

    public List<Projetil> getProjeteis() {
        synchronized (projeteis) {
            return new ArrayList<>(projeteis);
        }
    }

    public void limparProjeteis() {
        synchronized (projeteis) {
            projeteis.removeIf(p -> !p.isAtivo());
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngulo() { return angulo; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}