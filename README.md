# Ticketing System Project

## Overview

The Ticketing System is a comprehensive application designed to manage ticket generation and consumption processes. It features a Spring Boot backend, an Angular frontend, and a Command-Line Interface (CLI), providing real-time updates, configurable parameters, and statistical visualizations.

## Features

- **Dynamic Ticket Pool Management**
  - Configurable total tickets, release rate, retrieval rate, and maximum capacity
  - Real-time addition and retrieval of tickets

- **Control Panel**
  - Manage vendors and consumers dynamically
  - Start, stop, and reset processes

- **Real-Time Monitoring**
  - Live logs of system events
  - Interactive statistics and graphs
  - Ticket pool tracking

- **Data Persistence**
  - Configuration saved to JSON file
  - SQLite database integration

- **Multi-Interface Support**
  - Web GUI
  - Command-Line Interface (CLI)

## Prerequisites

- Java 11 or higher
- Node.js 18.x or higher
- npm
- Angular CLI 15.x or higher

## Setup Instructions

### Backend Setup (Spring Boot)

1. Clone the backend repository:
   ```bash
   git clone https://github.com/<your-repo>/ticketing-system-backend.git
   cd ticketing-system-backend
   ```

2. Build and Run:
   - Open in IDE (IntelliJ IDEA or Eclipse)
   - Ensure Maven dependencies are downloaded
   - Run: `./mvnw spring-boot:run`
   - Backend runs on: `http://localhost:8080`

### Frontend Setup (Angular)

1. Clone the frontend repository:
   ```bash
   git clone https://github.com/<your-repo>/ticketing-system-gui.git
   cd ticketing-system-gui
   ```

2. Install and Run:
   ```bash
   npm install
   ng serve
   ```
   - Frontend runs on: `http://localhost:4200`

### CLI Setup

1. Navigate to CLI directory:
   ```bash
   cd Coursework\ cli
   ```

2. Build and Run:
   - Open in IntelliJ IDEA
   - Resolve dependencies
   - Run: `java -jar out/production/Coursework\ cli.jar`

## Configuration

### GUI Configuration
- Access: `http://localhost:4200`
- Configure:
  - Total Tickets
  - Ticket Release Rate
  - Ticket Retrieval Rate
  - Max Ticket Capacity

### CLI Configuration
- Follow interactive prompts in the CLI application

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/ticket-pool/save` | POST | Save configuration |
| `/api/ticket-pool/start` | POST | Start processes |
| `/api/ticket-pool/stop` | POST | Stop processes |
| `/api/ticket-pool/reset` | POST | Reset ticket pool |
| `/api/ticket-pool/size` | GET | Get ticket pool size |

## Troubleshooting

### Common Issues
- **CORS Errors**: Add `@CrossOrigin(origins = "http://localhost:4200")` to controllers
- **Backend Startup**: Verify Java and Maven installation
- **Frontend Issues**: Check Node.js and Angular CLI versions
- **CLI Problems**: Ensure Java is correctly configured

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit changes: `git commit -m "Description of changes"`
4. Push to branch: `git push origin feature-name`
5. Open a pull request

## Contact Information 

- 
