package com.example.autotarget;

public abstract class Alvo extends Thread {
    protected double x;
    protected double y;
    protected double raio;
    protected double velocidade;
    protected boolean ativo;
    
    // Removi as constantes fixas para usar o tamanho dinâmico da tela
    protected int larguraTela = 1080;
    protected int alturaTela = 1920;

    public Alvo(double x, double y, double raio, double velocidade) {
        this.x = x;
        this.y = y;
        this.raio = raio;
        this.velocidade = velocidade;
        this.ativo = true;
    }

    /**
     * Define os limites da tela dinamicamente.
     */
    public void setLimitesTela(int largura, int altura) {
        this.larguraTela = largura;
        this.alturaTela = altura;
    }

    public abstract void mover();

    public boolean verificarColisao(Projetil projetil) {
        try {
            double dx = this.x - projetil.getX();
            double dy = this.y - projetil.getY();
            double distancia = Math.sqrt(dx * dx + dy * dy);
            return distancia < this.raio + projetil.getRaio();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                mover();
                Thread.sleep(50);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Getters e Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getRaio() { return raio; }
    public double getVelocidade() { return velocidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}