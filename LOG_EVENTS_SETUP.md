# Log Events - Setup e Testes

## Estrutura Criada

### 1. Model e Entity
- **LogEvent** (`model/LogEvent.kt`) - Modelo de dados com os campos:
  - `traceId`, `serviceName`, `timestamp`, `level`, `message`, `exception`, `metadata`
- **LogEventEntity** (`entity/LogEventEntity.kt`) - Entidade JPA com índices otimizados
- **LogLevel** - Enum com níveis: TRACE, DEBUG, INFO, WARN, ERROR, FATAL

### 2. Repository e Service
- **LogEventRepository** (`repository/LogEventRepository.kt`) - Queries customizadas
- **LogEventService** (`service/LogEventService.kt`) - Lógica de persistência

### 3. Stream Processor
- **LogStreamProcessor** (`stream/LogStreamProcessor.kt`) - Consumidor Kafka
- Tópico: `log-events`
- Configuração: `KafkaStreamsConfig` atualizada com `logEventSerde`

## Como Testar

### 1. Iniciar a infraestrutura
```bash
docker-compose up -d
```

### 2. Criar o tópico de logs
```bash
docker exec -it microtrack-kafka kafka-topics --create \
  --topic log-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 3. Verificar se o tópico foi criado
```bash
docker exec -it microtrack-kafka kafka-topics --list \
  --bootstrap-server localhost:9092
```

### 4. Iniciar a aplicação
```bash
./gradlew bootRun
```

### 5. Produzir mensagens de teste

#### Exemplo 1: Log de INFO
```bash
docker exec -it microtrack-kafka kafka-console-producer \
  --topic log-events \
  --bootstrap-server localhost:9092 \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Cole a mensagem (key:value):
```
550e8400-e29b-41d4-a716-446655440000:{"traceId":"550e8400-e29b-41d4-a716-446655440000","serviceName":"user-service","timestamp":"2026-06-12T14:00:00Z","level":"INFO","message":"User logged in successfully","exception":null,"metadata":{"userId":"user-456","ip":"192.168.1.1"}}
```

#### Exemplo 2: Log de ERROR com exception
```
660e8400-e29b-41d4-a716-446655440001:{"traceId":"660e8400-e29b-41d4-a716-446655440001","serviceName":"payment-service","timestamp":"2026-06-12T14:05:00Z","level":"ERROR","message":"Payment processing failed","exception":"java.lang.NullPointerException: Payment gateway returned null\n\tat com.example.PaymentService.process(PaymentService.java:42)","metadata":{"orderId":"order-789","amount":150.00}}
```

#### Exemplo 3: Log de WARN
```
770e8400-e29b-41d4-a716-446655440002:{"traceId":"770e8400-e29b-41d4-a716-446655440002","serviceName":"inventory-service","timestamp":"2026-06-12T14:10:00Z","level":"WARN","message":"Low stock detected","exception":null,"metadata":{"productId":"prod-321","currentStock":5,"threshold":10}}
```

### 6. Verificar os logs no banco de dados

Conectar ao PostgreSQL:
```bash
docker exec -it microtrack-db psql -U microtrack_user -d microtrack
```

Queries úteis:
```sql
-- Ver todos os logs
SELECT * FROM log_events ORDER BY timestamp DESC;

-- Ver logs por nível
SELECT level, COUNT(*) FROM log_events GROUP BY level;

-- Ver logs de um trace específico
SELECT * FROM log_events WHERE trace_id = '550e8400-e29b-41d4-a716-446655440000';

-- Ver logs com erro
SELECT * FROM log_events WHERE level = 'ERROR';

-- Ver logs com metadata
SELECT trace_id, service_name, message, metadata FROM log_events WHERE metadata IS NOT NULL;
```

### 7. Visualizar no Redpanda Console

Acesse: http://localhost:8081

- Navegue até "Topics" → "log-events"
- Visualize as mensagens consumidas
- Monitore o lag do consumer group

## Estrutura da Tabela

```sql
CREATE TABLE log_events (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    exception TEXT,
    metadata JSONB
);

CREATE INDEX idx_log_trace_id ON log_events(trace_id);
CREATE INDEX idx_log_service_time ON log_events(service_name, timestamp);
CREATE INDEX idx_log_level ON log_events(level);
```

## Próximos Passos (Opcional)

1. **Agregação de logs por nível**: Criar métricas de quantos logs de cada nível por serviço
2. **Alertas**: Implementar alertas quando houver muitos logs de ERROR
3. **Correlação**: Relacionar logs com trace events pelo `traceId`
4. **Retenção**: Implementar política de retenção de logs antigos
