#!/bin/bash

# Usage: ./start_rtp_stream.sh <port> <sdp_file>
#./start_rtp_stream.sh 5004 /path/to/sdp_file.sdp
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <port> <sdp_file>"
    exit 1
fi

PORT=$1
SDP_FILE=$2

# Construct FFmpeg command with SDP information
FFMPEG_COMMAND="/usr/bin/ffmpeg -protocol_whitelist file,crypto,udp,rtp -i $SDP_FILE -acodec opus -f rtp rtp://127.0.0.1:$PORT"

# Execute FFmpeg command
$FFMPEG_COMMAND
