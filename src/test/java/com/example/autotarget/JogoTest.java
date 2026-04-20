package com.example.autotarget;

import org.junit.Test;
import static org.junit.Assert.*;

public class JogoTest {

    @Test
    public void testDetectarColisao() {
        // Alvo na posição (100, 100) com raio 30
        Alvo alvo = new AlvoComum(100, 100, 30, 0);
        
        // Projetil na posição (110, 110) com raio 5 (Distância ~14.1, Colisão se < 35)
        Projetil p1 = new Projetil(110, 110, 0, 0);
        assertTrue("Deveria detectar colisão", alvo.verificarColisao(p1));
        
        // Projetil longe (200, 200)
        Projetil p2 = new Projetil(200, 200, 0, 0);
        assertFalse("Não deveria detectar colisão", alvo.verificarColisao(p2));
    }

    @Test
    public void testLimiteDeCanhoes() throws JogoException {
        Jogo jogo = new Jogo();
        for (int i = 0; i < 10; i++) {
            jogo.adicionarCanhao(100 * i, 100);
        }
        
        try {
            jogo.adicionarCanhao(500, 500);
            fail("Deveria ter lançado JogoException pelo limite de 10 canhões");
        } catch (JogoException e) {
            assertEquals("Máximo de 10 canhões atingido", e.getMessage());
        }
    }

    @Test
    public void testValidacaoPosicaoProjetil() {
        // Projetil criado no centro
        Projetil p = new Projetil(500, 500, 0, 100);
        assertTrue("Projetil deve começar ativo", p.isAtivo());
        
        // Move o projetil para fora da tela
        p.mover(); // x = 600
        p.setAtivo(true); 
        
        // Simulando saída da tela (Largura_Tela = 1080)
        // Se movermos muitas vezes:
        for(int i=0; i<10; i++) p.mover();
        
        // Como o mover() da classe original verifica bordas:
        // p.x += 100 * 11 = 1100 (Maior que 1080)
        assertFalse("Projetil deve ficar inativo ao sair da tela", p.isAtivo());
    }
}