cp ../database.sql liquibase-changelog.sql
docker build -t addis/addis-liquibase .
rm liquibase-changelog.sql