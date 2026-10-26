# Encurtador de URL (Projeto de Estudante)

Este é um projeto simples de **encurtador de URL** feito em Java com Spring Boot.

A ideia é criar um sistema que recebe uma URL longa e devolve uma versão curta, tipo:
`https://exemplo.com/pagina/muito/longa` → `http://localhost:8080/aB3xY9z`

O projeto foi pensado para aprender coisas importantes de backend:
- Geração de IDs
- Banco de dados (Postgres)
- Cache (Redis)
- Rate limiting
- Como aguentar bastante tráfego

---

## Tecnologias utilizadas

| Tecnologia          | Para que serve no projeto                          |
|---------------------|----------------------------------------------------|
| **Java**         | Linguagem principal                                |
| **Spring Boot 3**   | Framework web (API REST)                           |
| **Spring Data JPA** | Acesso ao banco de dados                           |
| **PostgreSQL**      | Banco de dados principal (guarda as URLs)          |
| **Redis**           | Cache rápido (para redirecionamentos)              |
| **Docker Compose**  | Sobe o Postgres e o Redis com um comando só        |
| **Maven**           | Gerenciador de dependências e build                |

---

## Como o projeto funciona

### 1. Criar uma URL curta (`POST /api/shorten`)

1. O usuário manda uma URL longa
2. O sistema gera um ID sequencial
3. Converte o ID para Base62 (vira um código curto tipo `aB3xY9z`)
4. Salva no **Postgres**
5. Coloca também no **Redis** (cache)
6. Devolve a URL curta

### 2. Usar a URL curta (`GET /{codigo}`)

1. Alguém acessa `http://localhost:8080/aB3xY9z`
2. O sistema primeiro procura no **Redis** (bem rápido)
3. Se não achar, busca no **Postgres**
4. Coloca de volta no Redis
5. Redireciona (302) para a URL original

### Rate Limiting

Só a criação de URL tem limite:
- 60 requisições por minuto por IP
- Feito com Redis (simples e fácil de entender)

---

## Matemática de capacidade (importante!)

Usamos códigos de **7 caracteres** em Base62:

```
62^7 ≈ 3,5 trilhões de combinações possíveis
```

Mesmo guardando 1 bilhão de URLs ainda sobra bastante espaço. Por isso 7 caracteres é um bom tamanho para esse projeto.

---

## Como rodar o projeto

### 1. Subir o Postgres e o Redis

```bash
docker compose up -d
```

### 2. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API fica em: `http://localhost:8080`

### 3. Testar

**Criar uma URL curta:**

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

**Testar o redirecionamento:**

```bash
curl -I http://localhost:8080/CODIGO_AQUI
```

**Health check:**

```bash
curl http://localhost:8080/api/health
```

---

## Estrutura do projeto

```
src/main/java/com/student/urlshortener/
├── UrlShortenerApplication.java      ← ponto de entrada
├── config/
│   └── RedisConfig.java
├── controller/
│   └── UrlController.java            ← endpoints da API
├── dto/
│   ├── ShortenRequest.java
│   └── ShortenResponse.java
├── model/
│   └── UrlMapping.java               ← entidade do banco
├── repository/
│   └── UrlMappingRepository.java
└── service/
    ├── Base62Encoder.java            ← gera o código curto
    ├── IdGenerator.java              ← gera IDs sequenciais
    ├── RateLimiterService.java       ← limita requisições
    └── UrlShortenerService.java      ← lógica principal
```

---

## Onde os dados ficam guardados

| Lugar       | O que guarda                          | Permanente? |
|-------------|---------------------------------------|-------------|
| **Postgres** | Todas as URLs (short → long)         | Sim         |
| **Redis**    | Cache das URLs mais acessadas         | Não (1 hora)|

O Postgres é a fonte da verdade.  
O Redis só deixa o redirecionamento mais rápido.

---

## Teste de carga (opcional)

O caminho de redirecionamento (`GET /{codigo}`) foi feito para aguentar bastante tráfego.

Exemplo com `wrk`:

```bash
wrk -t12 -c400 -d30s http://localhost:8080/CODIGO
```

Em uma máquina normal dá para chegar em alguns milhares de requisições por segundo.

---

## Possíveis melhorias (se sobrar tempo)

- Gerar IDs de forma distribuída (Snowflake)
- Rate limiter mais sofisticado (token bucket)
- Métricas com Prometheus
- Permitir códigos personalizados
- Expiração automática de URLs antigas

---

## Observações de estudante

Esse projeto não é "produção perfeita". Ele é feito para aprender os conceitos principais:

- Cache-aside pattern
- Geração de IDs + Base62
- Rate limiting simples
- Separação entre caminho de escrita e caminho de leitura

Foi divertido fazer e ajuda bastante a entender como sistemas reais funcionam.

---

Feito como projeto de estudo.