## Prerequisites
Java 17 or higher
Maven
Docker

## Running the Application with Docker Compose

### Ensure Docker and Docker Compose are Installed
Make sure you have Docker and Docker Compose installed on your machine.

### Build and Run the Application
From the root of your project directory, run:

```bash
docker-compose up --build
```

### Access the Application
Once the containers are running, you can access the application at ```http://localhost:8080```

## Stopping the Application
To stop the application, you can use:

```bash
docker-compose down
```

This will stop and remove the containers defined in your docker-compose.yml file.
