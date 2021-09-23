FROM webdevops/liquibase:postgres

COPY liquibase-changelog.sql .

ENV LIQUIBASE_CHANGELOG=liquibase-changelog.sql

CMD ["liquibase", "update"]
