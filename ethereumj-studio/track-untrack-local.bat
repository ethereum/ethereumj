rem *
rem * This file is used for local win developers to temporary
rem * disable tracking for property files
rem *

@echo off
:getConfirmation
set /p command=What to do with lame properties [track/untrack] ?:

if %command%==track goto :trackProps
if %command%==untrack goto :untrackProps
goto :end


rem # I use this to stop push every local propertie change
:untrackProps
@echo on
git update-index --assume-unchanged ./src/main/resources/log4j.properties
git update-index --assume-unchanged ./src/main/resources/system.properties

git update-index --assume-unchanged ./src/test/resources/log4j.properties
git update-index --assume-unchanged ./src/test/resources/system.properties
goto end

rem # If I want to get back to push this changes e.g. for some new propertie I run this
:trackProps
@echo on
git update-index --no-assume-unchanged ./src/main/resources/log4j.properties
git update-index --no-assume-unchanged ./src/main/resources/system.properties

git update-index --no-assume-unchanged ./src/test/resources/log4j.properties
git update-index --no-assume-unchanged ./src/test/resources/system.properties
goto end

:end