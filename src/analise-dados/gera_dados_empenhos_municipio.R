#!/usr/bin/env Rscript

# You also need either RMySQL or RPostgreSQL libraries installed,
# depending on which DB you will use
# 
# Reminder:
# - PostgreSQL default port: 5432
# - MySQL default port: 3306

library(argparser)
library(dplyr)

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

DefineAnoEleicaoPrefeito <- function(ano, ano_inicio = 1996, ano_fim = 2020, periodo_anos = 4) {
  anos_eleicoes <- seq(ano_inicio, ano_fim, periodo_anos)
  ano_eleicao <- sapply(ano, function(x) ifelse(is.na(x), NA,
                                                anos_eleicoes[which.min(x > anos_eleicoes) - 1]))
  if (length(ano_eleicao) == 0) {
    ano_eleicao <- NA
  }
  return(ano_eleicao)
}

GeraDadosEmpenhosPorMunicipio <- function(db_host, db_port, db_user = NULL, db_password = NULL,
                                          db_name = "SAGRES", db_tool = "postgres") {
  if (tolower(db_tool) == "mysql") {
    require(RMySQL)
    sagres_db <- src_mysql(dbname = db_name, host = db_host, port = db_port, user = db_user,
                           password = db_password)  
  } else {
    require(RPostgreSQL)
    sagres_db <- src_postgres(dbname = db_name, host = db_host, port = db_port, user = db_user,
                           password = db_password)
  }
  
  dbGetQuery(sagres_db$con, "set names utf8")
  
  fornecedores <- tbl(sagres_db, "fornecedores")
  codigo_ugestora <- tbl(sagres_db, "codigo_ugestora")
  contratos_db <- tbl(sagres_db, "contratos")
  licitacao_db <- tbl(sagres_db, "licitacao")
  propostas_db <- tbl(sagres_db, "propostas")
  empenhos_db <- tbl(sagres_db, "empenhos")
  credores_db <- tbl(sagres_db, "credores")
  tipo_modalidade_licitacao <- tbl(sagres_db, "tipo_modalidade_licitacao") %>% collect(n = Inf)
  
  municipio_fornecedores <- read.csv("../../dados/cnpj_cep_endereco_pb.csv", sep = ";", encoding = "utf8") %>%
    filter(estado == "PB", !is.na(ibge)) %>%
    select(cnpj, codigo_municipio_fornecedor = ibge) %>%
    distinct()
  
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
    filter(cd_Ibge != 0) %>%
    group_by(cd_Ugestora, cd_Ibge) %>%
    summarise(nome_ugestora = toupper(first(de_Ugestora))) %>%
    mutate(COD_MUNICIPIO = as.numeric(substr(cd_Ibge, 1, nchar(cd_Ibge) - 2))) %>%
    left_join(codigo_municipios) 
  
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
    group_by(nu_CPFCNPJ, nome_fornecedor, codigo_municipio_fornecedor, COD_MUNICIPIO,
             nome_municipio, ano_eleicao) %>%
    summarise(qt_Empenhos = n(), vl_Empenhos = sum(vl_Empenho)) %>%
    merge(res_prefeitos_pb, by = c("nome_municipio", "ano_eleicao")) %>%
    ungroup() %>%
    select(nu_CPFCNPJ, nome_fornecedor, codigo_municipio_fornecedor, COD_MUNICIPIO, ano_eleicao,
           qt_Empenhos, vl_Empenhos, sigla_partido)
  
  return(empenhos_stats_municipio)
}

Main <- function(argv = NULL) {
  
  opts <- arg_parser("Opcoes para extrair dados de empenhos da PB em CSV")
  
  opts <- add_argument(opts, "--db-host",
                       help = "Endereço de host do servidor de BD com os dados de entrada (SAGRES)",
                       default = "localhost", short = "-h")
  
  opts <- add_argument(opts, "--db-port",
                       help = "Porta do servidor de BD com os dados de entrada (SAGRES)",
                       default = 5432, short = "-p")
  
  opts <- add_argument(opts, "--db-user",
                       help = "Nome do usuario de acesso ao BD com os dados de entrada",
                       default = "ubuntu", short = "-u")
  
  opts <- add_argument(opts, "--db-password",
                       help = "Senha do servidor de BD com os dados de entrada",
                       short = "-s")
  
  opts <- add_argument(opts, "--db-name",
                       help = "Nome do BD com os dados de entrada",
                       default = "SAGRES", short = "-n")
  
  opts <- add_argument(opts, "--db-tool",
                       help = "Nome da ferramenta de SGBD onde estao os dados (postgres ou mysql)",
                       default = "postgres", short = "-t")
  
  opts <- add_argument(opts, "--output-file",
                       help = "Path do arquivo CSV de saida com os resultados",
                       default = "../../dados/empenhos_por_municipio.csv", short = "-o")
  
  params <- parse_args(opts, argv)

  empenhos_stats_municipio <- with(params,
                                   GeraDadosEmpenhosPorMunicipio(db_host, db_port, db_user,
                                                                 db_password, db_name, db_tool))
  
  write.csv(empenhos_stats_municipio, params$output_file, row.names = F)
  return(summary(empenhos_stats_municipio))
}

argv <- commandArgs(TRUE)
Main(argv)
