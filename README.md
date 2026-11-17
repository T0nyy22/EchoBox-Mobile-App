# EchoBox 📦⏳

**EchoBox** is an Android time-capsule application built with **Jetpack Compose** and **Kotlin**. It lets you write messages to your future self, attach photos, assign categories, and seal them until a specific target date and time. Once the chosen moment arrives, the capsule unlocks so you can rediscover your thoughts, memories, and goals.

---

## ✨ Features

* **🔒 Time-Locked Capsules:** Create messages that remain sealed until their exact unlock date and time arrive.
* **🖼️ Photo Attachments:** Attach images to your capsules using the native Android Photo Picker (`PickVisualMedia`) with local persistent storage.
* **🏷️ Category Filtering:** Organize and filter time capsules by category (e.g., Personal, Work, Goals).
* **⚡ Real-Time Unlock Updates:** A live second-by-second ticker updates capsule statuses in real time as soon as the target timestamp is reached.
* **🛠️ Full Capsule Management:** Create, view, edit (once unlocked), open, and delete capsules.

---

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose 
* **Local Persistence:** Room Database (`MessagesDatabase`, `MessagesDao`, `MessagesEntity`)

---

## 📂 Project Structure

```text
com.example.echobox/
├── MainActivity.kt              # Entry point, MainViewModel, and Compose UI screens/dialogs
├── MessageDetailsActivity.kt    # Detail view for reading unlocked time capsules
├── MessagesDatabase.kt          # Room database setup
├── MessagesDao.kt               # Data Access Object for database queries
├── MessagesEntity.kt            # Room entity representing a time capsule
├── MessageCategory.kt           # Predefined category constants & filter lists
└── ImageStorageManager.kt       # Helper for saving/deleting attached images locally
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** (Ladybug or newer recommended)
* **JDK 17+**
* **Android SDK** (Min SDK 24+ / Target SDK 34+)

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/EchoBox.git](https://github.com/your-username/EchoBox.git)
   ```
2. **Open in Android Studio:**
   Select **File > Open** and navigate to the cloned `EchoBox` directory.
3. **Sync Gradle:**
   Let Android Studio download the required dependencies (Jetpack Compose, Room, Lifecycle ViewModel, and Coil).
4. **Run the App:**
   Connect an Android device or launch an emulator, then click **Run (`Shift + F10`)**.

---

## 📖 How It Works

1. **Create a Capsule:** Tap the **`+`** button to open the creation dialog.
2. **Fill Details:** Enter a title, write your secret message, select a category, and optionally attach a photo.
3. **Set Unlock Time:** Pick a future date and time using the built-in Date and Time pickers, then tap **Seal Capsule**.
4. **Wait & Unlock:** Your message stays locked in the list until the target time passes. Once ready, tap **Open Capsule** to unlock it and click the card to view its full contents.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
