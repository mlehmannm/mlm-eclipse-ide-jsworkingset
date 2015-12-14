@setlocal
@call mvn -e -V install -f %~dp0\releng\repository\pom.xml %*
