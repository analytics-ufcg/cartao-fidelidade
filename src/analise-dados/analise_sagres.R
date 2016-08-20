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
  municipios_match <- municipios[which(sapply(municipios,
                                          function(x) grepl(x, ugestora, fixed = T)))]
  # Se houver mais de um municipio, pegar o de maior nome
  municipio <- municipios_match[which.max(nchar(municipios_match))]
  municipio <- ifelse(length(municipio) == 0, NA, municipio)
  return(municipio)
}

sagres_bd <- src_mysql("SAGRES", "localhost", 3306, password = "123456")

# Se der problema de encoding, rodar comando:
# - dbGetQuery(sagres_bd$con, "set names utf8")

fornecedores <- tbl(sagres_bd, "fornecedores")
codigo_ugestora <- tbl(sagres_bd, "codigo_ugestora")
contratos <- tbl(sagres_bd, "contratos")
tipo_modalidade_licitacao <- tbl(sagres_bd, "tipo_modalidade_licitacao")
licitacao_db <- tbl(sagres_bd, "licitacao")
liquidacao_db <- tbl(sagres_bd, "liquidacao")
propostas_db <- tbl(sagres_bd, "propostas")

write.csv(collect(licitacao_db, n = Inf), "/tmp/licitacao.csv", col.names = T, row.names = F)
write.csv(collect(liquidacao_db, n = Inf), "/tmp/liquidacao.csv", col.names = T, row.names = F)

codigo_municipios <- read.csv("../../dados/codigo_municipios.csv") %>%
                     select(COD_MUNICIPIO, NOME_MUNICIPIO, NOME_MESO, NOME_MICRO) %>%
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
  summarise(nome_fornecedor = trimws(first(no_Fornecedor)))
  
nomes_ugestora <- codigo_ugestora %>%
  collect(n = Inf) %>%
  group_by(cd_Ugestora, cd_Ibge) %>%
  summarise(nome_ugestora = toupper(first(de_Ugestora))) %>%
  mutate(COD_MUNICIPIO = as.numeric(substr(cd_Ibge, 1, nchar(cd_Ibge) - 2))) %>%
  full_join(codigo_municipios) %>%
  mutate(nome_municipio = toupper(NOME_MUNICIPIO))

propostas <- propostas_db %>%
  filter(st_Proposta == 1, !is.na(nu_CPFCNPJ)) %>%
  select(-cd_Item, -cd_SubGrupoItem, -cd_UGestoraItem, -dt_Ano, -dt_MesAno) %>%
  collect(n = Inf)

licitacao <- licitacao_db %>%
  filter(!is.na(nu_Propostas)) %>%
  select(-registroCGE) %>%
  collect(n = Inf) %>%
  #TODO:melhorar essa funcao
  mutate(ano_eleicao = ifelse(dt_Ano > 2012, 2012, ifelse(dt_Ano > 2008, 2008, NA))) %>%
  filter(!is.na(ano_eleicao))
    
licitacao_stats <- licitacao %>%
  full_join(propostas, by = c("nu_Licitacao", "tp_Licitacao", "cd_UGestora")) %>%
  full_join(nomes_ugestora, by = c("cd_UGestora" = "cd_Ugestora")) %>%
  # Agregando unidades gestoras de um municipio.
  group_by(COD_MUNICIPIO, nome_municipio, NOME_MESO, NOME_MICRO, ano_eleicao,
           nu_Licitacao, tp_Licitacao, nu_Propostas, vl_Licitacao, dt_Ano, dt_MesAno,
           dt_Homologacao, tp_Objeto, de_Obs, tp_regimeExecucao, nu_CPFCNPJ) %>%
  summarise(vl_Ofertado_soma = sum(vl_Ofertado), vl_Ofertado_first = first(vl_Ofertado),
            qt_Proposta = n()) %>%
  merge(res_prefeitos_pb, by = c("nome_municipio", "ano_eleicao")) %>%
  as_data_frame()

write.csv(licitacao_stats, "../../dados/licitacao_empresa_partido.csv", col.names = T,
          row.names = F)

# Codigo antigo abaixo. Pode não funcionar mais.
contratos_stats <- contratos %>%
  filter(nu_CPFCNPJ != "") %>%
  collect(n = Inf) %>%
  mutate(ano_eleicao = ifelse(dt_Ano > 2012, 2012, ifelse(dt_Ano > 2008, 2008, NA))) %>%
  group_by(nu_CPFCNPJ, ano_eleicao, cd_UGestora) %>%
  summarise(n_contratos=n(), valor_contratos = sum(vl_TotalContrato))

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
