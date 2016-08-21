library(dplyr)

canditatos = c()

for (ano in c(2008, 2012)) {
  
  df_candidato            = read.csv(file = paste("../../dados/consulta_cand_", ano, "_PB.txt", sep = ""), sep = ";", header = F, encoding = "latin1", row.names = NULL, stringsAsFactors = FALSE)
  df_candidato            = df_candidato[ , c(3, 10, 11, 15, 19, 20, 23, 43)]
  colnames(df_candidato)  = c("ano_eleicao", "cargo", "nome", "nome_fantasia", "parido", "partido_extenso", "coligacao", "resultado")

  df_candidato  = df_candidato %>% filter(resultado == "ELEITO")
  canditatos    = rbind(canditatos, df_candidato)
  
}

head(canditatos)
unique(canditatos$coligacao)