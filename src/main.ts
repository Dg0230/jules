import axios from 'axios';

const GEMINI_API_URL_BASE = 'https://generativelanguage.googleapis.com/v1beta/models/';
// Use the specific model provided by the user
const MODEL_NAME = 'gemini-2.0-flash'; 

function getApiKey(): string {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
        console.error('Error: GEMINI_API_KEY environment variable is not set.');
        process.exit(1);
    }
    return apiKey;
}

interface GeminiResponse {
    candidates?: Array<{
        content?: {
            parts?: Array<{
                text?: string;
            }>;
            role?: string;
        };
    }>;
    // Add other properties if known, like promptFeedback
}

async function callGeminiApi(promptText: string, apiKey: string): Promise<string> {
    const apiUrl = `${GEMINI_API_URL_BASE}${MODEL_NAME}:generateContent?key=${apiKey}`;
    const requestBody = {
        contents: [
            {
                parts: [
                    {
                        text: promptText,
                    },
                ],
            },
        ],
    };

    try {
        const response = await axios.post<GeminiResponse>(apiUrl, requestBody, {
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.data?.candidates?.[0]?.content?.parts?.[0]?.text) {
            return response.data.candidates[0].content.parts[0].text;
        } else {
            // Log the actual response structure if it's not what we expect.
            console.error('Unexpected response structure from Gemini API:', JSON.stringify(response.data, null, 2));
            throw new Error('Failed to extract text from Gemini API response. Unexpected structure.');
        }
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            console.error(`Error calling Gemini API: ${error.response.status} ${error.response.statusText}`);
            console.error('Response data:', JSON.stringify(error.response.data, null, 2));
        } else {
            console.error('Error calling Gemini API:', error.message);
        }
        throw error; // Re-throw the error to be caught by the main execution block
    }
}

(async () => {
    const prompt = process.argv[2];

    if (!prompt) {
        console.log('Usage: node dist/main.js "<your_prompt_here>"');
        process.exit(0); // Exit gracefully if no prompt
    }

    try {
        const apiKey = getApiKey();
        console.log('Calling Gemini API...'); // User feedback
        const generatedText = await callGeminiApi(prompt, apiKey);
        console.log('\nGenerated Text:');
        console.log(generatedText);
    } catch (error) {
        // Errors from getApiKey or callGeminiApi will be caught here
        // console.error('\nAn error occurred. See details above.'); // The error is already logged by callGeminiApi or getApiKey
        process.exit(1); // Exit with error
    }
})();
