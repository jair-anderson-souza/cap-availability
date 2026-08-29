# CAP Theorem: A Escolha que Ninguém Quer Fazer

Quando a rede falha, você escolhe: dados corretos ou sistema respondendo? Não dá pra ambos.

Optei por **disponibilidade + consistência eventual**. Cache reduz latência, PostgreSQL converge os dados depois. É e-commerce, não banco.

HPA no Kubernetes: 2→6 replicas quando CPU > 70%. Pod falha? Outro sobe automaticamente. Zero código extra.

Gatling prova onde quebra: milhares de requisições reais mostram seus limites verdadeiros.

**Lição:** Alta disponibilidade é arquitetura inteligente, não código bonito.

#CAPTheorem #Kubernetes #Gatling
