const fs = require('fs').promises;
const path = require('path');
const { Pool } = require('pg');

// pg library automatically uses environment variables for connection.
// See: https://node-postgres.com/features/connecting#environment-variables
// Required variables: PGHOST, PGDATABASE, PGUSER, PGPASSWORD, PGPORT

const PAGE_SIZE = 200;

exports.handler = async (event, context) => {
  try {
    const pool = new Pool();

    // 1. Get offset from query string, default to 0
    const offset = parseInt(event.queryStringParameters?.offset, 10) || 0;

    // 2. Read the user-defined SQL query from the file
    // The query file is included by Netlify in the same directory as the function.
    const queryFilePath = path.resolve(__dirname, 'query.sql');
    const userQuery = await fs.readFile(queryFilePath, 'utf8');

    if (!userQuery.trim()) {
      return {
        statusCode: 400,
        body: JSON.stringify({ error: 'SQL query file is empty.' }),
      };
    }

    // 3. Append pagination logic to the query
    const paginatedQuery = `${userQuery.trim().replace(/;$/, '')} LIMIT ${PAGE_SIZE} OFFSET ${offset};`;

    // 4. Execute the query
    const client = await pool.connect();
    try {
      const result = await client.query(paginatedQuery);
      return {
        statusCode: 200,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(result.rows),
      };
    } finally {
      client.release();
    }
  } catch (err) {
    console.error('Error executing query:', err);
    // Differentiate between file and DB errors for better client-side messages
    if (err.code === 'ENOENT') {
         return {
            statusCode: 500,
            body: JSON.stringify({ error: 'Server configuration error: query.sql file not found.' }),
        };
    }
    return {
      statusCode: 500,
      body: JSON.stringify({ error: 'Database query failed.', details: err.message }),
    };
  }
};
