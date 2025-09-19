document.addEventListener('DOMContentLoaded', function() {
    // --- Translation Toggle Feature ---
    const toggleButtons = document.querySelectorAll('.toggle-translation');
    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const translationSpan = this.nextElementSibling;
            if (translationSpan && translationSpan.classList.contains('translation')) {
                if (translationSpan.style.display === 'inline' || translationSpan.style.display === '') {
                    translationSpan.style.display = 'none';
                } else {
                    translationSpan.style.display = 'inline';
                }
            }
        });
    });

    // --- Post-class Exercises Quiz Feature ---
    const quizOptions = document.querySelectorAll('.quiz-option');
    quizOptions.forEach(option => {
        option.addEventListener('click', function() {
            const parentOptions = this.parentElement;
            // Prevent re-answering
            if (parentOptions.classList.contains('answered')) {
                return;
            }
            parentOptions.classList.add('answered');

            const isCorrect = this.dataset.correct === 'true';
            if (isCorrect) {
                this.classList.add('correct');
            } else {
                this.classList.add('incorrect');
                // Show the correct answer
                const correctOption = parentOptions.querySelector('[data-correct="true"]');
                if (correctOption) {
                    correctOption.classList.add('correct');
                }
            }
        });
    });
});
