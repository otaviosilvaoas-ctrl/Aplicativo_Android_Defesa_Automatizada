package com.example.autotarget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity principal do jogo AutoTarget.
 * Gerencia a view do jogo e a lógica de atualização.
 */
public class GameViewActivity extends AppCompatActivity {

    private JogoView jogoView;
    private Jogo jogo;
    private Handler handler;
    private Thread gameThread;
    private boolean emExecucao;
    private static final int INTERVALO_ATUALIZACAO = 50; // 50ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_view);

        // Inicializa componentes
        jogoView = findViewById(R.id.jogo_view);
        Button pauseButton = findViewById(R.id.pause_button);
        Button addCannonButton = findViewById(R.id.add_cannon_button);
        TextView statusText = findViewById(R.id.status_text);

        // Cria instância do jogo
        jogo = new Jogo();
        jogoView.setJogo(jogo);

        // Handler para atualizar UI na thread principal
        handler = new Handler(Looper.getMainLooper());

        // Inicia o jogo
        try {
            jogo.iniciar();
            emExecucao = true;
            iniciarThreadDeAtualizacao();
        } catch (JogoException e) {
            statusText.setText("Erro ao iniciar: " + e.getMessage());
        }

        // Configura botão de pausa
        pauseButton.setOnClickListener(v -> {
            if (emExecucao) {
                jogo.parar();
                emExecucao = false;
                pauseButton.setText("Retomar");
            } else {
                try {
                    jogo.iniciar();
                    emExecucao = true;
                    iniciarThreadDeAtualizacao();
                    pauseButton.setText("Pausar");
                } catch (JogoException e) {
                    statusText.setText("Erro ao retomar: " + e.getMessage());
                }
            }
        });

        // Configura botão de adicionar canhão
        addCannonButton.setOnClickListener(v -> {
            try {
                // Obtém o tamanho da JogoView para posicionar o canhão dentro dos limites visíveis
                float larguraView = jogoView.getWidth();
                float alturaView = jogoView.getHeight();

                // Se a view ainda não foi desenhada (0), usa valores padrão do centro
                if (larguraView == 0) larguraView = getResources().getDisplayMetrics().widthPixels;
                if (alturaView == 0) alturaView = getResources().getDisplayMetrics().heightPixels;

                // Adiciona o canhão em uma posição aleatória dentro da JogoView
                // Deixando uma margem de 100px para não ficar colado na borda
                float xAleatorio = 100 + (float)(Math.random() * (larguraView - 200));
                float yAleatorio = 100 + (float)(Math.random() * (alturaView - 200));

                jogo.adicionarCanhao(xAleatorio, yAleatorio);
            } catch (JogoException e) {
                statusText.setText("Erro: " + e.getMessage());
            }
        });
    }

    /**
     * Inicia a thread de atualização do jogo.
     */
    private void iniciarThreadDeAtualizacao() {
        gameThread = new Thread(() -> {
            while (emExecucao) {
                try {
                    // Verifica colisões
                    jogo.verificarColisoes();

                    // Atualiza a view
                    handler.post(() -> jogoView.invalidate());

                    Thread.sleep(INTERVALO_ATUALIZACAO);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (emExecucao) {
            jogo.parar();
            emExecucao = false;
        }
        if (jogoView != null) {
            jogoView.parar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!emExecucao && jogo != null) {
                jogo.iniciar();
                emExecucao = true;
                iniciarThreadDeAtualizacao();
            }
        } catch (JogoException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emExecucao) {
            jogo.parar();
            emExecucao = false;
        }
        if (jogoView != null) {
            jogoView.parar();
        }
    }
}