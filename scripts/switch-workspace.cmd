@echo off
setlocal
powershell -ExecutionPolicy Bypass -File "%~dp0switch-workspace.ps1" %*
