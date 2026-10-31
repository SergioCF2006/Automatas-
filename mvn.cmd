@echo off
set "NB_MAVEN=C:\Program Files\NetBeans-25\netbeans\java\maven\bin\mvn.cmd"
if exist "%NB_MAVEN%" (
    "%NB_MAVEN%" %*
) else (
    mvn %*
)
