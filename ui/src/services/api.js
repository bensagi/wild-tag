const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

/**
 * General function to make API calls.
 * @param {string} endpoint - API endpoint (e.g., '/wild-tag/users').
 * @param {string} method - HTTP method ('GET', 'POST', 'PUT', 'DELETE').
 * @param {object|null} body - Request body for POST/PUT requests.
 * @param {object} headers - Additional headers.
 * @param responseType
 * @returns {Promise<any>} - Resolves with response data or rejects with an error.
 */
async function apiCall(endpoint, method = "GET", body = null, headers = {}, responseType = "json") {
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
        console.log('Response status:', response.status);
        const rawText = await response.text();
        console.log('Raw response text:', rawText);

        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${rawText}`);
        }

        if (rawText.trim() === "" || response.status === 204) {
            return null; // Handle empty responses
        }

        if (responseType === "text") {
            return rawText;
        }

        return JSON.parse(rawText); // Avoid `response.json()` for debugging
    } catch (error) {
        console.error(`API call to ${url} failed:`, error);
        throw error;
    }
}

export default apiCall;
