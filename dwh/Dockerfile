FROM oracleinanutshell/oracle-xe-11g

ADD script.sql /docker-entrypoint-initdb.d/

ENV ORACLE_ALLOW_REMOTE=true
ENV ORACLE_CHARACTERSET=AL16UTF16
ENV NLS_LANG=.CL8MSWIN1251

EXPOSE 1521