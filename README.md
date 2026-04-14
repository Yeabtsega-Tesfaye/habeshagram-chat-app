# Habeshagram

A distributed real-time chat application built with Java RMI for Advanced Programming and Distributed Systems course on my 3rd year 2nd semester.

## Features

- 🔐 User registration and authentication with password hashing
- 💬 Real-time broadcast messaging
- 👥 Private one-to-one messaging with conversation history
- 👨‍👩‍👧‍👦 Group chats with member management
- 💾 Persistent message storage using SQLite
- 📱 Modern Swing UI with chat bubbles
- 📜 Message history (last 50 messages per conversation)
- 🔔 Offline message delivery

## Architecture

- **Client-Server** model using Java RMI
- **RMI Callbacks** for real-time message push
- **SQLite** for persistent storage
- **Multi-module Maven** project structure

## Technologies

- Java 25
- Java RMI (Remote Method Invocation)
- Swing GUI Framework
- SQLite JDBC
- BCrypt for password hashing
- Maven build tool

## Project Structure

```
habeshagram/
├── common/          # Shared models, interfaces, exceptions
├── server/          # Chat server with database persistence
├── client/          # Swing-based chat client
└── pom.xml          # Parent Maven configuration
```

## Prerequisites

- Java 17 or higher (tested with Java 25)
- Maven 3.6 or higher
- SQLite (included as dependency)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/habeshagram.git
cd habeshagram
```

2. Build the project:
```bash
mvn clean install
```

## Running the Application

### Start the Server:
```bash
cd server
mvn exec:java
```

### Start a Client (in a new terminal):
```bash
cd client
mvn exec:java
```

### Run Multiple Clients:
Open additional terminals and repeat the client start command.

## Usage

1. **Register** a new account or **Login** with existing credentials
2. **Broadcast messages** in the main chat window
3. **Private chat**: Double-click a user in the online list
4. **Group chat**: Use the Groups menu to create or join groups
5. **Message history**: Automatically loads when opening conversations

## Features Demonstrated

- Distributed Systems concepts
- Remote Method Invocation (RMI)
- Client callbacks for server push
- Thread-safe server implementation
- Persistent data storage
- Event-driven UI architecture

## Screenshots

./desktop.png

## License

This project is created for educational purposes as part of Advanced Programming and Distributed Systems coursework.

## Author

Yeabtsega Tesfaye
Woldia University
04/14/2 G.c.

## Acknowledgments

- Advanced Programming and Distributed Systems course instructors
- Java RMI documentation and community


### 2. `LICENSE`:

```
MIT License

Copyright (c) 2026 Yeabtsega Tesfaye

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
