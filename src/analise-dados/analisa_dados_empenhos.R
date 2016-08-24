
ReadDadosDoacoesCampanha <- function(file, encoding = "latin1") {
  receitas_partido <- read.csv("../../dados/receitas_candidatos_2012_PB.txt",
                               sep = ";", dec = ",", encoding = "latin1", header = T) %>%
    select(sigla_partido = Sigla..Partido, nu_CPFCNPJ = CPF.CNPJ.do.doador,
           valor_receita = Valor.receita) %>%
    mutate(valor_receita = as.numeric(valor_receita),
           nu_CPFCNPJ = as.character(nu_CPFCNPJ), ano_eleicao = 2012)
  
  receitas_partido <- read.csv("../../dados/receitas_comites_2012_PB.txt",
                               sep = ";", dec = ",", encoding = "latin1", header = T) %>%
    select(sigla_partido = Sigla..Partido, nu_CPFCNPJ = CPF.CNPJ.do.doador,
           valor_receita = Valor.receita) %>%
    mutate(valor_receita = as.numeric(valor_receita), nu_CPFCNPJ = as.character(nu_CPFCNPJ),
           ano_eleicao = 2012) %>%
    bind_rows(receitas_partido)
  
  receitas_partido <- read.csv("../../dados/receitas_partidos_2012_PB.txt", stringsAsFactors = F,
                               sep = ";", dec = ",", encoding = "latin1", header = T) %>%
    select(sigla_partido = Sigla..Partido, nu_CPFCNPJ = CPF.CNPJ.do.doador,
           valor_receita = Valor.receita) %>%
    mutate(valor_receita = as.numeric(valor_receita), nu_CPFCNPJ = as.character(nu_CPFCNPJ),
           ano_eleicao = 2012) %>%
    bind_rows(receitas_partido)
  
  receitas_partido <- read.csv("../../dados/receitas_candidatos_2008_PB.csv",
                               sep = ";", dec = ",", encoding = "latin1", header = T) %>%
    select(sigla_partido = SG_PARTIDO, nu_CPFCNPJ = CD_CPF_CNPJ_DOADOR,
           valor_receita = VR_RECEITA) %>%
    mutate(valor_receita = as.numeric(valor_receita),
           nu_CPFCNPJ = as.character(nu_CPFCNPJ), ano_eleicao = 2008) %>%
    bind_rows(receitas_partido)
  
  receitas_partido <- read.csv("../../dados/receitas_comites_2008_PB.csv",
                               sep = ";", dec = ",", encoding = "latin1", header = T) %>%
    select(sigla_partido = SG_PARTIDO, nu_CPFCNPJ = CD_CPF_CNPJ_DOADOR,
           valor_receita = VR_RECEITA) %>%
    mutate(valor_receita = as.numeric(valor_receita),
           nu_CPFCNPJ = as.character(nu_CPFCNPJ), ano_eleicao = 2008) %>%
    bind_rows(receitas_partido)
  
  receitas_partido <- receitas_partido %>%
    group_by(ano_eleicao, sigla_partido, nu_CPFCNPJ) %>%
    summarise(valor_doado = sum(valor_receita))
  
  return(receitas_partido)
}

empenhos_municipio_csv <- "../../dados/empenhos_por_municipio.csv"

empenhos_stats_municipio <- read.csv(empenhos_municipio_csv, header = T)

receitas_partido <- ReadDadosDoacoesCampanha()

empenhos_stats_partido <- empenhos_stats_municipio %>%
  filter(!is.na(sigla_partido)) %>%
  group_by(ano_eleicao, nu_CPFCNPJ, sigla_partido) %>%
  summarise(nome_fornecedor = first(nome_fornecedor),
            qt_Empenhos_partido = sum(qt_Empenhos),
            vl_Empenhos_partido = sum(vl_Empenhos),
            qt_Municipios_partido = n()) %>%
  group_by(ano_eleicao, nu_CPFCNPJ) %>%
  mutate(prop_qt_Empenhos_partido = qt_Empenhos_partido / sum(qt_Empenhos_partido),
         prop_vl_Empenhos_partido = vl_Empenhos_partido / sum(vl_Empenhos_partido),
         prop_qt_Municipios_partido = qt_Municipios_partido / sum(qt_Municipios_partido)) %>%
  arrange(desc(prop_qt_Empenhos_partido))


empenho_stats_partido_doacoes <- empenhos_stats_partido %>%
  left_join(receitas_partido,
            by = c("ano_eleicao", "nu_CPFCNPJ", "sigla_partido"))

empenho_stats_fornecedores <- empenho_stats_partido_doacoes %>%
  group_by(ano_eleicao, nu_CPFCNPJ, nome_fornecedor) %>%
  summarise(qt_Empenhos_fornecedor = sum(qt_Empenhos_partido),
            vl_Empenhos_fornecedor = sum(vl_Empenhos_partido),
            qt_Municipios_fornecedor = sum(qt_Municipios_partido),
            valor_doado = sum(valor_doado, na.rm = T)) %>%
  arrange(desc(valor_doado))

empenhos_fornecedores_com_doacoes <- filter(empenho_stats_partido_doacoes, !is.na(valor_doado)) %>%
  arrange(desc(valor_doado))

empenhos_stats_agg <- empenhos_stats_ugestora %>%
  group_by(ano_eleicao, de_TipoLicitacao, nu_CPFCNPJ, nome_fornecedor) %>%
  summarise(qt_Empenho_empresa = n(),
            vl_Empenho_empresa = sum(vl_Empenho)) %>%
  group_by(ano_eleicao, de_TipoLicitacao) %>%
  mutate(prop_qt_Empenho_empresa = qt_Empenho_empresa / sum(qt_Empenho_empresa),
         prop_vl_Empenho_empresa = vl_Empenho_empresa / sum(vl_Empenho_empresa)) %>%
  arrange(desc(qt_Empenho_empresa))
