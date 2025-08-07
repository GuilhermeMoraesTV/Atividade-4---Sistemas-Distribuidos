package eleicao;

public enum TipoMensagem {
    ELEICAO,       // Mensagem de início de eleição (Bully e Anel)
    OK,            // Mensagem de resposta a uma eleição (Bully)
    COORDENADOR,   // Mensagem anunciando o novo coordenador (Bully e Anel)
    PING           // Mensagem para verificar se um processo está ativo
}