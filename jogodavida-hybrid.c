#include <mpi.h>
#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define EXP_DIMENSAO_MIN 3
#define EXP_DIMENSAO_MAX 10

#define indice_2d(linha, coluna, dimensao) (linha) * ((dimensao) + 2) + (coluna)

typedef struct {
  int id_processo;
  int num_processos;
  int minha_linha_inicial;
  int meu_num_linhas;
} info_mpi_t;

typedef struct {
  int *contagens_recebimento;
  int *deslocamentos;
} info_comunicacao_t;

/**
 * @brief Obtém o tempo atual do sistema (wall clock time).
 * @return O tempo em segundos.
 */
double obter_tempo_real(void) {
  struct timeval tv;
  struct timezone tz;

  gettimeofday(&tv, &tz);

  return (tv.tv_sec + tv.tv_usec / 1000000.0);
}

/**
 * @brief Inicializa os tabuleiros, colocando a configuração "veleiro" (glider)
 * no tabuleiro atual e zerando o tabuleiro seguinte.
 */
void inicializar_veleiro(int *tabuleiro_atual, int *tabuleiro_proximo,
                         int dimensao) {
  int tamanho_total = (dimensao + 2) * (dimensao + 2);

  for (int i = 0; i < tamanho_total; i++) {
    tabuleiro_atual[i] = 0;
    tabuleiro_proximo[i] = 0;
  }

  tabuleiro_atual[indice_2d(1, 2, dimensao)] = 1;
  tabuleiro_atual[indice_2d(2, 3, dimensao)] = 1;
  tabuleiro_atual[indice_2d(3, 1, dimensao)] = 1;
  tabuleiro_atual[indice_2d(3, 2, dimensao)] = 1;
  tabuleiro_atual[indice_2d(3, 3, dimensao)] = 1;
}

/**
 * @brief Verifica se a configuração final do tabuleiro corresponde ao veleiro
 * na posição esperada no canto inferior direito.
 * @return 1 se o resultado for correto, 0 caso contrário.
 */
int verificar_resultado_final(int *tabuleiro, int dimensao) {
  int tamanho_total = (dimensao + 2) * (dimensao + 2);
  int celulas_vivas = 0;

  for (int i = 0; i < tamanho_total; i++)
    celulas_vivas += tabuleiro[i];

  int pos_final_correta =
      (celulas_vivas == 5 &&
       tabuleiro[indice_2d(dimensao - 2, dimensao - 1, dimensao)] &&
       tabuleiro[indice_2d(dimensao - 1, dimensao, dimensao)] &&
       tabuleiro[indice_2d(dimensao, dimensao - 2, dimensao)] &&
       tabuleiro[indice_2d(dimensao, dimensao - 1, dimensao)] &&
       tabuleiro[indice_2d(dimensao, dimensao, dimensao)]);

  return pos_final_correta;
}

/**
 * @brief Calcula uma geração do Jogo da Vida para uma fatia do tabuleiro.
 * @param tabuleiro_atual O tabuleiro da geração atual (leitura).
 * @param tabuleiro_proximo O tabuleiro da próxima geração (escrita).
 * @param dimensao A dimensão do tabuleiro (NxN).
 * @param info_mpi Ponteiro para a estrutura com informações do processo MPI.
 */
void simular_geracao_parcial(int *tabuleiro_atual, int *tabuleiro_proximo,
                             int dimensao, const info_mpi_t *info_mpi) {
  if (info_mpi->minha_linha_inicial >
      (info_mpi->minha_linha_inicial + info_mpi->meu_num_linhas - 1))
    return;

#pragma omp parallel for
  for (int i = info_mpi->minha_linha_inicial;
       i < info_mpi->minha_linha_inicial + info_mpi->meu_num_linhas; i++) {
    for (int j = 1; j <= dimensao; j++) {
      int vizinhos_vivos = tabuleiro_atual[indice_2d(i - 1, j - 1, dimensao)] +
                           tabuleiro_atual[indice_2d(i - 1, j, dimensao)] +
                           tabuleiro_atual[indice_2d(i - 1, j + 1, dimensao)] +
                           tabuleiro_atual[indice_2d(i, j - 1, dimensao)] +
                           tabuleiro_atual[indice_2d(i, j + 1, dimensao)] +
                           tabuleiro_atual[indice_2d(i + 1, j - 1, dimensao)] +
                           tabuleiro_atual[indice_2d(i + 1, j, dimensao)] +
                           tabuleiro_atual[indice_2d(i + 1, j + 1, dimensao)];

      int celula_idx = indice_2d(i, j, dimensao);

      if (tabuleiro_atual[celula_idx] == 1 &&
          (vizinhos_vivos < 2 || vizinhos_vivos > 3))
        tabuleiro_proximo[celula_idx] = 0; // Solidão ou superpopulação
      else if (tabuleiro_atual[celula_idx] == 0 && vizinhos_vivos == 3)
        tabuleiro_proximo[celula_idx] = 1; // Renascimento
      else
        tabuleiro_proximo[celula_idx] =
            tabuleiro_atual[celula_idx]; // Permanece igual
    }
  }
}

/**
 * @brief Calcula como as linhas do tabuleiro serão divididas entre os processos
 * e preenche as estruturas de informação do MPI.
 */
void calcular_distribuicao_linhas(info_mpi_t *info, info_comunicacao_t *comm,
                                  int dimensao) {
  int linhas_base = dimensao / info->num_processos;
  int linhas_extras = dimensao % info->num_processos;

  if (info->id_processo < linhas_extras) {
    info->minha_linha_inicial = info->id_processo * (linhas_base + 1) + 1;
    info->meu_num_linhas = linhas_base + 1;
  } else {
    info->minha_linha_inicial =
        linhas_extras * (linhas_base + 1) +
        (info->id_processo - linhas_extras) * linhas_base + 1;
    info->meu_num_linhas = linhas_base;
  }

  // Aloca e preenche os vetores para a chamada MPI_Allgatherv
  comm->contagens_recebimento =
      (int *)malloc(info->num_processos * sizeof(int));
  comm->deslocamentos = (int *)malloc(info->num_processos * sizeof(int));

  for (int p = 0; p < info->num_processos; ++p) {
    int p_linhas_base = dimensao / info->num_processos;
    int p_linhas_extras = dimensao % info->num_processos;
    int p_linha_inicial, p_num_linhas;

    if (p < p_linhas_extras) {
      p_linha_inicial = p * (p_linhas_base + 1) + 1;
      p_num_linhas = p_linhas_base + 1;
    } else {
      p_linha_inicial = p_linhas_extras * (p_linhas_base + 1) +
                        (p - p_linhas_extras) * p_linhas_base + 1;
      p_num_linhas = p_linhas_base;
    }

    comm->contagens_recebimento[p] = p_num_linhas * (dimensao + 2);
    comm->deslocamentos[p] = indice_2d(p_linha_inicial, 0, dimensao);
  }
}

/**
 * @brief Executa o laço principal da simulação, iterando pelas gerações.
 */
void executar_simulacao(int *tabuleiro_atual, int *tabuleiro_proximo,
                        const info_mpi_t *info, const info_comunicacao_t *comm,
                        int dimensao) {
  int num_geracoes = 4 * (dimensao - 3);

  for (int i = 0; i < num_geracoes; i++) {
    // 1. Calcula a próxima geração para a fatia local do tabuleiro
    simular_geracao_parcial(tabuleiro_atual, tabuleiro_proximo, dimensao, info);

    // 2. Sincroniza todos os tabuleiros. Cada processo envia sua fatia
    // calculada
    //    e recebe as fatias dos outros, montando o tabuleiro completo.
    MPI_Allgatherv(MPI_IN_PLACE, 0, MPI_DATATYPE_NULL, tabuleiro_proximo,
                   comm->contagens_recebimento, comm->deslocamentos, MPI_INT,
                   MPI_COMM_WORLD);

    // 3. Troca os ponteiros para a próxima iteração
    int *temp = tabuleiro_atual;
    tabuleiro_atual = tabuleiro_proximo;
    tabuleiro_proximo = temp;
  }
}

/**
 * @brief Função principal de um teste para uma dada dimensão.
 * Orquestra a alocação, inicialização, simulação e o relatório de resultados.
 */
void executar_teste(int expoente_dimensao, info_mpi_t *info) {
  int dimensao = 1 << expoente_dimensao;
  int tamanho_total_buffer = (dimensao + 2) * (dimensao + 2);

  // Alocação de memória
  int *tabuleiro_atual = (int *)malloc(tamanho_total_buffer * sizeof(int));
  int *tabuleiro_proximo = (int *)malloc(tamanho_total_buffer * sizeof(int));

  double t_inicio_total, t_fim_init, t_fim_comp;

  // Processo 0 inicializa o tabuleiro
  if (info->id_processo == 0) {
    t_inicio_total = obter_tempo_real();
    inicializar_veleiro(tabuleiro_atual, tabuleiro_proximo, dimensao);
  }

  // Sincroniza o tempo de início e distribui os tabuleiros para todos
  if (info->id_processo == 0)
    t_fim_init = obter_tempo_real();

  MPI_Bcast(tabuleiro_atual, tamanho_total_buffer, MPI_INT, 0, MPI_COMM_WORLD);
  MPI_Bcast(tabuleiro_proximo, tamanho_total_buffer, MPI_INT, 0,
            MPI_COMM_WORLD);

  // Calcula a distribuição de trabalho e executa a simulação
  info_comunicacao_t info_comm;

  calcular_distribuicao_linhas(info, &info_comm, dimensao);
  executar_simulacao(tabuleiro_atual, tabuleiro_proximo, info, &info_comm,
                     dimensao);

  MPI_Barrier(MPI_COMM_WORLD);

  if (info->id_processo == 0) {
    t_fim_comp = obter_tempo_real();

    // Determina qual ponteiro tem o resultado final
    int num_geracoes = 4 * (dimensao - 3);
    int *tabuleiro_final =
        (num_geracoes % 2 != 0) ? tabuleiro_proximo : tabuleiro_atual;

    if (verificar_resultado_final(tabuleiro_final, dimensao))
      printf("**RESULTADO CORRETO**\n");
    else
      printf("**RESULTADO ERRADO**\n");

    double t_fim_total = obter_tempo_real();

    printf("dimensao=%d; processos=%d; tempos: init=%7.7f, comp=%7.7f, "
           "fim=%7.7f, total=%7.7f \n",
           dimensao, info->num_processos, t_fim_init - t_inicio_total,
           t_fim_comp - t_fim_init, t_fim_total - t_fim_comp,
           t_fim_total - t_inicio_total);
  }

  // Liberação de memória
  free(tabuleiro_atual);
  free(tabuleiro_proximo);
  free(info_comm.contagens_recebimento);
  free(info_comm.deslocamentos);
}

int main(int argc, char **argv) {
  MPI_Init(&argc, &argv);

  info_mpi_t info_mpi;

  MPI_Comm_size(MPI_COMM_WORLD, &info_mpi.num_processos);
  MPI_Comm_rank(MPI_COMM_WORLD, &info_mpi.id_processo);

  for (int expoente = EXP_DIMENSAO_MIN; expoente <= EXP_DIMENSAO_MAX;
       expoente++)
    executar_teste(expoente, &info_mpi);

  MPI_Finalize();

  return 0;
}
