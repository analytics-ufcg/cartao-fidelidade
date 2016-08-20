library(dplyr)

canditatos = c()

for (ano in c(2008, 2012)) {
  
  df_candidato  = read.csv(file = "../../dados/consulta_cand_2008_PB.txt", sep = ";", header = F, encoding = "latin1")

  tail(df_candidato)

  summary(df_candidato$V20)
  
  
  df_candidato  = read.csv(file = "../../dados/consulta_legendas_2008_PB.txt", sep = ";", header = F, encoding = "latin1")

    
  df_candidato  = df_candidato[ , c(3, 10, 11, 15, 19, 20, 23, 43)]
  colnames(df_candidato) = c("ano_eleicao", "cargo", "nome", "nome_fantasia", "parido", "partido_extenso", "coligacao", "resultado")
    
  df_candidato  = df_candidato %>% filter(resultado == "ELEITO")
  df_candidato$coligacao[df_candidato$coligacao == "#NE#"] = NA
  
  canditatos    = rbind(canditatos, df_candidato)
}

summary(canditatos$coligacao)
