@echo off
cls
del /Q/F/S .\~Release\*.*
Call gradlew clean curseforge