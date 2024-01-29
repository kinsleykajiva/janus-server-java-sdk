@echo off

REM Usage: start_rtp_stream.bat <port> <sdp_file>

if "%~2"=="" (
    echo Usage: %0 <port> <sdp_file>
    exit /b 1
)

set PORT=%~1
set SDP_FILE=%~2

REM Construct FFmpeg command with SDP information
set FFMPEG_COMMAND=C:\ProgramData\chocolatey\bin\ffmpeg.exe -protocol_whitelist file,crypto,udp,rtp -i "%SDP_FILE%" -acodec opus -f rtp ^> "rtp://127.0.0.1:%PORT%"

REM Execute FFmpeg command
echo %FFMPEG_COMMAND%
%FFMPEG_COMMAND%
