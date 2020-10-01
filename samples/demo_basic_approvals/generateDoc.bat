
mvn clean install package -q

set CURRENT_PATH=%~dp0
pushd ..\..
set ROOT_PATH=%cd%
popd
set MYDIR1=%CURRENT_PATH:~0,-1%
for %%f in (%MYDIR1%) do set PROJECT_NAME=%%~nxf

set DOCS_INPUT_PATH=src\test\docs
set DOC_DESTINATION_PATH=%ROOT_PATH%\docs\%PROJECT_NAME%

call %ROOT_PATH%\scripts\convertAdocToHtml.bat %DOCS_INPUT_PATH% Documentation.adoc %DOC_DESTINATION_PATH%