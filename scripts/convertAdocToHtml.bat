
echo -n "Convert %2 to html: "

set DOCS_INPUT_PATH=%1
set ADOC_FILE=%2
set DOC_DESTINATION_PATH=%3

set REPO_GITHUB=https://github.com/sfauvel/documentationtesting

REM Styles come from : https://github.com/darshandsoni/asciidoctor-skins
REM set STYLE=adoc-readthedocs.css
REM set STYLE=adoc-rocket-panda.css
set STYLE=asciidoctor.css

set CURRENT_PATH=%~dp0
set PROJECT_PATH=%cd%
pushd %CURRENT_PATH%\..
set ROOT_PATH=%cd%
popd

set STYLESHEETS=%ROOT_PATH%\stylesheets

set DOCKER_IMAGE=asciidoctor/docker-asciidoctor:1.1.0
set DOCKER_WORKDIR=/documents

docker run -it ^
    -v %PROJECT_PATH%\%DOCS_INPUT_PATH%:%DOCKER_WORKDIR% ^
    -v %DOC_DESTINATION_PATH%:/destination ^
    -v %STYLESHEETS%:/stylesheets:ro ^
    -w %DOCKER_WORKDIR% ^
    %DOCKER_IMAGE% ^
    asciidoctor ^
    -D /destination ^
    -o index.html ^
    -r asciidoctor-diagram ^
    -a sourcedir=%DOCKER_WORKDIR%/src/main/java ^
    -a webfonts! ^
    -a stylesheet=/stylesheets/%STYLE% ^
    --attribute htmlOutput="html" ^
    --attribute github="%REPO_GITHUB%/tree/master" ^
    --attribute rootpath="%DOCKER_WORKDIR%" ^
    %ADOC_FILE%
