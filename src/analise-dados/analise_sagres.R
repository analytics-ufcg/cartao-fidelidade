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

eleicoes_pb_2008_file <- "../../dados/votacao_candidato_munzona_2008_PB.txt"
eleicoes_pb_2012_file <- "../../dados/votacao_candidato_munzona_2012_PB.txt"

res_prefeitos_pb <- ReadDadosEleicoesMunicipais(eleicoes_pb_2008_file) %>%
                    bind_rows(ReadDadosEleicoesMunicipais(eleicoes_pb_2012_file)) %>%
                    filter(descricao_cargo == "PREFEITO", desc_sit_cand_tot == "ELEITO") %>%
                    group_by(ano_eleicao, codigo_municipio, nome_municipio, nome_candidato,
                             sigla_partido) %>%
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
  group_by(cd_Ugestora) %>%
  summarise(nome_ugestora = toupper(first(de_Ugestora)))

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
  left_join(contratos_stats_agg, by = c("nu_CPFCNPJ", "ano_eleicao")) %>%
  left_join(nomes_ugestora, by = c("cd_UGestora" = "cd_Ugestora")) %>%
  rowwise() %>%
  mutate(nome_municipio = ExtraiMunicipioDeUnidadeGestora(municipios_pb, nome_ugestora)) %>%
  left_join(res_prefeitos_pb, by = c("nome_municipio", "ano_eleicao"))

fornecedores_top_n_ugestora <- contratos_stats %>%
  top_n(100, n_ugestora)%>%
  arrange(desc(n_ugestora))

fornecedores_top_valor <- fornecedores_stats %>%
  top_n(100, valor)%>%
  arrange(desc(valor))

# TODO: Analise - Cruzar fornecedores que ganharam contratos em mais municipios com os partidos
# do governante desses municípios na epoca.
# Fornecedor x Quantidade de contratos x Porcentagem por partido do governante
# Fornecedor x Montante de contratos x Porcentagem por partido do governante