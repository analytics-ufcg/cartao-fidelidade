sudo kill -9 `ps aux | grep -e java -e cartao-fidelidade | grep -v grep | awk '{ print $2 }'`
