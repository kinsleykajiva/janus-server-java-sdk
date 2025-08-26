document.addEventListener('DOMContentLoaded', () => {
    const liveEventsLog = document.getElementById('live-events-log');

    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

    const token = getCookie('session_token');

    if (!token) {
        console.error('No session token found. Redirecting to login.');
        window.location.href = '/login.html';
        return;
    }

    if (liveEventsLog) {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${wsProtocol}//${window.location.host}/ws?token=${token}`;

        const socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            console.log('WebSocket connection established.');
            const logEntry = document.createElement('div');
            logEntry.textContent = 'Connection established. Waiting for events...';
            liveEventsLog.appendChild(logEntry);
        };

        socket.onmessage = (event) => {
            const logEntry = document.createElement('pre');
            try {
                const eventData = JSON.parse(event.data);
                 logEntry.textContent = JSON.stringify(eventData, null, 2);
            } catch (e) {
                 logEntry.textContent = event.data;
            }
            liveEventsLog.appendChild(logEntry);
            // Scroll to the bottom
            liveEventsLog.scrollTop = liveEventsLog.scrollHeight;
        };

        socket.onclose = (event) => {
            console.log('WebSocket connection closed.', event);
            const logEntry = document.createElement('div');
            logEntry.textContent = `Connection closed. Code: ${event.code}, Reason: ${event.reason}`;
            logEntry.style.color = 'red';
            liveEventsLog.appendChild(logEntry);
             if (event.code === 401) {
                window.location.href = '/login.html';
            }
        };

        socket.onerror = (error) => {
            console.error('WebSocket error:', error);
             const logEntry = document.createElement('div');
            logEntry.textContent = 'An error occurred with the WebSocket connection.';
            logEntry.style.color = 'red';
            liveEventsLog.appendChild(logEntry);
        };
    }
});
