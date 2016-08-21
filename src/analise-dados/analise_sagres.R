library(dplyr)
library(RMySQL)

ReadDadosEleicoesMunicipais <- function(file, encoding = "latin1") {
  read.csv(file, header = F, encoding = encoding, sep = ";", stringsAsFactors = F,
           col.names = c("data_geracao", "hora_geracao", "ano_eleicao", "num_turno",
                         "descricao_eleicao", "sigla_uf", "sigla_ue", "codigo_municipio",
                         "nome_municipio", "numero_zona", "codigo_cargo", "numero_cand",
                         "sq_candidato", "nome_candidato", "nome_urna_candidato",
                         "descricao_cargo","cod_sit_cand_superior", "desc_sit_cand_superior",
                         "codigo_sit_candidato", "desc_sit_candidato", "codigo_sit_cand_tot",
                         "desc_sit_cand_tot", "numero_partido", "sigla_partido", "nome_partido",
                         "sequencial_legenda", "nome_coligacao", "composicao_legenda",
                         "total_votos"))
}

ExtraiMunicipioDeUnidadeGestora <- function(municipios, ugestora) {
  nome_municipios <- c()
  for (ug in ugestora) {
    municipios_match <- municipios[which(sapply(municipios, function(x) grepl(x, ug, fixed = T)))]
    # Se houver mais de um municipio, pegar o de maior nome
    municipio <- municipios_match[which.max(nchar(municipios_match))]
    nome_municipios <- c(nome_municipios, ifelse(length(municipio) == 0, NA, municipio))
  }
  return(nome_municipios)
}

ExtraiCodigoDeNomeDoMunicipio <- function(codigo_municipios, nomes_municipios) {
  codigos <- c()
  for (nome in nomes_municipios) {
    cod <- ifelse(is.na(nome), NA,
                  codigo_municipios$COD_MUNICIPIO[codigo_municipios$nome_municipio == nome])
    codigos <- c(codigos, cod)
  }
  return(codigos)
}

DefineAnoEleicaoPrefeito <- function(ano, ano_inicio = 1996, ano_fim = 2020, periodo_anos = 4) {
  anos_eleicoes <- seq(ano_inicio, ano_fim, periodo_anos)
  ano_eleicao <- sapply(ano, function(x) ifelse(is.na(x), NA,
                                                anos_eleicoes[which.min(x > anos_eleicoes) - 1]))
  if (length(ano_eleicao) == 0) {
    ano_eleicao <- NA
  }
  return(ano_eleicao)
}

sagres_bd <- src_mysql("SAGRES", "localhost", 3306, password = "123456")
dbGetQuery(sagres_bd$con, "set names utf8")

fornecedores <- tbl(sagres_bd, "fornecedores")
codigo_ugestora <- tbl(sagres_bd, "codigo_ugestora")
contratos_db <- tbl(sagres_bd, "contratos")
licitacao_db <- tbl(sagres_bd, "licitacao")
propostas_db <- tbl(sagres_bd, "propostas")
empenhos_db <- tbl(sagres_bd, "empenhos")
credores_db <- tbl(sagres_bd, "credores")
tipo_modalidade_licitacao <- tbl(sagres_bd, "tipo_modalidade_licitacao") %>% collect(n = Inf)

municipio_fornecedores <- read.csv("../../dados/mrg_all.csv", sep = ";", encoding = "utf8") %>%
  filter(estado == "PB", !is.na(ibge)) %>%
  select(cnpj, codigo_municipio_fornecedor = ibge)

codigo_municipios <- read.csv("../../dados/codigo_municipios.csv") %>%
                     select(COD_MUNICIPIO, nome_municipio = NOME_MUNICIPIO) %>%
                     mutate(nome_municipio = toupper(as.character(nome_municipio))) %>%
                     distinct()
                  
eleicoes_pb_2008_file <- "../../dados/votacao_candidato_munzona_2008_PB.txt"
eleicoes_pb_2012_file <- "../../dados/votacao_candidato_munzona_2012_PB.txt"

res_prefeitos_pb <- ReadDadosEleicoesMunicipais(eleicoes_pb_2008_file) %>%
                    bind_rows(ReadDadosEleicoesMunicipais(eleicoes_pb_2012_file)) %>%
                    filter(descricao_cargo == "PREFEITO", desc_sit_cand_tot == "ELEITO") %>%
                    group_by(ano_eleicao, nome_municipio, nome_candidato, sigla_partido) %>%
                    distinct()

eleitos_por_partido <- res_prefeitos_pb %>%
                       group_by(ano_eleicao, sigla_partido) %>%
                       summarise(n_eleitos_partido = n()) %>%
                       mutate(prop_eleitos_partido = n_eleitos_partido / sum(n_eleitos_partido)) %>%
                       arrange(desc(ano_eleicao), desc(n_eleitos_partido))

municipios_pb <- unique(res_prefeitos_pb$nome_municipio)

nomes_fornecedores <- fornecedores %>%
  collect(n = Inf) %>%
  group_by(nu_CPFCNPJ) %>%
  summarise(nome_fornecedor = trimws(first(no_Fornecedor))) %>%
  left_join(municipio_fornecedores, by = c("nu_CPFCNPJ" = "cnpj"))
  
nomes_ugestora <- codigo_ugestora %>%
  collect(n = Inf) %>%
  group_by(cd_Ugestora, cd_Ibge) %>%
  summarise(nome_ugestora = toupper(first(de_Ugestora))) %>%
  mutate(COD_MUNICIPIO = as.numeric(substr(cd_Ibge, 1, nchar(cd_Ibge) - 2))) %>%
  left_join(codigo_municipios) %>%
  ungroup() %>%
  mutate(nome_municipio = as.character(ifelse(is.na(nome_municipio),
                                 ExtraiMunicipioDeUnidadeGestora(municipios_pb, nome_ugestora),
                                 nome_municipio))) %>%
  mutate(COD_MUNICIPIO = ifelse(is.na(COD_MUNICIPIO),
                                ExtraiCodigoDeNomeDoMunicipio(codigo_municipios, nome_municipio),
                                COD_MUNICIPIO))

empenhos <- empenhos_db %>%
            select(nu_Empenho, tp_Empenho, cd_UGestora, dt_Ano, dt_MesAno, tp_Licitacao,
                   nu_CPFCNPJ = cd_Credor, tp_Credor, vl_Empenho) %>%
            collect(n = Inf) %>%
            mutate(ano_eleicao = DefineAnoEleicaoPrefeito(dt_Ano))

empenhos_stats_ugestora <- empenhos %>%
                           left_join(nomes_fornecedores, by = "nu_CPFCNPJ") %>%
                           left_join(nomes_ugestora, by = c("cd_UGestora" = "cd_Ugestora")) %>%
                           left_join(tipo_modalidade_licitacao, by = "tp_Licitacao")

empenhos_stats_municipio <- empenhos_stats_ugestora %>%
                            filter(!is.na(COD_MUNICIPIO)) %>%
                            group_by(nu_CPFCNPJ, nome_fornecedor, codigo_municipio_fornecedor,
                                     COD_MUNICIPIO, nome_municipio, ano_eleicao) %>%
                            summarise(qt_Empenhos = n(), vl_Empenhos = sum(vl_Empenho)) %>%
                            left_join(res_prefeitos_pb, by = c("nome_municipio", "ano_eleicao")) %>%
                            ungroup() %>%
                            select(-nome_candidato, -nome_municipio)
                            
write.csv(empenhos_stats_municipio, "../../dados/empenhos_por_municipio.csv", row.names = F)

# Codigo antigo abaixo. Pode não funcionar mais.

licitacao_stats_por_partido <- licitacao_stats_all_municipio %>%
  group_by(ano_eleicao, nu_CPFCNPJ, nome_fornecedor, sigla_partido) %>%
  summarise(qt_Licitacoes = n(), qt_Ugestora = sum(qt_Ugestora),
            vl_Ofertado_soma = sum(vl_Ofertado_soma),
            vl_Ofertado_first = sum(vl_Ofertado_first)) %>%
  group_by(ano_eleicao, nu_CPFCNPJ, nome_fornecedor) %>%
  mutate(prop_Licitacoes = qt_Licitacoes / n()) %>%
  arrange(desc(qt_Licitacoes))

licitacao_contratos <- licitacao_stats_all %>%
  left_join(contratos_stats, by = c("nu_CPFCNPJ", "ano_eleicao", "nu_Licitacao", "tp_Licitacao",
                                    "cd_UGestora"))

contratos_stats_agg <- contratos_stats %>%
  group_by(nu_CPFCNPJ, ano_eleicao) %>%
  summarise(n_ugestora = n(), n_total_contratos=sum(n_contratos),
            valor_total_contratos = sum(valor_contratos)) %>%
  left_join(nomes_fornecedores, by = "nu_CPFCNPJ")

contratos_stats <- contratos_stats %>%
  full_join(contratos_stats_agg, by = c("nu_CPFCNPJ", "ano_eleicao")) %>%
  full_join(nomes_ugestora, by = c("cd_UGestora" = "cd_Ugestora")) %>%
  full_join(codigo_municipios, by = c("cd"))
  rowwise() %>%
  mutate(nome_municipio = ExtraiMunicipioDeUnidadeGestora(municipios_pb, nome_ugestora)) %>%
  left_join(res_prefeitos_pb, by = c("nome_municipio", "ano_eleicao"))

fornecedores_top_n_ugestora <- contratos_stats %>%
  top_n(100, n_ugestora)%>%
  arrange(desc(n_ugestora))

fornecedores_top_valor <- contratos_stats %>%
  top_n(100, valor_total_contratos)%>%
  arrange(desc(valor_total_contratos))

# TODO: Analise - Cruzar fornecedores que ganharam contratos em mais municipios com os partidos
# do governante desses municípios na epoca.
# Fornecedor x Quantidade de contratos x Porcentagem por partido do governante
# Fornecedor x Montante de contratos x Porcentagem por partido do governante

# TODO: ter dado completo de cada contrato
# TODO: ter ano do contrato
