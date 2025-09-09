FROM mysql:8.0.37

ENV MYSQL_ROOT_PASSWORD=discord

ENV MYSQL_DATABASE=DISCORD

EXPOSE 3306

ENTRYPOINT ["docker-entrypoint.sh", "mysqld", "--ft_min_word_len=2"]