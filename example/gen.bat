@echo off
set path=%cd%\src\main\thrift\
set ext=*.thrift 
set gen=src\main\gen
rmdir /s/q %gen%
md %gen%
for /f %%i in ('dir /b/a-d/s %path%\%ext%') do (
	thrift-0.9.3.exe -gen java:private-members -out %gen% %%i
)