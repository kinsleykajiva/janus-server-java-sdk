document.addEventListener('DOMContentLoaded', function() {
    const eventsTableBody = document.getElementById('eventsTableBody');
    const clearCacheBtn = document.getElementById('clearCacheBtn');
    const ctx = document.getElementById('eventsChart').getContext('2d');
    let eventsChart;

    function fetchEvents() {
        fetch('/api/events')
            .then(response => response.json())
            .then(data => {
                renderTable(data);
                renderChart(data);
            })
            .catch(error => console.error('Error fetching events:', error));
    }

    function renderTable(events) {
        eventsTableBody.innerHTML = '';
        if (events.length === 0) {
            eventsTableBody.innerHTML = '<tr><td colspan="3" class="text-center">No events found.</td></tr>';
            return;
        }
        events.forEach(event => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${event.id}</td>
                <td>${event.timestamp}</td>
                <td><pre>${JSON.stringify(event.event_data, null, 2)}</pre></td>
            `;
            eventsTableBody.appendChild(row);
        });
    }

    function renderChart(events) {
        const labels = events.map(e => new Date(e.timestamp).toLocaleTimeString());
        const data = events.reduce((acc, event) => {
            const type = event.event_data.type;
            acc[type] = (acc[type] || 0) + 1;
            return acc;
        }, {});

        if (eventsChart) {
            eventsChart.destroy();
        }

        eventsChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Object.keys(data),
                datasets: [{
                    label: '# of Events',
                    data: Object.values(data),
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }


    clearCacheBtn.addEventListener('click', function() {
        if (!confirm('Are you sure you want to clear the cache?')) {
            return;
        }
        fetch('/api/events/clear', { method: 'POST' })
            .then(response => response.json())
            .then(data => {
                console.log(data.message);
                fetchEvents();
            })
            .catch(error => console.error('Error clearing cache:', error));
    });

    fetchEvents();
    setInterval(fetchEvents, 5000); // Refresh every 5 seconds
});
