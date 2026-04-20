package com.example.autotarget;

public class Projetil extends Thread {
    private double x;
    private double y;
    private double raio;
    private double velocidadeX;
    private double velocidadeY;
    private boolean ativo;
    private static final int Largura_Tela = 1080;
    private static final int Altura_Tela = 2408;

    public Projetil(double x, double y, double angulo, double velocidade) {
        this.x = x;
        this.y = y;
        this.raio = 5; // Raio pequeno do projetil
        this.velocidadeX = velocidade * Math.cos(angulo);
        this.velocidadeY = velocidade * Math.sin(angulo);
        this.ativo = true;
    }

    /*
     Mover o projetil em linha reta
    * */
    public void mover() {
        x += velocidadeX;
        y += velocidadeY;

        /* Verificar se saiu da tela */
        if (x < 0 || x > Largura_Tela || y < 0 || y > Altura_Tela) {
            ativo = false;
        }
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                mover();
                Thread.sleep(30); // Atualiza a cada 30 milissegundos
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRaio() {
        return raio;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}