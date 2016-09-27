#!/usr/bin/env Rscript

library(dplyr)

municipios <- read.csv("../../dados/codigo_municipios.csv") %>%
  select(COD_MUNICIPIO, NOME_MUNICIPIO) %>%
  unique() %>%
  arrange(NOME_MUNICIPIO)

colnames(municipios) <- c("cod", "nome")

write.csv(municipios, file = "../../dados/dados_municipios.csv", row.names = FALSE)