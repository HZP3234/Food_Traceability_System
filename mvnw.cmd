@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@echo off
setlocal enabledelayedexpansion

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist %WRAPPER_JAR% (
    echo Maven Wrapper JAR not found: %WRAPPER_JAR%
    exit /b 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8

if not defined JAVA_HOME (
    set JAVA_HOME=C:\Program Files\Java\jdk17
)

"%JAVA_HOME%\bin\java.exe" ^
    -classpath %WRAPPER_JAR% ^
    -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% ^
    -Dmaven.wrapper.properties="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" ^
    org.apache.maven.wrapper.MavenWrapperMain ^
    %*
