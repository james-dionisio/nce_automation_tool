@echo off
call taskkill /f /im phantomjs.exe
call taskkill /f /im phantomjs.exe
call taskkill /f /im chromedriver.exe
call taskkill /f /im geckodriver.exe
call taskkill /f /im MicrosoftWebDriver.exe
call taskkill /f /im IEDriverServer.exe 
pause 

@echo off

start cmd.exe /k "java -jar create.jar "1""

start cmd.exe /k "java -jar create.jar "2""

start cmd.exe /k "java -jar create.jar "3""
