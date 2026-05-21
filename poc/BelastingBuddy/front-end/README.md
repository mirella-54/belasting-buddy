# BelastingBuddy Front-end

Minimal front-end for the BelastingBuddy hour tracking PoC.

## Setup

```bash
cd poc/BelastingBuddy/front-end
npm install
```

## Run

Make sure the API gateway is running on port 8000, then:

```bash
npm run dev
```

Opens at `http://localhost:5173`

## Features

- View and create projects
- Log hours for projects
- View hours summary with total
- Export hours as CSV or JSON

Uses a demo user ID for the PoC (no auth implemented yet).
