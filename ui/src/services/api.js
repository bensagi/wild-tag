const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

/**
 * General function to make API calls.
 * @param {string} endpoint - API endpoint (e.g., '/wild-tag/users').
 * @param {string} method - HTTP method ('GET', 'POST', 'PUT', 'DELETE').
 * @param {object|null} body - Request body for POST/PUT requests.
 * @param {object} headers - Additional headers.
 * @returns {Promise<any>} - Resolves with response data or rejects with an error.
 */
async function apiCall(endpoint, method = "GET", body = null, headers = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            "Content-Type": "application/json",
            ...headers,
        },
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    console.log(`API Call: ${method} ${url}`, body);

    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error ${response.status}: ${errorText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`API call to ${url} failed:`, error);
        throw error;
    }
}

export default apiCall;
