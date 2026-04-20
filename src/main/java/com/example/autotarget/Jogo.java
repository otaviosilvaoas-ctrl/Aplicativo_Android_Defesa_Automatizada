package com.example.autotarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe principal que gerencia a lógica do jogo.
 * Controla alvos, canhões e o sistema de colisões.
 */
public class Jogo extends Thread {
    private List<Alvo> alvos;
    private List<Canhao> canhoes;
    private boolean emExecucao;
    private int abatesTotal;

    // Trava de sincronização para garantir segurança de thread nas listas
    private static final Object LOCK_ALVOS = new Object();
    private static final Object LOCK_CANHOES = new Object();
    private static final double DISTANCIA_MINIMA_CANHOES = 150.0;

    public Jogo() {
        this.alvos = new ArrayList<>();
        this.canhoes = new ArrayList<>();
        this.emExecucao = false;
        this.abatesTotal = 0;
    }

    @Override
    public void run() {
        while (emExecucao) {
            try {
                verificarColisoes();
                Thread.sleep(30); // Frequência de atualização da física
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Inicia as threads do jogo e dos objetos.
     */
    public void iniciar() throws JogoException {
        if (emExecucao) {
            throw new JogoException("Jogo já está em execução");
        }
        emExecucao = true;
        
        criarAlvosIniciais();

        // Inicializa as threads dos alvos existentes
        synchronized (LOCK_ALVOS) {
            for (Alvo alvo : alvos) {
                if (alvo.getState() == State.NEW) alvo.start();
            }
        }
        
        // Inicializa as threads dos canhões existentes
        synchronized (LOCK_CANHOES) {
            for (Canhao canhao : canhoes) {
                if (canhao.getState() == State.NEW) canhao.start();
            }
        }

        // Inicia a thread controladora do Jogo
        if (this.getState() == State.NEW) {
            this.start();
        }
    }

    public void parar() {
        emExecucao = false;
        synchronized (LOCK_ALVOS) {
            for (Alvo alvo : alvos) alvo.setAtivo(false);
        }
        synchronized (LOCK_CANHOES) {
            for (Canhao canhao : canhoes) canhao.setAtivo(false);
        }
    }

    private void criarAlvosIniciais() {
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            adicionarAlvo(new AlvoComum(random.nextDouble() * 800 + 100, 
                          random.nextDouble() * 1500 + 100, 40, 5));
        }
        for (int i = 0; i < 2; i++) {
            adicionarAlvo(new AlvoRapido(random.nextDouble() * 800 + 100, 
                          random.nextDouble() * 1500 + 100, 30, 8));
        }
    }

    private void adicionarAlvo(Alvo alvo) {
        synchronized (LOCK_ALVOS) {
            alvos.add(alvo);
            if (emExecucao && alvo.getState() == State.NEW) {
                alvo.start();
            }
        }
    }

    /**
     * Adiciona um canhão garantindo que não haja sobreposição.
     */
    public void adicionarCanhao(double x, double y) throws JogoException {
        synchronized (LOCK_CANHOES) {
            if (canhoes.size() >= 10) {
                throw new JogoException("Máximo de 10 canhões atingido");
            }

            for (Canhao existente : canhoes) {
                double dx = x - existente.getX();
                double dy = y - existente.getY();
                if (Math.sqrt(dx * dx + dy * dy) < DISTANCIA_MINIMA_CANHOES) {
                    x += 160; 
                }
            }

            // Passa 'this' para que o canhão possa localizar alvos
            Canhao novoCanhao = new Canhao(x, y, this);
            canhoes.add(novoCanhao);
            if (emExecucao && novoCanhao.getState() == State.NEW) {
                novoCanhao.start();
            }
        }
    }

    /**
     * Região crítica: verifica colisão entre projéteis de todos os canhões e todos os alvos.
     */
    public void verificarColisoes() {
        synchronized (LOCK_ALVOS) {
            synchronized (LOCK_CANHOES) {
                for (int i = alvos.size() - 1; i >= 0; i--) {
                    Alvo alvo = alvos.get(i);
                    if (!alvo.isAtivo()) continue;

                    for (Canhao canhao : canhoes) {
                        for (Projetil projetil : canhao.getProjeteis()) {
                            if (projetil.isAtivo() && alvo.verificarColisao(projetil)) {
                                // Polimorfismo: o método verificarColisao funciona para qualquer subclasse de Alvo
                                alvo.setAtivo(false);
                                projetil.setAtivo(false);
                                abatesTotal++;
                                break;
                            }
                        }
                    }
                }
                alvos.removeIf(a -> !a.isAtivo());
                
                if (alvos.size() < 5 && emExecucao) {
                    criarAlvosIniciais();
                }
            }
        }
        
        synchronized (LOCK_CANHOES) {
            for (Canhao c : canhoes) {
                c.limparProjeteis();
            }
        }
    }

    public List<Alvo> getAlvos() {
        synchronized (LOCK_ALVOS) { return new ArrayList<>(alvos); }
    }

    public List<Canhao> getCanhoes() {
        synchronized (LOCK_CANHOES) { return new ArrayList<>(canhoes); }
    }

    public int getAbatesTotal() { return abatesTotal; }
    public boolean isEmExecucao() { return emExecucao; }
}