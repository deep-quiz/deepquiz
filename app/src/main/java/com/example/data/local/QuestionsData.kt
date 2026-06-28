package com.example.data.local

import com.example.domain.model.Question

object QuestionsData {
    val questions = listOf(
        // === GENERAL KNOWLEDGE ===
        Question(
            id = 1,
            category = "General Knowledge",
            text = "Which country is home to the Kangaroo?",
            options = listOf("India", "Australia", "South Africa", "Kenya"),
            correctAnswerIndex = 1,
            explanation = "Kangaroos are marsupials that are indigenous to Australia."
        ),
        Question(
            id = 2,
            category = "General Knowledge",
            text = "What is the capital of France?",
            options = listOf("Berlin", "Madrid", "Rome", "Paris"),
            correctAnswerIndex = 3,
            explanation = "Paris is the capital and most populous city of France."
        ),
        Question(
            id = 3,
            category = "General Knowledge",
            text = "Which planet is known as the Red Planet?",
            options = listOf("Earth", "Mars", "Jupiter", "Venus"),
            correctAnswerIndex = 1,
            explanation = "Mars is often called the 'Red Planet' due to iron oxide on its surface."
        ),
        Question(
            id = 4,
            category = "General Knowledge",
            text = "Who wrote the play 'Romeo and Juliet'?",
            options = listOf("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"),
            correctAnswerIndex = 1,
            explanation = "Romeo and Juliet is a tragedy written by William Shakespeare early in his career."
        ),
        Question(
            id = 5,
            category = "General Knowledge",
            text = "What is the largest ocean on Earth?",
            options = listOf("Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"),
            correctAnswerIndex = 3,
            explanation = "The Pacific Ocean is the largest and deepest of Earth's oceanic divisions."
        ),
        Question(
            id = 16,
            category = "General Knowledge",
            text = "What is the tallest mountain in the world?",
            options = listOf("K2", "Kangchenjunga", "Mount Everest", "Lhotse"),
            correctAnswerIndex = 2,
            explanation = "Mount Everest is Earth's highest mountain above sea level."
        ),
        Question(
            id = 17,
            category = "General Knowledge",
            text = "Which element has the chemical symbol 'O'?",
            options = listOf("Gold", "Oxygen", "Osmium", "Oganesson"),
            correctAnswerIndex = 1,
            explanation = "Oxygen is a chemical element with the symbol O and atomic number 8."
        ),
        Question(
            id = 18,
            category = "General Knowledge",
            text = "In which year did the Titanic sink?",
            options = listOf("1910", "1912", "1914", "1916"),
            correctAnswerIndex = 1,
            explanation = "The RMS Titanic sank on 15 April 1912 after striking an iceberg."
        ),
        Question(
            id = 19,
            category = "General Knowledge",
            text = "Who painted the Mona Lisa?",
            options = listOf("Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Claude Monet"),
            correctAnswerIndex = 2,
            explanation = "The Mona Lisa is a half-length portrait painting by Italian artist Leonardo da Vinci."
        ),
        Question(
            id = 20,
            category = "General Knowledge",
            text = "What is the smallest continent by land area?",
            options = listOf("Europe", "Antarctica", "Australia", "South America"),
            correctAnswerIndex = 2,
            explanation = "Australia is the smallest continent by land area."
        ),
        // === SCIENCE ===
        Question(
            id = 6,
            category = "Science",
            text = "What is the chemical symbol for gold?",
            options = listOf("Au", "Ag", "Fe", "Cu"),
            correctAnswerIndex = 0,
            explanation = "The chemical symbol for gold is Au, from the Latin word 'aurum'."
        ),
        Question(
            id = 7,
            category = "Science",
            text = "What force keeps us on the ground?",
            options = listOf("Friction", "Magnetism", "Gravity", "Inertia"),
            correctAnswerIndex = 2,
            explanation = "Gravity is the fundamental interaction that attracts things with mass or energy to one another."
        ),
        Question(
            id = 8,
            category = "Science",
            text = "What is the powerhouse of the cell?",
            options = listOf("Nucleus", "Ribosome", "Mitochondria", "Endoplasmic Reticulum"),
            correctAnswerIndex = 2,
            explanation = "Mitochondria are often called the powerhouse of the cell because they generate most of the cell's supply of ATP."
        ),
        Question(
            id = 9,
            category = "Science",
            text = "What gas do plants absorb during photosynthesis?",
            options = listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"),
            correctAnswerIndex = 2,
            explanation = "Plants absorb carbon dioxide and release oxygen during the process of photosynthesis."
        ),
        Question(
            id = 10,
            category = "Science",
            text = "At what temperature does water boil at sea level (in Celsius)?",
            options = listOf("50", "75", "100", "125"),
            correctAnswerIndex = 2,
            explanation = "Under standard atmospheric pressure at sea level, water boils at exactly 100 degrees Celsius."
        ),
        Question(
            id = 21,
            category = "Science",
            text = "What is the most abundant gas in the Earth's atmosphere?",
            options = listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Argon"),
            correctAnswerIndex = 1,
            explanation = "Nitrogen makes up about 78% of the Earth's atmosphere."
        ),
        Question(
            id = 22,
            category = "Science",
            text = "What is the hardest natural substance on Earth?",
            options = listOf("Gold", "Iron", "Diamond", "Platinum"),
            correctAnswerIndex = 2,
            explanation = "Diamond is the hardest known naturally occurring substance."
        ),
        Question(
            id = 23,
            category = "Science",
            text = "What part of the brain controls balance?",
            options = listOf("Cerebrum", "Cerebellum", "Brain Stem", "Hypothalamus"),
            correctAnswerIndex = 1,
            explanation = "The cerebellum coordinates voluntary movements such as posture, balance, coordination, and speech."
        ),
        Question(
            id = 24,
            category = "Science",
            text = "How many bones are in the adult human body?",
            options = listOf("206", "208", "210", "212"),
            correctAnswerIndex = 0,
            explanation = "The adult human skeletal system consists of 206 bones."
        ),
        Question(
            id = 25,
            category = "Science",
            text = "What is the chemical formula for water?",
            options = listOf("CO2", "H2O", "O2", "NaCl"),
            correctAnswerIndex = 1,
            explanation = "Water is a chemical compound consisting of two hydrogen atoms and one oxygen atom (H2O)."
        ),
        // === TECHNOLOGY ===
        Question(
            id = 11,
            category = "Technology",
            text = "What does HTTP stand for?",
            options = listOf("HyperText Transfer Protocol", "HyperText Transmission Protocol", "HyperText Transfer Process", "HyperText Translation Protocol"),
            correctAnswerIndex = 0,
            explanation = "HTTP stands for HyperText Transfer Protocol, the foundation of data communication for the World Wide Web."
        ),
        Question(
            id = 12,
            category = "Technology",
            text = "Which company created the Android operating system?",
            options = listOf("Apple", "Microsoft", "Google", "IBM"),
            correctAnswerIndex = 2,
            explanation = "Android was developed by a consortium of developers known as the Open Handset Alliance, with the main contributor and commercial marketer being Google."
        ),
        Question(
            id = 13,
            category = "Technology",
            text = "What is the primary function of a computer's CPU?",
            options = listOf("To store data long-term", "To display graphics", "To process instructions", "To connect to the internet"),
            correctAnswerIndex = 2,
            explanation = "The Central Processing Unit (CPU) is the primary component of a computer that acts as its 'brain', executing instructions."
        ),
        Question(
            id = 14,
            category = "Technology",
            text = "In computer storage, what is larger than a Terabyte?",
            options = listOf("Gigabyte", "Megabyte", "Kilobyte", "Petabyte"),
            correctAnswerIndex = 3,
            explanation = "A Petabyte is 1024 Terabytes. The progression goes Kilo, Mega, Giga, Tera, Peta."
        ),
        Question(
            id = 15,
            category = "Technology",
            text = "What programming language is primarily used for Android app development?",
            options = listOf("Swift", "Kotlin", "Ruby", "C#"),
            correctAnswerIndex = 1,
            explanation = "Kotlin is a modern, statically typed programming language that is now the preferred language for Android development by Google."
        ),
        Question(
            id = 26,
            category = "Technology",
            text = "Who is known as the father of computers?",
            options = listOf("Alan Turing", "Charles Babbage", "Bill Gates", "Steve Jobs"),
            correctAnswerIndex = 1,
            explanation = "Charles Babbage is considered by some to be a 'father of the computer'."
        ),
        Question(
            id = 27,
            category = "Technology",
            text = "What does HTML stand for?",
            options = listOf("Hyper Text Markup Language", "High Text Machine Language", "Hyper Tabular Markup Language", "None of these"),
            correctAnswerIndex = 0,
            explanation = "HTML stands for Hyper Text Markup Language."
        ),
        Question(
            id = 28,
            category = "Technology",
            text = "Which of the following is an open-source operating system?",
            options = listOf("Windows", "macOS", "Linux", "iOS"),
            correctAnswerIndex = 2,
            explanation = "Linux is a family of open-source Unix-like operating systems based on the Linux kernel."
        ),
        Question(
            id = 29,
            category = "Technology",
            text = "What does GUI stand for?",
            options = listOf("General Utility Interface", "Graphical User Interface", "Global Unique Identifier", "Graphic Utility Interaction"),
            correctAnswerIndex = 1,
            explanation = "A Graphical User Interface is a form of user interface that allows users to interact with electronic devices through graphical icons."
        ),
        Question(
            id = 30,
            category = "Technology",
            text = "What is the main function of a router?",
            options = listOf("To store data", "To block viruses", "To forward data packets", "To display web pages"),
            correctAnswerIndex = 2,
            explanation = "A router is a networking device that forwards data packets between computer networks."
        )
    )
}
