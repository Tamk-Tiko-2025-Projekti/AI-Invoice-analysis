import {useState} from 'react';

function KillServer() {
    const [status, setStatus] = useState(null);

    const handleKillServer = async () => {
        if (!window.confirm('Are you sure you want to close the server?')) {
            return;
        }

        try {
            const response = await fetch("/shutdown", {
                method: "POST",
            });

            const text = await response.text();
            if (response.ok) {
                setStatus(text);
            } else {
                setStatus(`Error: ${text}`);
            }
        } catch (error) {
            setStatus(`Error: ${error.message}`);
        }
    }
    return (
        <div>
            <h2>Close server</h2>
            <button onClick={handleKillServer}>Close server</button>
            {status && <p>{status}</p>}
        </div>
    );
}
export default KillServer;
