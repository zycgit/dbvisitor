@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions DisableDelayedExpansion

set "DIRNAME=%~dp0"
if not defined DIRNAME set "DIRNAME=."
@rem This is normally unused
set "APP_BASE_NAME=%~n0"
set "APP_HOME=%DIRNAME%"

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set "APP_HOME=%%~fi"

@rem Keep launcher temporary files inside the repository.
if not exist "%APP_HOME%\.gradle\tmp" (
    mkdir "%APP_HOME%\.gradle\tmp"
    if errorlevel 1 exit /b 1
)
set "TMP=%APP_HOME%\.gradle\tmp"
set "TEMP=%TMP%"
set "TMPDIR=%TMP%"

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set "JAVA_EXE=java.exe"
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

exit /b 1

:findJavaFromJavaHome
set "JAVA_HOME=%JAVA_HOME:"=%"
set "JAVA_EXE=%JAVA_HOME%/bin/java.exe"

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: "%JAVA_HOME%" 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

exit /b 1

:execute
@rem Execute Gradle
@rem endlocal doesn't take effect until after the line is parsed and variables are expanded
@rem which allows us to clear the local environment before executing the java command
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Djava.io.tmpdir=%TMP%" "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %GRADLE_EXIT_CODE%
