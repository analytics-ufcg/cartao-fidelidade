csv_file="/home/ubuntu/cartao-fidelidade/dados/empenhos_por_municipio.csv"
sql_file="create_empenhos_por_municipio_from_csv.sql"
db_name="SAGRES"

sed "s|%CSV_FILE%|${csv_file}|g" $sql_file | psql $db_name
