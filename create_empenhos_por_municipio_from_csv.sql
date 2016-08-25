DROP TABLE IF EXISTS EMPENHOS_POR_MUNICIPIO; 

CREATE TABLE EMPENHOS_POR_MUNICIPIO (
    nu_CPFCNPJ VARCHAR NOT NULL,
    nome_fornecedor VARCHAR NOT NULL,
    codigo_municipio_fornecedor VARCHAR NOT NULL,
    cod_municipio VARCHAR NOT NULL,
    ano_eleicao INT NOT NULL,
    qt_Empenhos INT NOT NULL,
    vl_Empenhos FLOAT NOT NULL,
    sigla_partido VARCHAR NOT NULL);

COPY EMPENHOS_POR_MUNICIPIO FROM '%CSV_FILE%' DELIMITER ',' CSV HEADER;
