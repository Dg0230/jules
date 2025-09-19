document.addEventListener('DOMContentLoaded', function() {
    // Find all the translation toggle buttons on the page
    const toggleButtons = document.querySelectorAll('.toggle-translation');

    // Add a click event listener to each button
    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            // The translation span is the next element sibling
            const translationSpan = this.nextElementSibling;

            if (translationSpan && translationSpan.classList.contains('translation')) {
                // Check the current display style and toggle it
                if (translationSpan.style.display === 'inline' || translationSpan.style.display === '') {
                    translationSpan.style.display = 'none';
                } else {
                    translationSpan.style.display = 'inline';
                }
            }
        });
    });
});
