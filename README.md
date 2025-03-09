# GLaDOS

GLaDOS is an automated financial assistant that uses Gemini AI and SQLite to process expense messages from Telegram and answer financial questions.

## How it works

1. **Message Classification** (`entry-or-question?`)

Determines if a message is:

- An expense entry (e.g., "Paid 50 at the grocery store")
- A financial query (e.g., "How much did I spend this month?")

2. **Expense Structuring** (for new expense entries) (`structured-expense`)

It converts natural language text into a structured JSON to be inserted into the database, adhering to the schema and helping classifying the category and type.

>Spent 120 on groceries today using a credit card.

**Generated JSON:**
```json
{
  "amount": 120.00,
  "description": "groceries",
  "type": "credit_card",
  "category": "market",
  "date": "2025-03-08"
}
```

3. **Query Generation** (for financial questions) (`querier`)

Converts user questions into SQLite queries based on the expenses table schema.

> Which day did I spend the most money?

**Generated SQL:**
```sql
SELECT date, SUM(amount) AS total_spent
FROM expenses
WHERE date LIKE substr(date('now', '-3 hours'), 1, 7) || '%'
GROUP BY date
ORDER BY total_spent DESC
LIMIT 1;
```

4. **Humanized Responses** (`humanize`)

It combines the question, database schema, query, additional prompt instructions, database response, and returns a humanized message to the user.

> The day you spent the most money was 03/08/2025, with a total of R$1469.31.

## Setup

1. Make sure you have `sqlite` installed:
```bash
$ sudo apt install sqlite3    # Linux
$ brew install sqlite3        # MacOs
```

2. Clone the repository:

```bash
$ git clone https://github.com/lucianocarvalho/glados.git
$ cd glados
```

3. Make sure you set the right env values `.env`:

```bash
$ cp .env.example .env
```

```bash
TELEGRAM_TOKEN=foobar
TELEGRAM_CHAT_ID=foobar
GEMINI_URL=foobar
GEMINI_API_KEY=foobar
```

4. Run the following commands to create the database and table:

```bash
$ mkdir sqlite
$ sqlite3 sqlite/glados.db "
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    description TEXT NOT NULL,
    type TEXT NOT NULL
);"
```

To verify that the table was created successfully, run:

```bash
sqlite3 sqlite/glados.db "PRAGMA table_info(expenses);"
```

5. Build and run:

```bash
# Build the Docker image
docker build -t glados .

# Run the container
docker run -it --rm -p 3000:3000 -v ./sqlite:/app/sqlite/ --name glados glados

# Alternatively, use Docker Compose
docker-compose up -d
```