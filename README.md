# Schoolplanner
<p align="center">
  <img src="app/src/main/rounded-corners.png" alt="App Logo" width="200">
</p>

A comprehensive school planning application for Android that helps you organizing your academic life.

## Features

- **Subject Management**: Add, edit, and organize school subjects
- **Schedule Planning**: Create and manage your class schedule
- **Task Tracking**: Keep track of assignments and homework
- **Grade Reports**: Record and monitor your academic performance
- **School Year Organization**: Manage multiple school years
- **Data Import/Export**: Backup and transfer your data
- **Customization**: Theme and display settings

## Screenshots




## Installation

### Prerequisites
- Android Studio
- Android SDK
- Kotlin 1.9.0+

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/Sergey842248/Schoolplanner.git
```

2. Open the project in Android Studio

3. Build and run the application on your Android device or emulator

## Usage

1. **First Launch**: Set up your school year and add your subjects
2. **Add Schedule**: Create your weekly class schedule
3. **Track Tasks**: Add homework and assignments with due dates
4. **Record Grades**: Enter your grades and monitor your progress
5. **Customize**: Adjust themes and settings to your preference

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/future/schoolplanner/      # Main application code
│   │   ├── res/                               # Resources
│   │   └── AndroidManifest.xml                # App manifest
│   └── test/                                  # Unit tests
```

## Key Components

- **Data Models**: Subject, Schedule, Task, Report, SchoolYear
- **Persistence**: DataPersistenceManager for local storage
- **UI**: Jetpack Compose based interface
- **ViewModel**: GradeViewModel for grade management

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please open an issue on GitHub.

---

© 2024 Schoolplanner
